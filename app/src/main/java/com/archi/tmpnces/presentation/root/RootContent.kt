package com.archi.tmpnces.presentation.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.archi.tmpnces.presentation.main.MainContent
import com.archi.tmpnces.presentation.menu.MenuContent
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation

@Composable
fun RootContent(
	component: RootComponent, modifier: Modifier = Modifier
) {
	Children(
		stack = component.stack,
		modifier = modifier,
		animation = stackAnimation(slide()),
	) { child ->
		when (val instance = child.instance) {
			is RootComponent.Child.Menu -> MenuContent(instance.component)
			is RootComponent.Child.Main -> MainContent(instance.component)
		}
	}
}