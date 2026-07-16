package kattcrazy.sharemything.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private class TapTooltipCoordinator {
    private var activeId by mutableStateOf<Any?>(null)

    val hasActiveTooltip: Boolean
        get() = activeId != null

    fun isShowing(id: Any): Boolean = activeId == id

    fun toggle(id: Any) {
        activeId = if (activeId == id) null else id
    }

    fun dismiss() {
        activeId = null
    }
}

private val LocalTapTooltipCoordinator = staticCompositionLocalOf<TapTooltipCoordinator?> { null }

@Composable
fun TapTooltipContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val coordinator = remember { TapTooltipCoordinator() }

    CompositionLocalProvider(LocalTapTooltipCoordinator provides coordinator) {
        Box(modifier) {
            content()
            if (coordinator.hasActiveTooltip) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = coordinator::dismiss,
                        ),
                )
            }
        }
    }
}

private enum class BubblePlacement {
    Above,
    Below,
}

@Composable
fun TapTooltipAnchor(
    text: String,
    tooltip: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign? = null,
) {
    val coordinator = LocalTapTooltipCoordinator.current
        ?: error("TapTooltipAnchor must be used inside TapTooltipContainer")
    val tooltipId = remember { Any() }
    val showing = coordinator.isShowing(tooltipId)
    val density = LocalDensity.current
    val minSpaceAbovePx = with(density) { 120.dp.roundToPx() }
    val estimatedBubbleHeightPx = with(density) { 56.dp.roundToPx() }

    var placement by remember { mutableStateOf(BubblePlacement.Below) }

    val bubbleColor = MaterialTheme.colorScheme.secondaryContainer
    val onBubble = MaterialTheme.colorScheme.onSecondaryContainer

    Column(modifier = modifier) {
        AnimatedVisibility(
            visible = showing && placement == BubblePlacement.Above,
            enter = tooltipEnter(fromAbove = true),
            exit = tooltipExit(fromAbove = true),
        ) {
            ConversationBubble(
                text = tooltip,
                placement = BubblePlacement.Above,
                bubbleColor = bubbleColor,
                contentColor = onBubble,
                onClick = coordinator::dismiss,
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }

        Text(
            text = text,
            modifier = Modifier
                .onGloballyPositioned { coords ->
                    val top = coords.boundsInWindow().top
                    placement = if (top >= minSpaceAbovePx + estimatedBubbleHeightPx) {
                        BubblePlacement.Above
                    } else {
                        BubblePlacement.Below
                    }
                }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    role = Role.Button,
                    onClick = { coordinator.toggle(tooltipId) },
                )
                .padding(horizontal = 2.dp),
            style = style,
            color = color,
            textAlign = textAlign,
        )

        AnimatedVisibility(
            visible = showing && placement == BubblePlacement.Below,
            enter = tooltipEnter(fromAbove = false),
            exit = tooltipExit(fromAbove = false),
        ) {
            ConversationBubble(
                text = tooltip,
                placement = BubblePlacement.Below,
                bubbleColor = bubbleColor,
                contentColor = onBubble,
                onClick = coordinator::dismiss,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private fun tooltipEnter(fromAbove: Boolean) =
    fadeIn(animationSpec = tween(160)) +
        expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            expandFrom = if (fromAbove) Alignment.Bottom else Alignment.Top,
        ) +
        scaleIn(
            initialScale = 0.9f,
            transformOrigin = TransformOrigin(0.12f, if (fromAbove) 1f else 0f),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ) +
        slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
            initialOffsetY = { if (fromAbove) it / 4 else -it / 4 },
        )

private fun tooltipExit(fromAbove: Boolean) =
    fadeOut(animationSpec = tween(160)) +
        shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            shrinkTowards = if (fromAbove) Alignment.Bottom else Alignment.Top,
        ) +
        scaleOut(
            targetScale = 0.9f,
            transformOrigin = TransformOrigin(0.12f, if (fromAbove) 1f else 0f),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ) +
        slideOutVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
            targetOffsetY = { if (fromAbove) it / 4 else -it / 4 },
        )

@Composable
private fun ConversationBubble(
    text: String,
    placement: BubblePlacement,
    bubbleColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .widthIn(max = 260.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
    ) {
        if (placement == BubblePlacement.Below) {
            BubbleCaret(
                pointingUp = true,
                color = bubbleColor,
                modifier = Modifier.padding(start = 14.dp),
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = bubbleColor,
            contentColor = contentColor,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (placement == BubblePlacement.Above) {
            BubbleCaret(
                pointingUp = false,
                color = bubbleColor,
                modifier = Modifier.padding(start = 14.dp),
            )
        }
    }
}

@Composable
private fun BubbleCaret(
    pointingUp: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(width = 12.dp, height = 6.dp)) {
        val path = Path().apply {
            if (pointingUp) {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
            } else {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
            }
            close()
        }
        drawPath(path = path, color = color)
    }
}
