package com.myurrrjj.trackabite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.myurrrjj.trackabite.Navigation.CalorieTrackerApp
import com.myurrrjj.trackabite.ui.splash.SplashScreen
import com.myurrrjj.trackabite.ui.theme.AICalorieTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as CalorieTrackerApplication).container
        setContent {
            AICalorieTrackerTheme {
                var splashVisible by remember { mutableStateOf(true) }
                Box(modifier = Modifier.fillMaxSize()) {
                    CalorieTrackerApp(appContainer = appContainer)
                    AnimatedVisibility(
                        visible = splashVisible,
                        enter = EnterTransition.None,
                        exit = slideOutVertically(
                            targetOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(
                                durationMillis = 560,
                                easing = FastOutSlowInEasing
                            )
                        )
                    ) {
                        SplashScreen(onAnimationFinished = { splashVisible = false })
                    }
                }
            }
        }
    }
}
