package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.KamuiCrimson
import com.example.ui.theme.KamuiDarkRed

enum class SpinMode {
    SLOW, MEDIUM, FAST
}

@Composable
fun KamuiAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    spinMode: SpinMode = SpinMode.SLOW
) {
    val duration = when (spinMode) {
        SpinMode.SLOW -> 14000
        SpinMode.MEDIUM -> 4000
        SpinMode.FAST -> 1200
    }

    val infiniteTransition = rememberInfiniteTransition(label = "kamui_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 10.dp, shape = CircleShape, spotColor = KamuiCrimson)
            .border(width = 1.dp, color = KamuiDarkRed.copy(alpha = 0.8f), shape = CircleShape)
            .clip(CircleShape)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Sharingan Eye Graphic
        Image(
            painter = painterResource(id = R.drawable.kamui_sharingan),
            contentDescription = "KAMUI Mangekyo Sharingan Eye",
            modifier = Modifier
                .fillMaxSize()
                .rotate(angle)
        )

        // 3D Spherical Eye Glint & Reflection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        )

        // Specular glint highlight
        Box(
            modifier = Modifier
                .size(size * 0.3f)
                .align(Alignment.TopStart)
                .offset(x = size * 0.18f, y = size * 0.12f)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f))
        )
    }
}
