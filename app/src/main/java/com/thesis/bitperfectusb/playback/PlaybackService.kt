package com.thesis.bitperfectusb.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState as MediaPlaybackState
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.thesis.bitperfectusb.R
import com.thesis.bitperfectusb.domain.model.PlaybackState as DomainPlaybackState
import com.thesis.bitperfectusb.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Foreground audio service with full MediaSession and interactive Notification Bar media controls
 * (Lock-screen transport controls, Previous/Play/Pause/Next/Stop action buttons, media button routing).
 */
class PlaybackService : Service() {

    private val playbackController: PlaybackController by inject()
    private var mediaSession: MediaSession? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var isForegroundServiceStarted = false

    override fun onCreate() {
        super.onCreate()
        initMediaSession()
        observePlaybackState()
    }

    private fun initMediaSession() {
        mediaSession = MediaSession(this, "BitPerfectUSBMediaSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    scope.launch { playbackController.resume() }
                }

                override fun onPause() {
                    scope.launch { playbackController.pause() }
                }

                override fun onSkipToNext() {
                    scope.launch { playbackController.nextTrack() }
                }

                override fun onSkipToPrevious() {
                    scope.launch { playbackController.previousTrack() }
                }

                override fun onStop() {
                    scope.launch { playbackController.stop(stopService = true) }
                }

                override fun onSeekTo(pos: Long) {
                    scope.launch { playbackController.seekTo(pos) }
                }
            })

            val stateBuilder = MediaPlaybackState.Builder()
                .setActions(
                    MediaPlaybackState.ACTION_PLAY or
                            MediaPlaybackState.ACTION_PAUSE or
                            MediaPlaybackState.ACTION_PLAY_PAUSE or
                            MediaPlaybackState.ACTION_SKIP_TO_NEXT or
                            MediaPlaybackState.ACTION_SKIP_TO_PREVIOUS or
                            MediaPlaybackState.ACTION_SEEK_TO or
                            MediaPlaybackState.ACTION_STOP
                )
                .setState(MediaPlaybackState.STATE_PLAYING, 0L, 1.0f)
            setPlaybackState(stateBuilder.build())
            isActive = true
        }
    }

    private fun observePlaybackState() {
        scope.launch {
            playbackController.state.collect { state ->
                updateMediaSession(state)
                if (isForegroundServiceStarted) {
                    val notification = buildNotification(state)
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    notificationManager?.notify(NOTIFICATION_ID, notification)
                }
            }
        }
    }

    private fun updateMediaSession(state: DomainPlaybackState) {
        val session = mediaSession ?: return
        val track = state.currentTrack

        // 1. Update Metadata
        val metaBuilder = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, track?.title ?: "Bit-Perfect Track")
            .putString(MediaMetadata.METADATA_KEY_ARTIST, track?.artist ?: "BitPerfectUSB Hi-Fi")
            .putLong(MediaMetadata.METADATA_KEY_DURATION, track?.durationMs ?: 0L)
        session.setMetadata(metaBuilder.build())

        // 2. Update Playback State
        val playbackStateCode = when {
            state.isPlaying -> MediaPlaybackState.STATE_PLAYING
            state.isPaused -> MediaPlaybackState.STATE_PAUSED
            else -> MediaPlaybackState.STATE_STOPPED
        }

        val stateBuilder = MediaPlaybackState.Builder()
            .setActions(
                MediaPlaybackState.ACTION_PLAY or
                        MediaPlaybackState.ACTION_PAUSE or
                        MediaPlaybackState.ACTION_PLAY_PAUSE or
                        MediaPlaybackState.ACTION_SKIP_TO_NEXT or
                        MediaPlaybackState.ACTION_SKIP_TO_PREVIOUS or
                        MediaPlaybackState.ACTION_SEEK_TO or
                        MediaPlaybackState.ACTION_STOP
            )
            .setState(playbackStateCode, state.positionMs, if (state.isPlaying) 1.0f else 0.0f)
        session.setPlaybackState(stateBuilder.build())
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action != null) {
            handleNotificationAction(action)
        }

        val currentState = playbackController.state.value
        updateMediaSession(currentState)
        val notification = buildNotification(currentState)

        if (!isForegroundServiceStarted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            isForegroundServiceStarted = true
        }

        return START_NOT_STICKY
    }

    private fun handleNotificationAction(action: String) {
        when (action) {
            ACTION_PLAY -> scope.launch { playbackController.resume() }
            ACTION_PAUSE -> scope.launch { playbackController.pause() }
            ACTION_TOGGLE -> scope.launch {
                if (playbackController.state.value.isPlaying) {
                    playbackController.pause()
                } else {
                    playbackController.resume()
                }
            }
            ACTION_NEXT -> scope.launch { playbackController.nextTrack() }
            ACTION_PREVIOUS -> scope.launch { playbackController.previousTrack() }
            ACTION_STOP -> scope.launch { playbackController.stop(stopService = true) }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        scope.launch {
            playbackController.stop(stopService = true)
        }
        cleanupService()
        stopSelf()
    }

    override fun onDestroy() {
        cleanupService()
        super.onDestroy()
    }

    private fun cleanupService() {
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.cancel(NOTIFICATION_ID)
        isForegroundServiceStarted = false
    }

    private fun buildNotification(state: DomainPlaybackState): Notification {
        val channelId = ensureChannel()
        val openAppIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val track = state.currentTrack
        val titleText = track?.title ?: "Bit-Perfect Playback"
        val formatInfo = if (track != null) "${track.pcm.sampleRateHz / 1000}kHz / ${track.pcm.bitDepth}-bit" else "Hi-Res Direct"
        val artistText = if (!track?.artist.isNullOrBlank()) "${track?.artist} • $formatInfo" else formatInfo

        val isPlaying = state.isPlaying

        // PendingIntents for interactive notification action buttons
        val prevPendingIntent = createActionPendingIntent(ACTION_PREVIOUS, 1)
        val playPausePendingIntent = createActionPendingIntent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY, 2)
        val nextPendingIntent = createActionPendingIntent(ACTION_NEXT, 3)
        val stopPendingIntent = createActionPendingIntent(ACTION_STOP, 4)

        val builder = Notification.Builder(this, channelId)
            .setContentTitle(titleText)
            .setContentText(artistText)
            .setSmallIcon(if (isPlaying) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause)
            .setContentIntent(openAppIntent)
            .setOngoing(isPlaying)
            .setVisibility(Notification.VISIBILITY_PUBLIC)

        // Action 0: Previous Track
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, android.R.drawable.ic_media_previous),
                    "Previous",
                    prevPendingIntent
                ).build()
            )
        } else {
            @Suppress("DEPRECATION")
            builder.addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
        }

        // Action 1: Play / Pause Toggle
        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "Pause" else "Play"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, playPauseIcon),
                    playPauseTitle,
                    playPausePendingIntent
                ).build()
            )
        } else {
            @Suppress("DEPRECATION")
            builder.addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
        }

        // Action 2: Next Track
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, android.R.drawable.ic_media_next),
                    "Next",
                    nextPendingIntent
                ).build()
            )
        } else {
            @Suppress("DEPRECATION")
            builder.addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
        }

        // Action 3: Stop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel),
                    "Stop",
                    stopPendingIntent
                ).build()
            )
        } else {
            @Suppress("DEPRECATION")
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
        }

        // Apply MediaStyle with compact action buttons (Previous, Play/Pause, Next)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaSession?.sessionToken?.let { token ->
                val mediaStyle = Notification.MediaStyle()
                    .setMediaSession(token)
                    .setShowActionsInCompactView(0, 1, 2)
                builder.setStyle(mediaStyle)
            }
        }

        return builder.build()
    }

    private fun createActionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, PlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun ensureChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_playback),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "BitPerfectUSB Audio Playback Controls"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
        return CHANNEL_ID
    }

    companion object {
        const val ACTION_PLAY = "com.thesis.bitperfectusb.action.PLAY"
        const val ACTION_PAUSE = "com.thesis.bitperfectusb.action.PAUSE"
        const val ACTION_TOGGLE = "com.thesis.bitperfectusb.action.TOGGLE"
        const val ACTION_NEXT = "com.thesis.bitperfectusb.action.NEXT"
        const val ACTION_PREVIOUS = "com.thesis.bitperfectusb.action.PREVIOUS"
        const val ACTION_STOP = "com.thesis.bitperfectusb.action.STOP"

        private const val CHANNEL_ID = "playback"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "PlaybackService"

        fun start(context: Context) {
            val intent = Intent(context, PlaybackService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PlaybackService::class.java))
        }
    }
}
