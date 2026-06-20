package com.example.aicalorietracker.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aicalorietracker.health.HealthConnectManager
import com.example.aicalorietracker.local.MealLog
//import com.example.aicalorietracker.ui.home.CalorieProgressCard
//import com.example.aicalorietracker.ui.home.MealItemCard2

@Composable
fun DayView(
    healthConnectManager: HealthConnectManager,
    meals: List<MealLog>,
    totalCalories: Int,
    targetCalories: Int,
    burnedCalories: Int,
    activeMealId: MealLog?,
    onDelete: (MealLog) -> Unit,
    onEditGoal: () -> Unit,
    onMealLongClick: (MealLog) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    onCardClick: () -> Unit,
    showDailyAnalytics: Boolean,
    onQuantitySelected: (MealLog, Float) -> Unit,
    onHealthPermissionsGranted: () -> Unit,
    onNavigateToAnalyticsChart: () -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(key1 = meals.firstOrNull()?.id) {
        if (meals.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    with(sharedTransitionScope) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedVisibility(visible = !showDailyAnalytics) {
                        Column {
                            CaloriesBurnedCard(
                                healthConnectManager = healthConnectManager,
                                burnedCalories = burnedCalories,
                                onPermissionsGranted = { onHealthPermissionsGranted() }
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            CalorieProgressCard(
                                currentCalories = totalCalories,
                                targetCalories = targetCalories,
                                onEditClick = onEditGoal,
                                activeMealId = activeMealId,
                                onClick = onCardClick,
                                modifier = Modifier.sharedBounds(
                                    rememberSharedContentState("detailscreen"),
                                    resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                                    animatedVisibilityScope = this@AnimatedVisibility
                                ),
                                onChartClick = onNavigateToAnalyticsChart
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Recent Meals",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (meals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No meals logged yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                items(items = meals, key = { it.id }) { meal ->
                    AnimatedVisibility(
                        visible = meal != activeMealId,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier.animateItem()
                    ) {
                        Box(
                            Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "bounds-${meal.id}"),
                                animatedVisibilityScope = this@AnimatedVisibility,
                                resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                                clipInOverlayDuringTransition = OverlayClip(
                                    RoundedCornerShape(28.dp)
                                )
                            )
                        ) {
                            MealItemCard2(
                                onLongClick = { onMealLongClick(meal) },
                                meal = meal,
                                onDelete = { onDelete(meal) },
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(key = "userReq-${meal.id}"),
                                        animatedVisibilityScope = this@AnimatedVisibility,
                                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()
                                    ),
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = this@AnimatedVisibility,
                                onQuantitySelect = { mealLog, newQty ->
                                    onQuantitySelected(mealLog, newQty)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}