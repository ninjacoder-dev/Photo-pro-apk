package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.max

@Composable
fun LiveHistogramView(
    bins: IntArray,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CameraOverlayDark.copy(alpha = 0.75f))
            .border(1.dp, CameraBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HISTOGRAMME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = CameraCyanAccent
                )
                Text(
                    text = "LUMA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = CameraTextTertiary
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                if (bins.isEmpty()) return@Canvas

                val maxVal = bins.maxOrNull()?.coerceAtLeast(1) ?: 1
                val binWidth = size.width / bins.size.toFloat()

                val path = Path()
                path.moveTo(0f, size.height)

                for (i in bins.indices) {
                    val normalizedHeight = (bins[i].toFloat() / maxVal.toFloat()) * (size.height * 0.95f)
                    val x = i * binWidth + (binWidth / 2f)
                    val y = size.height - normalizedHeight
                    path.lineTo(x, y)
                }
                path.lineTo(size.width, size.height)
                path.close()

                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CameraCyanAccent.copy(alpha = 0.85f),
                            CameraAmberPrimary.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )

                // Outline stroke
                for (i in 0 until bins.size - 1) {
                    val h1 = (bins[i].toFloat() / maxVal.toFloat()) * (size.height * 0.95f)
                    val h2 = (bins[i + 1].toFloat() / maxVal.toFloat()) * (size.height * 0.95f)
                    val x1 = i * binWidth + (binWidth / 2f)
                    val y1 = size.height - h1
                    val x2 = (i + 1) * binWidth + (binWidth / 2f)
                    val y2 = size.height - h2
                    drawLine(
                        color = CameraCyanAccent,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }
        }
    }
}
