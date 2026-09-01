package com.archi.tmpnces.presentation.rates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.archi.tmpnces.R

@Composable
fun RatesContent(
	component: RatesComponent,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = stringResource(R.string.tab_rates),
			style = MaterialTheme.typography.headlineSmall,
		)
		Text(
			text = stringResource(R.string.screen_placeholder),
			style = MaterialTheme.typography.bodyMedium,
		)
	}
}