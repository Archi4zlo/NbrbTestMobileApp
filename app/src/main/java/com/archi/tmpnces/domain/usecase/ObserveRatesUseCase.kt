package com.archi.tmpnces.domain.usecase

import com.archi.tmpnces.domain.model.RateWithChange
import com.archi.tmpnces.domain.repository.RateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class ObserveRatesUseCase @Inject constructor(
	private val repository: RateRepository
) {
	
	operator fun invoke(date: LocalDate): Flow<List<RateWithChange>> = combine(
		repository.observeRates(date),
		repository.observeRates(date.minusDays(1)),
	) { current, previous ->
		
		val previousByCode = previous.associateBy { it.abbreviation }
		
		current.map { rate ->
			RateWithChange(
				rate = rate,
				previousRatePerUnit = previousByCode[rate.abbreviation]?.ratePerUnit,
			)
		}
	}
}