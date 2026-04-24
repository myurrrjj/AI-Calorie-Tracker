package com.example.aicalorietracker.ui.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.ui.Utils.bouncyClick

@Composable
fun SharedTransitionScope.MealDetailOverlay2(
    meal: MealLog?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onSaveToFavourites: (MealLog) -> Unit,
    onQuantityChange: (MealLog, Float) -> Unit

) {
    AnimatedContent(
        modifier = modifier,
        targetState = meal,
        label = "MealDetailOverlay",
        transitionSpec = { fadeIn() togetherWith fadeOut() }) { meal ->
        Box(
            Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter
        ) {
            if (meal != null) {

                var isLoaded by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    isLoaded = true
                }

                val contentAlpha by animateFloatAsState(
                    targetValue = if (isLoaded) 1f else 0f,
                    animationSpec = tween(durationMillis = 400, delayMillis = 100),
                    label = "alpha"
                )
                val contentOffset by animateDpAsState(
                    targetValue = if (isLoaded) 0.dp else 40.dp, animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ), label = "offset"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(
                                key = "bounds-${meal.id}"
                            ),
                            animatedVisibilityScope = this@AnimatedContent,
                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                            clipInOverlayDuringTransition = OverlayClip(
                                RoundedCornerShape(0.dp)
                            )
                        ),
                    shape = RoundedCornerShape(0.dp),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 64.dp, start = 24.dp, end = 24.dp, bottom = 48.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = meal.userRequest,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState("userReq-${meal.id}"),
                                        animatedVisibilityScope = this@AnimatedContent,
                                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()
                                    ),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { onSaveToFavourites(meal) },
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer, CircleShape
                                        )
                                        .size(40.dp)
                                        .bouncyClick { onSaveToFavourites(meal) }) {
                                    Icon(
                                        Icons.Rounded.BookmarkAdd,
                                        contentDescription = "Save Meal",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                IconButton(
                                    onClick = { onDismiss() }, modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant, CircleShape
                                        )
                                        .size(40.dp)
                                        .bouncyClick { onDismiss() }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }


                        Spacer(Modifier.height(32.dp))

                        Column(
                            modifier = Modifier.graphicsLayer {
                                alpha = contentAlpha
                                translationY = contentOffset.toPx()
                            }) {

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClick { },
                                shape = RoundedCornerShape(32.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                tonalElevation = 0.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${meal.effectiveCalories}",
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontWeight = FontWeight.Black, letterSpacing = (-2).sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.sharedBounds(
                                            sharedContentState = rememberSharedContentState("calories.${meal.id}"),
                                            animatedVisibilityScope = this@AnimatedContent,
                                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()

                                        )
                                    )
                                    Text(
                                        text = "Total Kilocalories",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.7f
                                        )
                                    )
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClick { },
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = meal.aiResponse,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        lineHeight = 26.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(24.dp)
                                )
                            }

                            Spacer(Modifier.height(40.dp))

                            Text(
                                "Macronutrients",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .bouncyClick { }) {
                                    MacroCard(
                                        "Protein",
                                        "${meal.effectiveProtein}g",
                                        Modifier.fillMaxWidth(),
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .bouncyClick { }) {
                                    MacroCard(
                                        "Carbs",
                                        "${meal.effectiveCarbs}g",
                                        Modifier.fillMaxWidth(),
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .bouncyClick { }) {
                                    MacroCard(
                                        "Fat",
                                        "${meal.effectiveFat}g",
                                        Modifier.fillMaxWidth(),
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                }
                            }

//                            QuantitySelector(
//                                quantity = meal.quantity,
//                                onQuantityChange = { newQty -> onQuantityChange(meal, newQty) }
//                            )
                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .bouncyClick { }) {
                                    MacroCard(
                                        "Fiber",
                                        "${meal.effectiveFiber}g",
                                        Modifier.fillMaxWidth(),
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .bouncyClick { }) {
                                    MacroCard(
                                        "Sugar",
                                        "${meal.effectiveSugar}g",
                                        Modifier.fillMaxWidth(),
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                }
                            }

                            Spacer(Modifier.height(40.dp))

                            Text(
                                "Micronutrients",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(Modifier.height(16.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClick { },
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                tonalElevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    MicroRowItem("Vitamin A", "${meal.effectiveVitaminA.toInt()}", "IU")
                                    MicroRowItem("Vitamin C", "${meal.effectiveVitaminC.toInt()}", "mg")
                                    MicroRowItem("Vitamin D", "${meal.effectiveVitaminD.toInt()}", "IU")
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f)
                                    )

                                    MicroRowItem("Iron", "${meal.effectiveIron.toInt()}", "mg")
                                    MicroRowItem("Calcium", "${meal.effectiveCalcium.toInt()}", "mg")
                                    MicroRowItem("Sodium", "${meal.effectiveSodium.toInt()}", "mg")
                                    MicroRowItem("Potassium", "${meal.effectivePotassium.toInt()}", "mg")     }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun MacroCard(
    label: String, value: String, modifier: Modifier = Modifier, color: Color
) {
    Surface(
        modifier = modifier, shape = RoundedCornerShape(16.dp), color = color.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MicroRowItem(label: String, value: String, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )


        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit, style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold, baselineShift = BaselineShift.None
                ), color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}