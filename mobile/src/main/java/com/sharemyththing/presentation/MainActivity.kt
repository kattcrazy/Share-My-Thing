package com.sharemyththing.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.sharemyththing.ShareMyThingApplication
import com.sharemyththing.presentation.theme.ShareMyThingTheme
import com.sharemyththing.ui.ItemsViewModel
import com.sharemyththing.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    private val viewModel: ItemsViewModel by viewModels {
        ItemsViewModel.Factory(
            repository = (application as ShareMyThingApplication).repository,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShareMyThingTheme {
                AppNavHost(viewModel = viewModel)
            }
        }
    }
}
