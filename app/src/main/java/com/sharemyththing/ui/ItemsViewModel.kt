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
            val savedId = repository.upsert(
                DisplayItem(
                    id = id ?: 0L,
                    title = title.trim(),
                    content = content.trim(),
                    type = type,
                    sortOrder = id?.let { existingId ->
                        repository.getItem(existingId)?.sortOrder
                    } ?: items.value.size,
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

    class Factory(
        private val repository: ItemsRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ItemsViewModel(repository) as T
        }
    }
}
