package com.example.aicalorietracker.ui.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun MacroVerticalCard(
    protein: Int,
    carbs: Int,
    fat: Int,
    proteinColor: Color,
    carbsColor: Color,
    fatColor: Color,
    modifier: Modifier = Modifier
) {
    val total = protein + carbs + fat

    val proteinRatio = if (total > 0) protein.toFloat() / total else 0f
    val carbsRatio = if (total > 0) carbs.toFloat() / total else 0f
    val fatRatio = if (total > 0) fat.toFloat() / total else 0f

    val proteinPercentage = (proteinRatio * 100).roundToInt()
    val carbsPercentage = (carbsRatio * 100).roundToInt()
    val fatPercentage = 100 - proteinPercentage - carbsPercentage

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Distribution by grams",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "$total g total",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (protein > 0) {
                Box(
                    modifier = Modifier
                        .weight(protein.toFloat())
                        .fillMaxHeight()
                        .background(proteinColor)
                )
            }

            if (carbs > 0) {
                Box(
                    modifier = Modifier
                        .weight(carbs.toFloat())
                        .fillMaxHeight()
                        .background(carbsColor)
                )
            }

            if (fat > 0) {
                Box(
                    modifier = Modifier
                        .weight(fat.toFloat())
                        .fillMaxHeight()
                        .background(fatColor)
                )
            }
        }

        if (total > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroDetail(
                    label = "Protein",
                    amount = protein,
                    percentage = proteinPercentage,
                    color = proteinColor
                )

                MacroDetail(
                    label = "Carbs",
                    amount = carbs,
                    percentage = carbsPercentage,
                    color = carbsColor
                )

                MacroDetail(
                    label = "Fat",
                    amount = fat,
                    percentage = fatPercentage,
                    color = fatColor
                )
            }
        } else {
            Text(
                text = "No macros logged yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MacroDetail(
    label: String,
    amount: Int,
    percentage: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$amount g",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = color
                )
            }
        }
    }
}