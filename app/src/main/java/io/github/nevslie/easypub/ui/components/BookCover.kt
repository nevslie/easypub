package io.github.nevslie.easypub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.nevslie.easypub.domain.Progress
import io.github.nevslie.easypub.ui.theme.accentColorFor
import java.io.File

/**
 * Cover eines Buchs im Seitenverhältnis 2:3. Ohne Cover-Bild wird ein farbiger Platzhalter
 * mit dem Titel angezeigt. Ein angefangenes Buch zeigt unten einen dünnen Fortschrittsbalken.
 */
@Composable
fun BookCover(
    bookId: Long,
    title: String,
    coverPath: String?,
    progression: Double?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(accentColorFor(bookId)),
    ) {
        if (coverPath != null) {
            AsyncImage(
                model = File(coverPath),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(12.dp),
            )
        }

        if (Progress.isStarted(progression)) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(Progress.percent(progression) / 100f)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}
