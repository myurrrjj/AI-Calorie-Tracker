package com.myurrrjj.trackabite.ui.home.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myurrrjj.trackabite.local.MealLog
import com.myurrrjj.trackabite.ui.Utils.bouncyClick
import kotlin.math.roundToInt

@Suppress("UNUSED_PARAMETER")
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
        targetValue = currentCalories, animationSpec = spring(
            stiffness = Spring.StiffnessLow
        )
    )

    val progressTarget = if (targetCalories > 0) {
        (currentCalories.toFloat() / targetCalories).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget, animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow
        )
    )

    val percentage = if (targetCalories > 0) {
        (animatedCalories.toFloat() / targetCalories * 100).roundToInt().coerceAtLeast(0)
    } else {
        0
    }

    val goalStatus = when {
        targetCalories <= 0 -> "Set a goal"
        animatedCalories < targetCalories -> "${targetCalories - animatedCalories} kcal left"
        animatedCalories == targetCalories -> "Goal reached"
        else -> "${animatedCalories - targetCalories} kcal over"
    }

    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .bouncyClick(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .heightIn(min = 182.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GOAL",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp
                    ),
                    color = contentColor,
                    maxLines = 1
                )

                IconButton(
                    onClick = onEditClick,
                    shape = CircleShape,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit daily calorie goal",
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onChartClick,
                    shape = CircleShape,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Insights,
                        contentDescription = "View calorie trends",
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$animatedCalories",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black, fontSize = 30.sp, letterSpacing = (-1).sp
                        ),
                        color = contentColor,
                        maxLines = 1
                    )

                    Spacer(Modifier.width(5.dp))

                    Text(
                        text = "kcal",
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.75f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Text(
                    text = if (targetCalories > 0) {
                        "of $targetCalories kcal target"
                    } else {
                        "Set a daily calorie goal"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.14f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(
                            color = MaterialTheme.colorScheme.primary, shape = CircleShape
                        )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (targetCalories > 0) "$percentage% used" else "No goal",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = contentColor.copy(alpha = 0.75f)
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = goalStatus, style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ), color = contentColor, textAlign = TextAlign.End, maxLines = 1
                )
            }
        }
    }
}
