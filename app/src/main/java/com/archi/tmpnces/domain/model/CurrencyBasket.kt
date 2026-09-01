package com.archi.tmpnces.domain.model

import java.time.LocalDate

data class CurrencyBasket(
	val date: LocalDate,
	val basket: BasketValue,
	val components: List<ComponentRate>,
)

data class BasketValue(
	val value: Double,
	val sinceYearStart: RubleChange?,
	val sincePreviousDay: RubleChange?,
)

data class ComponentRate(
	val abbreviation: String,
	val label: String,
	val officialRate: Double,
	val sinceYearStart: RubleChange?,
	val sincePreviousDay: RubleChange?,
)

@JvmInline
value class RubleChange(val percent: Double) {
	
	val trend: RubleTrend
		get() = when {
			percent > TREND_EPSILON -> RubleTrend.STRENGTHENED
			percent < -TREND_EPSILON -> RubleTrend.WEAKENED
			else -> RubleTrend.UNCHANGED
		}
	
	private companion object {
		const val TREND_EPSILON = 0.005
	}
}

enum class RubleTrend {
	STRENGTHENED,
	WEAKENED,
	UNCHANGED,
}