package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GalleryRepository
import com.example.model.AlbumInfo
import com.example.model.MediaItem
import com.example.model.PhotoFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GalleryTab(val title: String) {
    PHOTOS("Photos"),
    ALBUMS("Albums"),
    FAVORITES("Favorites"),
    FILTERS("Rose Studio")
}

data class GalleryUiState(
    val items: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val activeTab: GalleryTab = GalleryTab.PHOTOS,
    val columnCount: Int = 3,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedItemIds: Set<String> = emptySet(),
    val viewerCurrentItem: MediaItem? = null,
    val viewerCurrentIndex: Int = 0,
    val showInfoSheet: Boolean = false,
    val showFilterSheet: Boolean = false,
    val selectedAlbum: String? = null,
    val hasStoragePermission: Boolean = false
) {
    val filteredItems: List<MediaItem>
        get() {
            var list = items

            // Tab filter
            if (activeTab == GalleryTab.FAVORITES) {
                list = list.filter { it.isFavorite }
            }

            // Album drill down
            if (selectedAlbum != null) {
                list = list.filter { it.albumName.equals(selectedAlbum, ignoreCase = true) }
            }

            // Search query filter
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                list = list.filter {
                    it.title.lowercase().contains(q) ||
                    it.albumName.lowercase().contains(q)
                }
            }
            return list
        }

    val albums: List<AlbumInfo>
        get() {
            return items
                .groupBy { it.albumName }
                .map { (name, group) ->
                    val latest = group.maxByOrNull { it.dateAdded }?.dateAdded ?: 0L
                    val cover = group.firstOrNull()?.uri ?: ""
                    AlbumInfo(
                        name = name,
                        count = group.size,
                        coverUri = cover,
                        latestDate = latest
                    )
                }
                .sortedByDescending { it.count }
        }

    val favoriteCount: Int
        get() = items.count { it.isFavorite }
}

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GalleryRepository(application)
    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadMedia()
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(hasStoragePermission = isGranted) }
        loadMedia()
    }

    fun loadMedia() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val media = repository.loadAllMedia(_uiState.value.hasStoragePermission)
            _uiState.update { it.copy(items = media, isLoading = false) }
        }
    }

    fun setTab(tab: GalleryTab) {
        _uiState.update {
            it.copy(
                activeTab = tab,
                selectedAlbum = null,
                isSelectionMode = false,
                selectedItemIds = emptySet()
            )
        }
    }

    fun selectAlbum(albumName: String?) {
        _uiState.update { it.copy(selectedAlbum = albumName) }
    }

    fun toggleGridColumns() {
        _uiState.update { state ->
            val nextCol = when (state.columnCount) {
                2 -> 3
                3 -> 4
                else -> 2
            }
            state.copy(columnCount = nextCol)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleSearch() {
        _uiState.update {
            val nextActive = !it.isSearchActive
            it.copy(
                isSearchActive = nextActive,
                searchQuery = if (!nextActive) "" else it.searchQuery
            )
        }
    }

    fun toggleFavorite(item: MediaItem) {
        val newFav = repository.toggleFavorite(item.id)
        _uiState.update { state ->
            val updatedList = state.items.map {
                if (it.id == item.id) it.copy(isFavorite = newFav) else it
            }
            val updatedViewerItem = if (state.viewerCurrentItem?.id == item.id) {
                state.viewerCurrentItem.copy(isFavorite = newFav)
            } else {
                state.viewerCurrentItem
            }
            state.copy(items = updatedList, viewerCurrentItem = updatedViewerItem)
        }
    }

    fun toggleItemSelection(itemId: String) {
        _uiState.update { state ->
            val currentSelected = state.selectedItemIds.toMutableSet()
            if (currentSelected.contains(itemId)) {
                currentSelected.remove(itemId)
            } else {
                currentSelected.add(itemId)
            }
            state.copy(
                selectedItemIds = currentSelected,
                isSelectionMode = currentSelected.isNotEmpty()
            )
        }
    }

    fun enterSelectionMode(firstSelectedId: String) {
        _uiState.update {
            it.copy(
                isSelectionMode = true,
                selectedItemIds = setOf(firstSelectedId)
            )
        }
    }

    fun exitSelectionMode() {
        _uiState.update {
            it.copy(
                isSelectionMode = false,
                selectedItemIds = emptySet()
            )
        }
    }

    fun selectAll() {
        _uiState.update { state ->
            val allIds = state.filteredItems.map { it.id }.toSet()
            state.copy(selectedItemIds = allIds)
        }
    }

    fun favoriteSelected() {
        viewModelScope.launch {
            val selected = _uiState.value.selectedItemIds
            selected.forEach { id ->
                repository.toggleFavorite(id)
            }
            loadMedia()
            exitSelectionMode()
        }
    }

    fun deleteSelected() {
        val selected = _uiState.value.selectedItemIds
        _uiState.update { state ->
            val remaining = state.items.filterNot { selected.contains(it.id) }
            state.copy(
                items = remaining,
                isSelectionMode = false,
                selectedItemIds = emptySet()
            )
        }
    }

    fun deleteItem(item: MediaItem) {
        _uiState.update { state ->
            val remaining = state.items.filterNot { it.id == item.id }
            state.copy(
                items = remaining,
                viewerCurrentItem = null
            )
        }
    }

    fun openViewer(item: MediaItem) {
        val currentList = _uiState.value.filteredItems
        val index = currentList.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
        _uiState.update {
            it.copy(
                viewerCurrentItem = item,
                viewerCurrentIndex = index,
                showInfoSheet = false,
                showFilterSheet = false
            )
        }
    }

    fun closeViewer() {
        _uiState.update {
            it.copy(
                viewerCurrentItem = null,
                showInfoSheet = false,
                showFilterSheet = false
            )
        }
    }

    fun setViewerIndex(index: Int) {
        val list = _uiState.value.filteredItems
        if (index in list.indices) {
            _uiState.update {
                it.copy(
                    viewerCurrentIndex = index,
                    viewerCurrentItem = list[index]
                )
            }
        }
    }

    fun toggleInfoSheet() {
        _uiState.update { it.copy(showInfoSheet = !it.showInfoSheet) }
    }

    fun toggleFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = !it.showFilterSheet) }
    }

    fun applyFilterToViewerItem(filter: PhotoFilter) {
        _uiState.update { state ->
            val current = state.viewerCurrentItem ?: return@update state
            val updated = current.copy(colorFilter = filter)
            val updatedList = state.items.map {
                if (it.id == current.id) updated else it
            }
            state.copy(
                viewerCurrentItem = updated,
                items = updatedList
            )
        }
    }

    fun onImportPhoto(uri: Uri) {
        val newItem = repository.addImportedUri(uri)
        _uiState.update { state ->
            state.copy(items = listOf(newItem) + state.items)
        }
    }
}
