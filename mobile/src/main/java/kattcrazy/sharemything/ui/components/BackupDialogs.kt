package kattcrazy.sharemything.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kattcrazy.sharemything.R
import kattcrazy.sharemything.sync.ImportMode
import kattcrazy.sharemything.ui.pressBounce

@Composable
fun ExportBackupDialog(
    onDismiss: () -> Unit,
    onConfirm: (includeSlotAssignments: Boolean) -> Unit,
) {
    var includeAll by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export_dialog_title)) },
        text = {
            Column {
                ExportChoiceRow(
                    selected = !includeAll,
                    label = stringResource(R.string.export_items_only),
                    onClick = { includeAll = false },
                )
                ExportChoiceRow(
                    selected = includeAll,
                    label = stringResource(R.string.export_everything),
                    onClick = { includeAll = true },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(includeAll) },
                modifier = Modifier.pressBounce(),
            ) {
                Text(stringResource(R.string.export_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.pressBounce(),
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
fun ImportBackupDialog(
    onDismiss: () -> Unit,
    onConfirm: (ImportMode) -> Unit,
) {
    var selectedMode by remember { mutableStateOf(ImportMode.REPLACE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_dialog_title)) },
        text = {
            Column {
                ExportChoiceRow(
                    selected = selectedMode == ImportMode.REPLACE,
                    label = stringResource(R.string.import_replace_all),
                    onClick = { selectedMode = ImportMode.REPLACE },
                )
                ExportChoiceRow(
                    selected = selectedMode == ImportMode.MERGE,
                    label = stringResource(R.string.import_merge),
                    onClick = { selectedMode = ImportMode.MERGE },
                )
                ExportChoiceRow(
                    selected = selectedMode == ImportMode.ADD,
                    label = stringResource(R.string.import_add),
                    onClick = { selectedMode = ImportMode.ADD },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedMode) },
                modifier = Modifier.pressBounce(),
            ) {
                Text(stringResource(R.string.import_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.pressBounce(),
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun ExportChoiceRow(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}
