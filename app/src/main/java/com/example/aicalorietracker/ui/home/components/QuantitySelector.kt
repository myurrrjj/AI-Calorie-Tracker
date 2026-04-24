package com.example.aicalorietracker.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aicalorietracker.ui.Utils.bouncyClick
import com.example.aicalorietracker.ui.theme.AICalorieTrackerTheme

@Composable
fun QuantitySelector(
    quantity: Float,
    onQuantityChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier, verticalArrangement = Arrangement.Center) {


    }
}

@Preview(showBackground = true)
@Composable
fun QuantitySelectorPreview(){
    AICalorieTrackerTheme() {
        QuantitySelector(quantity = 1f, onQuantityChange={})
    }
}