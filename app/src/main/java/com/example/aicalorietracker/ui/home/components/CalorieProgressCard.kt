package com.example.aicalorietracker.ui.home.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.ui.Utils.bouncyClick

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalorieProgressCard(
    currentCalories: Int,
    targetCalories: Int,
    onEditClick: () -> Unit,
    activeMealId: MealLog?,
    onClick: () -> Unit,
    onChartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedCalories by animateIntAsState(
        targetValue = currentCalories,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "Calories"
    )
    val progress = (animatedCalories.toFloat() / targetCalories.toFloat())
    val visualProgress = progress.coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = visualProgress, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "Progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseDuration = if (percentage < 120) 1000 else if (percentage in 120..150) 500 else 100

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (progress > 0.8f) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDuration), repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .bouncyClick(onClick = onClick),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 8.dp
    ) {

        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.bouncyClick { onEditClick() }) {
                    Text(
                        text = "Daily Goal: $targetCalories",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$animatedCalories", style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black, fontSize = 48.sp, letterSpacing = (-2).sp
                    ), color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "kcal consumed",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.bouncyClick { onChartClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Insights,
                            contentDescription = "Trends",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "View Trends",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(110.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.1f),
                    strokeWidth = 14.dp,
                    trackColor = Color.Transparent
                )

                CircularWavyProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .size(110.dp)
                        .padding(10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent,
                    trackStroke = WavyProgressIndicatorDefaults.circularTrackStroke,
                    amplitude = WavyProgressIndicatorDefaults.indicatorAmplitude,
                    wavelength = WavyProgressIndicatorDefaults.CircularWavelength,
                    waveSpeed = WavyProgressIndicatorDefaults.CircularWavelength,
                    stroke = WavyProgressIndicatorDefaults.circularIndicatorStroke
                )

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}