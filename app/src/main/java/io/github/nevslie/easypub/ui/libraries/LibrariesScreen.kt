package io.github.nevslie.easypub.ui.libraries

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.nevslie.easypub.R
import io.github.nevslie.easypub.ui.components.EmptyState
import io.github.nevslie.easypub.ui.components.LargeTitle
import io.github.nevslie.easypub.ui.theme.accentColorFor

@Composable
fun LibrariesScreen(
    onOpenLibrary: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: LibrariesViewModel = viewModel(factory = LibrariesViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Ordner-Explorer von Android. Der Nutzer kann dort auch direkt einen neuen Ordner anlegen.
    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let(viewModel::addLibrary)
    }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(context.getString(it)) }
    }

    var libraryToRemove by remember { mutableStateOf<LibraryTile?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            uiState.isLoading -> Unit

            uiState.libraries.isEmpty() -> Column(Modifier.padding(padding)) {
                LibrariesHeader(onOpenSettings, Modifier.padding(horizontal = 16.dp))
                EmptyState(
                    icon = Icons.Outlined.AutoStories,
                    title = stringResource(R.string.libraries_empty_title),
                    message = stringResource(R.string.libraries_empty_message),
                    actionLabel = stringResource(R.string.libraries_add),
                    onAction = { pickFolder.launch(null) },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 24.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LibrariesHeader(onOpenSettings)
                }
                items(uiState.libraries, key = { it.library.id }) { tile ->
                    LibraryTileCard(
                        tile = tile,
                        onClick = { onOpenLibrary(tile.library.id) },
                        onRescan = { viewModel.rescan(tile.library.id) },
                        onRemove = { libraryToRemove = tile },
                    )
                }
                item(key = "add") {
                    AddLibraryTile(onClick = { pickFolder.launch(null) })
                }
            }
        }
    }

    libraryToRemove?.let { tile ->
        AlertDialog(
            onDismissRequest = { libraryToRemove = null },
            title = { Text(stringResource(R.string.library_remove_title, tile.library.name)) },
            text = { Text(stringResource(R.string.library_remove_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeLibrary(tile.library.id)
                    libraryToRemove = null
                }) { Text(stringResource(R.string.library_remove_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { libraryToRemove = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun LibrariesHeader(onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    LargeTitle(
        title = stringResource(R.string.libraries_title),
        modifier = modifier,
        actions = {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings_title))
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryTileCard(
    tile: LibraryTile,
    onClick: () -> Unit,
    onRescan: () -> Unit,
    onRemove: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box {
        Surface(
            color = accentColorFor(tile.library.id),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.1f)
                .clip(MaterialTheme.shapes.large)
                .combinedClickable(onClick = onClick, onLongClick = { menuOpen = true }),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = if (tile.hasAccess) Icons.Outlined.AutoStories else Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = if (tile.hasAccess) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                    if (tile.isScanning) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.TopEnd),
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = tile.library.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (tile.hasAccess) {
                        context.resources.getQuantityString(R.plurals.book_count, tile.bookCount, tile.bookCount)
                    } else {
                        stringResource(R.string.library_no_access_short)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.library_rescan)) },
                onClick = {
                    menuOpen = false
                    onRescan()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.library_remove)) },
                onClick = {
                    menuOpen = false
                    onRemove()
                },
            )
        }
    }
}

@Composable
private fun AddLibraryTile(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.libraries_add),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
