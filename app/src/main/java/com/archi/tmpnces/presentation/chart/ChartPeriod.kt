package com.archi.tmpnces.presentation.chart

import androidx.annotation.StringRes
import com.archi.tmpnces.R
import java.time.LocalDate

enum class ChartPeriod(
	@param:StringRes val titleRes: Int
) {
	WEEK(R.string.chart_period_week) {
		override fun startFrom(end: LocalDate): LocalDate = end.minusWeeks(1)
	},
	
	MONTH(R.string.chart_period_month) {
		override fun startFrom(end: LocalDate): LocalDate = end.minusMonths(1)
	},
	
	QUARTER(R.string.chart_period_quarter) {
		override fun startFrom(end: LocalDate): LocalDate = end.minusMonths(3)
	};
	
	abstract fun startFrom(end: LocalDate): LocalDate
}