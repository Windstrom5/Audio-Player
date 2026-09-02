package com.thesis.bitperfectusb

import android.app.Application
import com.thesis.bitperfectusb.di.allModules
import com.thesis.bitperfectusb.usb.UsbDacManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class BitPerfectApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@BitPerfectApp)
            modules(allModules)
        }
        // Start listening for USB attach/detach immediately so a DAC that's
        // already plugged in at launch is picked up without user action.
        org.koin.java.KoinJavaComponent.get<UsbDacManager>(UsbDacManager::class.java).start()
    }
}
