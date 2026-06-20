package com.sharemyththing.ui.list

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
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
    private val indexOfItem: (Long) -> Int,
    private val onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    private val onDragEnd: () -> Unit,
) {
    var draggingItemId by mutableLongStateOf(-1L)
        private set
    var dragOffset by mutableFloatStateOf(0f)
        private set

    val isDragging: Boolean
        get() = draggingItemId >= 0L

    internal fun onDragStart(itemId: Long) {
        draggingItemId = itemId
        dragOffset = 0f
    }

    internal fun onDrag(itemId: Long, deltaY: Float, itemHeightPx: Float) {
        if (draggingItemId != itemId) return

        dragOffset += deltaY
        val currentIndex = indexOfItem(itemId)
        if (currentIndex < 0) return

        val swapThreshold = itemHeightPx * 0.55f
        val count = itemCount()

        if (dragOffset > swapThreshold && currentIndex < count - 1) {
            onMove(currentIndex, currentIndex + 1)
            dragOffset -= itemHeightPx
        } else if (dragOffset < -swapThreshold && currentIndex > 0) {
            onMove(currentIndex, currentIndex - 1)
            dragOffset += itemHeightPx
        }
    }

    internal fun onDragEnd() {
        draggingItemId = -1L
        dragOffset = 0f
        onDragEnd()
    }
}

@Composable
fun rememberWearListReorderState(
    itemCount: () -> Int,
    indexOfItem: (Long) -> Int,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onDragEnd: () -> Unit = {},
): WearListReorderState {
    val onMoveState = rememberUpdatedState(onMove)
    val onDragEndState = rememberUpdatedState(onDragEnd)
    val indexOfItemState = rememberUpdatedState(indexOfItem)
    return remember {
        WearListReorderState(
            itemCount = itemCount,
            indexOfItem = { id -> indexOfItemState.value(id) },
            onMove = { from, to -> onMoveState.value(from, to) },
            onDragEnd = { onDragEndState.value() },
        )
    }
}

fun Modifier.wearListReorderHandle(
    state: WearListReorderState,
    itemId: Long,
): Modifier = composed {
    if (itemId < 0L) return@composed this

    val itemHeightPx = with(LocalDensity.current) { 52.dp.toPx() }

    pointerInput(state, itemId) {
        detectDragGesturesAfterLongPress(
            onDragStart = { state.onDragStart(itemId) },
            onDrag = { _, dragAmount -> state.onDrag(itemId, dragAmount.y, itemHeightPx) },
            onDragEnd = { state.onDragEnd() },
            onDragCancel = { state.onDragEnd() },
        )
    }
}
