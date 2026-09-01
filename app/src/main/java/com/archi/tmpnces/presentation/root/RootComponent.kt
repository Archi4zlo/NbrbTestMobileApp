package com.archi.tmpnces.presentation.root

import com.archi.tmpnces.presentation.main.MainComponent
import com.archi.tmpnces.presentation.menu.MenuComponent
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

interface RootComponent {
	
	val stack: Value<ChildStack<*, Child>>
	
	sealed interface Child {
		class Menu(val component: MenuComponent) : Child
		class Main(val component: MainComponent) : Child
	}
}