package com.archi.tmpnces.presentation.chart

import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.domain.model.CurrencyOption
import com.archi.tmpnces.domain.model.RateDynamics
import com.archi.tmpnces.domain.usecase.GetAvailableCurrenciesUseCase
import com.archi.tmpnces.domain.usecase.GetRateDynamicsUseCase
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

class ChartStoreFactory @Inject constructor(
	private val storeFactory: StoreFactory,
	private val getAvailableCurrencies: GetAvailableCurrenciesUseCase,
	private val getRateDynamics: GetRateDynamicsUseCase
) {
	
	fun create(
		initialCurrency: String,
		initialPeriod: ChartPeriod,
	): ChartStore =
		object : ChartStore, Store<ChartStore.Intent, ChartStore.State, ChartStore.Label> by storeFactory.create(
			name = "ChartStore",
			initialState = ChartStore.State(
				selectedCurrency = initialCurrency, period = initialPeriod
			),
			bootstrapper = SimpleBootstrapper(Action.Load),
			executorFactory = { ExecutorImpl(getAvailableCurrencies, getRateDynamics) },
			reducer = ReducerImpl,
		) {}
	
	private sealed interface Action {
		data object Load : Action
	}
	
	private sealed interface Msg {
		data class CurrenciesLoaded(val currencies: List<CurrencyOption>) : Msg
		data class CurrencyChanged(val abbreviation: String) : Msg
		data class PeriodChanged(val period: ChartPeriod) : Msg
		data class DynamicsLoaded(val dynamics: RateDynamics) : Msg
		data object LoadingStarted : Msg
		data object LoadingFinished : Msg
	}
	
	private class ExecutorImpl(
		private val getAvailableCurrencies: GetAvailableCurrenciesUseCase,
		private val getRateDynamics: GetRateDynamicsUseCase
	) : CoroutineExecutor<ChartStore.Intent, Action, ChartStore.State, Msg, ChartStore.Label>() {
		
		private var dynamicsJob: Job? = null
		
		override fun executeAction(action: Action) {
			when (action) {
				Action.Load -> {
					loadCurrencies()
					loadDynamics(state().selectedCurrency, state().period)
				}
			}
		}
		
		override fun executeIntent(intent: ChartStore.Intent) {
			when (intent) {
				
				is ChartStore.Intent.SelectCurrency -> {
					if (intent.abbreviation == state().selectedCurrency) return
					dispatch(Msg.CurrencyChanged(intent.abbreviation))
					loadDynamics(intent.abbreviation, state().period)
				}
				
				is ChartStore.Intent.SelectPeriod -> {
					if (intent.period == state().period) return
					dispatch(Msg.PeriodChanged(intent.period))
					loadDynamics(state().selectedCurrency, intent.period)
				}
				
				ChartStore.Intent.Reload -> {
					if (!state().isLoading) {
						if (state().currencies.isEmpty()) loadCurrencies()
						loadDynamics(state().selectedCurrency, state().period)
					}
				}
			}
		}
		
		private fun loadCurrencies() {
			scope.launch {
				val result = getAvailableCurrencies()
				if (result is Result.Success) {
					dispatch(Msg.CurrenciesLoaded(result.data))
				}
			}
		}
		
		private fun loadDynamics(abbreviation: String, period: ChartPeriod) {
			dynamicsJob?.cancel()
			
			dispatch(Msg.LoadingStarted)
			
			dynamicsJob = scope.launch {
				val end = LocalDate.now()
				val start = period.startFrom(end)
				
				when (val result = getRateDynamics(abbreviation, start, end)) {
					is Result.Success -> dispatch(Msg.DynamicsLoaded(result.data))
					is Result.Error -> publish(ChartStore.Label.LoadFailed(result.exception))
				}
				
				dispatch(Msg.LoadingFinished)
			}
		}
	}
	
	private object ReducerImpl : Reducer<ChartStore.State, Msg> {
		override fun ChartStore.State.reduce(msg: Msg): ChartStore.State = when (msg) {
			
			is Msg.CurrenciesLoaded -> copy(currencies = msg.currencies)
			
			is Msg.CurrencyChanged -> copy(selectedCurrency = msg.abbreviation, dynamics = null)
			is Msg.PeriodChanged -> copy(period = msg.period, dynamics = null)
			
			is Msg.DynamicsLoaded -> copy(dynamics = msg.dynamics)
			Msg.LoadingStarted -> copy(isLoading = true)
			Msg.LoadingFinished -> copy(isLoading = false)
		}
	}
}