package com.example.aicalorietracker.useless

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.ui.Utils.bouncyClick
import com.example.aicalorietracker.ui.home.components.MacroCard
import com.example.aicalorietracker.ui.home.components.MicroRowItem

//import com.example.aicalorietracker.ui.home.MacroCard
//import com.example.aicalorietracker.ui.home.MicroRowItem

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun CalorieProgressStrip(
    currentCalories: Int,
    targetCalories: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val progress = (currentCalories.toFloat() / targetCalories.toFloat())
    val visualProgress = progress.coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = visualProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by if (progress > 0.8f) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = if (percentage < 120) tween(1000) else if (percentage in 120..150) tween(
                    500
                ) else tween(100),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Pulse"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    with(sharedTransitionScope) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .bouncyClick { }
                .sharedBounds(
                    rememberSharedContentState(key = "CalorieProgressCard"),
                    animatedVisibilityScope = animatedContentScope
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$currentCalories",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "/ $targetCalories kcal",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 4.dp) // Align baseline visually
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                        .aspectRatio(1f) // Keep it circular
                        .fillMaxHeight()
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.1f),
                        strokeWidth = 8.dp,
                        trackColor = Color.Transparent
                    )

                    CircularWavyProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent,
                        trackStroke = WavyProgressIndicatorDefaults.circularTrackStroke,
                        amplitude = WavyProgressIndicatorDefaults.indicatorAmplitude,
                        wavelength = 10.dp,
                        stroke = Stroke(width = 8.dp.value)
                    )

                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}


@Composable
fun MealDetailOverlay(
    meal: MealLog,
    onDismiss: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        Surface(
            modifier = Modifier.padding(bottom = 120.dp, start = 24.dp, end = 24.dp, top = 24.dp)
//                    .padding(top = 80.dp)


            ,
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp

        ) {
            with(sharedTransitionScope) {

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(
                                key = "bounds-${meal.id}"
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                        )
                        .verticalScroll(rememberScrollState())

                        .widthIn(max = 360.dp), horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = meal.userRequest,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(Modifier.height(8.dp))
                            with(sharedTransitionScope) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
//                                        .sharedElement(
//                                        sharedContentState = rememberSharedContentState("kcal-${meal.id}"),
//                                        animatedVisibilityScope = animatedVisibilityScope
//                                    )
                                ) {
                                    Text(
                                        text = "${meal.macros.calories} kcal",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp, vertical = 6.dp
                                        )
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { onDismiss?.invoke() }, modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), CircleShape
                                )
                                .size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = meal.aiResponse, style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp
                        )
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f)
                    )

                    Text(
                        "Macronutrients",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MacroCard(
                            "Protein",
                            "${meal.macros.protein}g",
                            Modifier.weight(1f),
                            MaterialTheme.colorScheme.primaryContainer
                        )
                        MacroCard(
                            "Carbs",
                            "${meal.macros.carbs}g",
                            Modifier.weight(1f),
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                        MacroCard(
                            "Fat",
                            "${meal.macros.fat}g",
                            Modifier.weight(1f),
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MacroCard(
                            "Fiber",
                            "${meal.macros.fiber}g",
                            Modifier.weight(1f),
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        MacroCard(
                            "Sugar",
                            "${meal.macros.sugar}g",
                            Modifier.weight(1f),
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    Text(
                        "Micronutrients",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MicroRowItem("Vitamin A", "${meal.micros.vitaminA}", "IU")
                        MicroRowItem("Vitamin C", "${meal.micros.vitaminC}", "mg")
                        MicroRowItem("Vitamin D", "${meal.micros.vitaminD}", "IU")

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f)
                        )

                        MicroRowItem("Iron", "${meal.micros.iron}", "mg")
                        MicroRowItem("Calcium", "${meal.micros.calcium}", "mg")
                        MicroRowItem("Sodium", "${meal.micros.sodium}", "mg")
                        MicroRowItem("Potassium", "${meal.micros.potassium}", "mg")
                    }
                }
            }
        }

    }
}