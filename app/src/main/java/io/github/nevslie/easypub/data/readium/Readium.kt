package io.github.nevslie.easypub.data.readium

import android.content.Context
import android.net.Uri
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.toAbsoluteUrl
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser

/**
 * Bündelt die Readium-Komponenten zum Öffnen von EPUBs.
 *
 * Wird sowohl beim Scannen (Metadaten und Cover auslesen) als auch zum Lesen verwendet.
 */
class Readium(context: Context) {

    private val httpClient = DefaultHttpClient()

    private val assetRetriever = AssetRetriever(context.contentResolver, httpClient)

    private val publicationOpener = PublicationOpener(
        publicationParser = DefaultPublicationParser(
            context = context,
            httpClient = httpClient,
            assetRetriever = assetRetriever,
            pdfFactory = null,
        ),
    )

    /**
     * Öffnet das EPUB unter [uri] (eine `content://`-URI aus dem Ordner-Explorer).
     * Die zurückgegebene [Publication] muss mit [Publication.close] geschlossen werden.
     */
    suspend fun open(uri: Uri): Result<Publication> {
        val url = uri.toAbsoluteUrl()
            ?: return Result.failure(IllegalArgumentException("Ungültige URI: $uri"))

        val asset = when (val result = assetRetriever.retrieve(url)) {
            is Try.Success -> result.value
            is Try.Failure -> return Result.failure(ReadiumException(result.value.message))
        }

        return when (val result = publicationOpener.open(asset, allowUserInteraction = false)) {
            is Try.Success -> Result.success(result.value)
            is Try.Failure -> {
                asset.close()
                Result.failure(ReadiumException(result.value.message))
            }
        }
    }
}

class ReadiumException(message: String) : Exception(message)
