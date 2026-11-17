package io.github.nevslie.easypub.ui.util

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.nevslie.easypub.AppContainer
import io.github.nevslie.easypub.EasyPubApp

/** Zugriff auf den [AppContainer] innerhalb eines `viewModelFactory { initializer { ... } }`. */
fun CreationExtras.appContainer(): AppContainer =
    (checkNotNull(this[APPLICATION_KEY]) as EasyPubApp).container
