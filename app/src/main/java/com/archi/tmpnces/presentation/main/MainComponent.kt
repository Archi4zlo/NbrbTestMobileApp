package com.archi.tmpnces.presentation.main

import com.archi.tmpnces.presentation.basket.BasketComponent
import com.archi.tmpnces.presentation.chart.ChartComponent
import com.archi.tmpnces.presentation.rates.RatesComponent
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

interface MainComponent {
	
	val stack: Value<ChildStack<*, Child>>
	
	fun onTabSelected(tab: Tab)
	
	fun onBackClicked()
	
	sealed interface Child {
		val tab: Tab
		
		class Rates(val component: RatesComponent) : Child {
			override val tab = Tab.RATES
		}
		
		class Basket(val component: BasketComponent) : Child {
			override val tab = Tab.BASKET
		}
		
		class Chart(val component: ChartComponent) : Child {
			override val tab = Tab.CHART
		}
	}
}