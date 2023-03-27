package io.github.celeswuff.ktpwarp.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf

val LocalSnackbarHost = compositionLocalOf<SnackbarHostState?> { null }
