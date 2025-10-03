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

    internal fun setPressed(pressed: Boolean) {
        _isPressed.value = pressed
    }
}

// Extension function for cleaner API
fun Modifier.applyPressGesture(pressState: PressInteractionState): Modifier {
    return this.pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                pressState.setPressed(true)
                tryAwaitRelease()
                pressState.setPressed(false)
            }
        )
    }
}

@Composable
fun rememberPressInteractionState(
    config: PressAnimationConfig = PressAnimationConfig()
): PressInteractionState {
    return remember(config) { PressInteractionState(config) }
}