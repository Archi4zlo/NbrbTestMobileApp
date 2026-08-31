package com.archi.tmpnces.domain.repository

import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.domain.model.Rate
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RateRepository {
    
    fun observeRates(date: LocalDate): Flow<List<Rate>>
    
    suspend fun refreshRates(date: LocalDate): Result<Unit>

    suspend fun getRates(date: LocalDate): Result<List<Rate>>
}