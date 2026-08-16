package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GridGuideSetting
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun ViewfinderOverlay(
    gridGuide: GridGuideSetting,
    horizonTiltDegrees: Float,
    isLevel: Boolean,
    touchFocusPoint: Pair<Float, Float>?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Draw Grids & Horizon Leveler
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when (gridGuide) {
                GridGuideSetting.OFF -> {}
                GridGuideSetting.RULE_OF_THIRDS -> {
                    val x1 = w / 3f
                    val x2 = w * 2f / 3f
                    val y1 = h / 3f
                    val y2 = h * 2f / 3f
                    val gridColor = Color.White.copy(alpha = 0.28f)
                    val strokeW = 1.dp.toPx()

                    drawLine(gridColor, Offset(x1, 0f), Offset(x1, h), strokeW)
                    drawLine(gridColor, Offset(x2, 0f), Offset(x2, h), strokeW)
                    drawLine(gridColor, Offset(0f, y1), Offset(w, y1), strokeW)
                    drawLine(gridColor, Offset(0f, y2), Offset(w, y2), strokeW)
                }
                GridGuideSetting.GOLDEN_RATIO -> {
                    val phi = 0.618f
                    val x1 = w * (1f - phi)
                    val x2 = w * phi
                    val y1 = h * (1f - phi)
                    val y2 = h * phi
                    val gridColor = CameraAmberPrimary.copy(alpha = 0.32f)
                    val strokeW = 1.dp.toPx()

                    drawLine(gridColor, Offset(x1, 0f), Offset(x1, h), strokeW)
                    drawLine(gridColor, Offset(x2, 0f), Offset(x2, h), strokeW)
                    drawLine(gridColor, Offset(0f, y1), Offset(w, y1), strokeW)
                    drawLine(gridColor, Offset(0f, y2), Offset(w, y2), strokeW)
                }
                GridGuideSetting.HORIZON_LEVEL -> {
                    // Draw central crosshair & dynamic artificial horizon line
                    val cx = w / 2f
                    val cy = h / 2f
                    val barLen = 140.dp.toPx()
                    val levelColor = if (isLevel) CameraNeonGreen else CameraAmberPrimary

                    // Fixed reference reticle
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = 20.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(1.5.dp.toPx())
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f),
                        start = Offset(cx - 30.dp.toPx(), cy),
                        end = Offset(cx - 20.dp.toPx(), cy),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f),
                        start = Offset(cx + 20.dp.toPx(), cy),
                        end = Offset(cx + 30.dp.toPx(), cy),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Rotating horizon bar
                    rotate(degrees = -horizonTiltDegrees, pivot = Offset(cx, cy)) {
                        drawLine(
                            color = levelColor,
                            start = Offset(cx - barLen / 2f, cy),
                            end = Offset(cx + barLen / 2f, cy),
                            strokeWidth = if (isLevel) 3.dp.toPx() else 2.dp.toPx()
                        )
                        drawCircle(
                            color = levelColor,
                            radius = if (isLevel) 4.dp.toPx() else 3.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                    }
                }
            }
        }

        // Animated Touch-to-Focus Indicator
        if (touchFocusPoint != null) {
            FocusIndicatorBox(
                normalizedX = touchFocusPoint.first,
                normalizedY = touchFocusPoint.second
            )
        }
    }
}

@Composable
fun FocusIndicatorBox(
    normalizedX: Float,
    normalizedY: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "focus_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val px = normalizedX * size.width
            val py = normalizedY * size.height
            val boxSize = 64.dp.toPx() * scale
            val half = boxSize / 2f
            val cornerLen = 14.dp.toPx()

            val rectColor = CameraAmberPrimary

            // Draw corners
            // Top Left
            drawLine(rectColor, Offset(px - half, py - half), Offset(px - half + cornerLen, py - half), 2.5.dp.toPx())
            drawLine(rectColor, Offset(px - half, py - half), Offset(px - half, py - half + cornerLen), 2.5.dp.toPx())

            // Top Right
            drawLine(rectColor, Offset(px + half, py - half), Offset(px + half - cornerLen, py - half), 2.5.dp.toPx())
            drawLine(rectColor, Offset(px + half, py - half), Offset(px + half, py - half + cornerLen), 2.5.dp.toPx())

            // Bottom Left
            drawLine(rectColor, Offset(px - half, py + half), Offset(px - half + cornerLen, py + half), 2.5.dp.toPx())
            drawLine(rectColor, Offset(px - half, py + half), Offset(px - half, py + half - cornerLen), 2.5.dp.toPx())

            // Bottom Right
            drawLine(rectColor, Offset(px + half, py + half), Offset(px + half - cornerLen, py + half), 2.5.dp.toPx())
            drawLine(rectColor, Offset(px + half, py + half), Offset(px + half, py + half - cornerLen), 2.5.dp.toPx())

            // Center focus dot
            drawCircle(rectColor, radius = 2.5.dp.toPx(), center = Offset(px, py))
        }
    }
}
