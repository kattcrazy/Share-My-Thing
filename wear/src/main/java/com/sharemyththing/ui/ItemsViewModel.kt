package com.sharemyththing.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sharemyththing.data.DisplayItem
import com.sharemyththing.data.ItemType
import com.sharemyththing.data.ItemsRepository
import com.sharemyththing.data.SurfaceSlot
import com.sharemyththing.sync.SyncRepository
import com.sharemyththing.sync.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SyncFeedback {
    data object Syncing : SyncFeedback
    data object Success : SyncFeedback
    data object NoWatchConnected : SyncFeedback
    data class Error(val message: String) : SyncFeedback
}

class ItemsViewModel(
    private val repository: ItemsRepository,
    private val syncRepository: SyncRepository,
) : ViewModel() {
    val items: StateFlow<List<DisplayItem>> =
        repository.items.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
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

    suspend fun getItem(id: Long): DisplayItem? = repository.getItem(id)

    fun syncWithWatch(manual: Boolean = false) {
        viewModelScope.launch {
            if (manual) {
                _syncFeedback.value = SyncFeedback.Syncing
            }
            reportSyncResult(syncRepository.syncWithWatch(), manual = manual)
        }
    }

    private fun reportSyncResult(result: SyncResult, manual: Boolean) {
        when (result) {
            SyncResult.Success -> {
                if (manual) {
                    _syncFeedback.value = SyncFeedback.Success
                }
            }
            SyncResult.NoWatchConnected -> {
                if (manual) {
                    _syncFeedback.value = SyncFeedback.NoWatchConnected
                }
            }
            is SyncResult.Error -> {
                if (manual) {
                    _syncFeedback.value = SyncFeedback.Error(result.message)
                }
            }
        }
    }

    fun clearSyncFeedback() {
        _syncFeedback.value = null
    }

    fun saveItem(
        id: Long?,
        title: String,
        content: String,
        type: ItemType,
        onSaved: (Long) -> Unit,
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
                    sortOrder = existing?.sortOrder ?: items.value.size,
                    updatedAtMillis = existing?.updatedAtMillis ?: 0L,
                ),
            )
            onSaved(savedId)
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

    fun commitItemOrder(orderedIds: List<Long>) {
        if (orderedIds.isEmpty()) return
        viewModelScope.launch {
            repository.reorderItems(orderedIds)
        }
    }

    class Factory(
        private val repository: ItemsRepository,
        private val syncRepository: SyncRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ItemsViewModel(repository, syncRepository) as T
        }
    }
}
