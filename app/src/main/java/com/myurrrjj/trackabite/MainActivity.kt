package com.myurrrjj.trackabite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.myurrrjj.trackabite.Navigation.CalorieTrackerApp
import com.myurrrjj.trackabite.local.AppDatabase
import com.myurrrjj.trackabite.ui.theme.AICalorieTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val mealDao = AppDatabase.getDatabase(applicationContext).mealDao()
//        seedDatabaseForGraphTesting(mealDao)
        enableEdgeToEdge()
        val appContainer = (application as CalorieTrackerApplication).container
        setContent {
            AICalorieTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    CalorieTrackerApp(appContainer = appContainer)
                }
            }
        }
    }
}