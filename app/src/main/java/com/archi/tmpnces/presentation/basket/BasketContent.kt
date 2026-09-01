package com.archi.tmpnces.presentation.basket

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.archi.tmpnces.R
import com.archi.tmpnces.presentation.common.ErrorSnackbarEffect
import com.archi.tmpnces.domain.model.ComponentRate
import com.archi.tmpnces.domain.model.CurrencyBasket
import com.archi.tmpnces.domain.model.RubleChange
import com.archi.tmpnces.domain.model.RubleTrend
import com.archi.tmpnces.ui.theme.RateDown
import com.archi.tmpnces.ui.theme.RateUp
import com.archi.tmpnces.ui.theme.TableBorder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val DateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val RateColumnWidth = 78.dp
private val PercentColumnWidth = 58.dp

@Composable
fun BasketContent(
	component: BasketComponent, modifier: Modifier = Modifier
) {
	val model by component.model.collectAsStateWithLifecycle()
	
	val snackbarHostState = remember { SnackbarHostState() }
	val errorMessage = stringResource(R.string.basket_failed)
	ErrorSnackbarEffect(
		errors = component.errors,
		snackbarHostState = snackbarHostState,
		message = errorMessage,
		key = component,
	)
	
	var showDatePicker by remember { mutableStateOf(false) }
	
	if (showDatePicker) {
		DateDialog(initialDate = model.date, onDismiss = { showDatePicker = false }, onConfirm = { date ->
			showDatePicker = false
			component.onDateSelected(date)
		})
	}
	
	Scaffold(
		modifier = modifier.fillMaxSize(), snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
		
		Column(
			modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Text(
				text = stringResource(R.string.basket_header, model.date.format(DateFormat)),
				style = MaterialTheme.typography.bodyMedium,
				textAlign = TextAlign.Center,
				modifier = Modifier.fillMaxWidth()
			)
			
			DateSelector(
				date = model.date,
				isLoading = model.isLoading,
				onPickDate = { showDatePicker = true },
				onReload = component::onReloadClicked
			)
			
			Text(
				text = stringResource(R.string.basket_explanation),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			
			val basket = model.basket
			if (basket == null) {
				Text(
					text = stringResource(R.string.basket_empty),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					textAlign = TextAlign.Center,
					modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
				)
			} else {
				BasketTable(basket)
				
				Text(
					text = stringResource(R.string.basket_legend),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

@Composable
private fun DateSelector(
	date: LocalDate, isLoading: Boolean, onPickDate: () -> Unit, onReload: () -> Unit
) {
	Row(
		modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, TableBorder, RoundedCornerShape(4.dp))
            .padding(12.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = stringResource(R.string.basket_date_label),
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Text(
				text = date.format(DateFormat), style = MaterialTheme.typography.bodyLarge
			)
		}
		
		IconButton(onClick = onPickDate) {
			Icon(
				painter = painterResource(R.drawable.ic_calendar),
				contentDescription = stringResource(R.string.basket_pick_date)
			)
		}
		
		Button(
			onClick = onReload, enabled = !isLoading, shape = RoundedCornerShape(4.dp)
		) {
			if (isLoading) {
				CircularProgressIndicator(
					modifier = Modifier
                        .height(16.dp)
                        .width(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary
				)
			} else {
				Text(stringResource(R.string.basket_show))
			}
		}
	}
}

@Composable
private fun BasketTable(basket: CurrencyBasket) {
	Column(modifier = Modifier.fillMaxWidth()) {
		TableHeader(yearOfBase = basket.date.year - 1)
		TableRow(
			label = stringResource(R.string.basket_row_title),
			rate = basket.basket.value,
			sinceYearStart = basket.basket.sinceYearStart,
			sincePreviousDay = basket.basket.sincePreviousDay,
			emphasized = true
		)
		
		basket.components.forEach { component ->
			ComponentRow(component)
		}
	}
}

@Composable
private fun TableHeader(yearOfBase: Int) {
	Row(
		modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 8.dp, vertical = 8.dp)
	) {
		HeaderCell(stringResource(R.string.basket_col_currency), Modifier.weight(1f))
		HeaderCell(stringResource(R.string.basket_col_rate), Modifier.width(RateColumnWidth))
		HeaderCell(stringResource(R.string.basket_col_year, yearOfBase), Modifier.width(PercentColumnWidth))
		HeaderCell(stringResource(R.string.basket_col_prev), Modifier.width(PercentColumnWidth))
	}
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
	Text(
		text = text,
		modifier = modifier.padding(horizontal = 3.dp),
		color = MaterialTheme.colorScheme.onPrimary,
		fontSize = 9.sp,
		lineHeight = 11.sp,
		textAlign = TextAlign.Center
	)
}

@Composable
private fun ComponentRow(component: ComponentRate) {
	TableRow(
		label = component.label,
		rate = component.officialRate,
		sinceYearStart = component.sinceYearStart,
		sincePreviousDay = component.sincePreviousDay,
		emphasized = false
	)
}

@Composable
private fun TableRow(
	label: String, rate: Double, sinceYearStart: RubleChange?, sincePreviousDay: RubleChange?, emphasized: Boolean
) {
	val weight = if (emphasized) FontWeight.Bold else FontWeight.Normal
	
	Row(
		modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, TableBorder)
            .padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = label,
			modifier = Modifier
                .weight(1f)
                .padding(horizontal = 3.dp),
			fontSize = 12.sp,
			lineHeight = 15.sp,
			fontWeight = weight
		)
		
		Text(
			text = formatRate(rate),
			modifier = Modifier
                .width(RateColumnWidth)
                .padding(horizontal = 3.dp),
			fontSize = 12.sp,
			fontWeight = weight,
			textAlign = TextAlign.End
		)
		
		PercentCell(sinceYearStart, weight)
		PercentCell(sincePreviousDay, weight)
	}
}

@Composable
private fun PercentCell(change: RubleChange?, weight: FontWeight) {
	Row(
		modifier = Modifier
            .width(PercentColumnWidth)
            .padding(horizontal = 3.dp),
		horizontalArrangement = Arrangement.End,
		verticalAlignment = Alignment.CenterVertically
	) {
		if (change == null) return@Row
		
		val color = when (change.trend) {
			RubleTrend.STRENGTHENED -> RateUp
			RubleTrend.WEAKENED -> RateDown
			RubleTrend.UNCHANGED -> MaterialTheme.colorScheme.onSurface
		}
		
		Text(
			text = String.format(Locale.US, "%.2f", change.percent),
			fontSize = 12.sp,
			fontWeight = weight,
			color = color
		)
		
		val arrow = when (change.trend) {
			RubleTrend.STRENGTHENED -> "▲"
			RubleTrend.WEAKENED -> "▼"
			RubleTrend.UNCHANGED -> null
		}
		
		if (arrow != null) {
			Text(
				text = arrow, fontSize = 9.sp, color = color, modifier = Modifier.padding(start = 2.dp)
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDialog(
	initialDate: LocalDate, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit
) {
	val state = rememberDatePickerState(
		initialSelectedDateMillis = initialDate.toUtcMillis(),
		selectableDates = object : SelectableDates {
			override fun isSelectableDate(utcTimeMillis: Long): Boolean = !utcTimeMillis.toLocalDateUtc()
				.isAfter(LocalDate.now())
		},
	)
	
	DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
		TextButton(
			onClick = { state.selectedDateMillis?.let { onConfirm(it.toLocalDateUtc()) } }) {
			Text(stringResource(R.string.dialog_ok))
		}
	}, dismissButton = {
		TextButton(onClick = onDismiss) {
			Text(stringResource(R.string.dialog_cancel))
		}
	}) {
		DatePicker(state = state)
	}
}

private fun formatRate(value: Double): String = if (abs(value) >= LARGE_VALUE_THRESHOLD) {
	String.format(Locale.US, "%.2f", value)
} else {
	String.format(Locale.US, "%.4f", value)
}

private const val LARGE_VALUE_THRESHOLD = 1000.0

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant()
	.toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate = Instant.ofEpochMilli(this)
	.atZone(ZoneOffset.UTC)
	.toLocalDate()