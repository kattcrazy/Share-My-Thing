@file:OptIn(ExperimentalMaterial3Api::class)

package kattcrazy.sharemything.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch

private class TapTooltipCoordinator(
    val show: suspend (TooltipState) -> Unit,
    val dismiss: suspend () -> Unit,
)

private val LocalTapTooltipCoordinator = staticCompositionLocalOf<TapTooltipCoordinator?> { null }

@Composable
fun TapTooltipContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var activeTooltip by remember { mutableStateOf<TooltipState?>(null) }
    val scope = rememberCoroutineScope()
    val coordinator = remember {
        TapTooltipCoordinator(
            show = { state ->
                activeTooltip?.dismiss()
                activeTooltip = state
                state.show()
            },
            dismiss = {
                activeTooltip?.dismiss()
                activeTooltip = null
            },
        )
    }

    CompositionLocalProvider(LocalTapTooltipCoordinator provides coordinator) {
        Box(modifier) {
            content()
            if (activeTooltip != null) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            scope.launch { coordinator.dismiss() }
                        },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(tooltip) } },
        state = tooltipState,
        enableUserInput = false,
    ) {
        Text(
            text = text,
            modifier = modifier.clickable {
                scope.launch { coordinator.show(tooltipState) }
            },
            style = style,
            color = color,
            textAlign = textAlign,
        )
    }
}
