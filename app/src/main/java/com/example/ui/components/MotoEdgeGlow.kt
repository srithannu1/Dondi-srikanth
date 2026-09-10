package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.RosePrimary

@Composable
fun MotoEdgeGlow(
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (!enabled) return

    Box(modifier = modifier.fillMaxSize()) {
        // Left edge subtle curved illumination
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .align(Alignment.CenterStart)
                .background(
                    Brush.horizontalGradient(
                        listOf(RosePrimary.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )

        // Right edge subtle curved illumination
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .align(Alignment.CenterEnd)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, RosePrimary.copy(alpha = 0.35f))
                    )
                )
        )
    }
}
