package com.archi.tmpnces.domain.model

import java.time.LocalDate

data class CurrencyVersion(
	val curId: Int,
	val parentId: Int,
	val abbreviation: String,
	val name: String,
	val scale: Int,
	val validFrom: LocalDate,
	val validTo: LocalDate,
) {
	fun overlaps(from: LocalDate, to: LocalDate): Boolean = !validFrom.isAfter(to) && !validTo.isBefore(from)
}