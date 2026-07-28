package org.nongor.app

import android.app.Application
import org.nongor.app.data.AppPrefs

class NongorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Warm the preference file on the main thread once, so the first frame of the
        // home screen is already in the right language.
        AppPrefs.get(this)
    }
}
