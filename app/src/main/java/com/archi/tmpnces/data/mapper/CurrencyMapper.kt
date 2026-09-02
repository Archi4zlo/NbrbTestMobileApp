package com.archi.tmpnces.data.mapper

import com.archi.tmpnces.data.local.entity.RateDynamicsEntity
import com.archi.tmpnces.data.remote.dto.CurrencyDto
import com.archi.tmpnces.data.remote.dto.RateDynamicDto
import com.archi.tmpnces.domain.model.CurrencyVersion
import com.archi.tmpnces.domain.model.RatePoint
import java.time.LocalDate
import java.time.LocalDateTime

fun CurrencyDto.toDomain(): CurrencyVersion = CurrencyVersion(
	curId = curId,
	parentId = curParentId,
	abbreviation = abbreviation,
	name = name,
	scale = scale,
	validFrom = parseApiDate(dateStart),
	validTo = parseApiDate(dateEnd),
)

fun RateDynamicDto.toEntity(): RateDynamicsEntity = RateDynamicsEntity(
	curId = curId, date = parseApiDate(date), officialRate = officialRate
)

fun RateDynamicsEntity.toDomain(scale: Int): RatePoint = RatePoint(
	date = date, ratePerUnit = officialRate / scale
)

private fun parseApiDate(value: String): LocalDate = LocalDateTime.parse(value)
	.toLocalDate()