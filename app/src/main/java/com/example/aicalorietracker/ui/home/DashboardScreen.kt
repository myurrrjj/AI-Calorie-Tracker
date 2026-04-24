package com.example.aicalorietracker.ui.home

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.ui.DailyAnalyticsScreen
import com.example.aicalorietracker.ui.MealUiState
import com.example.aicalorietracker.ui.MealViewModel
import com.example.aicalorietracker.ui.Utils.bouncyClick
import com.example.aicalorietracker.ui.dialogs.ApiKeyQuickDialog
import com.example.aicalorietracker.ui.dialogs.EditTargetDialog
import com.example.aicalorietracker.ui.home.components.DayView2
import com.example.aicalorietracker.ui.home.components.InputArea
import com.example.aicalorietracker.ui.home.components.MealDetailOverlay2
import com.example.aicalorietracker.ui.home.components.SavedMealsBottomSheet
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val HEADER_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMM d")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardScreen2(
    viewModel: MealViewModel, onNavigateToApiGuide: () -> Unit
) {
    var showDailyAnalytics by remember { mutableStateOf(false) }
    var showApiDialog by remember { mutableStateOf(false) }
    var showSavedMealsSheet by remember { mutableStateOf(false) }

    var activeMeal by remember { mutableStateOf<MealLog?>(null) }
    BackHandler(enabled = activeMeal != null) {
        activeMeal = null
    }

    val startIndex = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(
        initialPage = startIndex, pageCount = { Int.MAX_VALUE })
    val scope = rememberCoroutineScope()

    fun getDateForPage(page: Int): LocalDate {
        val diff = page - startIndex
        return viewModel.today.plusDays(diff.toLong())
    }

    val currentDate = getDateForPage(pagerState.currentPage)

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val snackbarHostState = remember { SnackbarHostState() }

    val currentUiState by viewModel.getDayFlow(currentDate)
        .collectAsStateWithLifecycle(MealUiState())
    val savedMeals by viewModel.savedMeals.collectAsStateWithLifecycle()

    LaunchedEffect(currentUiState.errorMessage) {
        currentUiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error, withDismissAction = true
            )
            viewModel.errorShown()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate =
                            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        val daysDiff = ChronoUnit.DAYS.between(viewModel.today, selectedDate)
                        scope.launch { pagerState.scrollToPage(startIndex + daysDiff.toInt()) }
                    }
                    showDatePicker = false
                }) { Text("OK") }
        }) { DatePicker(state = datePickerState) }
    }

    var showTargetDialog by remember { mutableStateOf(false) }
    if (showTargetDialog) {
        EditTargetDialog(
            currentTarget = currentUiState.targetCalories,
            onDismiss = { showTargetDialog = false },
            onConfirm = { newTarget ->
                viewModel.updateTargetCalories(newTarget)
                showTargetDialog = false
            })
    }

    if (showApiDialog) {
        ApiKeyQuickDialog(
            initialKey = viewModel.getApiKey() ?: "",
            onDismiss = { showApiDialog = false },
            onSave = { key ->
                viewModel.saveApiKey(key)
                showApiDialog = false
            },
            onHelpClick = {
                showApiDialog = false
                onNavigateToApiGuide()
            })
    }
    if (showSavedMealsSheet) {
        SavedMealsBottomSheet(
            savedMeals = savedMeals,
            onDismiss = { showSavedMealsSheet = false },
            onLogSavedMeal = { savedMeal ->
                viewModel.quickLogSavedMeal(savedMeal, currentDate)
                showSavedMealsSheet = false
            },
            onDeleteSavedMeal = viewModel::deleteSavedMeal
        )
    }

    SharedTransitionLayout {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val dateText = remember(currentDate) {
                                if (currentDate == viewModel.today) "Today's Fuel"
                                else currentDate.format(HEADER_DATE_FORMATTER)
                            }

                            Text(
                                text = dateText,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.bouncyClick(onClick = { showApiDialog = true })
                            ) {
                                Box(Modifier.padding(12.dp)) {
                                    Icon(
                                        Icons.Rounded.Key,
                                        contentDescription = "API Key",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.bouncyClick(
                                    onClick = { showDatePicker = true },
                                    onLongPress = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(startIndex)
                                        }
                                    })
                            ) {
                                Box(Modifier.padding(12.dp)) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Calendar")
                                }
                            }
                        }
                    }
                }) { paddingValues ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = activeMeal == null
                    ) { page ->
                        val pageDate = getDateForPage(page)
                        val pageState by viewModel.getDayFlow(pageDate)
                            .collectAsStateWithLifecycle(MealUiState())

                        DayView2(
                            meals = pageState.meals,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            activeMealId = activeMeal,
                            onDelete = { viewModel.deleteMeal(it) },
                            onEditGoal = { showTargetDialog = true },
                            onMealLongClick = { activeMeal = it },
                            onCardClick = { showDailyAnalytics = true },
                            totalCalories = pageState.totalCalories,
                            targetCalories = pageState.targetCalories,
                            showDailyAnalytics = showDailyAnalytics
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                    ) {
                        InputArea(
                            onSubmit = viewModel::analyseAndAddMeal,
                            isLoading = currentUiState.isLoading,
                            targetDate = currentDate,
                            onOpenSavedMeals = { showSavedMealsSheet = true },
                        )
                    }
                }
            }

            MealDetailOverlay2(
                meal = activeMeal,
                onDismiss = { activeMeal = null },
                onSaveToFavourites = { mealToSave ->
                    viewModel.saveMealToFavorites(mealToSave)
                    activeMeal = null
                },
                onQuantityChange = { mealToUpdate, newQty ->
                    viewModel.updateMealQuantity(mealToUpdate, newQty)
                })


            AnimatedVisibility(
                visible = showDailyAnalytics,
                enter = EnterTransition.None,
                exit = ExitTransition.None
            ) {
                DailyAnalyticsScreen(
                    this, state = currentUiState, onDismiss = { showDailyAnalytics = false })
            }
        }
    }
}




fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile(
        "temp_image_${System.currentTimeMillis()}", ".jpg", context.cacheDir
    ).apply {
        createNewFile()
    }

    return FileProvider.getUriForFile(
        context, "${context.packageName}.provider", tempFile
    )
}


