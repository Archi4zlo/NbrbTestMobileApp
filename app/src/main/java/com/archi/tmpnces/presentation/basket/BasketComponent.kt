package com.archi.tmpnces.presentation.basket

import com.arkivanov.decompose.ComponentContext

interface BasketComponent

class DefaultBasketComponent(
	componentContext: ComponentContext
) : BasketComponent, ComponentContext by componentContext