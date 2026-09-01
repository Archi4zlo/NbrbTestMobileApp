package com.archi.tmpnces.domain.model

data class RateWithChange(
	val rate: Rate,
	val previousRatePerUnit: Double?,
) {
	val change: Double?
		get() = previousRatePerUnit?.let { rate.ratePerUnit - it }
	
	val changeInQuoteUnits: Double?
		get() = change?.let { it * rate.scale }
	
	val direction: ChangeDirection
		get() {
			val delta = changeInQuoteUnits ?: return ChangeDirection.UNKNOWN
			return when {
				delta > DISPLAY_THRESHOLD -> ChangeDirection.UP
				delta < -DISPLAY_THRESHOLD -> ChangeDirection.DOWN
				else -> ChangeDirection.UNCHANGED
			}
		}
	
	private companion object {
		const val DISPLAY_THRESHOLD = 0.00005
	}
}

enum class ChangeDirection {
	UP,
	DOWN,
	UNCHANGED,
	UNKNOWN,
}