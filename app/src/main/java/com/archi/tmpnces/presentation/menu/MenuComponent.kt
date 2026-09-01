package com.archi.tmpnces.presentation.menu

import com.archi.tmpnces.presentation.main.Tab
import com.arkivanov.decompose.ComponentContext

interface MenuComponent {
	fun onSectionClicked(tab: Tab)
}

class DefaultMenuComponent(
	componentContext: ComponentContext,
	private val onSectionSelected: (Tab) -> Unit,
) : MenuComponent, ComponentContext by componentContext {
	
	override fun onSectionClicked(tab: Tab) = onSectionSelected(tab)
}