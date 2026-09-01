package com.archi.tmpnces.presentation.common

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

@Composable
fun ErrorSnackbarEffect(
	errors: Flow<Throwable>,
	snackbarHostState: SnackbarHostState,
	message: String,
	key: Any,
) {
	LaunchedEffect(key) {
		errors.collect {
			if (snackbarHostState.currentSnackbarData == null) {
				snackbarHostState.showSnackbar(
					message = message, duration = SnackbarDuration.Short
				)
			}
		}
	}
}