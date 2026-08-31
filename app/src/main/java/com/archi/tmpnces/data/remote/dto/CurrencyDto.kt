package com.archi.tmpnces.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrencyDto(
	@SerialName("Cur_ID") val curId: Int,
	@SerialName("Cur_ParentID") val curParentId: Int,
	@SerialName("Cur_Abbreviation") val abbreviation: String,
	@SerialName("Cur_Name") val name: String,
	@SerialName("Cur_Scale") val scale: Int,
	@SerialName("Cur_DateStart") val dateStart: String,
	@SerialName("Cur_DateEnd") val dateEnd: String,
)