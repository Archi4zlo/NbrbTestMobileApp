package com.archi.tmpnces.domain.model

import java.time.LocalDate

data class RatePoint(
	val date: LocalDate,
	val ratePerUnit: Double,
)

data class RateDynamics(
	val abbreviation: String,
	val name: String,
	val points: List<RatePoint>,
)