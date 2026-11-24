
package ca.bcit.binged.ui.mylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.bcit.binged.data.local.WatchlistEntity
import ca.bcit.binged.data.model.WatchStatus
import ca.bcit.binged.data.repository.WatchlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MyListUiState(
    val isLoading: Boolean = true,
    val entries: List<WatchlistEntity> = emptyList(), // after filtering
    val totalCount: Int = 0,                          // before filtering
    val filter: WatchStatus? = null                   // null = show all
)

class MyListViewModel(repository: WatchlistRepository) : ViewModel() {

    private val filter = MutableStateFlow<WatchStatus?>(null)

    // Re-calculates whenever the database or the selected filter changes.
    val uiState: StateFlow<MyListUiState> =
        combine(repository.getAll(), filter) { entries, selected ->
            MyListUiState(
                isLoading = false,
                entries = if (selected == null) entries else entries.filter { it.status == selected },
                totalCount = entries.size,
                filter = selected
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MyListUiState()
        )

    fun setFilter(status: WatchStatus?) {
        filter.value = status
    }
}