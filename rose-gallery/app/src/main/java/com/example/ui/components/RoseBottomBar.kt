package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GalleryTab
import com.example.ui.theme.*

@Composable
fun RoseBottomBar(
    currentTab: GalleryTab,
    onTabSelected: (GalleryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Motorola Hello UI floating pill dock
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(DarkSurfaceElevated.copy(alpha = 0.95f), DarkSurfaceVariant.copy(alpha = 0.98f))
                    )
                )
                .border(1.dp, RosePrimary.copy(alpha = 0.25f), RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabItem(
                    label = "Photos",
                    selected = currentTab == GalleryTab.PHOTOS,
                    activeIcon = Icons.Filled.PhotoLibrary,
                    inactiveIcon = Icons.Outlined.PhotoLibrary,
                    onClick = { onTabSelected(GalleryTab.PHOTOS) },
                    testTag = "tab_photos"
                )

                TabItem(
                    label = "Albums",
                    selected = currentTab == GalleryTab.ALBUMS,
                    activeIcon = Icons.Filled.Folder,
                    inactiveIcon = Icons.Outlined.Folder,
                    onClick = { onTabSelected(GalleryTab.ALBUMS) },
                    testTag = "tab_albums"
                )

                TabItem(
                    label = "Favorites",
                    selected = currentTab == GalleryTab.FAVORITES,
                    activeIcon = Icons.Filled.Favorite,
                    inactiveIcon = Icons.Outlined.FavoriteBorder,
                    onClick = { onTabSelected(GalleryTab.FAVORITES) },
                    testTag = "tab_favorites"
                )

                TabItem(
                    label = "Studio",
                    selected = currentTab == GalleryTab.FILTERS,
                    activeIcon = Icons.Filled.AutoAwesome,
                    inactiveIcon = Icons.Outlined.AutoAwesome,
                    onClick = { onTabSelected(GalleryTab.FILTERS) },
                    testTag = "tab_studio"
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    label: String,
    selected: Boolean,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) RoseContainer else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "tab_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) RoseSecondary else TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "tab_content_color"
    )

    Box(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) activeIcon else inactiveIcon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            if (selected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}
