package kattcrazy.sharemything.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kattcrazy.sharemything.R
import kattcrazy.sharemything.data.DisplayItem
import kattcrazy.sharemything.data.ItemType
import kattcrazy.sharemything.data.usesQr
import kattcrazy.sharemything.data.SurfaceDisplayConstants
import kattcrazy.sharemything.ui.navigation.NavSharedKeys
import kattcrazy.sharemything.ui.navigation.navSharedBounds
import kattcrazy.sharemything.ui.pressBounce
import kattcrazy.sharemything.util.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrDetailScreen(
    item: DisplayItem,
    onEditClick: () -> Unit,
    onTipsClick: () -> Unit,
    onBack: () -> Unit,
) {
    var qrBitmap by remember(item.content) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(item.content) {
        qrBitmap = withContext(Dispatchers.Default) {
            runCatching { QrCodeGenerator.generate(item.content, sizePx = 512) }.getOrNull()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.title) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.pressBounce(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(color = Color.White) {
                val qrWidthFraction = if (item.type == ItemType.BOTH) {
                    SurfaceDisplayConstants.BOTH_QR_WIDTH_FRACTION
                } else {
                    SurfaceDisplayConstants.QR_IN_APP_WIDTH_FRACTION
                }
                val bitmap = qrBitmap
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.qr_content_description, item.title),
                        modifier = Modifier
                            .fillMaxWidth(qrWidthFraction)
                            .aspectRatio(1f)
                            .padding(16.dp),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.qr_generation_failed),
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )

            Text(
                text = item.content,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .navSharedBounds(NavSharedKeys.edit(item.id))
                        .pressBounce(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.edit_item),
                    )
                }
                if (item.type.usesQr) {
                    Spacer(modifier = Modifier.width(12.dp))
                    FilledTonalIconButton(
                        onClick = onTipsClick,
                        modifier = Modifier
                            .navSharedBounds(NavSharedKeys.QrTips)
                            .pressBounce(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = stringResource(R.string.qr_tips_title),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDetailScreen(
    item: DisplayItem,
    onEditClick: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.title) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.pressBounce(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .navSharedBounds(NavSharedKeys.edit(item.id))
                            .pressBounce(),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.edit_item),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = item.content,
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrTipsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.qr_tips_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.pressBounce(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.qr_tips_url),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.qr_tips_scan),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
