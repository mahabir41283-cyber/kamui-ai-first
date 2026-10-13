package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AotesDarkBlue
import com.example.ui.theme.AotesIndigo
import com.example.ui.theme.AotesPrimaryBlue

@Composable
fun AotesAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = AotesPrimaryBlue)
            .border(width = 1.5.dp, color = Color.White.copy(alpha = 0.9f), shape = CircleShape)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(AotesPrimaryBlue, AotesDarkBlue, AotesIndigo)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = KamuiIcons.School,
            contentDescription = "AOTES Study Assistant",
            tint = Color.White,
            modifier = Modifier.size(size * 0.55f)
        )

        // Depth glint
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Reflection highlight
        Box(
            modifier = Modifier
                .size(size * 0.32f)
                .align(Alignment.TopStart)
                .offset(x = size * 0.16f, y = size * 0.10f)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f))
        )
    }
}
