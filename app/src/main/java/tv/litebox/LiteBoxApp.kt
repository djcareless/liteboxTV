package tv.litebox

import android.app.Application
import android.os.StrictMode
import tv.litebox.data.db.LiteBoxDatabase
import tv.litebox.plugin.PluginManager
import tv.litebox.theme.ThemeManager

class LiteBoxApp : Application() {

    lateinit var database: LiteBoxDatabase
        private set

    lateinit var pluginManager: PluginManager
        private set

    lateinit var themeManager: ThemeManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
        }

        database = LiteBoxDatabase.getInstance(this)
        pluginManager = PluginManager(this)
        themeManager = ThemeManager(this)
    }

    companion object {
        lateinit var instance: LiteBoxApp
            private set
    }
}
