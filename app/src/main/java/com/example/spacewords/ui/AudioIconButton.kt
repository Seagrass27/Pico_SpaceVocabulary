package com.example.spacewords.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect

@Composable
fun AudioIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.34f))
            .clickable(onClick = onClick)
            .spatialHoverEffect(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(21.dp)) {
            val iconColor = Color(0xFF5F6D7D)
            val speaker = Path().apply {
                moveTo(size.width * 0.08f, size.height * 0.38f)
                lineTo(size.width * 0.30f, size.height * 0.38f)
                lineTo(size.width * 0.52f, size.height * 0.18f)
                lineTo(size.width * 0.52f, size.height * 0.82f)
                lineTo(size.width * 0.30f, size.height * 0.62f)
                lineTo(size.width * 0.08f, size.height * 0.62f)
                close()
            }
            drawPath(speaker, iconColor)
            drawArc(
                color = iconColor,
                startAngle = -38f,
                sweepAngle = 76f,
                useCenter = false,
                topLeft = Offset(size.width * 0.42f, size.height * 0.24f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.38f, size.height * 0.52f),
                style = Stroke(width = 2.1.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = iconColor.copy(alpha = 0.72f),
                startAngle = -42f,
                sweepAngle = 84f,
                useCenter = false,
                topLeft = Offset(size.width * 0.48f, size.height * 0.08f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.50f, size.height * 0.84f),
                style = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
