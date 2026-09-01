package com.archi.tmpnces.presentation.rates

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.archi.tmpnces.R
import com.archi.tmpnces.presentation.common.ErrorSnackbarEffect
import com.archi.tmpnces.domain.model.ChangeDirection
import com.archi.tmpnces.domain.model.RateWithChange
import com.archi.tmpnces.presentation.common.CurrencyFlag
import com.archi.tmpnces.ui.theme.RateDown
import com.archi.tmpnces.ui.theme.RateUp
import com.archi.tmpnces.ui.theme.TableBorder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@Composable
fun RatesContent(
	component: RatesComponent,
	modifier: Modifier = Modifier,
) {
	val model by component.model.collectAsStateWithLifecycle()
	
	val snackbarHostState = remember { SnackbarHostState() }
	val errorMessage = stringResource(R.string.rates_refresh_failed)
	ErrorSnackbarEffect(
		errors = component.errors,
		snackbarHostState = snackbarHostState,
		message = errorMessage,
		key = component,
	)
	
	Scaffold(
		modifier = modifier.fillMaxSize(), snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
		
		LazyColumn(
			modifier = Modifier
                .fillMaxSize()
                .padding(padding), contentPadding = PaddingValues(bottom = 24.dp)
		) {
			item {
				Header(
					date = model.date,
					isLoading = model.isLoading,
					onRefresh = component::onRefreshClicked,
				)
			}
			
			if (model.rates.isEmpty() && !model.isLoading) {
				item {
					Text(
						text = stringResource(R.string.rates_empty),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
						textAlign = TextAlign.Center
					)
				}
			}
			
			items(
				items = model.rates,
				key = { it.rate.abbreviation },
			) { item ->
				RateRow(item)
			}
		}
	}
}

@Composable
private fun Header(
	date: LocalDate,
	isLoading: Boolean,
	onRefresh: () -> Unit,
) {
	Column(
		modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Text(
			text = stringResource(R.string.rates_header, date.format(DateFormat)),
			style = MaterialTheme.typography.bodyMedium,
			textAlign = TextAlign.Center
		)
		
		Button(
			onClick = onRefresh, enabled = !isLoading, shape = RoundedCornerShape(6.dp)
		) {
			if (isLoading) {
				CircularProgressIndicator(
					modifier = Modifier
                        .height(18.dp)
                        .width(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary
				)
			} else {
				Text(stringResource(R.string.rates_refresh))
			}
		}
	}
}

@Composable
private fun RateRow(item: RateWithChange) {
	Row(
		modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(width = 0.5.dp, color = TableBorder)
            .padding(horizontal = 10.dp, vertical = 11.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = CurrencyFlag.emojiFor(item.rate.abbreviation) ?: stringResource(R.string.rates_flag_fallback),
			style = MaterialTheme.typography.bodyLarge
		)
		
		Text(
			text = item.rate.name,
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
		)
		
		Text(
			text = formatRate(item.rate.officialRate),
			style = MaterialTheme.typography.bodyMedium,
			textAlign = TextAlign.End,
			modifier = Modifier.width(72.dp)
		)
		
		Box(
			modifier = Modifier.width(72.dp),
			contentAlignment = Alignment.CenterEnd,
		) {
			val change = item.changeInQuoteUnits
			if (change != null && item.direction != ChangeDirection.UNCHANGED) {
				Text(
					text = formatChange(change),
					style = MaterialTheme.typography.bodyMedium,
					color = when (item.direction) {
						ChangeDirection.UP -> RateUp
						ChangeDirection.DOWN -> RateDown
						else -> MaterialTheme.colorScheme.onSurface
					}
				)
			}
		}
	}
}

private fun formatRate(value: Double): String = String.format(Locale.US, "%.4f", value)

private fun formatChange(value: Double): String = String.format(Locale.US, "%+.4f", value)