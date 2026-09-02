package com.archi.tmpnces.presentation.chart

import com.archi.tmpnces.domain.model.CurrencyOption
import com.archi.tmpnces.domain.model.RateDynamics
import com.arkivanov.mvikotlin.core.store.Store

interface ChartStore : Store<ChartStore.Intent, ChartStore.State, ChartStore.Label> {
	
	sealed interface Intent {
		data class SelectCurrency(val abbreviation: String) : Intent
		data class SelectPeriod(val period: ChartPeriod) : Intent
		data object Reload : Intent
	}
	
	data class State(
		val currencies: List<CurrencyOption> = emptyList(),
		val selectedCurrency: String = DEFAULT_CURRENCY,
		val period: ChartPeriod = ChartPeriod.WEEK,
		val dynamics: RateDynamics? = null,
		val isLoading: Boolean = false,
	)
	
	sealed interface Label {
		data class LoadFailed(val throwable: Throwable) : Label
	}
	
	companion object {
		const val DEFAULT_CURRENCY = "USD"
	}
}