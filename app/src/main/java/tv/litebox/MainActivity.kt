package tv.litebox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import tv.litebox.theme.ThemeManager
import tv.litebox.ui.LiteBoxNavHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeManager = LiteBoxApp.instance.themeManager

        setContent {
            val theme by themeManager.currentTheme.collectAsState()
            LiteBoxNavHost(theme = theme)
        }
    }
}
