package com.archi.tmpnces.domain.model

import java.time.LocalDate

data class Rate(
	val curId: Int,
	val date: LocalDate,
	val abbreviation: String,
	val name: String,
	val scale: Int,
	val officialRate: Double,
) {
	val ratePerUnit: Double
		get() = officialRate / scale
}