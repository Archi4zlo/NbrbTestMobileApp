package com.archi.tmpnces.domain.usecase

import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.domain.model.CurrencyVersion
import com.archi.tmpnces.domain.model.RateDynamics
import com.archi.tmpnces.domain.model.RatePoint
import com.archi.tmpnces.domain.repository.RateRepository
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

class GetRateDynamicsUseCase @Inject constructor(
	private val repository: RateRepository
) {
	
	suspend operator fun invoke(
		abbreviation: String,
		from: LocalDate,
		to: LocalDate,
	): Result<RateDynamics> {
		
		val versionsResult = repository.getCurrencyVersions()
		if (versionsResult is Result.Error) return versionsResult
		
		val allVersions = (versionsResult as Result.Success).data
		val relevant = selectVersions(allVersions, abbreviation, from, to)
		
		if (relevant.isEmpty()) {
			return Result.Error(IllegalStateException("Валюта $abbreviation не найдена за период $from..$to"))
		}
		
		Timber.d("Период %s..%s покрывают %d версии %s", from, to, relevant.size, abbreviation)
		
		val points = mutableListOf<RatePoint>()
		
		for (version in relevant) {
			val windowFrom = maxOf(from, version.validFrom)
			val windowTo = minOf(to, version.validTo)
			
			when (val result = repository.getRateDynamics(version, windowFrom, windowTo)) {
				is Result.Success -> points += result.data
				is Result.Error -> return result
			}
		}
		
		return Result.Success(
			RateDynamics(
				abbreviation = abbreviation,
				name = relevant.last().name,
				points = points.sortedBy { it.date },
			)
		)
	}
	
	private fun selectVersions(
		all: List<CurrencyVersion>,
		abbreviation: String,
		from: LocalDate,
		to: LocalDate,
	): List<CurrencyVersion> = all.filter { it.abbreviation.equals(abbreviation, ignoreCase = true) }
		.filter { it.overlaps(from, to) }
		.sortedBy { it.validFrom }
}