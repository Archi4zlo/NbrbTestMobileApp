package com.archi.tmpnces.presentation.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.archi.tmpnces.R
import com.archi.tmpnces.presentation.main.Tab
import com.archi.tmpnces.presentation.main.titleRes

@Composable
fun MenuContent(
	component: MenuComponent, modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Text(
			text = stringResource(R.string.menu_title),
			style = MaterialTheme.typography.headlineSmall,
			modifier = Modifier.padding(top = 48.dp, bottom = 24.dp)
		)
		
		Tab.entries.forEach { tab ->
			Button(
				onClick = { component.onSectionClicked(tab) },
				modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .height(56.dp),
				shape = RoundedCornerShape(8.dp)
			) {
				Text(
					text = stringResource(tab.titleRes), style = MaterialTheme.typography.titleMedium
				)
			}
		}
	}
}