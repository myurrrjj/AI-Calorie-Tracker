package com.example.aicalorietracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.ui.Utils.bouncyClick
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MealViewModel
) {
    var activeMeal by remember { mutableStateOf<MealLog?>(null) }

    val startIndex = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { Int.MAX_VALUE }
    )
    val scope = rememberCoroutineScope()

    fun getDateForPage(page: Int): LocalDate {
        val diff = page - startIndex
        return viewModel.today.plusDays(diff.toLong())
    }

    val currentDate = getDateForPage(pagerState.currentPage)

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var showTargetDialog by remember { mutableStateOf(false) }

    if (showTargetDialog) {
        val currentTarget =
            viewModel.getDayFlow(viewModel.today)
                .collectAsState(MealUiState())
                .value.targetCalories

        EditTargetDialog(
            currentTarget = currentTarget,
            onDismiss = { showTargetDialog = false },
            onConfirm = { newTarget ->
                viewModel.updateTargetCalories(newTarget)
                showTargetDialog = false
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate =
                                Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()

                            val daysDiff = ChronoUnit.DAYS.between(
                                viewModel.today,
                                selectedDate
                            )

                            scope.launch {
                                pagerState.scrollToPage(
                                    startIndex + daysDiff.toInt()
                                )
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 48.dp,
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 16.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (currentDate == viewModel.today)
                            "Today's Fuel"
                        else
                            currentDate.format(
                                DateTimeFormatter.ofPattern("EEEE, MMM d")
                            ),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
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
                        }
                    )
                ) {
                    Box(Modifier.padding(12.dp)) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Calendar"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        SharedTransitionLayout {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {


//                AnimatedContent(
//                    targetState = activeMeal,
//
//                ) { it->
//                    val animVisScope = this
//
//                    if(it==null){
//
//                    }
//                }
            AnimatedVisibility(visible = true,enter = EnterTransition.None, exit = ExitTransition.None){
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val pageDate = getDateForPage(page)

                    val pageState by viewModel.getDayFlow(pageDate)
                        .collectAsState(initial = MealUiState())

                    DayView(
                        state = pageState,
                        onDelete = { viewModel.deleteMeal(it) },
                        onEditGoal = { showTargetDialog = true },
                        onMealLongClick = { activeMeal = it },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedVisibility
                    )
                }
            }


            AnimatedVisibility(
                visible = activeMeal != null,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
                ) {
                val anim = this@AnimatedVisibility

                activeMeal?.let { meal->
                    MealDetailOverlay(
                        meal = meal,
                        onDismiss = { activeMeal = null },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedVisibility
                    )
                }


//


                }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            ) {
                InputArea(
                    viewModel = viewModel,
                    isLoading = viewModel.getDayFlow(currentDate)
                        .collectAsState(MealUiState())
                        .value.isLoading,
                    targetDate = currentDate
                )
            }
        }
    }
}}


@Composable
fun MealDetailOverlay(
    meal: MealLog,
    onDismiss: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 120.dp, start = 24.dp, end = 24.dp, top = 24.dp)
//                    .padding(top = 80.dp)


                   ,
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp

            ) {
                with(sharedTransitionScope) {

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(
                                key = "bounds-${meal.id}"
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                        )
                        .verticalScroll(rememberScrollState())

                        .widthIn(max = 360.dp), horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = meal.userRequest,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(Modifier.height(8.dp))
                            with(sharedTransitionScope) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
//                                        .sharedElement(
//                                        sharedContentState = rememberSharedContentState("kcal-${meal.id}"),
//                                        animatedVisibilityScope = animatedVisibilityScope
//                                    )
                                ) {
                                    Text(
                                        text = "${meal.macros.calories} kcal",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp, vertical = 6.dp
                                        )
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { onDismiss?.invoke() }, modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), CircleShape
                                )
                                .size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = meal.aiResponse, style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp
                        )
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f)
                    )

                    Text(
                        "Macronutrients",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MacroCard(
                            "Protein",
                            "${meal.macros.protein}g",
                            Modifier.weight(1f),
                            MaterialTheme.colorScheme.primaryContainer
                        )
                        MacroCard(
                            "Carbs",
                            "${meal.macros.carbs}g",
                            Modifier.weight(1f),
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                        MacroCard(
                            "Fat",
                            "${meal.macros.fat}g",
                            Modifier.weight(1f),
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MacroCard(
                            "Fiber",
                            "${meal.macros.fiber}g",
                            Modifier.weight(1f),
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        MacroCard(
                            "Sugar",
                            "${meal.macros.sugar}g",
                            Modifier.weight(1f),
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    Text(
                        "Micronutrients",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MicroRowItem("Vitamin A", "${meal.micros.vitaminA}", "IU")
                        MicroRowItem("Vitamin C", "${meal.micros.vitaminC}", "mg")
                        MicroRowItem("Vitamin D", "${meal.micros.vitaminD}", "IU")

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
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

}}

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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InputArea(viewModel: MealViewModel, isLoading: Boolean, targetDate: LocalDate) {
    var text by remember { mutableStateOf("") }
    val isExpanded = text.isNotEmpty()

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow
                    )
                ),
            shadowElevation = 6.dp,
            tonalElevation = 4.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
//                if (isLoading) {
//                    Box(Modifier.padding(16.dp)) {
//                        CircularWavyProgressIndicator(
//                            modifier = Modifier.size(24.dp),
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                } else {
                TextField(
                    singleLine = true,
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            "e.g. one roti and aloo sabzi",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                )
//                }
            }
        }

        val buttonColor by animateColorAsState(
            targetValue = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            label = "ButtonColor"
        )
        val iconColor by animateColorAsState(
            targetValue = if (isExpanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
            label = "IconColor"
        )

        Surface(
            modifier = Modifier
                .size(64.dp)
                .bouncyClick {
                    if (text.isNotBlank()) {
                        viewModel.analyseAndAddMeal(text, targetDate)
                        text = ""
                    }
                }, shape = CircleShape, color = buttonColor, shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(32.dp),
                    tint = iconColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MealItemCard(
    meal: MealLog,
    onDelete: () -> Unit,
    onLongClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
//    isActive: Boolean
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by if (meal.isAnalysing) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f, targetValue = 1f, animationSpec = infiniteRepeatable(
                animation = tween(800), repeatMode = RepeatMode.Reverse
            )
        )
    } else {
        remember { mutableStateOf(1f) }
    }


    Surface(
        modifier = Modifier
            .fillMaxWidth()


            .alpha(alpha)

            .bouncyClick(onClick = {}, onLongPress = {
                if (!meal.isAnalysing) onLongClick()
            }),
        shape = RoundedCornerShape(28.dp),
        color = if (meal.isAnalysing) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = if (meal.isAnalysing) 0.dp else 2.dp
    ) {
        with(sharedTransitionScope) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(
                            key = "bounds-${meal.id}"
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                    ), verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = if (meal.isAnalysing) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (meal.isAnalysing) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp, modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("🥗", fontSize = 28.sp)
                        }
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = meal.userRequest,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Spacer(Modifier.height(4.dp))

                    if (meal.isAnalysing) {
                        Text(
                            "Analysing…",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        with(sharedTransitionScope) {
                            Text(
                                "${meal.macros.calories} kcal • ${meal.macros.protein}g protein",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
//                                    .sharedElement(
//                                    sharedContentState = rememberSharedContentState("kcal-${meal.id}"),
//                                    animatedVisibilityScope = animatedVisibilityScope
//                                )
                            )
                        }
                    }
                }

                if (!meal.isAnalysing) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
            }
        }

    }
}




@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalorieProgressCard(currentCalories: Int, targetCalories: Int, onEditClick: () -> Unit) {
    val progress = (currentCalories.toFloat() / targetCalories.toFloat())
    val visualProgress = progress.coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = visualProgress, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "Progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by if (progress > 0.8f) {
        infiniteTransition.animateFloat(
            initialValue = 1f, targetValue = 1.05f, animationSpec = infiniteRepeatable(
                animation = if (percentage < 120) tween(1000) else if (percentage in 120..150) tween(
                    500
                ) else tween(100), repeatMode = RepeatMode.Reverse
            ), label = "Pulse"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .bouncyClick { },
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
                    text = "$currentCalories", style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black, fontSize = 48.sp, letterSpacing = (-2).sp
                    ), color = MaterialTheme.colorScheme.onPrimaryContainer
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
                contentAlignment = Alignment.Center, modifier = Modifier.scale(pulseScale)
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
//                    max = { 1f },
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

                // Percentage Text
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
fun DayView(
    state: MealUiState,
//    activeMealId:Int?,
    onDelete: (MealLog) -> Unit,
    onEditGoal: () -> Unit,
    onMealLongClick: (MealLog) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    val animatedCalories by animateIntAsState(
        targetValue = state.totalCalories,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "Calories"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        CalorieProgressCard(animatedCalories, state.targetCalories, onEditClick = onEditGoal)

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Meals",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        if (state.meals.isEmpty()) {
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
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = state.meals, key = { it.id }) { meal ->
                    MealItemCard(
//                        isActive = meal.id == activeMealId,
                        onLongClick = { onMealLongClick(meal) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        meal = meal,
                        onDelete = { onDelete(meal) })
                }
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