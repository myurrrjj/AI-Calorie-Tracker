package com.myurrrjj.trackabite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.myurrrjj.trackabite.Navigation.CalorieTrackerApp
import com.myurrrjj.trackabite.local.AppDatabase
import com.myurrrjj.trackabite.ui.splash.SplashScreen
import com.myurrrjj.trackabite.ui.theme.AICalorieTrackerTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as CalorieTrackerApplication).container
        setContent {
            AICalorieTrackerTheme {
                var showSplash by remember { mutableStateOf(true) }
                if (showSplash) {
                    SplashScreen(onAnimationFinished = { showSplash = false })
                } else {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        CalorieTrackerApp(appContainer = appContainer)
                    }
                }
            }
        }
    }

}