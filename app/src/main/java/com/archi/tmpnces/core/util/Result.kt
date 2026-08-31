package com.archi.tmpnces.core.util

import kotlin.coroutines.cancellation.CancellationException

sealed interface Result<out T> {
	
	data class Success<T>(val data: T) : Result<T>
	
	data class Error(val exception: Throwable) : Result<Nothing>
}

inline fun <T> runCatchingCancellable(block: () -> T): Result<T> = try {
	Result.Success(block())
} catch (e: CancellationException) {
	throw e
} catch (e: Throwable) {
	Result.Error(e)
}