package com.example.aicalorietracker.ui.Utils

import android.view.HapticFeedbackConstants // Import this
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView // Import this

enum class ButtonState { Idle, Pressed }

fun Modifier.bouncyClick(
    onLongPress: (() -> Unit)? = null,
    scaleDown: Float = 0.95f,
    onClick: () -> Unit
) = composed {
    var currentState by remember { mutableStateOf(ButtonState.Idle) }

    val view = LocalView.current

    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongPress by rememberUpdatedState(onLongPress)

    val scale by animateFloatAsState(
        targetValue = if (currentState == ButtonState.Pressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Button Scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
//                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                    currentState = ButtonState.Pressed
                    tryAwaitRelease()
                    currentState = ButtonState.Idle
                },
                onLongPress = {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    currentOnLongPress?.invoke()
                },
                onTap = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    currentOnClick()
                }
            )
        }
}