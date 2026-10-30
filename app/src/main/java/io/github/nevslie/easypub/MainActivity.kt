package io.github.nevslie.easypub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.nevslie.easypub.data.settings.AppTheme
import io.github.nevslie.easypub.ui.navigation.EasyPubNavHost
import io.github.nevslie.easypub.ui.theme.EasyPubTheme
import io.github.nevslie.easypub.ui.theme.isDark

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsRepository = appContainer.settingsRepository

        setContent {
            val appTheme by settingsRepository.appTheme.collectAsStateWithLifecycle(initialValue = AppTheme.SYSTEM)
            val darkTheme = appTheme.isDark()

            // Statusleisten-Symbole passend zum gewählten App-Theme einfärben.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                )
                onDispose {}
            }

            EasyPubTheme(darkTheme = darkTheme) {
                EasyPubNavHost()
            }
        }
    }
}
