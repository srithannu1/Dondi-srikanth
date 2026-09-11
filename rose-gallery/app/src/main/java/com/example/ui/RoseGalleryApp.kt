package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.MediaItem
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun RoseGalleryApp(
    viewModel: GalleryViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Permission check
    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var permissionRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    // Photo picker launcher (zero permission modern Android visual media picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            viewModel.onImportPhoto(uri)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // Handle captured preview if needed, or launch device camera
        viewModel.loadMedia()
    }

    // Check permission on launch
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            requiredPermission
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onPermissionResult(granted)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Motorola Edge 50 ambient edge lighting glow
        MotoEdgeGlow(enabled = true)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                val subtitle = when {
                    uiState.isSelectionMode -> null
                    uiState.selectedAlbum != null -> "Album: ${uiState.selectedAlbum}"
                    uiState.activeTab == GalleryTab.PHOTOS -> "${uiState.filteredItems.size} items • Motorola Edge 50"
                    uiState.activeTab == GalleryTab.ALBUMS -> "${uiState.albums.size} collections"
                    uiState.activeTab == GalleryTab.FAVORITES -> "${uiState.favoriteCount} favorited"
                    uiState.activeTab == GalleryTab.FILTERS -> "Pantone Color Tuning"
                    else -> null
                }

                RoseTopAppBar(
                    title = if (uiState.selectedAlbum != null) uiState.selectedAlbum!! else "Rose Gallery",
                    subtitle = subtitle,
                    columnCount = uiState.columnCount,
                    activeTab = uiState.activeTab,
                    onTabSelected = { tab -> viewModel.setTab(tab) },
                    isSelectionMode = uiState.isSelectionMode,
                    selectedCount = uiState.selectedItemIds.size,
                    isSearchActive = uiState.isSearchActive,
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onToggleSearch = { viewModel.toggleSearch() },
                    onToggleColumns = { viewModel.toggleGridColumns() },
                    onExitSelection = { viewModel.exitSelectionMode() },
                    onSelectAll = { viewModel.selectAll() },
                    onFavoriteSelected = { viewModel.favoriteSelected() },
                    onDeleteSelected = { viewModel.deleteSelected() },
                    onPickPhotos = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onLaunchCamera = {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        try {
                            context.startActivity(cameraIntent)
                        } catch (e: Exception) {
                            cameraLauncher.launch(null)
                        }
                    }
                )
            },
            bottomBar = {
                if (!uiState.isSelectionMode && uiState.viewerCurrentItem == null) {
                    RoseBottomBar(
                        currentTab = uiState.activeTab,
                        onTabSelected = { tab ->
                            viewModel.setTab(tab)
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main content switch based on Tab & Album selection
                when {
                    uiState.selectedAlbum != null -> {
                        PhotoGrid(
                            items = uiState.filteredItems,
                            columnCount = uiState.columnCount,
                            isSelectionMode = uiState.isSelectionMode,
                            selectedIds = uiState.selectedItemIds,
                            onItemClick = { item -> viewModel.openViewer(item) },
                            onItemLongClick = { item ->
                                if (uiState.isSelectionMode) {
                                    viewModel.toggleItemSelection(item.id)
                                } else {
                                    viewModel.enterSelectionMode(item.id)
                                }
                            },
                            onToggleFavorite = { item -> viewModel.toggleFavorite(item) }
                        )
                    }

                    uiState.activeTab == GalleryTab.PHOTOS -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Permission reminder banner if not granted
                            if (!uiState.hasStoragePermission && !permissionRequested) {
                                StoragePermissionBanner(
                                    onRequestPermission = {
                                        permissionRequested = true
                                        permissionLauncher.launch(requiredPermission)
                                    },
                                    onPickPhotos = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )
                            }

                            PhotoGrid(
                                items = uiState.filteredItems,
                                columnCount = uiState.columnCount,
                                isSelectionMode = uiState.isSelectionMode,
                                selectedIds = uiState.selectedItemIds,
                                onItemClick = { item -> viewModel.openViewer(item) },
                                onItemLongClick = { item ->
                                    if (uiState.isSelectionMode) {
                                        viewModel.toggleItemSelection(item.id)
                                    } else {
                                        viewModel.enterSelectionMode(item.id)
                                    }
                                },
                                onToggleFavorite = { item -> viewModel.toggleFavorite(item) }
                            )
                        }
                    }

                    uiState.activeTab == GalleryTab.ALBUMS -> {
                        AlbumView(
                            albums = uiState.albums,
                            selectedAlbum = uiState.selectedAlbum,
                            onSelectAlbum = { albumName -> viewModel.selectAlbum(albumName) }
                        )
                    }

                    uiState.activeTab == GalleryTab.FAVORITES -> {
                        PhotoGrid(
                            items = uiState.filteredItems,
                            columnCount = uiState.columnCount,
                            isSelectionMode = uiState.isSelectionMode,
                            selectedIds = uiState.selectedItemIds,
                            onItemClick = { item -> viewModel.openViewer(item) },
                            onItemLongClick = { item ->
                                if (uiState.isSelectionMode) {
                                    viewModel.toggleItemSelection(item.id)
                                } else {
                                    viewModel.enterSelectionMode(item.id)
                                }
                            },
                            onToggleFavorite = { item -> viewModel.toggleFavorite(item) }
                        )
                    }

                    uiState.activeTab == GalleryTab.FILTERS -> {
                        RoseStudioView(
                            items = uiState.items,
                            onApplyFilterToItem = { item, filter ->
                                viewModel.applyFilterToViewerItem(filter)
                            },
                            onOpenViewer = { item -> viewModel.openViewer(item) }
                        )
                    }
                }
            }
        }

        // Fullscreen Lightbox Viewer Overlay
        AnimatedVisibility(
            visible = uiState.viewerCurrentItem != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val currentList = uiState.filteredItems
            val initialIdx = uiState.viewerCurrentIndex.coerceIn(0, (currentList.size - 1).coerceAtLeast(0))

            FullscreenViewer(
                items = currentList,
                initialIndex = initialIdx,
                onClose = { viewModel.closeViewer() },
                onToggleFavorite = { item -> viewModel.toggleFavorite(item) },
                onDelete = { item -> viewModel.deleteItem(item) },
                onApplyFilter = { filter -> viewModel.applyFilterToViewerItem(filter) }
            )
        }
    }
}

@Composable
fun StoragePermissionBanner(
    onRequestPermission: () -> Unit,
    onPickPhotos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, RosePrimary.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RoseContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = RoseSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sync Device Photos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Allow access to show all device photos in Rose Gallery",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_grant_storage_permission")
            ) {
                Text("Allow", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
