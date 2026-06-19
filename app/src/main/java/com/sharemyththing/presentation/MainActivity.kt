package com.sharemyththing.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sharemyththing.ShareMyThingApplication
import com.sharemyththing.presentation.theme.ShareMyThingTheme
import com.sharemyththing.ui.ItemsViewModel
import com.sharemyththing.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    private val viewModel: ItemsViewModel by viewModels {
        ItemsViewModel.Factory((application as ShareMyThingApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        var startItemId = intent.getLongExtra(EXTRA_ITEM_ID, NO_ITEM_ID).let { id ->
            if (id == NO_ITEM_ID) null else id
        }

        setContent {
            ShareMyThingTheme {
                var pendingStartItemId by remember { mutableLongStateOf(startItemId ?: NO_ITEM_ID) }

                AppNavHost(
                    viewModel = viewModel,
                    startItemId = pendingStartItemId.takeIf { it != NO_ITEM_ID },
                    onStartItemHandled = { pendingStartItemId = NO_ITEM_ID },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, NO_ITEM_ID)
        if (itemId != NO_ITEM_ID) {
            recreate()
        }
    }

    companion object {
        const val EXTRA_ITEM_ID = "item_id"
        private const val NO_ITEM_ID = -1L

        fun launchIntent(context: Context, itemId: Long): Intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_ITEM_ID, itemId)
            }
    }
}
