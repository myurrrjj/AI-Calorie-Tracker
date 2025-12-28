//package com.example.aicalorietracker.ui
//
//import androidx.compose.animation.AnimatedContent
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.EnterTransition
//import androidx.compose.animation.ExitTransition
//import androidx.compose.animation.SharedTransitionDefaults
//import androidx.compose.animation.SharedTransitionLayout
//import androidx.compose.animation.SharedTransitionScope
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.Spring
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.animateIntAsState
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.spring
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.scaleIn
//import androidx.compose.animation.scaleOut
//import androidx.compose.animation.togetherWith
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.pager.HorizontalPager
//import androidx.compose.foundation.pager.rememberPagerState
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.DateRange
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.DatePicker
//import androidx.compose.material3.DatePickerDialog
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LoadingIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.rememberDatePickerState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.platform.LocalHapticFeedback
//import androidx.compose.ui.text.font.FontStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.aicalorietracker.local.MealLog
//import com.example.aicalorietracker.ui.Utils.bouncyClick
//import kotlinx.coroutines.launch
//import java.time.Instant
//import java.time.LocalDate
//import java.time.ZoneId
//import java.time.format.DateTimeFormatter
//import java.time.temporal.ChronoUnit
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DashboardScreen2(
//    viewModel: MealViewModel
//) {
//    var activeMeal by remember { mutableStateOf<MealLog?>(null) }
//
//    val startIndex = Int.MAX_VALUE / 2
//    val pagerState = rememberPagerState(
//        initialPage = startIndex, pageCount = { Int.MAX_VALUE })
//    val scope = rememberCoroutineScope()
//
//    fun getDateForPage(page: Int): LocalDate {
//        val diff = page - startIndex
//        return viewModel.today.plusDays(diff.toLong())
//    }
//
//    val currentDate = getDateForPage(pagerState.currentPage)
//
//    var showDatePicker by remember { mutableStateOf(false) }
//    val datePickerState = rememberDatePickerState()
//
//    var showTargetDialog by remember { mutableStateOf(false) }
//
//    if (showTargetDialog) {
//        val currentTarget =
//            viewModel.getDayFlow(viewModel.today).collectAsState(MealUiState()).value.targetCalories
//
//        EditTargetDialog(
//            currentTarget = currentTarget,
//            onDismiss = { showTargetDialog = false },
//            onConfirm = { newTarget ->
//                viewModel.updateTargetCalories(newTarget)
//                showTargetDialog = false
//            })
//    }
//
//    if (showDatePicker) {
//        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
//            TextButton(
//                onClick = {
//                    datePickerState.selectedDateMillis?.let { millis ->
//                        val selectedDate =
//                            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
//                                .toLocalDate()
//
//                        val daysDiff = ChronoUnit.DAYS.between(
//                            viewModel.today, selectedDate
//                        )
//
//                        scope.launch {
//                            pagerState.scrollToPage(
//                                startIndex + daysDiff.toInt()
//                            )
//                        }
//                    }
//                    showDatePicker = false
//                }) {
//                Text("OK")
//            }
//        }) {
//            DatePicker(state = datePickerState)
//        }
//    }
//
//    Scaffold(
//        containerColor = MaterialTheme.colorScheme.background, topBar = {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(
//                        top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp
//                    ),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column {
//                    Text(
//                        text = if (currentDate == viewModel.today) "Today's Fuel"
//                        else currentDate.format(
//                            DateTimeFormatter.ofPattern("EEEE, MMM d")
//                        ), style = MaterialTheme.typography.headlineLarge.copy(
//                            fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp
//                        ), color = MaterialTheme.colorScheme.onBackground
//                    )
//                }
//
//                Surface(
//                    shape = CircleShape,
//                    color = MaterialTheme.colorScheme.surfaceVariant,
//                    modifier = Modifier.bouncyClick(
//                        onClick = { showDatePicker = true },
//                        onLongPress = {
//                            scope.launch {
//                                pagerState.animateScrollToPage(startIndex)
//                            }
//                        })
//                ) {
//                    Box(Modifier.padding(12.dp)) {
//                        Icon(
//                            Icons.Default.DateRange, contentDescription = "Calendar"
//                        )
//                    }
//                }
//            }
//        }) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            AnimatedVisibility(
//                visible = true, enter = EnterTransition.None, exit = ExitTransition.None
//            ) {
//                HorizontalPager(
//                    state = pagerState, modifier = Modifier.fillMaxSize()
//                ) { page ->
//                    val pageDate = getDateForPage(page)
//
//                    val pageState by viewModel.getDayFlow(pageDate)
//                        .collectAsState(initial = MealUiState())
//
//                    DayView2(
//                        state = pageState,
//                        onDelete = { viewModel.deleteMeal(it) },
//                        onEditGoal = { showTargetDialog = true },
//                        onMealLongClick = { activeMeal = it },
//                        activeMealId = activeMeal,
//                        onDismiss = { activeMeal = null })
//                }
//            }
//
//            Box(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(24.dp)
//            ) {
//                InputArea(
//                    viewModel = viewModel,
//                    isLoading = viewModel.getDayFlow(currentDate)
//                        .collectAsState(MealUiState()).value.isLoading,
//                    targetDate = currentDate
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun DayView2(
//    state: MealUiState,
//    activeMealId: MealLog?,
//    onDelete: (MealLog) -> Unit,
//    onEditGoal: () -> Unit,
//    onMealLongClick: (MealLog) -> Unit,
//    onDismiss: () -> Unit
//) {
//    val animatedCalories by animateIntAsState(
//        targetValue = state.totalCalories,
//        animationSpec = spring(stiffness = Spring.StiffnessLow),
//        label = "Calories"
//    )
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = 16.dp)
//    ) {
//        Spacer(modifier = Modifier.height(16.dp))
//
//        CalorieProgressCard(animatedCalories, state.targetCalories, onEditClick = onEditGoal)
//
//        Spacer(Modifier.height(32.dp))
//
//        Text(
//            text = "Recent Meals",
//            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        if (state.meals.isEmpty()) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "No meals logged yet.",
//                    style = MaterialTheme.typography.bodyLarge,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
//                )
//            }
//        } else {
//            SharedTransitionLayout(Modifier.fillMaxSize()) {
//                LazyColumn(
//                    contentPadding = PaddingValues(bottom = 120.dp),
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    items(items = state.meals, key = { it.id }) { meal ->
//                        AnimatedVisibility(
//                            visible = meal != activeMealId,
//                            enter = fadeIn() + scaleIn(),
//                            exit = fadeOut() + scaleOut(),
//                            modifier = Modifier.animateItem()
//                        ) {
//
//                            Box(
//                                Modifier.sharedBounds(
//                                    sharedContentState = rememberSharedContentState(key = "bounds-${meal.id}"),
//                                    animatedVisibilityScope = this,
//                                    resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
//                                    clipInOverlayDuringTransition = OverlayClip(
//                                        RoundedCornerShape(28.dp)
//                                    )
//                                )
//                            ) {
//                                MealItemCard2(
//                                    onLongClick = { onMealLongClick(meal) },
//                                    meal = meal,
//                                    onDelete = { onDelete(meal) },
//                                    modifier = Modifier.sharedElement(
//                                        sharedContentState = rememberSharedContentState(key = "userReq-${meal.id}"),
//                                        animatedVisibilityScope = this@AnimatedVisibility,
//                                    ),
//                                )
//                            }
//                        }
//
//                    }
//                }
//                MealDetailOverlay2(
//                    meal = activeMealId,
//                    onDismiss = onDismiss,
//                )
//            }
//        }
//    }
//}
//
//
//@Composable
//fun SharedTransitionScope.MealDetailOverlay2(
//    meal: MealLog?,
//    onDismiss: () -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    AnimatedContent(
//        modifier = modifier,
//        targetState = meal,
//        label = "MealDetailOverlay",
//        transitionSpec = { fadeIn() togetherWith fadeOut() }) { meal ->
//        Box(
//            Modifier.fillMaxSize(), contentAlignment = Alignment.Center
//        ) {
//            if (meal != null) {
//                Surface(
//                    modifier = Modifier
//                        .padding(bottom = 120.dp)
//                        .sharedBounds(
//                            sharedContentState = rememberSharedContentState(
//                                key = "bounds-${meal.id}"
//                            ),
//                            animatedVisibilityScope = this@AnimatedContent,
//                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
//
//                            clipInOverlayDuringTransition = OverlayClip(
//                                RoundedCornerShape(28.dp)
//                            )
//                        ),
//                    shape = RoundedCornerShape(28.dp),
//                    color = MaterialTheme.colorScheme.surface,
//                    tonalElevation = 6.dp
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .padding(28.dp)
//                            .verticalScroll(rememberScrollState())
//                    ) {
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.Top
//                        ) {
//                            Column(modifier = Modifier.weight(1f)) {
//                                Text(
//                                    text = meal.userRequest,
//                                    modifier = Modifier.sharedElement(
//                                        sharedContentState = rememberSharedContentState("userReq-${meal.id}"),
//
//                                        animatedVisibilityScope = this@AnimatedContent,
//
//                                        ),
//                                    style = MaterialTheme.typography.headlineMedium.copy(
//                                        fontWeight = FontWeight.Bold
//                                    )
//                                )
//
//                                Spacer(Modifier.height(8.dp))
//                                Surface(
//                                    shape = RoundedCornerShape(8.dp),
//                                    color = MaterialTheme.colorScheme.primaryContainer,
//                                    modifier = Modifier
//                                ) {
//                                    Text(
//                                        text = "${meal.macros.calories} kcal",
//                                        style = MaterialTheme.typography.labelLarge.copy(
//                                            fontWeight = FontWeight.Bold
//                                        ),
//                                        modifier = Modifier.padding(
//                                            horizontal = 12.dp, vertical = 6.dp
//                                        )
//                                    )
//                                }
//                            }
//
//
//                            IconButton(
//                                onClick = { onDismiss?.invoke() }, modifier = Modifier
//                                    .background(
//                                        MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
//                                        CircleShape
//                                    )
//                                    .size(32.dp)
//                            ) {
//                                Icon(
//                                    Icons.Default.Close,
//                                    contentDescription = "Close",
//                                    modifier = Modifier.size(18.dp)
//                                )
//                            }
//                        }
//
//
//                        Spacer(Modifier.height(24.dp))
//
//                        Text(
//                            text = meal.aiResponse,
//                            style = MaterialTheme.typography.bodyMedium.copy(
//                                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                lineHeight = 22.sp
//                            )
//                        )
//
//                        HorizontalDivider(
//                            modifier = Modifier.padding(vertical = 24.dp),
//                            color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f)
//                        )
//
//                        Text(
//                            "Macronutrients",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//
//                        Spacer(Modifier.height(16.dp))
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            MacroCard(
//                                "Protein",
//                                "${meal.macros.protein}g",
//                                Modifier.weight(1f),
//                                MaterialTheme.colorScheme.primaryContainer
//                            )
//                            MacroCard(
//                                "Carbs",
//                                "${meal.macros.carbs}g",
//                                Modifier.weight(1f),
//                                MaterialTheme.colorScheme.secondaryContainer
//                            )
//                            MacroCard(
//                                "Fat",
//                                "${meal.macros.fat}g",
//                                Modifier.weight(1f),
//                                MaterialTheme.colorScheme.tertiaryContainer
//                            )
//                        }
//
//                        Spacer(Modifier.height(12.dp))
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            MacroCard(
//                                "Fiber",
//                                "${meal.macros.fiber}g",
//                                Modifier.weight(1f),
//                                MaterialTheme.colorScheme.surfaceContainerHigh
//                            )
//                            MacroCard(
//                                "Sugar",
//                                "${meal.macros.sugar}g",
//                                Modifier.weight(1f),
//                                MaterialTheme.colorScheme.surfaceContainerHigh
//                            )
//                        }
//
//                        Spacer(Modifier.height(32.dp))
//
//                        Text(
//                            "Micronutrients",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//
//                        Spacer(Modifier.height(16.dp))
//
//                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                            MicroRowItem("Vitamin A", "${meal.micros.vitaminA}", "IU")
//                            MicroRowItem("Vitamin C", "${meal.micros.vitaminC}", "mg")
//                            MicroRowItem("Vitamin D", "${meal.micros.vitaminD}", "IU")
//
//                            HorizontalDivider(
//                                modifier = Modifier.padding(vertical = 8.dp),
//                                color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f)
//                            )
//
//                            MicroRowItem("Iron", "${meal.micros.iron}", "mg")
//                            MicroRowItem("Calcium", "${meal.micros.calcium}", "mg")
//                            MicroRowItem("Sodium", "${meal.micros.sodium}", "mg")
//                            MicroRowItem("Potassium", "${meal.micros.potassium}", "mg")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3ExpressiveApi::class)
//@Composable
//fun MealItemCard2(
//    meal: MealLog,
//    onDelete: () -> Unit,
//    onLongClick: () -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    LocalHapticFeedback.current
//    val infiniteTransition = rememberInfiniteTransition()
//    val alpha by if (meal.isAnalysing) {
//        infiniteTransition.animateFloat(
//            initialValue = 0.5f, targetValue = 1f, animationSpec = infiniteRepeatable(
//                animation = tween(800), repeatMode = RepeatMode.Reverse
//            )
//        )
//    } else {
//        remember { mutableStateOf(1f) }
//    }
//
//
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .alpha(alpha)
//            .bouncyClick(onClick = {
//                if (!meal.isAnalysing) onLongClick()
//            }, onLongPress = {
//                if (!meal.isAnalysing) onLongClick()
//            }),
//        shape = RoundedCornerShape(28.dp),
//        color = if (meal.isAnalysing) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
//        else MaterialTheme.colorScheme.surfaceContainer,
//        tonalElevation = if (meal.isAnalysing) 0.dp else 2.dp
//    ) {
//        Row(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Surface(
//                modifier = Modifier.size(56.dp),
//                shape = RoundedCornerShape(18.dp),
//                color = if (meal.isAnalysing) MaterialTheme.colorScheme.surface
//                else MaterialTheme.colorScheme.secondaryContainer
//            ) {
//                Box(contentAlignment = Alignment.Center, modifier = Modifier.background(
//                    MaterialTheme.colorScheme.secondaryContainer)) {
//                    if (meal.isAnalysing) {
//                        LoadingIndicator()
////
//                    } else {
//                        Text("🥗", fontSize = 28.sp)
//                    }
//                }
//            }
//
//            Spacer(Modifier.width(16.dp))
//
//            Column(Modifier.weight(1f)) {
//                Text(
//                    text = meal.userRequest,
//                    modifier = modifier,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold,
//                    maxLines = 1
//                )
//
//                Spacer(Modifier.height(4.dp))
//
//                if (meal.isAnalysing) {
//                    Text(
//                        "Analysing…",
//                        style = MaterialTheme.typography.bodySmall,
//                        fontStyle = FontStyle.Italic,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                } else {
//                    Text(
//                        "${meal.macros.calories} kcal • ${meal.macros.protein}g protein",
//                        style = MaterialTheme.typography.bodySmall,
//                        modifier = Modifier
//                    )
//                }
//            }
//
//            if (!meal.isAnalysing) {
//                IconButton(onClick = onDelete) {
//                    Icon(Icons.Default.Delete, contentDescription = null)
//                }
//            }
//        }
//    }
//}