package kattcrazy.sharemything.ui.list

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScreenScaffoldDefaults
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import kattcrazy.sharemything.R
import kattcrazy.sharemything.data.DisplayItem
import kattcrazy.sharemything.theme.ShareMyThingColorSchemes
import kattcrazy.sharemything.ui.SyncFeedback
import kattcrazy.sharemything.ui.edgeButtonTopScrollSpacer
import kattcrazy.sharemything.ui.pressBounce
import kotlinx.coroutines.delay

@Composable
fun ItemListScreen(
    items: List<DisplayItem>,
    isPeerAvailable: Boolean,
    onItemClick: (DisplayItem) -> Unit,
    onAddClick: () -> Unit,
    onTilesComplicationsClick: () -> Unit,
    onSyncClick: () -> Unit,
    syncFeedback: SyncFeedback?,
    onSyncFeedbackShown: () -> Unit,
) {
    val isSyncing = syncFeedback == SyncFeedback.Syncing
    val isSyncSuccess = syncFeedback == SyncFeedback.Success
    val isSyncError = syncFeedback is SyncFeedback.Error || syncFeedback == SyncFeedback.NoWatchConnected

    LaunchedEffect(syncFeedback) {
        when (syncFeedback) {
            SyncFeedback.Success -> {
                delay(SYNC_SUCCESS_DISPLAY_MS)
                onSyncFeedbackShown()
            }
            is SyncFeedback.Error, SyncFeedback.NoWatchConnected -> {
                delay(SYNC_ERROR_DISPLAY_MS)
                onSyncFeedbackShown()
            }
            else -> Unit
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "syncSpin")
    val spinningRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "syncRotation",
    )
    val syncRotation = if (isSyncing) spinningRotation else 0f
    val context = LocalContext.current
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
    val privacyPolicyLabel = stringResource(R.string.privacy_policy)

    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    AppScaffold {
        ScreenScaffold(
            scrollState = listState,
            edgeButtonSpacing = ScreenScaffoldDefaults.EdgeButtonMinSpacing,
            edgeButton = {
                EdgeButton(
                    onClick = onAddClick,
                    modifier = Modifier.pressBounce(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(stringResource(R.string.add_item))
                }
            },
        ) { contentPadding ->
            TransformingLazyColumn(
                contentPadding = contentPadding,
                state = listState,
            ) {
                edgeButtonTopScrollSpacer(transformationSpec = transformationSpec)

                if (isPeerAvailable) {
                    item(key = "sync") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 16.dp)
                                .transformedHeight(this, transformationSpec),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(
                                onClick = onSyncClick,
                                enabled = !isSyncing,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .pressBounce(),
                                transformation = SurfaceTransformation(transformationSpec),
                                contentPadding = PaddingValues(0.dp),
                                colors = when {
                                    isSyncError -> ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                        disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                                        disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                    isSyncSuccess || isSyncing -> ButtonDefaults.buttonColors(
                                        containerColor = ShareMyThingColorSchemes.watchSyncSuccessContainer,
                                        contentColor = ShareMyThingColorSchemes.watchSyncSuccessOn,
                                        disabledContainerColor = ShareMyThingColorSchemes.watchSyncSuccessContainer,
                                        disabledContentColor = ShareMyThingColorSchemes.watchSyncSuccessOn,
                                    )
                                    else -> ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                },
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_sync),
                                        contentDescription = stringResource(R.string.sync_with_watch),
                                        modifier = Modifier
                                            .size(26.dp)
                                            .graphicsLayer {
                                                rotationZ = syncRotation
                                            },
                                    )
                                }
                            }
                        }
                    }
                }

                if (items.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = stringResource(R.string.empty_items),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .transformedHeight(this, transformationSpec),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    items.forEach { item ->
                        item(key = item.id) {
                            Button(
                                onClick = { onItemClick(item) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pressBounce()
                                    .transformedHeight(this, transformationSpec),
                                transformation = SurfaceTransformation(transformationSpec),
                            ) {
                                Text(
                                    text = item.title,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }

                item(key = "tiles") {
                    Button(
                        onClick = onTilesComplicationsClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .pressBounce()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Text(stringResource(R.string.tiles_and_complications))
                    }
                }

                item(key = "privacy") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)),
                                )
                            }
                            .transformedHeight(this, transformationSpec),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = privacyPolicyLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

private const val SYNC_SUCCESS_DISPLAY_MS = 600L
private const val SYNC_ERROR_DISPLAY_MS = 2_500L
