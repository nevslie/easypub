package io.github.nevslie.easypub.ui.reader

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.fragment.compose.AndroidFragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.nevslie.easypub.R
import io.github.nevslie.easypub.data.settings.FontSize
import io.github.nevslie.easypub.ui.theme.EasyPubTheme
import io.github.nevslie.easypub.ui.theme.ReaderDarkBackground
import io.github.nevslie.easypub.ui.theme.ReaderLightBackground
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.input.InputListener
import org.readium.r2.navigator.input.TapEvent

@Composable
fun ReaderScreen(
    title: String,
    viewModel: ReaderViewModel,
    onBack: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var navigator by remember { mutableStateOf<EpubNavigatorFragment?>(null) }
    var barVisible by rememberSaveable { mutableStateOf(false) }

    // Die Leiste folgt dem Hell/Dunkel des Buchinhalts, damit sie sich ins Lesen einfügt.
    EasyPubTheme(darkTheme = settings.darkContent) {
        SystemBarAppearance(darkContent = settings.darkContent)

        Box(
            Modifier
                .fillMaxSize()
                .background(if (settings.darkContent) ReaderDarkBackground else ReaderLightBackground),
        ) {
            AndroidFragment<EpubNavigatorFragment>(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                onUpdate = { fragment -> navigator = fragment },
            )

            AnimatedVisibility(
                visible = barVisible,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier.align(Alignment.TopCenter),
            ) {
                ReaderBar(
                    title = title,
                    darkContent = settings.darkContent,
                    fontSize = settings.fontSize,
                    onBack = onBack,
                    onToggleDark = viewModel::toggleDarkContent,
                    onDecreaseFont = viewModel::decreaseFontSize,
                    onIncreaseFont = viewModel::increaseFontSize,
                )
            }
        }
    }

    // Einmal pro Navigator: Tippen blendet die Leiste ein/aus, Positionswechsel werden gespeichert.
    LaunchedEffect(navigator) {
        val nav = navigator ?: return@LaunchedEffect
        nav.addInputListener(object : InputListener {
            override fun onTap(event: TapEvent): Boolean {
                barVisible = !barVisible
                return true
            }
        })
        nav.currentLocator.collect { viewModel.onLocatorChanged(it) }
    }

    // Einstellungen an Readium weitergeben, sobald sie sich ändern.
    LaunchedEffect(navigator, settings) {
        navigator?.submitPreferences(settings.toEpubPreferences())
    }
}

@Composable
private fun ReaderBar(
    title: String,
    darkContent: Boolean,
    fontSize: Double,
    onBack: () -> Unit,
    onToggleDark: () -> Unit,
    onDecreaseFont: () -> Unit,
    onIncreaseFont: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 6.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(4.dp))

            TextButton(onClick = onDecreaseFont, enabled = FontSize.canDecrease(fontSize)) {
                Text("A", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = stringResource(R.string.reader_font_size_percent, FontSize.percent(fontSize)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onIncreaseFont, enabled = FontSize.canIncrease(fontSize)) {
                Text("A", fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            }

            IconButton(onClick = onToggleDark) {
                Icon(
                    imageVector = if (darkContent) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = stringResource(
                        if (darkContent) R.string.reader_switch_to_light else R.string.reader_switch_to_dark
                    ),
                )
            }
        }
    }
}

/** Passt die Farbe der Statusleisten-Symbole an den Hintergrund des Buchinhalts an. */
@Composable
private fun SystemBarAppearance(darkContent: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, darkContent) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkContent
                isAppearanceLightNavigationBars = !darkContent
            }
        }
        onDispose {}
    }
}
