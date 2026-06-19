package com.sharemyththing.ui.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.LocalContentColor
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.sharemyththing.R
import com.sharemyththing.data.DisplayItem
import com.sharemyththing.data.ItemType
import com.sharemyththing.ui.bottomScrollSpacer

@Composable
fun EditItemScreen(
    existingItem: DisplayItem?,
    onSave: (title: String, content: String, type: ItemType) -> Unit,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit,
) {
    var title by remember(existingItem?.id) {
        mutableStateOf(existingItem?.title.orEmpty())
    }
    var content by remember(existingItem?.id) {
        mutableStateOf(existingItem?.content.orEmpty())
    }
    var type by remember(existingItem?.id) {
        mutableStateOf(existingItem?.type ?: ItemType.TEXT)
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val validationRequiredMessage = stringResource(R.string.validation_required)

    AppScaffold {
        val listState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()
        ScreenScaffold(scrollState = listState) { contentPadding ->
            TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                item {
                    ListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    ) {
                        Text(
                            if (existingItem == null) {
                                stringResource(R.string.add_item)
                            } else {
                                stringResource(R.string.edit_item)
                            },
                        )
                    }
                }

                item {
                    WearEditableField(
                        fieldLabel = stringResource(R.string.field_title),
                        value = title,
                        onValueChange = { updated ->
                            title = updated
                            validationError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    )
                }

                item {
                    WearEditableField(
                        fieldLabel = stringResource(R.string.field_content),
                        value = content,
                        onValueChange = { updated ->
                            content = updated
                            validationError = null
                        },
                        keyboardType = KeyboardType.Uri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .transformedHeight(this, transformationSpec),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TypeIconToggle(
                            selected = type == ItemType.TEXT,
                            iconRes = R.drawable.ic_item_text,
                            contentDescription = stringResource(R.string.type_text),
                            onClick = { type = ItemType.TEXT },
                            modifier = Modifier.weight(1f),
                            transformation = SurfaceTransformation(transformationSpec),
                        )
                        TypeIconToggle(
                            selected = type == ItemType.QR_CODE,
                            iconRes = R.drawable.ic_item_qr,
                            contentDescription = stringResource(R.string.type_qr),
                            onClick = { type = ItemType.QR_CODE },
                            modifier = Modifier.weight(1f),
                            transformation = SurfaceTransformation(transformationSpec),
                        )
                    }
                }

                validationError?.let { error ->
                    item {
                        Text(
                            text = error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (title.isBlank() || content.isBlank()) {
                                validationError = validationRequiredMessage
                            } else {
                                onSave(title.trim(), content.trim(), type)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }

                item {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }

                if (existingItem != null && onDelete != null) {
                    item {
                        Button(
                            onClick = {
                                if (showDeleteConfirm) {
                                    onDelete()
                                } else {
                                    showDeleteConfirm = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                        ) {
                            Text(
                                if (showDeleteConfirm) {
                                    stringResource(R.string.confirm_delete)
                                } else {
                                    stringResource(R.string.delete)
                                },
                            )
                        }
                    }
                }

                bottomScrollSpacer(transformationSpec = transformationSpec)
            }
        }
    }
}

@Composable
private fun WearEditableField(
    fieldLabel: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { focusRequester.requestFocus() },
        verticalAlignment = Alignment.Top,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = androidx.compose.ui.graphics.Color.Transparent,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = keyboardType,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            decorationBox = { innerTextField ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = fieldLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) {
                            Text(
                                text = fieldLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        innerTextField()
                    }
                }
            },
        )
        Icon(
            painter = painterResource(R.drawable.ic_edit),
            contentDescription = stringResource(R.string.edit_item),
            modifier = Modifier
                .padding(start = 8.dp)
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { focusRequester.requestFocus() },
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TypeIconToggle(
    selected: Boolean,
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        transformation = transformation,
        colors = if (selected) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        },
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp),
                tint = LocalContentColor.current,
            )
        }
    }
}
