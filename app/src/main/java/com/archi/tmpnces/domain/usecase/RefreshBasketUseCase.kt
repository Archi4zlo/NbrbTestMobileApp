package com.archi.tmpnces.domain.usecase

import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.domain.repository.RateRepository
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

class RefreshBasketUseCase @Inject constructor(
	private val repository: RateRepository
) {
	
	suspend operator fun invoke(date: LocalDate): Result<Unit> {
		val previousDay = date.minusDays(1)
		val yearStart = ObserveBasketUseCase.yearStartOf(date)
		
		loadComparisonBase(previousDay, "предыдущий день")
		loadComparisonBase(yearStart, "конец прошлого года")
		
		return repository.refreshRates(date)
	}
	
	private suspend fun loadComparisonBase(date: LocalDate, description: String) {
		val result = repository.getRates(date)
		if (result is Result.Error) {
			Timber.w(result.exception, "Не удалось получить курсы за %s (%s)", date, description)
		}
	}
}