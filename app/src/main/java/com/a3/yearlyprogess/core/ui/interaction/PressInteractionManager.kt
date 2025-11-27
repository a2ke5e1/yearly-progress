package com.a3.yearlyprogess.core.ui.interaction

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.core.ui.style.CardCornerStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

data class PressAnimationConfig(
    val pressedRadius: Dp = 16.dp, // Amount to add when pressed
    val animationSpec: AnimationSpec<Dp> = TweenSpec(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
)

data class AnimatedCorners(
    val topStart: Dp,
    val topEnd: Dp,
    val bottomStart: Dp,
    val bottomEnd: Dp
) {
    fun toShape(): Shape = RoundedCornerShape(
        topStart = topStart,
        topEnd = topEnd,
        bottomStart = bottomStart,
        bottomEnd = bottomEnd
    )
}

class PressInteractionState(
    private val config: PressAnimationConfig
) {
    private var _isPressed = mutableStateOf(false)
    val isPressed: Boolean get() = _isPressed.value

    @Composable
    fun animateCorners(default: CardCornerStyle = CardCornerStyle.Default): AnimatedCorners {

        val topStart by animateDpAsState(
            targetValue = if (isPressed) config.pressedRadius else default.topStart,
            animationSpec = config.animationSpec,
            label = "topStart"
        )

        val topEnd by animateDpAsState(
            targetValue = if (isPressed) config.pressedRadius else default.topEnd,
            animationSpec = config.animationSpec,
            label = "topEnd"
        )

        val bottomStart by animateDpAsState(
            targetValue = if (isPressed) config.pressedRadius else default.bottomStart,
            animationSpec = config.animationSpec,
            label = "bottomStart"
        )

        val bottomEnd by animateDpAsState(
            targetValue = if (isPressed) config.pressedRadius else default.bottomEnd,
            animationSpec = config.animationSpec,
            label = "bottomEnd"
        )

        return AnimatedCorners(
            topStart = topStart,
            topEnd = topEnd,
            bottomStart = bottomStart,
            bottomEnd = bottomEnd
        )
    }

    fun setPressed(pressed: Boolean) {
        _isPressed.value = pressed
    }
}

// Extension function for cleaner API
fun Modifier.applyPressGesture(
    pressState: PressInteractionState,
    debounceTime: Long = 200L,
    onTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
): Modifier {
    val isClickable = AtomicBoolean(true)
    return this.pointerInput(Unit) {
        val scope = CoroutineScope(currentCoroutineContext() + Job())
        detectTapGestures(
            onPress = {
                // Start press animation
                pressState.setPressed(true)

                // Wait until user lifts finger or gesture is cancelled
                val released = try {
                    tryAwaitRelease()
                } finally {
                    pressState.setPressed(false)
                }

                // Optionally you can do something after release if needed
            },
            onTap = {
                if (isClickable.getAndSet(false)) {
                    onTap?.invoke()
                    // Re-enable click after debounce delay
                    scope.launch {
                        delay(debounceTime)
                        isClickable.set(true)
                    }
                }
            },
            onLongPress = { onLongPress?.invoke() }
        )
    }
}

@Composable
fun rememberPressInteractionState(
    config: PressAnimationConfig = PressAnimationConfig()
): PressInteractionState {
    return remember(config) { PressInteractionState(config) }
}