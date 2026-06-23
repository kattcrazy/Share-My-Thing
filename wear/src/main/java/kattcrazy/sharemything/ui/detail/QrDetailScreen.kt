package kattcrazy.sharemything.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import kattcrazy.sharemything.R
import kattcrazy.sharemything.data.DisplayItem
import kattcrazy.sharemything.data.ItemType
import kattcrazy.sharemything.data.SurfaceDisplayConstants
import kattcrazy.sharemything.util.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun Modifier.bleedHorizontal(start: Dp, end: Dp): Modifier = layout { measurable, constraints ->
    val startPx = start.roundToPx()
    val endPx = end.roundToPx()
    val placeable = measurable.measure(
        constraints.copy(
            maxWidth = constraints.maxWidth + startPx + endPx,
            minWidth = 0,
        ),
    )
    layout(constraints.maxWidth, placeable.height) {
        placeable.placeRelative(-startPx, 0)
    }
}

@Composable
fun QrDetailScreen(
    item: DisplayItem,
    onEditClick: () -> Unit,
    onTipsClick: () -> Unit,
) {
    var qrBitmap by remember(item.content) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(item.content) {
        qrBitmap = withContext(Dispatchers.Default) {
            runCatching { QrCodeGenerator.generate(item.content) }.getOrNull()
        }
    }

    AppScaffold {
        key(item.id) {
            val listState = rememberTransformingLazyColumnState(initialAnchorItemIndex = -1)
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                val layoutDirection = LocalLayoutDirection.current
                val edgeBleedStart = contentPadding.calculateStartPadding(layoutDirection)
                val edgeBleedEnd = contentPadding.calculateEndPadding(layoutDirection)
                val edgeBleedTop = contentPadding.calculateTopPadding()
                val extraBottomScroll = 40.dp

                TransformingLazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        top = 0.dp,
                        bottom = edgeBleedTop + extraBottomScroll,
                        start = edgeBleedStart,
                        end = edgeBleedEnd,
                    ),
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .bleedHorizontal(edgeBleedStart, edgeBleedEnd),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(
                                        top = edgeBleedTop + 20.dp,
                                        start = 20.dp,
                                        end = 20.dp,
                                        bottom = 12.dp,
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                val qrWidthFraction = if (item.type == ItemType.BOTH) {
                                    SurfaceDisplayConstants.BOTH_QR_WIDTH_FRACTION
                                } else {
                                    SurfaceDisplayConstants.QR_IN_APP_WIDTH_FRACTION
                                }
                                val bitmap = qrBitmap
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = stringResource(
                                            R.string.qr_content_description,
                                            item.title,
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth(qrWidthFraction)
                                            .aspectRatio(1f),
                                        contentScale = ContentScale.Fit,
                                    )
                                }
                            }

                            Text(
                                text = item.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, start = 12.dp, end = 12.dp),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )

                            Text(
                                text = item.content,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, start = 12.dp, end = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 28.dp, bottom = 24.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                DetailEditButton(
                                    onEditClick = onEditClick,
                                    transformation = SurfaceTransformation(transformationSpec),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                DetailHelpButton(
                                    onHelpClick = onTipsClick,
                                    transformation = SurfaceTransformation(transformationSpec),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
