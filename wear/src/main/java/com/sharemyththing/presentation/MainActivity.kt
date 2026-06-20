package com.sharemyththing.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sharemyththing.ShareMyThingApplication
import com.sharemyththing.data.SurfaceSlot
import com.sharemyththing.presentation.theme.ShareMyThingTheme
import com.sharemyththing.ui.ItemsViewModel
import com.sharemyththing.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    private val viewModel: ItemsViewModel by viewModels {
        val app = application as ShareMyThingApplication
        ItemsViewModel.Factory(
            repository = app.repository,
            syncRepository = app.syncRepository,
            appContext = applicationContext,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val startItemId = intent.getLongExtra(EXTRA_ITEM_ID, NO_ITEM_ID).let { id ->
            if (id == NO_ITEM_ID) null else id
        }
        val startSurfaceSlot = SurfaceSlot.fromName(intent.getStringExtra(EXTRA_SURFACE_SLOT))

        setContent {
            ShareMyThingTheme {
                var pendingStartItemId by remember { mutableLongStateOf(startItemId ?: NO_ITEM_ID) }
                var pendingStartSurfaceSlot by remember { mutableStateOf(startSurfaceSlot) }

                AppNavHost(
                    viewModel = viewModel,
                    startItemId = pendingStartItemId.takeIf { it != NO_ITEM_ID },
                    onStartItemHandled = { pendingStartItemId = NO_ITEM_ID },
                    startSurfaceSlot = pendingStartSurfaceSlot,
                    onStartSurfaceSlotHandled = { pendingStartSurfaceSlot = null },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncWithWatch(manual = false)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, NO_ITEM_ID)
        val surfaceSlot = intent.getStringExtra(EXTRA_SURFACE_SLOT)
        if (itemId != NO_ITEM_ID || surfaceSlot != null) {
            recreate()
        }
    }

    companion object {
        const val EXTRA_ITEM_ID = "item_id"
        const val EXTRA_SURFACE_SLOT = "surface_slot"
        private const val NO_ITEM_ID = -1L

        fun launchIntent(context: Context, itemId: Long): Intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_ITEM_ID, itemId)
            }

        fun launchIntentForSlot(context: Context, slot: SurfaceSlot): Intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_SURFACE_SLOT, slot.name)
            }
    }
}
