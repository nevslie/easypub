package io.github.nevslie.easypub.ui.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.nevslie.easypub.R
import io.github.nevslie.easypub.data.db.BookEntity
import io.github.nevslie.easypub.ui.components.BookCover
import io.github.nevslie.easypub.ui.components.EmptyState
import io.github.nevslie.easypub.ui.components.LargeTitle
import io.github.nevslie.easypub.ui.reader.ReaderActivity

@Composable
fun LibraryScreen(
    onBack: () -> Unit,
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let(viewModel::relink)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryEvent.OpenReader -> context.startActivity(ReaderActivity.intent(context, event.bookId))
                is LibraryEvent.ShowMessage -> snackbarHostState.showSnackbar(context.getString(event.message))
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 108.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LargeTitle(
                    title = uiState.library?.name.orEmpty(),
                    navigation = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                            )
                        }
                    },
                    actions = {
                        if (uiState.isScanning) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(20.dp),
                            )
                        } else if (uiState.hasAccess) {
                            IconButton(onClick = viewModel::refresh) {
                                Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.library_rescan))
                            }
                        }
                    },
                )
            }

            when {
                uiState.isLoading -> Unit

                !uiState.hasAccess -> fullWidthItem {
                    EmptyState(
                        icon = Icons.Outlined.FolderOff,
                        title = stringResource(R.string.library_no_access_title),
                        message = stringResource(R.string.library_no_access_message),
                        actionLabel = stringResource(R.string.library_select_folder_again),
                        onAction = { pickFolder.launch(null) },
                    )
                }

                uiState.totalBookCount == 0 -> fullWidthItem {
                    if (uiState.isScanning) {
                        EmptyState(
                            icon = Icons.AutoMirrored.Outlined.MenuBook,
                            title = stringResource(R.string.library_scanning_title),
                            message = stringResource(R.string.library_scanning_message),
                        )
                    } else {
                        EmptyState(
                            icon = Icons.AutoMirrored.Outlined.MenuBook,
                            title = stringResource(R.string.library_empty_title),
                            message = stringResource(R.string.library_empty_message),
                        )
                    }
                }

                else -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SearchField(query = query, onQueryChange = viewModel::onQueryChange)
                    }
                    if (uiState.books.isEmpty()) {
                        fullWidthItem {
                            EmptyState(
                                icon = Icons.Outlined.SearchOff,
                                title = stringResource(R.string.search_no_results_title),
                                message = stringResource(R.string.search_no_results_message, query),
                            )
                        }
                    }
                    items(uiState.books, key = { it.id }) { book ->
                        BookItem(
                            book = book,
                            isOpening = uiState.openingBookId == book.id,
                            onClick = { viewModel.openBook(book) },
                        )
                    }
                }
            }
        }
    }
}

private fun LazyGridScope.fullWidthItem(
    content: @Composable () -> Unit,
) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.search_clear))
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    )
}

@Composable
private fun BookItem(book: BookEntity, isOpening: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = MaterialTheme.shapes.small,
    ) {
        Column {
            Box {
                BookCover(
                    bookId = book.id,
                    title = book.title,
                    coverPath = book.coverPath,
                    progression = book.progression,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f),
                )
                if (isOpening) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(28.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = book.title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (book.author != null) {
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
