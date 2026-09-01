package com.archi.tmpnces.presentation.rates

import com.arkivanov.decompose.ComponentContext

interface RatesComponent
class DefaultRatesComponent(
	componentContext: ComponentContext
) : RatesComponent, ComponentContext by componentContext