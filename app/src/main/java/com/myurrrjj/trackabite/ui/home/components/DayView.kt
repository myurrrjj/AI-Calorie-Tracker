package com.myurrrjj.trackabite.ui.home.components

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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.myurrrjj.trackabite.health.HealthConnectManager
import com.myurrrjj.trackabite.local.MealLog

//import com.myurrrjj.trackabite.ui.home.CalorieProgressCard
//import com.myurrrjj.trackabite.ui.home.MealItemCard2

@Composable
fun DayView(
    healthConnectManager: HealthConnectManager,
    meals: List<MealLog>,
    totalCalories: Int,
    targetCalories: Int,
    activeCalories: Int,
    basalCalories: Int,
    isBasalEstimated: Boolean,
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EnergyBurnCard(
                                activeCalories = activeCalories,
                                basalCalories = basalCalories,
                                isBasalEstimated = isBasalEstimated,
                                healthConnectManager = healthConnectManager,
                                onPermissionsGranted = { onHealthPermissionsGranted() },
                                modifier = Modifier.weight(1f)
                            )

//                            Spacer(modifier = Modifier.height(16.dp))

                            CalorieProgressCard(
                                currentCalories = totalCalories,
                                targetCalories = targetCalories,
                                onEditClick = onEditGoal,
                                activeMealId = activeMealId,
                                onClick = onCardClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .sharedBounds(
                                        rememberSharedContentState("detailscreen"),
                                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                                        animatedVisibilityScope = this@AnimatedVisibility
                                    ),
                                onChartClick = onNavigateToAnalyticsChart
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))

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

                    val currentOnLongClick by rememberUpdatedState(onMealLongClick)
                    val currentOnDelete by rememberUpdatedState(onDelete)
                    val currentOnQuantity by rememberUpdatedState(onQuantitySelected)

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
                                onLongClick = remember(meal.id) { { currentOnLongClick(meal) } },
                                onDelete = remember(meal.id) { { currentOnDelete(meal) } },
                                meal = meal,
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(key = "userReq-${meal.id}"),
                                        animatedVisibilityScope = this@AnimatedVisibility,
                                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()
                                    ),
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = this@AnimatedVisibility,
                                onQuantitySelect = remember(meal.id) {
                                    { m, q ->
                                        currentOnQuantity(
                                            m, q
                                        )
                                    }
                                },

                                )
                        }
                    }
                }
            }
        }
    }
}