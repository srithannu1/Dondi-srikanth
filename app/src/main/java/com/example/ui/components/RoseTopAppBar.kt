package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.GalleryTab
import com.example.ui.theme.*

@Composable
fun RoseTopAppBar(
    title: String,
    subtitle: String?,
    columnCount: Int,
    activeTab: GalleryTab,
    onTabSelected: (GalleryTab) -> Unit,
    isSelectionMode: Boolean,
    selectedCount: Int,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onToggleColumns: () -> Unit,
    onExitSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onFavoriteSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onPickPhotos: () -> Unit,
    onLaunchCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkBackground,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBackground)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isSelectionMode) {
                // Selection action bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onExitSelection,
                            modifier = Modifier
                                .testTag("btn_exit_selection")
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit Selection",
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$selectedCount selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = onSelectAll,
                            modifier = Modifier.testTag("btn_select_all"),
                            colors = ButtonDefaults.textButtonColors(contentColor = RosePrimary)
                        ) {
                            Text("Select All", fontWeight = FontWeight.SemiBold)
                        }

                        IconButton(
                            onClick = onFavoriteSelected,
                            modifier = Modifier
                                .testTag("btn_favorite_selected")
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite Selected",
                                tint = RosePrimary
                            )
                        }

                        IconButton(
                            onClick = onDeleteSelected,
                            modifier = Modifier
                                .testTag("btn_delete_selected")
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            } else {
                // Natural Tones Header with Rose Logo & Moto Edge 50 styling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Rose Logo Circle Badge: #FFB4AB container
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RosePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_rose_logo),
                                contentDescription = "Rose Logo",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    letterSpacing = 0.2.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Edge 50 pill badge in natural tone
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkSurfaceElevated)
                                        .border(0.5.dp, BorderHighlight.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "Edge 50",
                                        color = RosePrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (subtitle != null) {
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Top Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Search Button
                        IconButton(
                            onClick = onToggleSearch,
                            modifier = Modifier
                                .testTag("btn_toggle_search")
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = if (isSearchActive) RosePrimary else TextSecondary
                            )
                        }

                        // Column toggle button
                        IconButton(
                            onClick = onToggleColumns,
                            modifier = Modifier
                                .testTag("btn_toggle_columns")
                                .minimumInteractiveComponentSize()
                        ) {
                            val icon = when (columnCount) {
                                2 -> Icons.Outlined.ViewStream
                                3 -> Icons.Outlined.GridView
                                else -> Icons.Outlined.ViewModule
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = "Toggle Grid Columns ($columnCount)",
                                tint = TextSecondary
                            )
                        }

                        // Add photo button via Photo Picker
                        IconButton(
                            onClick = onPickPhotos,
                            modifier = Modifier
                                .testTag("btn_add_photos")
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                contentDescription = "Add Photos",
                                tint = RosePrimary
                            )
                        }

                        // Camera shortcut
                        IconButton(
                            onClick = onLaunchCamera,
                            modifier = Modifier
                                .testTag("btn_launch_camera")
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = "Camera",
                                tint = TextTertiary
                            )
                        }

                        // Natural Tones decorative profile indicator circle
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceHigh)
                                .border(1.dp, BorderHighlight, CircleShape)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(GradientRoseStart, GradientRoseEnd)))
                            )
                        }
                    }
                }

                // Search Bar Input
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search photos, albums, dates...", color = TextTertiary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = RosePrimary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RosePrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .testTag("input_gallery_search")
                    )
                }

                // Natural Tones Sub-Navigation Bar
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = BorderSubtle,
                            shape = RoundedCornerShape(0.dp)
                        )
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    NavTabItem(
                        label = "Photos",
                        isSelected = activeTab == GalleryTab.PHOTOS,
                        onClick = { onTabSelected(GalleryTab.PHOTOS) }
                    )
                    NavTabItem(
                        label = "Albums",
                        isSelected = activeTab == GalleryTab.ALBUMS,
                        onClick = { onTabSelected(GalleryTab.ALBUMS) }
                    )
                    NavTabItem(
                        label = "Favorites",
                        isSelected = activeTab == GalleryTab.FAVORITES,
                        onClick = { onTabSelected(GalleryTab.FAVORITES) }
                    )
                    NavTabItem(
                        label = "For You",
                        isSelected = activeTab == GalleryTab.FILTERS,
                        onClick = { onTabSelected(GalleryTab.FILTERS) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) RosePrimary else TextSecondary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(2.dp)
                .background(if (isSelected) RosePrimary else Color.Transparent)
        )
    }
}
