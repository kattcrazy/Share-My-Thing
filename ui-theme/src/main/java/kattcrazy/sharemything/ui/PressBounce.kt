package kattcrazy.sharemything.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Light press feedback: tiny scale up then settle. Skipped when [LocalMotionEnabled] is false.
 * Do not use on text fields.
 */
@Composable
fun Modifier.pressBounce(
    pressedScale: Float = 1.04f,
    heldScale: Float = 0.98f,
): Modifier {
    if (!LocalMotionEnabled.current) return this

    var pressed by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(pressed) {
        if (pressed) {
            scale.animateTo(
                targetValue = pressedScale,
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 900f),
            )
            scale.animateTo(
                targetValue = heldScale,
                animationSpec = spring(dampingRatio = 0.9f, stiffness = 800f),
            )
        } else {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.9f, stiffness = 800f),
            )
        }
    }

    return this
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    waitForUpOrCancellation()
                    pressed = false
                }
            }
        }
        .graphicsLayer {
            clip = false
            scaleX = scale.value
            scaleY = scale.value
        }
}
