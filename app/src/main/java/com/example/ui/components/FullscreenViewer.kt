package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.MediaItem
import com.example.model.PhotoFilter
import com.example.ui.theme.*
import com.example.ui.util.ColorFilterUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenViewer(
    items: List<MediaItem>,
    initialIndex: Int,
    onClose: () -> Unit,
    onToggleFavorite: (MediaItem) -> Unit,
    onDelete: (MediaItem) -> Unit,
    onApplyFilter: (PhotoFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = initialIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))) {
        items.size
    }

    val currentItem = items.getOrNull(pagerState.currentPage)
    var showControls by remember { mutableStateOf(true) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("fullscreen_viewer_container")
    ) {
        // Pager of photos
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = items.getOrNull(page)
            if (item != null) {
                ZoomableImageItem(
                    item = item,
                    onTap = { showControls = !showControls }
                )
            }
        }

        // Top Overlay Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .testTag("btn_close_viewer")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close Viewer",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${items.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (currentItem != null) {
                            Text(
                                text = currentItem.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = {
                                if (currentItem != null) onToggleFavorite(currentItem)
                            },
                            modifier = Modifier
                                .testTag("btn_viewer_favorite")
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = if (currentItem?.isFavorite == true) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (currentItem?.isFavorite == true) RosePrimary else Color.White
                            )
                        }

                        IconButton(
                            onClick = { showInfoSheet = true },
                            modifier = Modifier
                                .testTag("btn_viewer_info")
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Photo Info",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Bottom Overlay Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            ) {
                // Filter picker strip when open
                if (showFilterSheet && currentItem != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceVariant.copy(alpha = 0.95f))
                            .border(1.dp, RosePrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Motorola Tone & Rose Profiles",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoseSecondary
                                )
                                IconButton(
                                    onClick = { showFilterSheet = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Close",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(PhotoFilter.values()) { filter ->
                                    val isSelected = currentItem.colorFilter == filter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onApplyFilter(filter) },
                                        label = {
                                            Text(
                                                text = filter.displayName,
                                                fontSize = 11.sp,
                                                color = if (isSelected) Color.White else TextSecondary
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RosePrimary,
                                            containerColor = DarkSurfaceElevated
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("viewer_filter_${filter.name}")
                                    )
                                }
                            }
                        }
                    }
                }

                // Action icons row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share
                    ViewerActionButton(
                        icon = Icons.Outlined.Share,
                        label = "Share",
                        testTag = "btn_viewer_share",
                        onClick = {
                            if (currentItem != null) {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Shared from Rose Gallery: ${currentItem.title}")
                                    val uri = Uri.parse(currentItem.uri)
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    type = currentItem.mimeType
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Photo")
                                context.startActivity(shareIntent)
                            }
                        }
                    )

                    // Tone / Filter
                    ViewerActionButton(
                        icon = Icons.Outlined.Palette,
                        label = "Tone",
                        testTag = "btn_viewer_tone",
                        onClick = { showFilterSheet = !showFilterSheet }
                    )

                    // Info Sheet
                    ViewerActionButton(
                        icon = Icons.Outlined.Info,
                        label = "Details",
                        testTag = "btn_viewer_details",
                        onClick = { showInfoSheet = true }
                    )

                    // Delete
                    ViewerActionButton(
                        icon = Icons.Outlined.Delete,
                        label = "Delete",
                        tint = Color(0xFFF87171),
                        testTag = "btn_viewer_delete",
                        onClick = { showDeleteConfirm = true }
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirm && currentItem != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                containerColor = DarkSurfaceVariant,
                titleContentColor = TextPrimary,
                textContentColor = TextSecondary,
                title = { Text("Delete Photo?") },
                text = { Text("Are you sure you want to remove '${currentItem.title}' from your gallery?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirm = false
                            onDelete(currentItem)
                        }
                    ) {
                        Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // Photo Info Bottom Sheet
        if (showInfoSheet && currentItem != null) {
            ModalBottomSheet(
                onDismissRequest = { showInfoSheet = false },
                containerColor = DarkSurfaceVariant,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(RoseSecondary.copy(alpha = 0.5f))
                    )
                }
            ) {
                PhotoInfoContent(
                    item = currentItem,
                    onClose = { showInfoSheet = false }
                )
            }
        }
    }
}

@Composable
fun ZoomableImageItem(
    item: MediaItem,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2.5f
                        offset = Offset.Zero
                    },
                    onTap = { onTap() }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f)
                    if (scale > 1f) {
                        offset += pan
                    } else {
                        offset = Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val colorFilter = ColorFilterUtils.getColorFilter(item.colorFilter)

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(item.uri)
                .crossfade(true)
                .build(),
            contentDescription = item.title,
            colorFilter = colorFilter,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )
    }
}

@Composable
fun ViewerActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.White,
    testTag: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = tint.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun PhotoInfoContent(
    item: MediaItem,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(item.dateAdded) {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(item.dateAdded * 1000L))
    }

    val sizeFormatted = remember(item.sizeBytes) {
        if (item.sizeBytes <= 0) "Unknown"
        else {
            val mb = item.sizeBytes / (1024.0 * 1024.0)
            String.format(Locale.US, "%.2f MB", mb)
        }
    }

    val megaPixels = remember(item.width, item.height) {
        val mp = (item.width * item.height) / 1_000_000.0
        String.format(Locale.US, "%.1f MP", mp)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Photo Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        InfoRow(label = "Title", value = item.title)
        InfoRow(label = "Date Taken", value = dateStr)
        InfoRow(label = "Resolution", value = "${item.width} × ${item.height} ($megaPixels)")
        InfoRow(label = "File Size", value = sizeFormatted)
        InfoRow(label = "Album", value = item.albumName)
        InfoRow(label = "Format", value = item.mimeType)
        InfoRow(label = "Display Profile", value = "Motorola Edge 50 pOLED")
        InfoRow(label = "Applied Filter", value = item.colorFilter.displayName)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
