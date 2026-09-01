package com.archi.tmpnces.presentation.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.archi.tmpnces.R

val Tab.titleRes: Int
	@StringRes get() = when (this) {
		Tab.RATES -> R.string.tab_rates
		Tab.BASKET -> R.string.tab_basket
		Tab.CHART -> R.string.tab_chart
	}

val Tab.iconRes: Int
	@DrawableRes get() = when (this) {
		Tab.RATES -> R.drawable.ic_tab_rates
		Tab.BASKET -> R.drawable.ic_tab_basket
		Tab.CHART -> R.drawable.ic_tab_chart
	}