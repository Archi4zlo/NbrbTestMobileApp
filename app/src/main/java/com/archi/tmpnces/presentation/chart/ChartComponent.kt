package com.archi.tmpnces.presentation.chart

import com.archi.tmpnces.domain.model.CurrencyOption
import com.archi.tmpnces.domain.model.RateDynamics
import com.archi.tmpnces.presentation.common.retainedStore
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.builtins.serializer

interface ChartComponent {
	
	val model: StateFlow<Model>
	
	val errors: Flow<Throwable>
	
	fun onCurrencySelected(abbreviation: String)
	
	fun onPeriodSelected(period: ChartPeriod)
	
	fun onReloadClicked()
	
	data class Model(
		val currencies: List<CurrencyOption>,
		val selectedCurrency: String,
		val period: ChartPeriod,
		val dynamics: RateDynamics?,
		val isLoading: Boolean,
	)
}

class DefaultChartComponent @AssistedInject constructor(
	storeFactory: ChartStoreFactory, @Assisted componentContext: ComponentContext
) : ChartComponent, ComponentContext by componentContext {
	
	@AssistedFactory
	interface Factory {
		fun create(componentContext: ComponentContext): DefaultChartComponent
	}
	
	private val store = retainedStore {
		storeFactory.create(
			initialCurrency = restoreCurrency(), initialPeriod = restorePeriod()
		)
	}
	
	init {
		stateKeeper.register(KEY_CURRENCY, String.serializer()) {
			store.state.selectedCurrency
		}
		stateKeeper.register(KEY_PERIOD, String.serializer()) {
			store.state.period.name
		}
	}
	
	private fun restoreCurrency(): String =
		stateKeeper.consume(KEY_CURRENCY, String.serializer()) ?: ChartStore.DEFAULT_CURRENCY
	
	private fun restorePeriod(): ChartPeriod = stateKeeper.consume(KEY_PERIOD, String.serializer())
		?.let { name -> ChartPeriod.entries.firstOrNull { it.name == name } } ?: ChartPeriod.WEEK
	
	private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())
	
	override val model: StateFlow<ChartComponent.Model> = store.states.map(::toModel)
		.stateIn(
			scope = scope,
			started = SharingStarted.Eagerly,
			initialValue = toModel(store.state),
		)
	
	override val errors: Flow<Throwable> = store.labels.map { label ->
		when (label) {
			is ChartStore.Label.LoadFailed -> label.throwable
		}
	}
	
	override fun onCurrencySelected(abbreviation: String) {
		store.accept(ChartStore.Intent.SelectCurrency(abbreviation))
	}
	
	override fun onPeriodSelected(period: ChartPeriod) {
		store.accept(ChartStore.Intent.SelectPeriod(period))
	}
	
	override fun onReloadClicked() {
		store.accept(ChartStore.Intent.Reload)
	}
	
	private fun toModel(state: ChartStore.State) = ChartComponent.Model(
		currencies = state.currencies,
		selectedCurrency = state.selectedCurrency,
		period = state.period,
		dynamics = state.dynamics,
		isLoading = state.isLoading,
	)
	
	private companion object {
		const val KEY_CURRENCY = "chart_selected_currency"
		const val KEY_PERIOD = "chart_selected_period"
	}
}