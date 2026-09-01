package com.archi.tmpnces.presentation.rates

import com.archi.tmpnces.domain.model.RateWithChange
import com.arkivanov.mvikotlin.core.store.Store
import java.time.LocalDate

interface RatesStore : Store<RatesStore.Intent, RatesStore.State, RatesStore.Label> {
	
	sealed interface Intent {
		data object Refresh : Intent
	}
	
	data class State(
		val date: LocalDate,
		val rates: List<RateWithChange> = emptyList(),
		val isLoading: Boolean = false,
	)
	
	sealed interface Label {
		data class RefreshFailed(val throwable: Throwable) : Label
	}
}