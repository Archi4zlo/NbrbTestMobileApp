package com.archi.tmpnces.presentation.root

import com.archi.tmpnces.presentation.main.DefaultMainComponent
import com.archi.tmpnces.presentation.main.Tab
import com.archi.tmpnces.presentation.menu.DefaultMenuComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

class DefaultRootComponent(
	componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {
	
	private val navigation = StackNavigation<Config>()
	
	override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
		source = navigation,
		serializer = Config.serializer(),
		initialConfiguration = Config.Menu,
		handleBackButton = true,
		childFactory = ::createChild,
	)
	
	private fun createChild(
		config: Config, componentContext: ComponentContext
	): RootComponent.Child = when (config) {
		
		Config.Menu -> RootComponent.Child.Menu(
			DefaultMenuComponent(
				componentContext = componentContext, onSectionSelected = { tab ->
					navigation.push(Config.Main(tab))
				}),
		)
		
		is Config.Main -> RootComponent.Child.Main(
			DefaultMainComponent(
				componentContext = componentContext,
				initialTab = config.tab,
				onBack = navigation::pop,
			)
		)
	}
	@Serializable
	private sealed interface Config {
		
		@Serializable
		data object Menu : Config
		@Serializable
		data class Main(val tab: Tab) : Config
	}
}