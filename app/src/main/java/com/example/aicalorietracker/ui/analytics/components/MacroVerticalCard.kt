package com.example.aicalorietracker.ui.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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