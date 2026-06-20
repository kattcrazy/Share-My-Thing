package com.sharemyththing.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sharemyththing.data.DisplayItem
import com.sharemyththing.data.ItemType
import com.sharemyththing.data.ItemsRepository
import com.sharemyththing.data.SurfaceSlot
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemsViewModel(
    private val repository: ItemsRepository,
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

    suspend fun getItem(id: Long): DisplayItem? = repository.getItem(id)

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
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ItemsViewModel(repository) as T
        }
    }
}
