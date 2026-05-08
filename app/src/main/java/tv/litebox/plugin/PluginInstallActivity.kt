package tv.litebox.plugin

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import tv.litebox.LiteBoxApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles litebox://install-plugin?url=... intents */
class PluginInstallActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent?.data?.getQueryParameter("url")
        if (url.isNullOrBlank()) {
            Toast.makeText(this, "Invalid plugin URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val result = LiteBoxApp.instance.pluginManager.installFromUrl(url)
            runOnUiThread {
                result.fold(
                    onSuccess = { Toast.makeText(this@PluginInstallActivity, "Plugin installed: ${it.manifest.name}", Toast.LENGTH_LONG).show() },
                    onFailure = { Toast.makeText(this@PluginInstallActivity, "Install failed: ${it.message}", Toast.LENGTH_LONG).show() }
                )
                finish()
            }
        }
    }
}
