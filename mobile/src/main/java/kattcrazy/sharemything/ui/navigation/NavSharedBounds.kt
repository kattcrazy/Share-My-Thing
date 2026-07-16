package kattcrazy.sharemything.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kattcrazy.sharemything.data.SurfaceSlot
import kattcrazy.sharemything.ui.LocalMotionEnabled

/** Keys that pair a tapped control with the screen that expands from it. */
object NavSharedKeys {
    fun detail(itemId: Long) = "detail-$itemId"
    fun edit(itemId: Long?) = if (itemId == null) "edit-new" else "edit-$itemId"
    const val About = "about"
    const val PhoneWidgets = "phone-widgets"
    const val AppShortcuts = "app-shortcuts"
    const val WatchTiles = "watch-tiles"
    const val WatchComplications = "watch-complications"
    const val QrTips = "qr-tips"
    fun slotPicker(slot: SurfaceSlot) = "slot-${slot.name}"
}

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/** No clip so elevated controls keep their shadows during morphs. */
@OptIn(ExperimentalSharedTransitionApi::class)
private val NoOverlayClip = object : SharedTransitionScope.OverlayClip {
    override fun getClipPath(
        sharedContentState: SharedTransitionScope.SharedContentState,
        bounds: Rect,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Path? = null
}

/**
 * Container-transform expand/collapse from the matching [key] source.
 * No-op when motion is disabled or not inside SharedTransitionLayout.
 *
 * [scaleContent] = true keeps default ScaleToBounds (needed for Extended FAB so
 * RemeasureToBounds doesn't crush width and drop the label). Prefer false for list cards.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.navSharedBounds(
    key: String,
    scaleContent: Boolean = false,
): Modifier {
    if (!LocalMotionEnabled.current) return this
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(sharedScope) {
        if (scaleContent) {
            // Default resizeMode scales laid-out content (keeps FAB label/size stable).
            this@navSharedBounds.sharedBounds(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = visibilityScope,
                boundsTransform = { _, _ ->
                    spring(dampingRatio = 0.95f, stiffness = 700f)
                },
                clipInOverlayDuringTransition = NoOverlayClip,
            )
        } else {
            this@navSharedBounds.sharedBounds(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = visibilityScope,
                boundsTransform = { _, _ ->
                    spring(dampingRatio = 0.95f, stiffness = 700f)
                },
                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                clipInOverlayDuringTransition = NoOverlayClip,
            )
        }
    }
}

fun AppScreen.destinationSharedKey(): String? = when (this) {
    AppScreen.List -> null
    AppScreen.About -> NavSharedKeys.About
    is AppScreen.QrDetail -> NavSharedKeys.detail(itemId)
    is AppScreen.TextDetail -> NavSharedKeys.detail(itemId)
    AppScreen.QrTips -> NavSharedKeys.QrTips
    is AppScreen.Edit -> NavSharedKeys.edit(itemId)
    AppScreen.PhoneWidgets -> NavSharedKeys.PhoneWidgets
    AppScreen.AppShortcuts -> NavSharedKeys.AppShortcuts
    AppScreen.WatchTiles -> NavSharedKeys.WatchTiles
    AppScreen.WatchComplications -> NavSharedKeys.WatchComplications
    is AppScreen.PickSlotItem -> NavSharedKeys.slotPicker(slot)
}

fun AppScreen.contentKey(): String = when (this) {
    AppScreen.List -> "list"
    AppScreen.About -> "about"
    is AppScreen.QrDetail -> "qr/${itemId}"
    is AppScreen.TextDetail -> "text/${itemId}"
    AppScreen.QrTips -> "qr-tips"
    is AppScreen.Edit -> "edit/${itemId}"
    AppScreen.PhoneWidgets -> "phone-widgets"
    AppScreen.AppShortcuts -> "app-shortcuts"
    AppScreen.WatchTiles -> "watch-tiles"
    AppScreen.WatchComplications -> "watch-complications"
    is AppScreen.PickSlotItem -> "slot/${slot.name}"
}
