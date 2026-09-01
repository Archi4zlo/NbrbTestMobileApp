package com.archi.tmpnces.presentation.basket

import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.domain.model.CurrencyBasket
import com.archi.tmpnces.domain.usecase.ObserveBasketUseCase
import com.archi.tmpnces.domain.usecase.RefreshBasketUseCase
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

class BasketStoreFactory @Inject constructor(
	private val storeFactory: StoreFactory,
	private val observeBasket: ObserveBasketUseCase,
	private val refreshBasket: RefreshBasketUseCase
) {
	
	fun create(initialDate: LocalDate): BasketStore =
		object : BasketStore, Store<BasketStore.Intent, BasketStore.State, BasketStore.Label> by storeFactory.create(
			name = "BasketStore",
			initialState = BasketStore.State(date = initialDate),
			bootstrapper = SimpleBootstrapper(Action.Load),
			executorFactory = { ExecutorImpl(observeBasket, refreshBasket) },
			reducer = ReducerImpl,
		) {}
	
	private sealed interface Action {
		data object Load : Action
	}
	
	private sealed interface Msg {
		data class DateChanged(val date: LocalDate) : Msg
		data class BasketUpdated(val basket: CurrencyBasket?) : Msg
		data object LoadingStarted : Msg
		data object LoadingFinished : Msg
	}
	
	private class ExecutorImpl(
		private val observeBasket: ObserveBasketUseCase, private val refreshBasket: RefreshBasketUseCase
	) : CoroutineExecutor<BasketStore.Intent, Action, BasketStore.State, Msg, BasketStore.Label>() {
		
		private var observeJob: Job? = null
		
		override fun executeAction(action: Action) {
			when (action) {
				Action.Load -> load(state().date)
			}
		}
		
		override fun executeIntent(intent: BasketStore.Intent) {
			when (intent) {
				BasketStore.Intent.Reload -> load(state().date)
				
				is BasketStore.Intent.SelectDate -> {
					dispatch(Msg.DateChanged(intent.date))
					load(intent.date)
				}
			}
		}
		
		private fun load(date: LocalDate) {
			observeDatabase(date)
			refresh(date)
		}
		
		private fun observeDatabase(date: LocalDate) {
			observeJob?.cancel()
			
			observeJob = scope.launch {
				observeBasket(date).collect { basket ->
					dispatch(Msg.BasketUpdated(basket))
				}
			}
		}
		
		private fun refresh(date: LocalDate) {
			dispatch(Msg.LoadingStarted)
			
			scope.launch {
				when (val result = refreshBasket(date)) {
					is Result.Success -> Unit
					is Result.Error -> publish(BasketStore.Label.LoadFailed(result.exception))
				}
				
				dispatch(Msg.LoadingFinished)
			}
		}
	}
	
	private object ReducerImpl : Reducer<BasketStore.State, Msg> {
		override fun BasketStore.State.reduce(msg: Msg): BasketStore.State = when (msg) {
			is Msg.DateChanged -> copy(date = msg.date, basket = null)
			
			is Msg.BasketUpdated -> copy(basket = msg.basket)
			Msg.LoadingStarted -> copy(isLoading = true)
			Msg.LoadingFinished -> copy(isLoading = false)
		}
	}
}