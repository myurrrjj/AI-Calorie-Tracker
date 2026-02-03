package com.example.aicalorietracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aicalorietracker.local.MealLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyAnalyticsScreen(
    state: MealUiState, onDismiss: () -> Unit
) {
    val totals = remember(state.meals) {
        state.meals.fold(AnalyticsTotals()) { acc, meal ->
            acc.add(meal)
        }
    }

    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }

    BackHandler {
        visibleState.targetState = false
        onDismiss()
    }

    AnimatedVisibility(
        visibleState = visibleState, enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
        ), exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface, topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Daily Breakdown", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {
                            visibleState.targetState = false
                            onDismiss()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    BigCalorieRing(
                        current = state.totalCalories, target = state.targetCalories
                    )
                }

                Text(
                    "Macro Split",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MacroVerticalCard(
                        "Protein",
                        totals.protein,
                        150,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        Modifier.weight(1f)
                    )
                    MacroVerticalCard(
                        "Carbs",
                        totals.carbs,
                        300,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer,
                        Modifier.weight(1f)
                    )
                    MacroVerticalCard(
                        "Fats",
                        totals.fat,
                        80,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer,
                        Modifier.weight(1f)
                    )
                }

                Text(
                    "Nutrition Details",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailStatCard("Fiber", "${totals.fiber}g", "🥗", Modifier.weight(1f))
                        DetailStatCard("Sugar", "${totals.sugar}g", "🍬", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailStatCard("Vitamin A", "${totals.vitaminA} IU", "🥕", Modifier.weight(1f))
                        DetailStatCard("Vitamin C", "${totals.vitaminC}mg", "🍊", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailStatCard("Vitamin D", "${totals.vitaminD} IU", "☀️", Modifier.weight(1f))
                        DetailStatCard("Calcium", "${totals.calcium}mg", "🥛", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailStatCard("Iron", "${totals.iron}mg", "🦾", Modifier.weight(1f))
                        DetailStatCard("Potassium", "${totals.potassium}mg", "🍌", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailStatCard("Sodium", "${totals.sodium}mg", "🧂", Modifier.weight(1f))
                        Spacer(Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun BigCalorieRing(current: Int, target: Int) {
    val progress = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress, animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "RingProgress"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            strokeWidth = 20.dp,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round
        )

        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 20.dp,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$current",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "/ $target kcal",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun MacroVerticalCard(
    label: String,
    amount: Int,
    target: Int,
    color: Color,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier
) {
    Surface(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(28.dp),
        color = containerColor
    ) {
        Column(
            Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor.copy(0.7f)
                )
                Text(
                    "$amount",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                Text(
                    "g",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(0.7f)
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(0.1f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction = (amount.toFloat() / target).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun DetailStatCard(label: String, value: String, icon: String, modifier: Modifier) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(icon, fontSize = 24.sp)
        }
    }
}

data class AnalyticsTotals(
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val fiber: Int = 0,
    val sugar: Int = 0,
    val vitaminA: Int = 0,
    val vitaminC: Int = 0,
    val vitaminD: Int = 0,
    val iron: Int = 0,
    val calcium: Int = 0,
    val sodium: Int = 0,
    val potassium: Int = 0
) {
    fun add(meal: MealLog) = AnalyticsTotals(
        protein = protein + meal.macros.protein,
        carbs = carbs + meal.macros.carbs,
        fat = fat + meal.macros.fat,
        fiber = fiber + meal.macros.fiber,
        sugar = sugar + meal.macros.sugar,
        vitaminA = vitaminA + meal.micros.vitaminA.toInt(),
        vitaminC = vitaminC + meal.micros.vitaminC.toInt(),
        vitaminD = vitaminD + meal.micros.vitaminD.toInt(),
        iron = iron + meal.micros.iron.toInt(),
        calcium = calcium + meal.micros.calcium.toInt(),
        sodium = sodium + meal.micros.sodium.toInt(),
        potassium = potassium + meal.micros.potassium.toInt()
    )
}