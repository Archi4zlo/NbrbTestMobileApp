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
import kotlinx.serialization.Serializable

class DefaultMainComponent(
	componentContext: ComponentContext, initialTab: Tab, private val onBack: () -> Unit
) : MainComponent, ComponentContext by componentContext {
	
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
			DefaultRatesComponent(componentContext)
		)
		
		Tab.BASKET -> MainComponent.Child.Basket(
			DefaultBasketComponent(componentContext)
		)
		
		Tab.CHART -> MainComponent.Child.Chart(
			DefaultChartComponent(componentContext)
		)
	}
	
	override fun onTabSelected(tab: Tab) {
		navigation.bringToFront(Config(tab))
	}
	
	override fun onBackClicked() = onBack()
	
	@Serializable
	private data class Config(val tab: Tab)
}