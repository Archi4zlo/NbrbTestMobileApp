package com.archi.tmpnces.data.local.entity

import androidx.room.Entity
import java.time.LocalDate

@Entity(
	tableName = "rates",
	primaryKeys = ["date", "curId"],
)
data class RateEntity(
	val date: LocalDate,
	val curId: Int,
	val abbreviation: String,
	val name: String,
	val scale: Int,
	val officialRate: Double,
)