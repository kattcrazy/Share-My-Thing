package kattcrazy.sharemything.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kattcrazy.sharemything.R
import kattcrazy.sharemything.core.R as CoreR
import kattcrazy.sharemything.data.DisplayItem
import kattcrazy.sharemything.data.ItemIcon
import kattcrazy.sharemything.data.ItemType
import kattcrazy.sharemything.data.asSingleLineQrContent
import kattcrazy.sharemything.data.usesQr
import kattcrazy.sharemything.ui.ItemIconPicker
import kattcrazy.sharemything.ui.EditControlShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    existingItem: DisplayItem?,
    onSave: (title: String, content: String, type: ItemType, icon: ItemIcon) -> Unit,
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
    var icon by remember(existingItem?.id) {
        mutableStateOf(existingItem?.icon ?: ItemIcon.defaultFor(existingItem?.type ?: ItemType.TEXT))
    }
    var iconManuallySet by remember(existingItem?.id) {
        mutableStateOf(existingItem != null)
    }
    LaunchedEffect(type) {
        if (!iconManuallySet) {
            icon = ItemIcon.defaultFor(type)
        }
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val validationRequiredMessage = stringResource(R.string.validation_required)
    val multilineQrWarningMessage = stringResource(R.string.validation_qr_multiline)
    val showMultilineQrWarning = type.usesQr && content.contains('\n')
    val examplePlaceholder = stringResource(R.string.field_placeholder_example)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingItem == null) {
                            stringResource(R.string.add_item)
                        } else {
                            stringResource(R.string.edit_item)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = { PlainTooltip { Text(stringResource(R.string.tooltip_title)) } },
                state = rememberTooltipState(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.field_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            validationError = null
                        },
                        placeholder = { Text(examplePlaceholder) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = { PlainTooltip { Text(stringResource(R.string.tooltip_content)) } },
                state = rememberTooltipState(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.field_content),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = {
                            content = it
                            validationError = null
                        },
                        placeholder = { Text(examplePlaceholder) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = if (type.usesQr) 1 else 4,
                        maxLines = 12,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = if (type.usesQr) KeyboardType.Uri else KeyboardType.Text,
                        ),
                    )
                }
            }

            if (showMultilineQrWarning) {
                Text(
                    text = multilineQrWarningMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = type == ItemType.TEXT,
                    onClick = {
                        type = ItemType.TEXT
                        iconManuallySet = false
                    },
                    label = { Text(stringResource(R.string.type_text)) },
                    shape = EditControlShape,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_item_text),
                            contentDescription = null,
                        )
                    },
                )
                FilterChip(
                    selected = type == ItemType.BOTH,
                    onClick = {
                        type = ItemType.BOTH
                        iconManuallySet = false
                    },
                    label = { Text(stringResource(R.string.type_both)) },
                    shape = EditControlShape,
                    leadingIcon = {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(
                                painter = painterResource(CoreR.drawable.ic_item_text),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Icon(
                                painter = painterResource(CoreR.drawable.ic_item_qr),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                )
                FilterChip(
                    selected = type == ItemType.QR_CODE,
                    onClick = {
                        type = ItemType.QR_CODE
                        iconManuallySet = false
                    },
                    label = { Text(stringResource(R.string.type_qr)) },
                    shape = EditControlShape,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_item_qr),
                            contentDescription = null,
                        )
                    },
                )
            }

            Text(
                text = stringResource(CoreR.string.field_icon),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ItemIconPicker(
                selectedIcon = icon,
                onIconSelected = { selected ->
                    icon = selected
                    iconManuallySet = true
                },
                modifier = Modifier.fillMaxWidth(),
            )

            validationError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank()) {
                        validationError = validationRequiredMessage
                    } else {
                        onSave(
                            title.trim(),
                            content.let { if (type.usesQr) it.asSingleLineQrContent() else it.trim() },
                            type,
                            icon,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }

            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                Text(stringResource(R.string.cancel))
            }

            if (existingItem != null && onDelete != null) {
                TextButton(
                    onClick = {
                        if (showDeleteConfirm) {
                            onDelete()
                        } else {
                            showDeleteConfirm = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
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
    }
}
