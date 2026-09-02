package com.archi.tmpnces.data.local.entity

import androidx.room.Entity
import java.time.LocalDate

@Entity(
	tableName = "rate_dynamics",
	primaryKeys = ["curId", "date"],
)
data class RateDynamicsEntity(
	val curId: Int,
	val date: LocalDate,
	val officialRate: Double,
)