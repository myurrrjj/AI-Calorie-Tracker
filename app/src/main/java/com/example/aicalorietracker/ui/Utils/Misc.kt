package com.example.aicalorietracker.ui.Utils

import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun loading(){
    CircularWavyProgressIndicator(progress = {1f})
//    LoadingIndicator()
}

@Preview(showBackground = true)
@Composable
fun LoadingPreview() {
    loading()
}