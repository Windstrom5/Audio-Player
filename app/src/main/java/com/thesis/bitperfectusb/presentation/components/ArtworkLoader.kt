package com.thesis.bitperfectusb.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thesis.bitperfectusb.presentation.theme.HifiGold
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.SurfaceCharcoal
import com.thesis.bitperfectusb.presentation.theme.SurfaceRaised
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.palette.graphics.Palette

data class ArtworkPalette(
    val dominantColor: Color = Color(0xFF10141D),
    val vibrantColor: Color = SignalTeal,
    val darkVibrantColor: Color = Color(0xFF0A0E17),
    val mutedColor: Color = SurfaceRaised,
    val lightMutedColor: Color = Color(0xFF8B9CB0)
)

object ArtworkCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8 // Use 1/8th of available memory for artwork cache

    private val lruCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    private val paletteCache = LruCache<String, ArtworkPalette>(50)

    suspend fun getArtwork(context: Context, uriString: String, targetSize: Int = 512): ImageBitmap? {
        if (uriString.isEmpty()) return null

        val cacheKey = "$uriString-$targetSize"
        lruCache.get(cacheKey)?.let {
            return it.asImageBitmap()
        }

        return withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                val uri = Uri.parse(uriString)
                retriever.setDataSource(context, uri)
                val pictureBytes = retriever.embeddedPicture ?: return@withContext null

                // Decode with inSampleSize to optimize memory
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.size, options)

                options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
                options.inJustDecodeBounds = false

                val bitmap = BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.size, options)
                if (bitmap != null) {
                    lruCache.put(cacheKey, bitmap)
                    bitmap.asImageBitmap()
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun getPalette(context: Context, uriString: String): ArtworkPalette {
        if (uriString.isEmpty()) return ArtworkPalette()

        paletteCache.get(uriString)?.let { return it }

        return withContext(Dispatchers.IO) {
            val bitmap = lruCache.get("$uriString-512") ?: run {
                val retriever = MediaMetadataRetriever()
                try {
                    val uri = Uri.parse(uriString)
                    retriever.setDataSource(context, uri)
                    val pictureBytes = retriever.embeddedPicture ?: return@run null
                    val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                    BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.size, options)
                } catch (_: Exception) {
                    null
                } finally {
                    try { retriever.release() } catch (_: Exception) {}
                }
            }

            if (bitmap != null) {
                val palette = Palette.from(bitmap).generate()
                val dominant = palette.dominantSwatch?.rgb?.let { Color(it) } ?: Color(0xFF10141D)
                val vibrant = palette.vibrantSwatch?.rgb?.let { Color(it) }
                    ?: palette.lightVibrantSwatch?.rgb?.let { Color(it) }
                    ?: SignalTeal
                val darkVibrant = palette.darkVibrantSwatch?.rgb?.let { Color(it) }
                    ?: palette.darkMutedSwatch?.rgb?.let { Color(it) }
                    ?: Color(0xFF0A0E17)
                val muted = palette.mutedSwatch?.rgb?.let { Color(it) } ?: SurfaceRaised
                val lightMuted = palette.lightMutedSwatch?.rgb?.let { Color(it) } ?: Color(0xFF8B9CB0)

                val artPalette = ArtworkPalette(
                    dominantColor = dominant,
                    vibrantColor = vibrant,
                    darkVibrantColor = darkVibrant,
                    mutedColor = muted,
                    lightMutedColor = lightMuted
                )
                paletteCache.put(uriString, artPalette)
                artPalette
            } else {
                ArtworkPalette()
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}

@Composable
fun rememberArtworkPalette(uriString: String): ArtworkPalette {
    val context = LocalContext.current
    var palette by remember(uriString) { mutableStateOf(ArtworkPalette()) }

    LaunchedEffect(uriString) {
        palette = ArtworkCache.getPalette(context, uriString)
    }

    return palette
}

@Composable
fun TrackArtwork(
    uriString: String,
    title: String,
    artist: String?,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isLarge: Boolean = false,
    cornerRadius: Dp = if (isLarge) 20.dp else 8.dp
) {
    val context = LocalContext.current
    var artwork by remember(uriString) { mutableStateOf<ImageBitmap?>(null) }
    var loaded by remember(uriString) { mutableStateOf(false) }

    LaunchedEffect(uriString) {
        artwork = ArtworkCache.getArtwork(context, uriString, if (isLarge) 600 else 180)
        loaded = true
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(SurfaceCharcoal)
            .border(
                1.dp,
                if (isPlaying) SignalTeal.copy(alpha = 0.4f) else OutlineSubtle,
                RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = artwork, label = "art_crossfade") { art ->
            if (art != null) {
                Image(
                    bitmap = art,
                    contentDescription = "$title - $artist",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Procedural Audiophile Vinyl Art
                ProceduralCoverArt(
                    title = title,
                    isPlaying = isPlaying,
                    isLarge = isLarge,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ProceduralCoverArt(
    title: String,
    isPlaying: Boolean,
    isLarge: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_art")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 6000 else 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotate"
    )

    Box(
        modifier = modifier
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        SurfaceRaised,
                        Color(0xFF0D1117),
                        Color(0xFF06090E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLarge) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .rotate(if (isPlaying) angle else 0f)
            ) {
                val radius = size.width / 2f
                drawCircle(color = Color(0xFF14171F), radius = radius)
                drawCircle(color = Color(0xFF0B0D12), radius = radius * 0.88f)
                drawCircle(color = Color.White.copy(alpha = 0.08f), radius = radius * 0.76f, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
                drawCircle(color = Color.White.copy(alpha = 0.08f), radius = radius * 0.62f, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
                drawCircle(color = Color.White.copy(alpha = 0.08f), radius = radius * 0.48f, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
                drawCircle(color = HifiGold.copy(alpha = 0.9f), radius = radius * 0.35f)
                drawCircle(color = Color(0xFF040608), radius = radius * 0.12f)
            }
            Icon(
                imageVector = Icons.Filled.Album,
                contentDescription = null,
                tint = if (isPlaying) SignalTeal else HifiGold.copy(alpha = 0.8f),
                modifier = Modifier.size(54.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = if (isPlaying) SignalTeal else HifiGold.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
