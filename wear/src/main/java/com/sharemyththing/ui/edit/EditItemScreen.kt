package com.sharemyththing.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.sharemyththing.data.usesQr
import com.sharemyththing.ui.bottomScrollSpacer

private enum class EditFieldTarget {
    Title,
    Content,
}

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
    var activeField by remember { mutableStateOf<EditFieldTarget?>(null) }
    val validationRequiredMessage = stringResource(R.string.validation_required)
    val titleLabel = stringResource(R.string.field_title)
    val contentLabel = stringResource(R.string.field_content)

    BackHandler(enabled = activeField != null) {
        activeField = null
    }

    when (activeField) {
        EditFieldTarget.Title -> {
            EditFieldScreen(
                fieldLabel = titleLabel,
                value = title,
                onValueChange = {
                    title = it
                    validationError = null
                },
                onDone = { activeField = null },
            )
        }

        EditFieldTarget.Content -> {
            EditFieldScreen(
                fieldLabel = contentLabel,
                value = content,
                onValueChange = {
                    content = it
                    validationError = null
                },
                onDone = { activeField = null },
                keyboardType = if (type.usesQr) KeyboardType.Uri else KeyboardType.Text,
            )
        }

        null -> EditItemMainScreen(
            existingItem = existingItem,
            title = title,
            content = content,
            type = type,
            validationError = validationError,
            showDeleteConfirm = showDeleteConfirm,
            validationRequiredMessage = validationRequiredMessage,
            onTitleClick = { activeField = EditFieldTarget.Title },
            onContentClick = { activeField = EditFieldTarget.Content },
            onTypeChange = { type = it },
            onSave = onSave,
            onDelete = onDelete,
            onCancel = onCancel,
            onShowDeleteConfirm = { showDeleteConfirm = true },
            onValidationFailed = { validationError = validationRequiredMessage },
        )
    }
}

@Composable
private fun EditItemMainScreen(
    existingItem: DisplayItem?,
    title: String,
    content: String,
    type: ItemType,
    validationError: String?,
    showDeleteConfirm: Boolean,
    validationRequiredMessage: String,
    onTitleClick: () -> Unit,
    onContentClick: () -> Unit,
    onTypeChange: (ItemType) -> Unit,
    onSave: (title: String, content: String, type: ItemType) -> Unit,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit,
    onShowDeleteConfirm: () -> Unit,
    onValidationFailed: () -> Unit,
) {
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
                    WearFieldSummaryRow(
                        fieldLabel = stringResource(R.string.field_title),
                        value = title,
                        onClick = onTitleClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    )
                }

                item {
                    WearFieldSummaryRow(
                        fieldLabel = stringResource(R.string.field_content),
                        value = content,
                        onClick = onContentClick,
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
                            onClick = { onTypeChange(ItemType.TEXT) },
                            modifier = Modifier.weight(1f),
                            transformation = SurfaceTransformation(transformationSpec),
                        )
                        TypeBothToggle(
                            selected = type == ItemType.BOTH,
                            onClick = { onTypeChange(ItemType.BOTH) },
                            modifier = Modifier.weight(1f),
                            transformation = SurfaceTransformation(transformationSpec),
                        )
                        TypeIconToggle(
                            selected = type == ItemType.QR_CODE,
                            iconRes = R.drawable.ic_item_qr,
                            contentDescription = stringResource(R.string.type_qr),
                            onClick = { onTypeChange(ItemType.QR_CODE) },
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
                                onValidationFailed()
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
                                    onShowDeleteConfirm()
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
private fun WearFieldSummaryRow(
    fieldLabel: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fieldLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value.ifBlank { fieldLabel },
                style = MaterialTheme.typography.bodyMedium,
                color = if (value.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_edit),
            contentDescription = stringResource(R.string.edit_item),
            modifier = Modifier
                .padding(start = 8.dp)
                .size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TypeBothToggle(
    selected: Boolean,
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
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_item_text),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = LocalContentColor.current,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_item_qr),
                    contentDescription = stringResource(R.string.type_both),
                    modifier = Modifier.size(18.dp),
                    tint = LocalContentColor.current,
                )
            }
        }
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
