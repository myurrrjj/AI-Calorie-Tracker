package com.example.aicalorietracker.ui.homescreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.local.SavedMeal
import com.example.aicalorietracker.ui.DailyAnalyticsScreen
import com.example.aicalorietracker.ui.MealUiState
import com.example.aicalorietracker.ui.MealViewModel
import com.example.aicalorietracker.ui.Utils.bouncyClick
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
    viewModel: MealViewModel,
    onNavigateToApiGuide: () -> Unit
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
                message = error,
                withDismissAction = true
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
            }
        )
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
                }
            ) { paddingValues ->

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
                }
            )

            AnimatedVisibility(
                visible = showDailyAnalytics,
                enter = EnterTransition.None,
                exit = ExitTransition.None
            ) {
                DailyAnalyticsScreen(
                    this,
                    state = currentUiState,
                    onDismiss = { showDailyAnalytics = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedMealsBottomSheet(
    savedMeals: List<SavedMeal>,
    onDismiss: () -> Unit,
    onLogSavedMeal: (SavedMeal) -> Unit,
    onDeleteSavedMeal: (SavedMeal) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Saved Meals",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap any meal to instantly log it without using the AI.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (savedMeals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved meals yet. Save a meal from its detail screen.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = savedMeals, key = { it.id }) { savedMeal ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bouncyClick { onLogSavedMeal(savedMeal) },
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            tonalElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🥗", fontSize = 24.sp)
                                    }
                                }

                                Spacer(Modifier.width(16.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = savedMeal.userRequest,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "${savedMeal.macros.calories} kcal",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(
                                                    horizontal = 6.dp,
                                                    vertical = 2.dp
                                                )
                                            )
                                        }
                                        Text(
                                            text = "•",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                        Text(
                                            text = "Used ${savedMeal.frequency}x",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDeleteSavedMeal(savedMeal) },
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.errorContainer.copy(
                                                alpha = 0.5f
                                            ), CircleShape
                                        )
                                        .size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ApiKeyQuickDialog(
    initialKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onHelpClick: () -> Unit
) {
    var keyInput by remember { mutableStateOf(initialKey) }
    val isKeyValid = keyInput.startsWith("AIza") && keyInput.length > 30

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Gemini API Key",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your key is stored securely on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    placeholder = { Text("AIzaSy...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onHelpClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.HelpOutline,
                            contentDescription = "Help",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("How to get this?")
                    }

                    Button(
                        onClick = { onSave(keyInput.trim()) },
                        enabled = isKeyValid,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DayView2(
    meals: List<MealLog>,
    totalCalories: Int,
    targetCalories: Int,
    activeMealId: MealLog?,
    onDelete: (MealLog) -> Unit,
    onEditGoal: () -> Unit,
    onMealLongClick: (MealLog) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    onCardClick: () -> Unit,
    showDailyAnalytics: Boolean
) {
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        with(sharedTransitionScope) {
            AnimatedVisibility(visible = !showDailyAnalytics) {

                CalorieProgressCard(
                    currentCalories = totalCalories,
                    targetCalories = targetCalories,
                    onEditClick = onEditGoal,
                    activeMealId = activeMealId,
                    onClick = onCardClick,
                    modifier = Modifier
                        .sharedBounds(
                            rememberSharedContentState("detailscreen"),
                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                            animatedVisibilityScope = this@AnimatedVisibility

                        )

                )
            }
        }
        Spacer(Modifier.height(32.dp))

        Text(
            text = "Recent Meals",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        if (meals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No meals logged yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        } else {
            LaunchedEffect(key1 = meals.firstOrNull()?.id) {
                if (meals.isNotEmpty()) {
                    listState.animateScrollToItem(0)
                }
            }
            with(sharedTransitionScope) {

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = meals, key = { it.id }) { meal ->
                        AnimatedVisibility(
                            visible = meal != activeMealId,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut(),
                            modifier = Modifier.animateItem()
                        ) {

                            Box(
                                Modifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "bounds-${meal.id}"),
                                    animatedVisibilityScope = this@AnimatedVisibility,
                                    resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                                    clipInOverlayDuringTransition = OverlayClip(
                                        RoundedCornerShape(28.dp)
                                    )
                                )
                            ) {
                                MealItemCard2(
                                    onLongClick = { onMealLongClick(meal) },
                                    meal = meal,
                                    onDelete = { onDelete(meal) },
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .sharedBounds(
                                            sharedContentState = rememberSharedContentState(key = "userReq-${meal.id}"),

                                            animatedVisibilityScope = this@AnimatedVisibility,
                                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()
                                        ),
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedVisibilityScope = this@AnimatedVisibility,
                                )
                            }
                        }

                    }
                }

            }
        }
    }
}


@Composable
fun SharedTransitionScope.MealDetailOverlay2(
    meal: MealLog?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onSaveToFavourites: (MealLog) -> Unit

) {
    AnimatedContent(
        modifier = modifier,
        targetState = meal,
        label = "MealDetailOverlay",
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { meal ->
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (meal != null) {

                var isLoaded by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    isLoaded = true
                }

                val contentAlpha by animateFloatAsState(
                    targetValue = if (isLoaded) 1f else 0f,
                    animationSpec = tween(durationMillis = 400, delayMillis = 100),
                    label = "alpha"
                )
                val contentOffset by animateDpAsState(
                    targetValue = if (isLoaded) 0.dp else 40.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "offset"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(
                                key = "bounds-${meal.id}"
                            ),
                            animatedVisibilityScope = this@AnimatedContent,
                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                            clipInOverlayDuringTransition = OverlayClip(
                                RoundedCornerShape(0.dp)
                            )
                        ),
                    shape = RoundedCornerShape(0.dp),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 64.dp, start = 24.dp, end = 24.dp, bottom = 48.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = meal.userRequest,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState("userReq-${meal.id}"),
                                        animatedVisibilityScope = this@AnimatedContent,
                                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()
                                    ),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-1).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { onSaveToFavourites(meal) },
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape
                                        )
                                        .size(40.dp)
                                        .bouncyClick { onSaveToFavourites(meal) }
                                ) {
                                    Icon(
                                        Icons.Rounded.BookmarkAdd,
                                        contentDescription = "Save Meal",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                IconButton(
                                    onClick = { onDismiss() },
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        )
                                        .size(40.dp)
                                        .bouncyClick { onDismiss() }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }


                        Spacer(Modifier.height(32.dp))

                        Column(
                            modifier = Modifier.graphicsLayer {
                                alpha = contentAlpha
                                translationY = contentOffset.toPx()
                            }
                        ) {

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClick { },
                                shape = RoundedCornerShape(32.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                tonalElevation = 0.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${meal.macros.calories}",
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-2).sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.sharedBounds(
                                            sharedContentState = rememberSharedContentState("calories.${meal.id}"),
                                            animatedVisibilityScope = this@AnimatedContent,
                                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()

                                        )
                                    )
                                    Text(
                                        text = "Total Kilocalories",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.7f
                                        )
                                    )
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClick { },
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = meal.aiResponse,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        lineHeight = 26.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(24.dp)
                                )
                            }

                            Spacer(Modifier.height(40.dp))

                            Text(
                                "Macronutrients",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .bouncyClick { }) {
                                    MacroCard(
                                        "Protein",
                                        "${meal.macros.protein}g",
                                        Modifier.fillMaxWidth(),
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .bouncyClick { }) {
                                    MacroCard(
                                        "Carbs",
                                        "${meal.macros.carbs}g",
                                        Modifier.fillMaxWidth(),
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .bouncyClick { }) {
                                    MacroCard(
                                        "Fat",
                                        "${meal.macros.fat}g",
                                        Modifier.fillMaxWidth(),
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .bouncyClick { }) {
                                    MacroCard(
                                        "Fiber",
                                        "${meal.macros.fiber}g",
                                        Modifier.fillMaxWidth(),
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .bouncyClick { }) {
                                    MacroCard(
                                        "Sugar",
                                        "${meal.macros.sugar}g",
                                        Modifier.fillMaxWidth(),
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                }
                            }

                            Spacer(Modifier.height(40.dp))

                            Text(
                                "Micronutrients",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(Modifier.height(16.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClick { },
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                tonalElevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    MicroRowItem("Vitamin A", "${meal.micros.vitaminA}", "IU")
                                    MicroRowItem("Vitamin C", "${meal.micros.vitaminC}", "mg")
                                    MicroRowItem("Vitamin D", "${meal.micros.vitaminD}", "IU")

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f)
                                    )

                                    MicroRowItem("Iron", "${meal.micros.iron}", "mg")
                                    MicroRowItem("Calcium", "${meal.micros.calcium}", "mg")
                                    MicroRowItem("Sodium", "${meal.micros.sodium}", "mg")
                                    MicroRowItem("Potassium", "${meal.micros.potassium}", "mg")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MealItemCard2(
    meal: MealLog,
    onDelete: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val infiniteTransition = rememberInfiniteTransition(label = "analyzingPulse")
    val animatedAlpha by if (meal.isAnalysing) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f, targetValue = 1f, animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "alpha"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = animatedAlpha }
            .bouncyClick(onClick = {
                if (!meal.isAnalysing) onLongClick()
            }, onLongPress = {
                if (!meal.isAnalysing) onLongClick()
            }),
        shape = RoundedCornerShape(24.dp),
        color = if (meal.isAnalysing) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = if (meal.isAnalysing) 0.dp else 4.dp,
        shadowElevation = if (meal.isAnalysing) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (meal.isAnalysing) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (meal.imagePath != null) {
                        AsyncImage(
                            model = meal.imagePath,
                            contentDescription = "Meal Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (!meal.isAnalysing) {
                        Text("🥗", fontSize = 24.sp)
                    }

                    if (meal.isAnalysing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                strokeWidth = 3.dp,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = meal.userRequest,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                if (meal.isAnalysing) {
                    Text(
                        "Analysing...",
                        style = MaterialTheme.typography.labelMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            with(sharedTransitionScope) {
                                Text(
                                    text = "${meal.macros.calories} kcal",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .sharedBounds(
                                            sharedContentState = rememberSharedContentState("calories.${meal.id}"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()

                                        )
                                )
                            }
                        }

                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )

                        Text(
                            text = "${meal.macros.protein}g protein",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!meal.isAnalysing) {
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun MacroCard(
    label: String, value: String, modifier: Modifier = Modifier, color: Color
) {
    Surface(
        modifier = modifier, shape = RoundedCornerShape(16.dp), color = color.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MicroRowItem(label: String, value: String, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )


        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit, style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold, baselineShift = BaselineShift.None
                ), color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        context,
        "${context.packageName}.provider",
        tempFile
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputArea(
    onSubmit: (Uri?, String, LocalDate) -> Unit,
    isLoading: Boolean,
    targetDate: LocalDate,
    modifier: Modifier = Modifier,
    onOpenSavedMeals: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var attachMenuOpen by remember { mutableStateOf(false) }

    val isReadyToSend = text.isNotBlank() || selectedImageUri != null
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { selectedImageUri = it } }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success && tempCameraUri != null) selectedImageUri = tempCameraUri }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val attachIconRotation by animateFloatAsState(
        targetValue = if (attachMenuOpen) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "AttachRotation"
    )

    val addButtonColor by animateColorAsState(
        targetValue = if (attachMenuOpen) MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
        else MaterialTheme.colorScheme.primary,
        animationSpec = tween(200),
        label = "AddColor"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {

        AnimatedVisibility(
            visible = selectedImageUri != null,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f)
        ) {
            Box(modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)) {
                selectedImageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-6).dp)
                            .bouncyClick { selectedImageUri = null }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.padding(5.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp, start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            AttachOptionChip(
                label = "Saved",
                icon = Icons.Rounded.Bookmark,
                visible = attachMenuOpen,
                delayMillis = 0,
                containerColor = Color(0xFF2D1A4A),
                iconBgColor = Color(0xFF6D28D9),
                iconTint = Color(0xFFD8B4FE),
                labelColor = Color(0xFFEDE9FE),
                onClick = {
                    attachMenuOpen = false
                    onOpenSavedMeals()
                }
            )
            AttachOptionChip(
                label = "Gallery",
                icon = Icons.Rounded.Image,
                visible = attachMenuOpen,
                delayMillis = 60,
                containerColor = Color(0xFF1A3830),
                iconBgColor = Color(0xFF166534),
                iconTint = Color(0xFF86EFAC),
                labelColor = Color(0xFFBBF7D0),
                onClick = {
                    attachMenuOpen = false
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
            AttachOptionChip(
                label = "Camera",
                icon = Icons.Rounded.CameraAlt,
                visible = attachMenuOpen,
                delayMillis = 120,
                containerColor = Color(0xFF1A3A5C),
                iconBgColor = Color(0xFF2563A8),
                iconTint = Color(0xFF93C5FD),
                labelColor = Color(0xFFBFDBFE),
                onClick = {
                    attachMenuOpen = false
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        val uri = createTempImageUri(context)
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            )
        }

        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) {

                Surface(
                    shape = CircleShape,
                    color = addButtonColor,
                    shadowElevation = if (attachMenuOpen) 0.dp else 8.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .bouncyClick(scaleDown = 0.88f) {
                            attachMenuOpen = !attachMenuOpen
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Attach",
                            modifier = Modifier
                                .size(26.dp)
                                .rotate(attachIconRotation),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        if (it.isNotBlank()) attachMenuOpen = false
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        lineHeight = 24.sp
                    ),
                    maxLines = 5,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (text.isEmpty()) {
                                Text(
                                    text = "e.g. a slice of pizza",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.45f
                                        ),
                                        fontSize = 17.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                SendButton(
                    isLoading = isLoading,
                    isActive = isReadyToSend,
                    onClick = {
                        val currentText = text
                        val currentUri = selectedImageUri
                        text = ""
                        selectedImageUri = null
                        attachMenuOpen = false
                        onSubmit(currentUri, currentText, targetDate)
                    }
                )
            }
        }
    }
}

@Composable
private fun AttachOptionChip(
    label: String,
    icon: ImageVector,
    visible: Boolean,
    delayMillis: Int,
    containerColor: Color,
    iconBgColor: Color,
    iconTint: Color,
    labelColor: Color,
    onClick: () -> Unit
) {
    val enterTransition = remember {
        fadeIn(animationSpec = tween(durationMillis = 200, delayMillis = delayMillis)) +
                slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    initialOffsetY = { it / 2 }
                ) +
                scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    initialScale = 0.7f
                )
    }
    val exitTransition = remember {
        fadeOut(animationSpec = tween(150)) +
                slideOutVertically(animationSpec = tween(150), targetOffsetY = { it / 2 }) +
                scaleOut(animationSpec = tween(150), targetScale = 0.7f)
    }

    AnimatedVisibility(visible = visible, enter = enterTransition, exit = exitTransition) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = containerColor,
            shadowElevation = 6.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .bouncyClick(scaleDown = 0.93f, onClick = onClick)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconBgColor)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp),
                        tint = iconTint
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = labelColor,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun SendButton(
    isLoading: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(250),
        label = "SendBg"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
        animationSpec = tween(250),
        label = "SendIcon"
    )

    Surface(
        shape = CircleShape,
        color = bgColor,
        shadowElevation = if (isActive) 8.dp else 0.dp,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .bouncyClick(scaleDown = 0.88f) {
                if (isActive) onClick()
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = iconColor,
                    strokeWidth = 2.5.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = "Send",
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalorieProgressCard(
    currentCalories: Int,
    targetCalories: Int,
    onEditClick: () -> Unit,
    activeMealId: MealLog?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedCalories by animateIntAsState(
        targetValue = currentCalories,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "Calories"
    )
    val progress = (animatedCalories.toFloat() / targetCalories.toFloat())
    val visualProgress = progress.coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = visualProgress, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "Progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseDuration = if (percentage < 120) 1000 else if (percentage in 120..150) 500 else 100

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (progress > 0.8f) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDuration),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .bouncyClick(onClick = onClick),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 8.dp
    ) {

        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.bouncyClick { onEditClick() }) {
                    Text(
                        text = "Daily Goal: $targetCalories",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$animatedCalories",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 48.sp,
                        letterSpacing = (-2).sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "kcal consumed",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }

            val backgroundStrokeWidth = 14.dp
            val wavyStrokeWidth = 4.dp
            val paddingCorrection = (backgroundStrokeWidth - wavyStrokeWidth) / 2
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(110.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.1f),
                    strokeWidth = 14.dp,
                    trackColor = Color.Transparent
                )

                CircularWavyProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .size(110.dp)
                        .padding(10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent,
                    trackStroke = WavyProgressIndicatorDefaults.circularTrackStroke,
                    amplitude = WavyProgressIndicatorDefaults.indicatorAmplitude,
                    wavelength = WavyProgressIndicatorDefaults.CircularWavelength,
                    waveSpeed = WavyProgressIndicatorDefaults.CircularWavelength,
                    stroke = WavyProgressIndicatorDefaults.circularIndicatorStroke

                )

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}


@Composable
fun EditTargetDialog(
    currentTarget: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(currentTarget.toString()) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Set Daily Goal") }, text = {
        TextField(
            value = text,
            onValueChange = { if (it.all { char -> char.isDigit() }) text = it },
            label = { Text("Calories") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
    }, confirmButton = {
        TextButton(onClick = {
            val newTarget = text.toIntOrNull()
            if (newTarget != null) onConfirm(newTarget)
        }) { Text("Save") }
    }, dismissButton = {
        TextButton(onClick = onDismiss) { Text("Cancel") }
    })
}