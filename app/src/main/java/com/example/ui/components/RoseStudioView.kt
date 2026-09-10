package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun RoseStudioView(
    items: List<MediaItem>,
    onApplyFilterToItem: (MediaItem, PhotoFilter) -> Unit,
    onOpenViewer: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedItem by remember(items) { mutableStateOf(items.firstOrNull()) }
    var selectedFilter by remember { mutableStateOf(PhotoFilter.VIVID_ROSE) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        // Motorola Edge 50 Pantone banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(RoseContainer, DarkSurfaceContainer)
                    )
                )
                .border(1.dp, RosePrimary.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(RosePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Motorola Edge 50 Studio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pantone Rose profiles optimized for 1.5K pOLED",
                        style = MaterialTheme.typography.bodySmall,
                        color = RoseSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Selected photo preview canvas
        val activePhoto = selectedItem
        if (activePhoto != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .clickable { onOpenViewer(activePhoto) }
                    .testTag("studio_preview_canvas")
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(activePhoto.uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = activePhoto.title,
                    colorFilter = ColorFilterUtils.getColorFilter(selectedFilter),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Bottom gradient with title & applied filter
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.85f)
                            )
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = activePhoto.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "Profile: ${selectedFilter.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = RoseSecondary
                            )
                        }

                        Button(
                            onClick = {
                                onApplyFilterToItem(activePhoto, selectedFilter)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RosePrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_save_filter_profile")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Color Profile Filter Selector
        Text(
            text = "Select Color Profile",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(PhotoFilter.values()) { filter ->
                val isCurrent = selectedFilter == filter
                FilterChip(
                    selected = isCurrent,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            text = filter.displayName,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) Color.White else TextSecondary
                        )
                    },
                    leadingIcon = {
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RosePrimary,
                        containerColor = DarkSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isCurrent,
                        borderColor = BorderSubtle,
                        selectedBorderColor = RosePrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_chip_${filter.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Select Photo Strip
        Text(
            text = "Choose Photo to Enhance",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items, key = { it.id }) { photo ->
                val isChosen = selectedItem?.id == photo.id
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .border(
                            width = if (isChosen) 2.5.dp else 1.dp,
                            color = if (isChosen) RosePrimary else BorderSubtle,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedItem = photo }
                        .testTag("strip_photo_${photo.id}")
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photo.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = photo.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
