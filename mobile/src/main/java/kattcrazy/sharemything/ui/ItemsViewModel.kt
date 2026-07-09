package kattcrazy.sharemything.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kattcrazy.sharemything.data.DisplayItem
import kattcrazy.sharemything.data.ItemIcon
import kattcrazy.sharemything.data.ItemType
import kattcrazy.sharemything.data.ItemsRepository
import kattcrazy.sharemything.data.SurfaceSlot
import kattcrazy.sharemything.sync.BackupPayload
import kattcrazy.sharemything.sync.ImportMode
import kattcrazy.sharemything.sync.PeerAvailability
import kattcrazy.sharemything.sync.SyncFeedbackBridge
import kattcrazy.sharemything.sync.SyncRepository
import kattcrazy.sharemything.sync.SyncResult
import kattcrazy.sharemything.sync.WearSyncSupport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface SyncFeedback {
    data object Syncing : SyncFeedback
    data object Success : SyncFeedback
    data object NoWatchConnected : SyncFeedback
    data object AutoFailed : SyncFeedback
    data class Error(val message: String) : SyncFeedback
}

sealed interface ImportExportFeedback {
    data object ExportSuccess : ImportExportFeedback
    data class ImportSuccess(val mode: ImportMode) : ImportExportFeedback
    data class Error(val message: String) : ImportExportFeedback
    data object ImportReady : ImportExportFeedback
}

class ItemsViewModel(
    private val repository: ItemsRepository,
    private val syncRepository: SyncRepository,
    private val appContext: Context,
) : ViewModel() {
    val items: StateFlow<List<DisplayItem>> =
        repository.items.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val watchVisibleItems: StateFlow<List<DisplayItem>> =
        repository.watchVisibleItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val isPeerAvailable: StateFlow<Boolean> =
        PeerAvailability.observePeerConnected(appContext)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 0),
                initialValue = false,
            )

    val slotAssignments: StateFlow<Map<SurfaceSlot, Long?>> =
        repository.slotAssignments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    val surfacesPlacedOnWatch: StateFlow<Set<SurfaceSlot>> =
        repository.surfacesPlacedOnWatch.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet(),
        )

    private val _syncFeedback = MutableStateFlow<SyncFeedback?>(null)
    val syncFeedback: StateFlow<SyncFeedback?> = _syncFeedback.asStateFlow()

    private val _importExportFeedback = MutableStateFlow<ImportExportFeedback?>(null)
    val importExportFeedback: StateFlow<ImportExportFeedback?> = _importExportFeedback.asStateFlow()

    private var pendingImportBackup: BackupPayload? = null

    init {
        viewModelScope.launch {
            SyncFeedbackBridge.failures.collect { result ->
                if (result == SyncResult.Success || result == SyncResult.NoWatchConnected) return@collect
                if (!WearSyncSupport.isWearDataLayerAvailable(appContext)) return@collect
                _syncFeedback.value = SyncFeedback.AutoFailed
            }
        }
    }

    suspend fun getItem(id: Long): DisplayItem? = repository.getItem(id)

    fun syncWithWatch(manual: Boolean = false) {
        viewModelScope.launch {
            if (!WearSyncSupport.isWearDataLayerAvailable(appContext)) {
                if (manual) {
                    _syncFeedback.value = SyncFeedback.NoWatchConnected
                }
                return@launch
            }
            if (!PeerAvailability.hasSyncPeer(appContext)) {
                if (manual) {
                    _syncFeedback.value = SyncFeedback.NoWatchConnected
                }
                return@launch
            }
            if (manual) {
                _syncFeedback.value = SyncFeedback.Syncing
            }
            reportSyncResult(syncRepository.syncWithWatch(force = manual), manual = manual)
        }
    }

    private fun reportSyncResult(result: SyncResult, manual: Boolean) {
        if (!manual) return
        when (result) {
            SyncResult.Success -> {
                _syncFeedback.value = SyncFeedback.Success
            }
            SyncResult.NoWatchConnected -> {
                _syncFeedback.value = SyncFeedback.NoWatchConnected
            }
            is SyncResult.Error -> {
                _syncFeedback.value = SyncFeedback.Error(result.message)
            }
        }
    }

    fun clearSyncFeedback() {
        _syncFeedback.value = null
    }

    fun clearImportExportFeedback() {
        _importExportFeedback.value = null
    }

    fun exportBackup(uri: Uri, includeSlotAssignments: Boolean) {
        viewModelScope.launch {
            runCatching {
                val backup = repository.buildBackupPayload(includeSlotAssignments)
                appContext.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(backup.toJson().toByteArray(Charsets.UTF_8))
                } ?: throw IOException("Could not write file")
            }.onSuccess {
                _importExportFeedback.value = ImportExportFeedback.ExportSuccess
            }.onFailure { error ->
                _importExportFeedback.value = ImportExportFeedback.Error(
                    error.message ?: "Export failed",
                )
            }
        }
    }

    fun loadImportBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val json = appContext.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                } ?: throw IOException("Could not read file")
                BackupPayload.fromJson(json)
            }.onSuccess { backup ->
                if (backup.syncPayload.items.none { !it.deleted }) {
                    _importExportFeedback.value = ImportExportFeedback.Error("No items found in file")
                    pendingImportBackup = null
                } else {
                    pendingImportBackup = backup
                    _importExportFeedback.value = ImportExportFeedback.ImportReady
                }
            }.onFailure { error ->
                pendingImportBackup = null
                _importExportFeedback.value = ImportExportFeedback.Error(
                    error.message ?: "Import failed",
                )
            }
        }
    }

    fun confirmImport(mode: ImportMode) {
        val backup = pendingImportBackup ?: return
        pendingImportBackup = null
        viewModelScope.launch {
            runCatching {
                repository.importBackup(backup, mode)
            }.onSuccess {
                _importExportFeedback.value = ImportExportFeedback.ImportSuccess(mode = mode)
            }.onFailure { error ->
                _importExportFeedback.value = ImportExportFeedback.Error(
                    error.message ?: "Import failed",
                )
            }
        }
    }

    fun cancelPendingImport() {
        pendingImportBackup = null
    }

    fun saveItem(
        id: Long?,
        title: String,
        content: String,
        type: ItemType,
        icon: ItemIcon,
        onSaved: (DisplayItem) -> Unit,
    ) {
        viewModelScope.launch {
            val existing = id?.let { repository.getItem(it) }
            val savedId = repository.upsert(
                DisplayItem(
                    id = id ?: 0L,
                    uuid = existing?.uuid.orEmpty(),
                    title = title.trim(),
                    content = content.trim(),
                    type = type,
                    icon = icon,
                    sortOrder = existing?.sortOrder ?: items.value.size,
                    updatedAtMillis = existing?.updatedAtMillis ?: 0L,
                    visibleOnWatch = existing?.visibleOnWatch ?: true,
                ),
            )
            val savedItem = repository.getItem(savedId) ?: return@launch
            onSaved(savedItem)
        }
    }

    fun deleteItem(item: DisplayItem, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.delete(item)
            onDeleted()
        }
    }

    fun setSlotItemId(slot: SurfaceSlot, id: Long?) {
        viewModelScope.launch {
            repository.setSlotItemId(slot, id)
        }
    }

    fun setVisibleOnWatch(item: DisplayItem, visible: Boolean) {
        viewModelScope.launch {
            repository.setVisibleOnWatch(item.id, visible)
        }
    }

    fun commitItemOrder(orderedIds: List<Long>) {
        if (orderedIds.isEmpty()) return
        viewModelScope.launch {
            repository.reorderItems(orderedIds)
        }
    }

    class Factory(
        private val repository: ItemsRepository,
        private val syncRepository: SyncRepository,
        private val appContext: Context,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ItemsViewModel(repository, syncRepository, appContext) as T
        }
    }
}
