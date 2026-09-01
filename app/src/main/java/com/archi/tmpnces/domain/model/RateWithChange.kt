package com.archi.tmpnces.domain.model

data class RateWithChange(
	val rate: Rate,
	val previousRatePerUnit: Double?,
) {
	val change: Double?
		get() = previousRatePerUnit?.let { rate.ratePerUnit - it }
	
	val direction: ChangeDirection
		get() {
			val delta = change ?: return ChangeDirection.UNKNOWN
			return when {
				delta > CHANGE_EPSILON -> ChangeDirection.UP
				delta < -CHANGE_EPSILON -> ChangeDirection.DOWN
				else -> ChangeDirection.UNCHANGED
			}
		}
	
	private companion object {
		const val CHANGE_EPSILON = 1e-6
	}
}

enum class ChangeDirection {
	UP,
	DOWN,
	UNCHANGED,
	UNKNOWN,
}