package com.archi.tmpnces.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RateDto(
	@SerialName("Cur_ID") val curId: Int,
	@SerialName("Date") val date: String,
	@SerialName("Cur_Abbreviation") val abbreviation: String,
	@SerialName("Cur_Scale") val scale: Int,
	@SerialName("Cur_Name") val name: String,
	@SerialName("Cur_OfficialRate") val officialRate: Double,
)