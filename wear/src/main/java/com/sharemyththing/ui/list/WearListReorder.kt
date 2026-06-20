package com.sharemyththing.ui.list

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Stable
class WearListReorderState internal constructor(
    private val itemCount: () -> Int,
    private val onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    private val onDragEnd: () -> Unit,
) {
    var draggingIndex by mutableIntStateOf(-1)
        private set
    var dragOffset by mutableFloatStateOf(0f)
        private set

    val isDragging: Boolean
        get() = draggingIndex >= 0

    internal fun onDragStart(index: Int) {
        draggingIndex = index
        dragOffset = 0f
    }

    internal fun onDrag(index: Int, deltaY: Float, thresholdPx: Float) {
        if (draggingIndex != index) return
        dragOffset += deltaY
        val count = itemCount()
        while (dragOffset > thresholdPx && draggingIndex < count - 1) {
            val from = draggingIndex
            onMove(from, from + 1)
            draggingIndex = from + 1
            dragOffset -= thresholdPx
        }
        while (dragOffset < -thresholdPx && draggingIndex > 0) {
            val from = draggingIndex
            onMove(from, from - 1)
            draggingIndex = from - 1
            dragOffset += thresholdPx
        }
    }

    internal fun onDragEnd() {
        draggingIndex = -1
        dragOffset = 0f
        onDragEnd()
    }
}

@Composable
fun rememberWearListReorderState(
    itemCount: () -> Int,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onDragEnd: () -> Unit = {},
): WearListReorderState {
    val onMoveState = rememberUpdatedState(onMove)
    val onDragEndState = rememberUpdatedState(onDragEnd)
    return remember {
        WearListReorderState(
            itemCount = itemCount,
            onMove = { from, to -> onMoveState.value(from, to) },
            onDragEnd = { onDragEndState.value() },
        )
    }
}

fun Modifier.wearListReorderHandle(
    state: WearListReorderState,
    index: Int,
): Modifier = composed {
    if (index < 0) return@composed this

    val thresholdPx = with(LocalDensity.current) { 36.dp.toPx() }

    pointerInput(state, index) {
        detectDragGesturesAfterLongPress(
            onDragStart = { state.onDragStart(index) },
            onDrag = { _, dragAmount -> state.onDrag(index, dragAmount.y, thresholdPx) },
            onDragEnd = { state.onDragEnd() },
            onDragCancel = { state.onDragEnd() },
        )
    }
}
