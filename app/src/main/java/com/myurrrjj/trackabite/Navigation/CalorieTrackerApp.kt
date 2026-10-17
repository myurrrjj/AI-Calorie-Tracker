package com.myurrrjj.trackabite.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myurrrjj.trackabite.AppContainer
import com.myurrrjj.trackabite.ui.MealViewModel
import com.myurrrjj.trackabite.ui.analytics.AnalyticsViewModel
import com.myurrrjj.trackabite.ui.analytics.chart.AnalyticsChartScreen
import com.myurrrjj.trackabite.ui.apiSetupScreen.ApiKeySetupScreen
import com.myurrrjj.trackabite.ui.home.DashboardScreen
import com.myurrrjj.trackabite.ui.onboarding.OnboardingScreen
import com.myurrrjj.trackabite.ui.settings.SettingsScreen

@Composable
fun CalorieTrackerApp(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val mealViewModel: MealViewModel = viewModel(factory = MealViewModel.Factory)
    val preferences = appContainer.userPreferencesRepository
    val profile  by preferences.profile.collectAsStateWithLifecycle()
    val startDestination = remember {
        if (preferences.hasCompletedOnboarding()) DashboardRoute else OnboardingRoute
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                onGetStarted = {
                    preferences.setOnboardingCompleted()
                    navController.navigate(ApiKeySetupRoute) {
                        popUpTo<OnboardingRoute> { inclusive = true }
                    }
                },
                onSkip = {
                    preferences.setOnboardingCompleted()
                    navController.navigate(DashboardRoute) {
                        popUpTo<OnboardingRoute> { inclusive = true }
                    }
                },
                initialProfile = profile,
                onProfileSubmitted = { preferences.saveProfile(it) }
            )
        }

        composable<ApiKeySetupRoute> {
            ApiKeySetupScreen(
                repository = preferences,
                onNavigateBack = {
                    navController.navigate(DashboardRoute) {
                        popUpTo<ApiKeySetupRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSkip = {
                    navController.navigate(DashboardRoute) {
                        popUpTo<ApiKeySetupRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<DashboardRoute> {
            DashboardScreen(
                viewModel = mealViewModel,
                onNavigateToApiGuide = { navController.navigate(ApiKeySetupRoute) },
                onNavigateToAnalyticsChart = { navController.navigate(AnalyticsChartRoute) },
                onNavigateToSettings = {navController.navigate(SettingsRoute)}
            )
        }

        composable<AnalyticsChartRoute> {
            val analyticsViewModel: AnalyticsViewModel = viewModel(factory = AnalyticsViewModel.Factory)
            AnalyticsChartScreen(
                viewModel = analyticsViewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                viewModel = mealViewModel,
                initialProfile = profile,
                onSaveProfile = { p ->
                    preferences.saveProfile(p)
                    mealViewModel.refreshHealthData()
                },
                onNavigateToApiGuide = { navController.navigate(ApiKeySetupRoute) },
                onBack = { navController.navigateUp() }
            )
        }

    }
}
