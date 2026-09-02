package com.archi.tmpnces.presentation.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.archi.tmpnces.R
import com.archi.tmpnces.domain.model.RateDynamics
import com.archi.tmpnces.presentation.common.CurrencyFlag
import com.archi.tmpnces.presentation.common.ErrorSnackbarEffect
import com.archi.tmpnces.ui.theme.Brand
import java.time.format.DateTimeFormatter

private val AxisLabelStyle @Composable get() = TextStyle(
    fontSize = 10.sp,
    color = Color(0xFF9E9E9E),
)

@Composable
fun ChartContent(
    component: ChartComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.chart_failed)
    ErrorSnackbarEffect(
        errors = component.errors,
        snackbarHostState = snackbarHostState,
        message = errorMessage,
        key = component,
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CurrencyPicker(
                currencies = model.currencies.map { it.abbreviation },
                selected = model.selectedCurrency,
                onSelect = component::onCurrencySelected,
            )
            
            val flag = CurrencyFlag.emojiFor(model.selectedCurrency)
            val titleCurrency = if (flag != null) "$flag ${model.selectedCurrency}" else model.selectedCurrency
            Text(
                text = stringResource(R.string.chart_title, titleCurrency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            
            val lastPoint = model.dynamics?.points?.lastOrNull()
            if (lastPoint != null) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.chart_today),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "%.4f".format(lastPoint.ratePerUnit),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            
            Text(
                text = stringResource(R.string.chart_dynamics_label, model.selectedCurrency),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center,
            ) {
                val dynamics = model.dynamics
                when {
                    model.isLoading -> CircularProgressIndicator(color = Brand)

                    dynamics == null -> {
                        /* nothing yet — initial state */
                    }

                    dynamics.points.isEmpty() -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.chart_no_data),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = component::onReloadClicked) {
                                Text(
                                    text = stringResource(R.string.chart_reload),
                                    color = Brand,
                                )
                            }
                        }
                    }

                    else -> LineChart(
                        dynamics = dynamics,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            
            PeriodSelector(
                selected = model.period,
                onSelect = component::onPeriodSelected,
            )
        }
    }
}


@Composable
private fun CurrencyPicker(
    currencies: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clickable(enabled = currencies.isNotEmpty()) { expanded = true }
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val flag = CurrencyFlag.emojiFor(selected)
            if (flag != null) {
                Text(text = flag, fontSize = 20.sp)
            }
            Text(
                text = selected,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "▼",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            currencies.forEach { abbr ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val flag = CurrencyFlag.emojiFor(abbr)
                            if (flag != null) Text(text = flag, fontSize = 16.sp)
                            Text(text = abbr)
                        }
                    },
                    onClick = {
                        onSelect(abbr)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selected: ChartPeriod,
    onSelect: (ChartPeriod) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChartPeriod.entries.forEach { period ->
            val isSelected = period == selected
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected) Brand else Color.Transparent,
                        shape = RoundedCornerShape(20.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Brand else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(20.dp),
                    )
                    .clickable { onSelect(period) }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(period.titleRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    dynamics: RateDynamics,
    modifier: Modifier = Modifier,
) {
    val points = dynamics.points
    if (points.isEmpty()) return
    
    val progress = remember(points) { Animatable(0f) }
    LaunchedEffect(points) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(600))
    }

    val textMeasurer = rememberTextMeasurer()
    val axisStyle = AxisLabelStyle

    val chartLineColor = Brand
    val fillTop = Brand.copy(alpha = 0.25f)
    val fillBottom = Brand.copy(alpha = 0f)
    val dotColor = Brand
    val dotLastColor = Color.White
    val dotBorderColor = Brand

    val minRate = points.minOf { it.ratePerUnit }
    val maxRate = points.maxOf { it.ratePerUnit }
    val rateRange = (maxRate - minRate).let { if (it == 0.0) 1.0 else it }
    
    val dateFmt = remember { DateTimeFormatter.ofPattern("dd.MM") }
    
    val maxLabels = 7

    Canvas(modifier = modifier) {
        val paddingLeft = 56f
        val paddingRight = 16f
        val paddingTop = 16f
        val paddingBottom = 36f

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        val n = points.size

        fun xFor(i: Int) = paddingLeft + i * chartWidth / (n - 1).coerceAtLeast(1)
        fun yFor(v: Double) =
            paddingTop + chartHeight * (1.0 - (v - minRate) / rateRange).toFloat()
        
        val ySteps = 4
        for (s in 0..ySteps) {
            val v = minRate + rateRange * s / ySteps
            val y = yFor(v)
            val label = "%.4f".format(v)
            val measured = textMeasurer.measure(label, style = axisStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(0f, y - measured.size.height / 2f),
            )
            // Grid line
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(paddingLeft, y),
                end = Offset(size.width - paddingRight, y),
                strokeWidth = 1f,
            )
        }
        
        val step = (n / maxLabels).coerceAtLeast(1)
        val labelIndices = (0 until n step step).toMutableList()
        if (labelIndices.lastOrNull() != n - 1) labelIndices += n - 1

        for (i in labelIndices) {
            val x = xFor(i)
            val label = points[i].date.format(dateFmt)
            val measured = textMeasurer.measure(label, style = axisStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(x - measured.size.width / 2f, size.height - measured.size.height),
            )
        }
        
        clipRect(right = paddingLeft + chartWidth * progress.value) {
            
            val fillPath = Path().apply {
                moveTo(xFor(0), yFor(points[0].ratePerUnit))
                for (i in 1 until n) {
                    val x0 = xFor(i - 1); val y0 = yFor(points[i - 1].ratePerUnit)
                    val x1 = xFor(i);     val y1 = yFor(points[i].ratePerUnit)
                    val cx = (x0 + x1) / 2f
                    cubicTo(cx, y0, cx, y1, x1, y1)
                }
                lineTo(xFor(n - 1), paddingTop + chartHeight)
                lineTo(xFor(0), paddingTop + chartHeight)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillTop, fillBottom),
                    startY = paddingTop,
                    endY = paddingTop + chartHeight,
                ),
            )
            
            val linePath = Path().apply {
                moveTo(xFor(0), yFor(points[0].ratePerUnit))
                for (i in 1 until n) {
                    val x0 = xFor(i - 1); val y0 = yFor(points[i - 1].ratePerUnit)
                    val x1 = xFor(i);     val y1 = yFor(points[i].ratePerUnit)
                    val cx = (x0 + x1) / 2f
                    cubicTo(cx, y0, cx, y1, x1, y1)
                }
            }
            drawPath(
                path = linePath,
                color = chartLineColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            
            for (i in points.indices) {
                val cx = xFor(i)
                val cy = yFor(points[i].ratePerUnit)
                val isLast = i == n - 1
                val radius = if (isLast) 7f else 4f
                if (isLast) {
                    drawCircle(color = dotLastColor, radius = radius, center = Offset(cx, cy))
                    drawCircle(
                        color = dotBorderColor,
                        radius = radius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 3f),
                    )
                } else {
                    drawCircle(color = dotColor, radius = radius, center = Offset(cx, cy))
                }
            }
        }
    }
}