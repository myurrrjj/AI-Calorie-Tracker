package com.myurrrjj.trackabite.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val note: String? = null
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Rounded.AutoAwesome,
        title = "Log meals in a sentence",
        description = "Type what you ate, or snap a photo. Gemini estimates the calories, macros and micronutrients for you."
    ),
    OnboardingPage(
        icon = Icons.Rounded.Insights,
        title = "See the full picture",
        description = "Animated daily rings plus weekly, monthly and yearly charts for calories, protein, carbs, fat, fibre and sugar."
    ),
    OnboardingPage(
        icon = Icons.Rounded.Favorite,
        title = "Balance intake and activity",
        description = "Connect Health Connect to weigh what you eat against the calories you burn each day."
    ),
    OnboardingPage(
        icon = Icons.Rounded.Lock,
        title = "Your key stays yours",
        description = "TrackABite runs on your own free Gemini API key, encrypted on this device with Android's EncryptedSharedPreferences. It never leaves your phone.",
        note = "Without a key the app can't analyse meals, so it won't do much. Skip ahead to explore, or add your key now."
    )
)

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == onboardingPages.lastIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
        ) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(72.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                PageBody(
                    page = onboardingPages[page],
                    accent = accentFor(page),
                    modifier = Modifier.graphicsLayer {
                        val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                            .absoluteValue
                            .coerceIn(0f, 1f)
                        val s = 1f - offset * 0.12f
                        scaleX = s
                        scaleY = s
                        alpha = 1f - offset * 0.65f
                    }
                )
            }

            PagerIndicator(
                pageCount = onboardingPages.size,
                current = pagerState.currentPage
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isLast) {
                        onGetStarted()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (isLast) "Get started" else "Next",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun accentFor(index: Int): Color = when (index % 4) {
    0 -> MaterialTheme.colorScheme.primary
    1 -> MaterialTheme.colorScheme.tertiary
    2 -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.secondary
}

@Composable
private fun PageBody(
    page: OnboardingPage,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(170.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.30f),
                            accent.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.16f),
                modifier = Modifier.size(104.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = accent
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        if (page.note != null) {
            Spacer(Modifier.height(22.dp))
            NoteCard(text = page.note, accent = accent)
        }
    }
}

@Composable
private fun NoteCard(text: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PagerIndicator(pageCount: Int, current: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selected = index == current
            val indicatorWidth by animateDpAsState(
                targetValue = if (selected) 28.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "indicatorWidth"
            )
            val indicatorColor by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
                },
                label = "indicatorColor"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(8.dp)
                    .width(indicatorWidth)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(onGetStarted = {}, onSkip = {})
}
