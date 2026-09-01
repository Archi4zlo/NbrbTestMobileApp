package com.archi.tmpnces.presentation.rates

import com.archi.tmpnces.domain.model.RateWithChange
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

interface RatesComponent {
	
	val model: StateFlow<Model>
	
	val errors: Flow<Throwable>
	
	fun onRefreshClicked()
	
	data class Model(
		val date: LocalDate,
		val rates: List<RateWithChange>,
		val isLoading: Boolean,
	)
}

class DefaultRatesComponent @AssistedInject constructor(
	storeFactory: RatesStoreFactory, @Assisted componentContext: ComponentContext
) : RatesComponent, ComponentContext by componentContext {
	
	@AssistedFactory
	interface Factory {
		fun create(componentContext: ComponentContext): DefaultRatesComponent
	}
	
	private val store = retainedStore {
		storeFactory.create(LocalDate.now())
	}
	
	private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())
	
	override val model: StateFlow<RatesComponent.Model> = store.states.map(::toModel)
		.stateIn(
			scope = scope,
			started = SharingStarted.Eagerly,
			initialValue = toModel(store.state),
		)
	
	override val errors: Flow<Throwable> = store.labels.map { label ->
		when (label) {
			is RatesStore.Label.RefreshFailed -> label.throwable
		}
	}
	
	override fun onRefreshClicked() {
		store.accept(RatesStore.Intent.Refresh)
	}
	
	private fun toModel(state: RatesStore.State) = RatesComponent.Model(
		date = state.date,
		rates = state.rates,
		isLoading = state.isLoading,
	)
}