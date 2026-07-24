package kattcrazy.sharemything.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
import kattcrazy.sharemything.data.DisplayItem
import kattcrazy.sharemything.ui.ItemContentText
import kattcrazy.sharemything.ui.edgeButtonBottomScrollSpacer
import kattcrazy.sharemything.ui.edgeButtonTopScrollSpacer

@Composable
fun TextDetailScreen(
    item: DisplayItem,
    onEditClick: () -> Unit,
) {
    val context = LocalContext.current
    AppScaffold {
        val listState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()
        ScreenScaffold(scrollState = listState) { contentPadding ->
            TransformingLazyColumn(
                state = listState,
                contentPadding = contentPadding,
            ) {
                edgeButtonTopScrollSpacer(transformationSpec = transformationSpec)

                item(key = "content") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = item.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                        )

                        ItemContentText(
                            text = item.content,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, start = 12.dp, end = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            linkColor = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            selectable = false,
                            onUrlClick = { url ->
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                                )
                            },
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp, bottom = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            DetailEditButton(
                                onEditClick = onEditClick,
                                transformation = SurfaceTransformation(transformationSpec),
                            )
                        }
                    }
                }

                edgeButtonBottomScrollSpacer(transformationSpec = transformationSpec)
            }
        }
    }
}
