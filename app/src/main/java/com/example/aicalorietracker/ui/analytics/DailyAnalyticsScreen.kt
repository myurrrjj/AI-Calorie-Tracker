package com.example.aicalorietracker.ui

import com.example.aicalorietracker.ui.analytics.components.DetailStatCard
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aicalorietracker.ui.analytics.AnalyticsTotals
import com.example.aicalorietracker.ui.analytics.components.BigCalorieRing
import com.example.aicalorietracker.ui.analytics.components.MacroVerticalCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.DailyAnalyticsScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    state: MealUiState,
    onDismiss: () -> Unit
) {
    val totals = remember(state.meals) {
        state.meals.fold(AnalyticsTotals()) { acc, meal ->
            acc.add(meal)
        }
    }

    BackHandler {
        onDismiss()
    }

    Scaffold(
        modifier = Modifier
            .sharedBounds(
                rememberSharedContentState("detailscreen"),
                animatedVisibilityScope = animatedVisibilityScope,
                resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()
            ),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Daily Breakdown",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BigCalorieRing(
                    current = state.totalCalories,
                    target = state.targetCalories
                )
            }

            Text(
                text = "Macro Split",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            MacroVerticalCard(
                protein = totals.protein,
                carbs = totals.carbs,
                fat = totals.fat,
                proteinColor = MaterialTheme.colorScheme.primary,
                carbsColor = MaterialTheme.colorScheme.secondary,
                fatColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Nutrition Details",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatCard(
                        "Fiber",
                        "${totals.fiber} g",
                        "🥗",
                        Modifier.weight(1f)
                    )
                    DetailStatCard(
                        "Sugar",
                        "${totals.sugar} g",
                        "🍬",
                        Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatCard(
                        "Vitamin A",
                        "${totals.vitaminA} mcg RAE",
                        "🥕",
                        Modifier.weight(1f)
                    )
                    DetailStatCard(
                        "Vitamin C",
                        "${totals.vitaminC} mg",
                        "🍊",
                        Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatCard(
                        "Vitamin D",
                        "${totals.vitaminD} mcg",
                        "☀️",
                        Modifier.weight(1f)
                    )
                    DetailStatCard(
                        "Calcium",
                        "${totals.calcium} mg",
                        "🥛",
                        Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatCard(
                        "Iron",
                        "${totals.iron} mg",
                        "🦾",
                        Modifier.weight(1f)
                    )
                    DetailStatCard(
                        "Potassium",
                        "${totals.potassium} mg",
                        "🍌",
                        Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatCard(
                        "Sodium",
                        "${totals.sodium} mg",
                        "🧂",
                        Modifier.weight(1f)
                    )
                    Spacer(
                        Modifier.weight(1f)
                    )
                }
            }

            Spacer(
                Modifier.height(48.dp)
            )
        }
    }
}