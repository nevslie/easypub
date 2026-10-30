package io.github.nevslie.easypub.ui.reader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import io.github.nevslie.easypub.appContainer
import io.github.nevslie.easypub.data.readium.ReaderSession
import io.github.nevslie.easypub.data.settings.ReaderSettings
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment

/**
 * Zeigt ein Buch an. Der Buchinhalt selbst wird vom Readium-Navigator dargestellt
 * (ein Fragment mit WebView), die Leiste darüber ist Compose.
 *
 * Das Buch wird vorher von der Library-Ansicht geöffnet und hier über die
 * [io.github.nevslie.easypub.data.readium.ReaderSessions] abgeholt.
 */
class ReaderActivity : FragmentActivity() {

    private val bookId: Long by lazy { intent.getLongExtra(EXTRA_BOOK_ID, -1L) }

    private val session: ReaderSession? by lazy { appContainer.readerSessions.get(bookId) }

    private val viewModel: ReaderViewModel by viewModels {
        ReaderViewModel.factory(bookId, session?.initialSettings ?: ReaderSettings())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val session = session

        // Die FragmentFactory muss vor super.onCreate() gesetzt werden, weil Android dort
        // ein bestehendes Navigator-Fragment wiederherstellt (z. B. nach einer Konfigurationsänderung).
        supportFragmentManager.fragmentFactory = session
            ?.let {
                EpubNavigatorFactory(it.publication).createFragmentFactory(
                    initialLocator = it.initialLocator,
                    initialPreferences = it.initialSettings.toEpubPreferences(),
                )
            }
            // Das Buch ist nicht mehr geöffnet (z. B. weil Android die App beendet hatte).
            // Die Dummy-Factory verhindert einen Absturz beim Wiederherstellen, danach wird beendet.
            ?: EpubNavigatorFragment.createDummyFactory()

        super.onCreate(savedInstanceState)

        if (session == null) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            ReaderScreen(
                title = session.book.title,
                viewModel = viewModel,
                onBack = ::finish,
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (session != null) viewModel.saveNow()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) appContainer.readerSessions.close(bookId)
    }

    companion object {
        private const val EXTRA_BOOK_ID = "bookId"

        fun intent(context: Context, bookId: Long): Intent =
            Intent(context, ReaderActivity::class.java).putExtra(EXTRA_BOOK_ID, bookId)
    }
}
