package com.archi.tmpnces.presentation.basket

import com.archi.tmpnces.domain.model.CurrencyBasket
import com.arkivanov.mvikotlin.core.store.Store
import java.time.LocalDate

interface BasketStore : Store<BasketStore.Intent, BasketStore.State, BasketStore.Label> {
	
	sealed interface Intent {
		data object Reload : Intent
		data class SelectDate(val date: LocalDate) : Intent
	}
	
	data class State(
		val date: LocalDate,
		val basket: CurrencyBasket? = null,
		val isLoading: Boolean = false,
	)
	
	sealed interface Label {
		data class LoadFailed(val throwable: Throwable) : Label
	}
}