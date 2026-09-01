package com.archi.tmpnces.domain.usecase

import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.domain.repository.RateRepository
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

class RefreshRatesUseCase @Inject constructor(
	private val repository: RateRepository
) {
	
	suspend operator fun invoke(date: LocalDate): Result<Unit> {
		val previousDay = repository.getRates(date.minusDays(1))
		
		if (previousDay is Result.Error) {
			Timber.w(previousDay.exception, "Курсы за %s недоступны, изменение показано не будет", date.minusDays(1))
		}
		
		return repository.refreshRates(date)
	}
}