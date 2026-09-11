package com.example.data

import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.R
import com.example.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GalleryRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("rose_gallery_prefs", Context.MODE_PRIVATE)

    private val favoriteIds: MutableSet<String> =
        prefs.getStringSet("favorite_ids", emptySet())?.toMutableSet() ?: mutableSetOf()

    private val userImportedItems: MutableList<MediaItem> = mutableListOf()

    fun getFavoriteIds(): Set<String> = favoriteIds.toSet()

    fun toggleFavorite(itemId: String): Boolean {
        val isFav = if (favoriteIds.contains(itemId)) {
            favoriteIds.remove(itemId)
            false
        } else {
            favoriteIds.add(itemId)
            true
        }
        prefs.edit().putStringSet("favorite_ids", favoriteIds).apply()
        return isFav
    }

    fun addImportedUri(uri: Uri): MediaItem {
        val id = "imported_${System.currentTimeMillis()}_${userImportedItems.size}"
        val item = MediaItem(
            id = id,
            uri = uri.toString(),
            title = "Imported Photo ${userImportedItems.size + 1}",
            dateAdded = System.currentTimeMillis() / 1000L,
            albumName = "Recent Imports",
            isFavorite = false,
            width = 2400,
            height = 1800,
            sizeBytes = 3_200_000L,
            mimeType = "image/jpeg"
        )
        userImportedItems.add(0, item)
        return item
    }

    suspend fun loadAllMedia(hasStoragePermission: Boolean): List<MediaItem> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<MediaItem>()

        // 1. Add User imported items
        resultList.addAll(userImportedItems)

        // 2. Query Device MediaStore if permission granted
        if (hasStoragePermission) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                } else {
                    MediaStore.Images.Media.DATA
                }
            )

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            try {
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val nameCol = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val dateCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                    val mimeCol = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
                    val sizeCol = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                    val widthCol = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
                    val heightCol = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                    val bucketCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                    } else -1

                    while (cursor.moveToNext()) {
                        val mediaId = cursor.getLong(idCol)
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            mediaId
                        )
                        val name = if (nameCol >= 0) cursor.getString(nameCol) ?: "IMG_$mediaId" else "IMG_$mediaId"
                        val date = if (dateCol >= 0) cursor.getLong(dateCol) else System.currentTimeMillis() / 1000L
                        val mime = if (mimeCol >= 0) cursor.getString(mimeCol) ?: "image/jpeg" else "image/jpeg"
                        val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                        val width = if (widthCol >= 0) cursor.getInt(widthCol) else 1920
                        val height = if (heightCol >= 0) cursor.getInt(heightCol) else 1080
                        val album = if (bucketCol >= 0) cursor.getString(bucketCol) ?: "Camera" else "Camera"

                        val itemId = "device_$mediaId"
                        resultList.add(
                            MediaItem(
                                id = itemId,
                                uri = contentUri.toString(),
                                title = name,
                                dateAdded = date,
                                albumName = album,
                                isFavorite = favoriteIds.contains(itemId),
                                width = if (width > 0) width else 1920,
                                height = if (height > 0) height else 1080,
                                sizeBytes = size,
                                mimeType = mime
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore query failure, fallback gracefully
            }
        }

        // 3. Always include curated Motorola Edge 50 & Rose showcase photos
        // This ensures the user gets a rich, immediate gallery experience showcasing the rose aesthetic
        val curatedItems = getCuratedSampleItems()
        resultList.addAll(curatedItems)

        // Ensure favorite status reflects stored state
        resultList.map { item ->
            item.copy(isFavorite = favoriteIds.contains(item.id))
        }
    }

    private fun getCuratedSampleItems(): List<MediaItem> {
        val now = System.currentTimeMillis() / 1000L
        val pkg = context.packageName

        return listOf(
            MediaItem(
                id = "curated_rose_macro",
                uri = "android.resource://$pkg/${R.drawable.img_rose_macro}",
                title = "Crimson Dew Petals - Edge 50 Macro",
                dateAdded = now - 3600L,
                albumName = "Rose Curation",
                isFavorite = favoriteIds.contains("curated_rose_macro"),
                width = 3840,
                height = 2880,
                sizeBytes = 4_850_000L,
                mimeType = "image/jpeg",
                isCuratedSample = true
            ),
            MediaItem(
                id = "curated_edge_sunset",
                uri = "android.resource://$pkg/${R.drawable.img_edge_sunset}",
                title = "Alpine Horizon Sunset",
                dateAdded = now - 86400L,
                albumName = "Nature & Landscapes",
                isFavorite = favoriteIds.contains("curated_edge_sunset"),
                width = 4000,
                height = 3000,
                sizeBytes = 5_120_000L,
                mimeType = "image/jpeg",
                isCuratedSample = true
            ),
            MediaItem(
                id = "curated_neon_night",
                uri = "android.resource://$pkg/${R.drawable.img_neon_night}",
                title = "Cyber Architecture - Night Vision",
                dateAdded = now - (86400L * 2),
                albumName = "City & Architecture",
                isFavorite = favoriteIds.contains("curated_neon_night"),
                width = 3840,
                height = 2560,
                sizeBytes = 4_200_000L,
                mimeType = "image/jpeg",
                isCuratedSample = true
            ),
            MediaItem(
                id = "curated_rose_emblem",
                uri = "android.resource://$pkg/${R.drawable.img_rose_logo}",
                title = "Rose Gallery Signature Emblem",
                dateAdded = now - (86400L * 3),
                albumName = "Rose Curation",
                isFavorite = favoriteIds.contains("curated_rose_emblem"),
                width = 2048,
                height = 2048,
                sizeBytes = 2_150_000L,
                mimeType = "image/jpeg",
                isCuratedSample = true
            )
        )
    }
}
