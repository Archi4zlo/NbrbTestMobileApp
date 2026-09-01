package com.archi.tmpnces.presentation.basket

import com.archi.tmpnces.domain.model.CurrencyBasket
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
import java.time.LocalDate

interface BasketComponent {
	
	val model: StateFlow<Model>
	
	val errors: Flow<Throwable>
	
	fun onReloadClicked()
	
	fun onDateSelected(date: LocalDate)
	
	data class Model(
		val date: LocalDate, val basket: CurrencyBasket?, val isLoading: Boolean
	)
}

class DefaultBasketComponent @AssistedInject constructor(
	storeFactory: BasketStoreFactory, @Assisted componentContext: ComponentContext
) : BasketComponent, ComponentContext by componentContext {
	
	@AssistedFactory
	interface Factory {
		fun create(componentContext: ComponentContext): DefaultBasketComponent
	}
	
	private val store = retainedStore {
		storeFactory.create(LocalDate.now())
	}
	
	private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())
	
	override val model: StateFlow<BasketComponent.Model> = store.states.map(::toModel)
		.stateIn(
			scope = scope,
			started = SharingStarted.Eagerly,
			initialValue = toModel(store.state),
		)
	
	override val errors: Flow<Throwable> = store.labels.map { label ->
		when (label) {
			is BasketStore.Label.LoadFailed -> label.throwable
		}
	}
	
	override fun onReloadClicked() {
		store.accept(BasketStore.Intent.Reload)
	}
	
	override fun onDateSelected(date: LocalDate) {
		store.accept(BasketStore.Intent.SelectDate(date))
	}
	
	private fun toModel(state: BasketStore.State) = BasketComponent.Model(
		date = state.date, basket = state.basket, isLoading = state.isLoading
	)
}