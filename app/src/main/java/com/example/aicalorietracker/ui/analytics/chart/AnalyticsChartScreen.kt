package com.example.aicalorietracker.ui.analytics.chart

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aicalorietracker.ui.analytics.AnalyticsViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsChartScreen(
    viewModel: AnalyticsViewModel,
    onNavigateBack: () -> Unit
) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()
    val selectedMacro by viewModel.selectedMacro.collectAsStateWithLifecycle()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsStateWithLifecycle()

    val targetValue = remember(selectedMacro) {
        when (selectedMacro) {
            MacroType.CALORIES -> 2000f
            MacroType.PROTEIN -> 150f
            MacroType.CARBS -> 250f
            MacroType.FAT -> 70f
            MacroType.FIBER -> 30f
            MacroType.SUGAR -> 50f
        }
    }

    val yValues = remember(chartData, selectedMacro) {
        chartData.map {
            when (selectedMacro) {
                MacroType.CALORIES -> it.calories
                MacroType.PROTEIN -> it.protein
                MacroType.CARBS -> it.carbs
                MacroType.FAT -> it.fat
                MacroType.FIBER -> it.fiber
                MacroType.SUGAR -> it.sugar
            }
        }
    }

    val avg = if (yValues.isNotEmpty()) yValues.average().toInt() else 0
    val max = if (yValues.isNotEmpty()) yValues.maxOrNull() ?: 0 else 0
    val total = yValues.sum()

    val animAvg by animateIntAsState(targetValue = avg, animationSpec = tween(800), label = "animAvg")
    val animMax by animateIntAsState(targetValue = max, animationSpec = tween(800), label = "animMax")
    val animTotal by animateIntAsState(targetValue = total, animationSpec = tween(800), label = "animTotal")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Macro Trends",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SquishyTimeRangeSelector(
                selectedRange = selectedTimeRange,
                onRangeSelected = { viewModel.setTimeRange(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .animateContentSize()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatSummaryItem(label = "Average", value = animAvg, macro = selectedMacro)
                        StatSummaryItem(label = "Maximum", value = animMax, macro = selectedMacro)
                        StatSummaryItem(label = "Total", value = animTotal, macro = selectedMacro)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (chartData.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Not enough data yet.\nStart logging your meals!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        AnimatedMacroChart(
                            data = chartData,
                            selectedMacro = selectedMacro,
                            targetValue = targetValue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SquishyMacroSelector(
                selectedMacro = selectedMacro,
                onMacroSelected = { viewModel.setMacroType(it) }
            )
        }
    }
}

@Composable
fun StatSummaryItem(label: String, value: Int, macro: MacroType) {
    val suffix = if (macro == MacroType.CALORIES) "" else "g"
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(value)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$formatted$suffix",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 28.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}