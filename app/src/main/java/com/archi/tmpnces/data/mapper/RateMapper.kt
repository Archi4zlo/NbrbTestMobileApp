package com.archi.tmpnces.data.mapper

import com.archi.tmpnces.data.local.entity.RateEntity
import com.archi.tmpnces.data.remote.dto.RateDto
import com.archi.tmpnces.domain.model.Rate
import java.time.LocalDate
import java.time.LocalDateTime

fun RateDto.toEntity(): RateEntity = RateEntity(
	date = parseApiDate(date),
	curId = curId,
	abbreviation = abbreviation,
	name = name,
	scale = scale,
	officialRate = officialRate,
)

fun RateEntity.toDomain(): Rate = Rate(
	curId = curId,
	date = date,
	abbreviation = abbreviation,
	name = name,
	scale = scale,
	officialRate = officialRate,
)

fun List<RateDto>.toEntities(): List<RateEntity> = map { it.toEntity() }

fun List<RateEntity>.toDomainModels(): List<Rate> = map { it.toDomain() }

private fun parseApiDate(value: String): LocalDate = LocalDateTime.parse(value)
	.toLocalDate()