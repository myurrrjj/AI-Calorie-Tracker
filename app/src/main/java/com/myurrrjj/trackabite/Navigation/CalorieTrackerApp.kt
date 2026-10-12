package com.myurrrjj.trackabite.Navigation

import androidx.compose.runtime.Composable
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

@Composable
fun CalorieTrackerApp(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val mealViewModel: MealViewModel = viewModel(factory = MealViewModel.Factory)

    NavHost(
        navController = navController,
        startDestination = DashboardRoute
    ) {
        composable<DashboardRoute> {

            DashboardScreen(
                viewModel = mealViewModel,
                onNavigateToApiGuide = {
                    navController.navigate(ApiKeySetupRoute)
                },
                onNavigateToAnalyticsChart = {
                    navController.navigate(AnalyticsChartRoute)
                }
            )
        }

        composable<ApiKeySetupRoute> {
            ApiKeySetupScreen(
                repository = appContainer.userPreferencesRepository,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable<AnalyticsChartRoute> {
            val analyticsViewModel: AnalyticsViewModel = viewModel(factory = AnalyticsViewModel.Factory)
            AnalyticsChartScreen(
                viewModel = analyticsViewModel,
                onNavigateBack = {
                    navController.navigateUp()
                },
            )
        }
    }
}