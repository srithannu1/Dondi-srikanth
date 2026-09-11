package com.example.model

data class MediaItem(
    val id: String,
    val uri: String,
    val title: String,
    val dateAdded: Long,
    val albumName: String = "Camera",
    val isFavorite: Boolean = false,
    val width: Int = 1920,
    val height: Int = 1080,
    val sizeBytes: Long = 2_450_000L,
    val mimeType: String = "image/jpeg",
    val isCuratedSample: Boolean = false,
    val colorFilter: PhotoFilter = PhotoFilter.ORIGINAL
)

enum class PhotoFilter(val displayName: String) {
    ORIGINAL("Original"),
    VIVID_ROSE("Vivid Rose"),
    OLED_NOIR("OLED Noir"),
    WARM_EMBER("Warm Ember"),
    COOL_CYAN("Cool Cyan"),
    VINTAGE("Vintage")
}

data class AlbumInfo(
    val name: String,
    val count: Int,
    val coverUri: String,
    val latestDate: Long
)
