package com.archi.tmpnces.presentation.chart

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
fun ChartContent(
	component: ChartComponent, modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = stringResource(R.string.tab_chart),
			style = MaterialTheme.typography.headlineSmall,
		)
		Text(
			text = stringResource(R.string.screen_placeholder),
			style = MaterialTheme.typography.bodyMedium,
		)
	}
}