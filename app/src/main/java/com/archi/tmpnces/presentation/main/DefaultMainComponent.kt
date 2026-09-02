package com.archi.tmpnces.presentation.main

import com.archi.tmpnces.presentation.basket.DefaultBasketComponent
import com.archi.tmpnces.presentation.chart.DefaultChartComponent
import com.archi.tmpnces.presentation.rates.DefaultRatesComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable

class DefaultMainComponent @AssistedInject constructor(
	private val ratesComponentFactory: DefaultRatesComponent.Factory,
	private val basketComponentFactory: DefaultBasketComponent.Factory,
	private val chartComponentFactory: DefaultChartComponent.Factory,
	@Assisted componentContext: ComponentContext,
	@Assisted private val initialTab: Tab,
	@Assisted private val onBack: () -> Unit
) : MainComponent, ComponentContext by componentContext {
	
	@AssistedFactory
	interface Factory {
		fun create(
			componentContext: ComponentContext, initialTab: Tab, onBack: () -> Unit
		): DefaultMainComponent
	}
	
	private val navigation = StackNavigation<Config>()
	
	override val stack: Value<ChildStack<*, MainComponent.Child>> = childStack(
		source = navigation,
		serializer = Config.serializer(),
		initialConfiguration = Config(initialTab),
		handleBackButton = false,
		childFactory = ::createChild,
	)
	
	private fun createChild(
		config: Config, componentContext: ComponentContext
	): MainComponent.Child = when (config.tab) {
		
		Tab.RATES -> MainComponent.Child.Rates(
			ratesComponentFactory.create(componentContext)
		)
		
		Tab.BASKET -> MainComponent.Child.Basket(
			basketComponentFactory.create(componentContext)
		)
		
		Tab.CHART -> MainComponent.Child.Chart(
			chartComponentFactory.create(componentContext)
		)
	}
	
	override fun onTabSelected(tab: Tab) {
		navigation.bringToFront(Config(tab))
	}
	
	override fun onBackClicked() = onBack()
	
	@Serializable
	private data class Config(val tab: Tab)
}