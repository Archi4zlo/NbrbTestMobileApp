package com.archi.tmpnces.presentation.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.archi.tmpnces.R
import com.archi.tmpnces.presentation.basket.BasketContent
import com.archi.tmpnces.presentation.chart.ChartContent
import com.archi.tmpnces.presentation.rates.RatesContent
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
	component: MainComponent,
	modifier: Modifier = Modifier,
) {
	
	val stack by component.stack.subscribeAsState()
	val activeTab = stack.active.instance.tab
	
	Scaffold(modifier = modifier.fillMaxSize(), topBar = {
		TopAppBar(
			title = { Text(stringResource(activeTab.titleRes)) }, navigationIcon = {
			IconButton(onClick = component::onBackClicked) {
				Icon(
					painter = painterResource(R.drawable.ic_arrow_back),
					contentDescription = stringResource(R.string.back)
				)
			}
		}, colors = TopAppBarDefaults.topAppBarColors(
			containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
			titleContentColor = Color.White,
			navigationIconContentColor = Color.White
		)
		)
	}, bottomBar = {
		NavigationBar {
			Tab.entries.forEach { tab ->
				NavigationBarItem(selected = tab == activeTab, onClick = { component.onTabSelected(tab) }, icon = {
					Icon(
						painter = painterResource(tab.iconRes), contentDescription = null
					)
				}, label = { Text(stringResource(tab.titleRes)) })
			}
		}
	}) { innerPadding ->
		
		Children(
			stack = component.stack,
			modifier = Modifier.padding(innerPadding),
			animation = stackAnimation(fade()),
		) { child ->
			when (val instance = child.instance) {
				is MainComponent.Child.Rates -> RatesContent(instance.component)
				is MainComponent.Child.Basket -> BasketContent(instance.component)
				is MainComponent.Child.Chart -> ChartContent(instance.component)
			}
		}
	}
}