package com.archi.tmpnces.presentation.rates

import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.domain.model.RateWithChange
import com.archi.tmpnces.domain.usecase.ObserveRatesUseCase
import com.archi.tmpnces.domain.usecase.RefreshRatesUseCase
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

class RatesStoreFactory @Inject constructor(
	private val storeFactory: StoreFactory,
	private val observeRates: ObserveRatesUseCase,
	private val refreshRates: RefreshRatesUseCase
) {
	
	fun create(date: LocalDate): RatesStore =
		object : RatesStore, Store<RatesStore.Intent, RatesStore.State, RatesStore.Label> by storeFactory.create(
			name = "RatesStore",
			initialState = RatesStore.State(date = date),
			bootstrapper = SimpleBootstrapper(Action.Load),
			executorFactory = { ExecutorImpl(date, observeRates, refreshRates) },
			reducer = ReducerImpl,
		) {}
	
	private sealed interface Action {
		data object Load : Action
	}
	
	private sealed interface Msg {
		data class RatesUpdated(val rates: List<RateWithChange>) : Msg
		data object LoadingStarted : Msg
		data object LoadingFinished : Msg
	}
	
	private class ExecutorImpl(
		private val date: LocalDate,
		private val observeRates: ObserveRatesUseCase,
		private val refreshRates: RefreshRatesUseCase
	) : CoroutineExecutor<RatesStore.Intent, Action, RatesStore.State, Msg, RatesStore.Label>() {
		
		override fun executeAction(action: Action) {
			when (action) {
				Action.Load -> {
					observeRatesFromDatabase()
					refresh()
				}
			}
		}
		
		override fun executeIntent(intent: RatesStore.Intent) {
			when (intent) {
				RatesStore.Intent.Refresh -> {
					if (!state().isLoading) refresh()
				}
			}
		}
		
		private fun observeRatesFromDatabase() {
			scope.launch {
				observeRates(date).collect { rates ->
					dispatch(Msg.RatesUpdated(rates))
				}
			}
		}
		
		private fun refresh() {
			dispatch(Msg.LoadingStarted)
			
			scope.launch {
				when (val result = refreshRates(date)) {
					is Result.Success -> Unit
					is Result.Error -> publish(RatesStore.Label.RefreshFailed(result.exception))
				}
				
				dispatch(Msg.LoadingFinished)
			}
		}
	}
	
	private object ReducerImpl : Reducer<RatesStore.State, Msg> {
		override fun RatesStore.State.reduce(msg: Msg): RatesStore.State = when (msg) {
			is Msg.RatesUpdated -> copy(rates = msg.rates)
			Msg.LoadingStarted -> copy(isLoading = true)
			Msg.LoadingFinished -> copy(isLoading = false)
		}
	}
}