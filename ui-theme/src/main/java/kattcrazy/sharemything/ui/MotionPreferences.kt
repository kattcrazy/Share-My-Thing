package kattcrazy.sharemything.ui

import android.app.ActivityManager
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * When false, premium motion (screen morphs, press bounce, fades) should snap/off.
 * Tooltip animations stay on regardless.
 */
val LocalMotionEnabled = compositionLocalOf { true }

/**
 * Auto-off when any of:
 * - System animator duration scale is 0 (Accessibility / Developer options)
 * - Android below 12 (API 31) — shared morphs are heavier on older graphics stacks
 * - Low-RAM device ([ActivityManager.isLowRamDevice])
 * - Fewer than 4 CPU cores
 */
@Composable
fun rememberMotionEnabled(): Boolean {
    val context = LocalContext.current
    val resolver = context.contentResolver
    var enabled by remember {
        mutableStateOf(computeMotionEnabled(context))
    }

    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                enabled = computeMotionEnabled(context)
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                enabled = computeMotionEnabled(context)
            }
        }
        val uri = Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE)
        resolver.registerContentObserver(uri, false, observer)
        enabled = computeMotionEnabled(context)
        onDispose { resolver.unregisterContentObserver(observer) }
    }

    return enabled
}

private fun computeMotionEnabled(context: Context): Boolean {
    if (!isAnimatorScaleEnabled(context.contentResolver)) return false
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false // Android 12+
    if (Runtime.getRuntime().availableProcessors() < 4) return false
    val am = context.getSystemService(ActivityManager::class.java) ?: return true
    if (am.isLowRamDevice) return false
    return true
}

private fun isAnimatorScaleEnabled(resolver: android.content.ContentResolver): Boolean {
    return runCatching {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    }.getOrDefault(1f) != 0f
}
