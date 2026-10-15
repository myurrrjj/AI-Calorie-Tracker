package com.myurrrjj.trackabite.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private val Green = Color(0xFF0F9D58)
private val GreenMid = Color(0xFF12A862)
private val GreenDark = Color(0xFF0A7C44)
private val GreenDeep = Color(0xFF06572F)

private fun leafBodyPath(): Path = Path().apply {
    moveTo(22.90f, 77.70f)
    cubicTo(54.92f, 73.19f, 73.19f, 54.92f, 77.69f, 22.90f)
    cubicTo(45.08f, 26.81f, 26.82f, 45.08f, 22.31f, 77.10f)
    close()
}

private fun leafVeinPath(): Path = Path().apply {
    moveTo(24.67f, 75.93f)
    lineTo(75.92f, 24.67f)
}

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit = {}
) {
    val leafScale = remember { Animatable(0f) }
    val leafRotation = remember { Animatable(-16f) }
    val veinProgress = remember { Animatable(0f) }
    val glowIn = remember { Animatable(0f) }
    val wordAlpha = remember { Animatable(0f) }
    val wordOffsetY = remember { Animatable(24f) }
    val ruleProgress = remember { Animatable(0f) }
    val tagAlpha = remember { Animatable(0f) }
    val tagOffsetY = remember { Animatable(18f) }

    val bouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    LaunchedEffect(Unit) {
        launch { glowIn.animateTo(1f, tween(durationMillis = 800)) }
        launch { leafScale.animateTo(1f, bouncy) }
        launch { leafRotation.animateTo(0f, bouncy) }

        launch {
            delay(560)
            veinProgress.animateTo(1f, tween(durationMillis = 620, easing = FastOutSlowInEasing))
        }
        launch {
            delay(980)
            wordAlpha.animateTo(1f, tween(durationMillis = 500))
        }
        launch {
            delay(980)
            wordOffsetY.animateTo(0f, tween(durationMillis = 560, easing = FastOutSlowInEasing))
        }
        launch {
            delay(1220)
            ruleProgress.animateTo(1f, tween(durationMillis = 640, easing = FastOutSlowInEasing))
        }
        launch {
            delay(1300)
            tagAlpha.animateTo(1f, tween(durationMillis = 500))
        }
        launch {
            delay(1300)
            tagOffsetY.animateTo(0f, tween(durationMillis = 560, easing = FastOutSlowInEasing))
        }

        delay(2450)
        onAnimationFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(GreenMid, Green, GreenDark, GreenDeep),
                        center = Offset(size.width / 2f, size.height * 0.34f),
                        radius = size.maxDimension * 0.95f
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Specks(Modifier.fillMaxSize())

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                BreathingGlow(entry = glowIn.value)
                Leaf(
                    entryScale = leafScale.value,
                    rotation = leafRotation.value,
                    veinProgress = veinProgress.value
                )
            }

            Spacer(Modifier.height(30.dp))

            Text(
                text = "TrackABite",
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.6).sp,
                modifier = Modifier.graphicsLayer {
                    alpha = wordAlpha.value
                    translationY = wordOffsetY.value
                }
            )

            Spacer(Modifier.height(16.dp))

            Box(
                Modifier
                    .width(58.dp)
                    .height(3.dp)
                    .graphicsLayer {
                        scaleX = ruleProgress.value
                        alpha = ruleProgress.value
                    }
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(3.dp))
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "AI calorie & nutrition tracker",
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.graphicsLayer {
                    alpha = tagAlpha.value
                    translationY = tagOffsetY.value
                }
            )
        }
    }
}

@Composable
private fun Leaf(
    entryScale: Float,
    rotation: Float,
    veinProgress: Float,
    modifier: Modifier = Modifier
) {
    val body = remember { leafBodyPath() }
    val vein = remember { leafVeinPath() }
    val measure = remember { PathMeasure() }

    Canvas(
        modifier = modifier
            .size(152.dp)
            .graphicsLayer {
                scaleX = entryScale
                scaleY = entryScale
                rotationZ = rotation
            }
    ) {
        val unit = size.minDimension / 100f
        scale(unit, unit, pivot = Offset.Zero) {
            drawPath(body, color = Color.White)

            if (veinProgress > 0f) {
                measure.setPath(vein, false)
                val segment = Path()
                measure.getSegment(0f, measure.length * veinProgress, segment, true)
                drawPath(
                    path = segment,
                    color = Green,
                    style = Stroke(width = 1.9f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun BreathingGlow(entry: Float, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "glow")
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val breathe by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    Box(
        modifier = modifier
            .size(340.dp)
            .graphicsLayer {
                alpha = entry * breathe
                scaleX = pulse
                scaleY = pulse
            }
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.32f),
                        Color.White.copy(alpha = 0.06f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun Specks(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "specks")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing)
        ),
        label = "t"
    )

    val specks = remember {
        listOf(
            Triple(0.22f, 0.26f, 5f),
            Triple(0.79f, 0.33f, 4f),
            Triple(0.72f, 0.71f, 6f),
            Triple(0.27f, 0.69f, 3f)
        )
    }

    Canvas(modifier) {
        specks.forEachIndexed { index, (fx, fy, radius) ->
            val phase = (t + index * 0.22f) % 1f
            val alpha = (1f - abs(phase - 0.5f) * 2f).coerceIn(0f, 1f) * 0.65f
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = Offset(
                    x = fx * size.width,
                    y = fy * size.height + (0.5f - phase) * 40f
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}