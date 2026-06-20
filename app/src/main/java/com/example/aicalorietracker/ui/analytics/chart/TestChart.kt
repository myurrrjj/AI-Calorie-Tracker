package com.example.aicalorietracker.ui.analytics.chart

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.units.celsius
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnimatedMacroChart(
    data: List<DailyAggregateData>,
    selectedMacro: MacroType,
    targetValue: Float,
    modifier: Modifier = Modifier            // BUG 1 FIX: actually applied below now
) {
    val yValues = remember(data, selectedMacro) {
        data.map {
            when (selectedMacro) {
                MacroType.CALORIES -> it.calories.toFloat()
                MacroType.PROTEIN  -> it.protein.toFloat()
                MacroType.CARBS    -> it.carbs.toFloat()
                MacroType.FAT      -> it.fat.toFloat()
                MacroType.FIBER    -> it.fiber.toFloat()
                MacroType.SUGAR    -> it.sugar.toFloat()
            }
        }
    }

    val avg    = if (yValues.isNotEmpty()) yValues.average().toFloat() else 0f
    val maxRaw = yValues.maxOrNull() ?: 1f
    val targetMax = maxRaw.coerceAtLeast(targetValue * 1.25f).coerceAtLeast(1f)
    val targetGoalFraction = if (targetMax > 0f) (targetValue / targetMax).coerceIn(0f, 1f) else 0f
    val targetAvgFraction = if (targetMax > 0f) (avg / targetMax).coerceIn(0f, 1f) else 0f

    val animatedGoalFraction by animateFloatAsState(
        targetValue = targetGoalFraction,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "goalFraction"
    )
    val animatedAvgFraction by animateFloatAsState(
        targetValue = targetAvgFraction,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "avgFraction"
    )
    val targetFractions = yValues.map { (it/targetMax).coerceIn(0f,1f) }


    val animatedMax by animateFloatAsState(
        targetValue = targetMax,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "maxAnim"
    )

//    var trigger by remember { mutableStateOf(false) }
//    LaunchedEffect(data, selectedMacro) {
//        trigger = false
//        delay(80)
//        trigger = true
//    }

    val animatedHeights = targetFractions.mapIndexed { index, fraction ->
        animateFloatAsState(
            targetValue = fraction,
            animationSpec = tween(
                durationMillis = 700,
                delayMillis    = (index * 40).coerceAtMost(1200),
                easing         = FastOutSlowInEasing
            ),
            label = "barHeight_$index"
        ).value
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val suffix      = if (selectedMacro == MacroType.CALORIES) "" else "g"
    val formatValue = { v: Float -> NumberFormat.getNumberInstance(Locale.US).format(v.toInt()) + suffix }

    val primary   = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val error     = MaterialTheme.colorScheme.error
    val goalColor = if (avg > targetValue) error else MaterialTheme.colorScheme.tertiary

    val yAxisWidth    = 44.dp
    val rightPadding  = 60.dp
    val labelAreaHeight = 36.dp
    val gridLevels    = listOf(1f, .9f, .8f, .7f, .6f, .5f, .4f, .3f, .2f, .1f, 0f)

    BoxWithConstraints(modifier = modifier) {
        val chartHeight = maxHeight - labelAreaHeight

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            gridLevels.forEach { pct ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatValue(animatedMax * pct),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier.width(yAxisWidth)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .height(chartHeight)
                .fillMaxWidth()
                .padding(start = yAxisWidth, end = rightPadding),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.Bottom
        ) {
            yValues.forEachIndexed { i, value ->
                val animVal   = animatedHeights[i]
                val barFrac   = animVal

//                    if (animatedMax > 0f) (animVal / animatedMax).coerceIn(0f, 1f) else 0f
                val ratio     = if (targetValue > 0f) value / targetValue else 0f
                val barColor  = when {
                    ratio < 0.8f -> lerp(primary.copy(alpha = 0.6f), primary, ratio / 0.8f)
                    ratio <= 1f  -> lerp(primary, secondary, (ratio - 0.8f) / 0.2f)
                    else         -> lerp(secondary, error, ((ratio - 1f) / 0.5f).coerceAtMost(1f))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(barFrac)
                        .padding(horizontal = if (yValues.size > 15) 1.dp else 4.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(barColor.copy(alpha = 0.9f), barColor.copy(alpha = 0.4f))
                            ),
                            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null
                        ) { selectedIndex = if (selectedIndex == i) null else i }
                )
            }
        }

        Row(
            modifier = Modifier
                .height(labelAreaHeight)
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(start = yAxisWidth, end = rightPadding),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEachIndexed { i, entry ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (yValues.size <= 15 || i % 7 == 0) {
                        val fmt = if (yValues.size > 7) "dd" else "EEE"
                        Text(
                            text = SimpleDateFormat(fmt, Locale.US).format(Date(entry.timestamp)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

//        val goalFraction = if (animatedMax > 0f) (targetValue / animatedMax).coerceIn(0f, 1f) else 0f
        Box(
            modifier = Modifier
                .height(chartHeight)
                .fillMaxWidth()
                .padding(start = yAxisWidth),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedGoalFraction),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color    = goalColor.copy(alpha = 0.8f),
                        thickness = 2.dp
                    )
                    Text(
                        text     = "Goal",
                        color    = goalColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }
        }

//        val avgFraction = if (animatedMax > 0f) (avg / animatedMax).coerceIn(0f, 1f) else 0f
        Box(
            modifier = Modifier
                .height(chartHeight)
                .fillMaxWidth()
                .padding(start = yAxisWidth),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedAvgFraction),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier  = Modifier.weight(1f),
                        color     = primary.copy(alpha = 0.6f),
                        thickness = 2.dp
                    )
                    Text(
                        text     = "Avg: ${avg.toInt()}$suffix",
                        color    = primary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }
        }

        if (selectedIndex != null) {
            Row(
                modifier = Modifier
                    .height(chartHeight)
                    .fillMaxWidth()
                    .padding(start = yAxisWidth, end = rightPadding),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.Bottom
            ) {
                yValues.forEachIndexed { i, value ->
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (selectedIndex == i) {
                            val animVal  = animatedHeights[i]
                            val barFrac  = animVal

//                                if (animatedMax > 0f) (animVal / animatedMax).coerceIn(0f, 1f) else 0f
                            val dateStr  = SimpleDateFormat("MMM dd", Locale.US).format(Date(data[i].timestamp))

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(barFrac)
                                    .wrapContentHeight(Alignment.Top)
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text       = "${formatValue(value)} • $dateStr",
                                    color      = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 14.sp,
                                    maxLines   = 1,
                                    softWrap   = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}