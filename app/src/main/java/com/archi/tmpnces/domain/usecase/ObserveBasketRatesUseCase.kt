package com.archi.tmpnces.domain.usecase

import com.archi.tmpnces.domain.model.Rate
import com.archi.tmpnces.domain.repository.RateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class ObserveBasketRatesUseCase @Inject constructor(
	private val repository: RateRepository
) {
	
	operator fun invoke(date: LocalDate): Flow<List<Rate>> = repository.observeRates(date)
		.map { rates ->
			BASKET_CURRENCIES.mapNotNull { code ->
				rates.firstOrNull { it.abbreviation == code }
			}
		}
	
	companion object {
		val BASKET_CURRENCIES = listOf("USD", "EUR", "RUB")
	}
}