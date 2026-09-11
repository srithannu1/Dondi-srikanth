package com.example.ui.util

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.example.model.PhotoFilter

object ColorFilterUtils {

    fun getColorFilter(filter: PhotoFilter): ColorFilter? {
        return when (filter) {
            PhotoFilter.ORIGINAL -> null

            PhotoFilter.VIVID_ROSE -> {
                // Boost red and magenta channels, enrich contrast
                val matrix = ColorMatrix(
                    floatArrayOf(
                        1.25f, 0.00f, 0.05f, 0.00f, 15f,
                        0.00f, 0.95f, 0.00f, 0.00f, 0f,
                        0.05f, 0.00f, 1.15f, 0.00f, 10f,
                        0.00f, 0.00f, 0.00f, 1.00f, 0f
                    )
                )
                ColorFilter.colorMatrix(matrix)
            }

            PhotoFilter.OLED_NOIR -> {
                // Deep high-contrast black and white for OLED
                val matrix = ColorMatrix()
                matrix.setToSaturation(0f)
                // Boost contrast
                val contrast = 1.35f
                val translate = (-0.5f * contrast + 0.5f) * 255f
                val contrastMatrix = ColorMatrix(
                    floatArrayOf(
                        contrast, 0f, 0f, 0f, translate,
                        0f, contrast, 0f, 0f, translate,
                        0f, 0f, contrast, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.timesAssign(contrastMatrix)
                ColorFilter.colorMatrix(matrix)
            }

            PhotoFilter.WARM_EMBER -> {
                // Warm sunset amber glow
                val matrix = ColorMatrix(
                    floatArrayOf(
                        1.20f, 0.00f, 0.00f, 0.00f, 25f,
                        0.00f, 1.05f, 0.00f, 0.00f, 15f,
                        0.00f, 0.00f, 0.80f, 0.00f, -10f,
                        0.00f, 0.00f, 0.00f, 1.00f, 0f
                    )
                )
                ColorFilter.colorMatrix(matrix)
            }

            PhotoFilter.COOL_CYAN -> {
                // Crisp cool cyberpunk cyan & blue
                val matrix = ColorMatrix(
                    floatArrayOf(
                        0.85f, 0.00f, 0.00f, 0.00f, -10f,
                        0.00f, 1.10f, 0.00f, 0.00f, 10f,
                        0.00f, 0.00f, 1.30f, 0.00f, 25f,
                        0.00f, 0.00f, 0.00f, 1.00f, 0f
                    )
                )
                ColorFilter.colorMatrix(matrix)
            }

            PhotoFilter.VINTAGE -> {
                // Sepia-tinted vintage fade
                val matrix = ColorMatrix(
                    floatArrayOf(
                        0.393f * 1.1f, 0.769f, 0.189f, 0f, 10f,
                        0.349f, 0.686f * 1.05f, 0.168f, 0f, 5f,
                        0.272f, 0.534f, 0.131f * 0.9f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                ColorFilter.colorMatrix(matrix)
            }
        }
    }
}
