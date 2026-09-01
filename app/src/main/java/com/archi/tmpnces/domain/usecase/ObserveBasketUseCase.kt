package com.archi.tmpnces.domain.usecase

import com.archi.tmpnces.domain.model.BasketValue
import com.archi.tmpnces.domain.model.ComponentRate
import com.archi.tmpnces.domain.model.CurrencyBasket
import com.archi.tmpnces.domain.model.Rate
import com.archi.tmpnces.domain.model.RubleChange
import com.archi.tmpnces.domain.repository.RateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.pow

class ObserveBasketUseCase @Inject constructor(
	private val repository: RateRepository
) {
	
	operator fun invoke(date: LocalDate): Flow<CurrencyBasket?> = combine(
		repository.observeRates(date),
		repository.observeRates(date.minusDays(1)),
		repository.observeRates(yearStartOf(date))
	) { current, previousDay, yearStart ->
		buildBasket(date, current, previousDay, yearStart)
	}
	
	private fun buildBasket(
		date: LocalDate, current: List<Rate>, previousDay: List<Rate>, yearStart: List<Rate>
	): CurrencyBasket? {
		val currentByCode = current.associateBy { it.abbreviation }
		val previousByCode = previousDay.associateBy { it.abbreviation }
		val yearStartByCode = yearStart.associateBy { it.abbreviation }
		
		val basketNow = basketValue(currentByCode) ?: return null
		
		val components = WEIGHTS.map { (code, _) ->
			val rate = currentByCode.getValue(code)
			
			ComponentRate(
				abbreviation = code,
				label = "${rate.scale} ${rate.name}",
				officialRate = rate.officialRate,
				sinceYearStart = rubleChange(
					now = rate.ratePerUnit, base = yearStartByCode[code]?.ratePerUnit
				),
				sincePreviousDay = rubleChange(
					now = rate.ratePerUnit, base = previousByCode[code]?.ratePerUnit
				),
			)
		}
		
		return CurrencyBasket(
			date = date, basket = BasketValue(
				value = basketNow,
				sinceYearStart = rubleChange(basketNow, basketValue(yearStartByCode)),
				sincePreviousDay = rubleChange(basketNow, basketValue(previousByCode))
			), components = components
		)
	}
	
	private fun basketValue(ratesByCode: Map<String, Rate>): Double? = WEIGHTS.fold(1.0) { product, (code, weight) ->
		val rate = ratesByCode[code] ?: return null
		product * rate.ratePerUnit.pow(weight)
	}
	
	private fun rubleChange(now: Double, base: Double?): RubleChange? {
		if (base == null || base == 0.0) return null
		return RubleChange(-((now - base) / base) * 100.0)
	}
	
	companion object {
		val WEIGHTS: List<Pair<String, Double>> = listOf(
			"RUB" to 0.6, "USD" to 0.3, "CNY" to 0.1
		)
		
		fun yearStartOf(date: LocalDate): LocalDate = LocalDate.of(date.year - 1, 12, 31)
	}
}