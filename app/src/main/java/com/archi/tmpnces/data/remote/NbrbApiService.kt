package com.archi.tmpnces.data.remote

import com.archi.tmpnces.data.remote.dto.CurrencyDto
import com.archi.tmpnces.data.remote.dto.RateDynamicDto
import com.archi.tmpnces.data.remote.dto.RateDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NbrbApiService {
	
	// GET https://api.nbrb.by/exrates/rates?periodicity=0
	// GET https://api.nbrb.by/exrates/rates?periodicity=0&ondate=2026-08-30
	@GET("exrates/rates")
	suspend fun getRates(
		@Query("periodicity") periodicity: Int = 0,
		@Query("ondate") onDate: String? = null,
	): List<RateDto>
	
	// GET https://api.nbrb.by/exrates/rates/dynamics/431?startdate=2026-08-01&enddate=2026-08-31
	@GET("exrates/rates/dynamics/{curId}")
	suspend fun getRateDynamics(
		@Path("curId") curId: Int,
		@Query("startdate") startDate: String,
		@Query("enddate") endDate: String,
	): List<RateDynamicDto>
	
	// GET https://api.nbrb.by/exrates/currencies
	@GET("exrates/currencies")
	suspend fun getCurrencies(): List<CurrencyDto>
	
	// GET https://api.nbrb.by/exrates/currencies/456
	@GET("exrates/currencies/{curId}")
	suspend fun getCurrency(@Path("curId") curId: Int): CurrencyDto
}