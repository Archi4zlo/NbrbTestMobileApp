package com.archi.tmpnces.domain.usecase

import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.domain.model.CurrencyOption
import com.archi.tmpnces.domain.repository.RateRepository
import java.time.LocalDate
import javax.inject.Inject

class GetAvailableCurrenciesUseCase @Inject constructor(
	private val repository: RateRepository
) {
	
	suspend operator fun invoke(onDate: LocalDate = LocalDate.now()): Result<List<CurrencyOption>> =
		when (val result = repository.getCurrencyVersions()) {
			
			is Result.Error -> result
			
			is Result.Success -> Result.Success(result.data.filter { version -> version.overlaps(onDate, onDate) }
				                                    .map { version ->
					                                    CurrencyOption(
						                                    abbreviation = version.abbreviation.uppercase(),
						                                    name = version.name,
					                                    )
				                                    }
				                                    .distinctBy { option -> option.abbreviation }
				                                    .sortedBy { option -> option.abbreviation })
		}
}