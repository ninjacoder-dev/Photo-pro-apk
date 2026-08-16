package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ZoomCapabilities
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun ZoomControlBar(
    zoomCapabilities: ZoomCapabilities,
    currentZoom: Float,
    onZoomSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val opticalComp = zoomCapabilities.opticalZoomComponent
    val digitalComp = zoomCapabilities.digitalZoomComponent
    val maxOptical = zoomCapabilities.maxOpticalZoom
    val maxDigital = zoomCapabilities.maxDigitalZoom

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // TOP BADGES: Max Optical & Max Digital Zoom (Strictly requested by User)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(CameraDarkBackground.copy(alpha = 0.88f))
                .border(1.dp, CameraBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Zoom Physique Max Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CameraAmberPrimary)
                )
                Text(
                    text = "Zoom Physique Max : ",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = CameraTextSecondary
                )
                Text(
                    text = String.format(Locale.US, "%.1fx", maxOptical),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CameraAmberPrimary
                )
            }

            Divider(
                modifier = Modifier
                    .height(14.dp)
                    .width(1.dp),
                color = CameraBorder
            )

            // Zoom Numérique Max Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CameraCyanAccent)
                )
                Text(
                    text = "Zoom Numérique Max : ",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = CameraTextSecondary
                )
                Text(
                    text = String.format(Locale.US, "%.0fx", maxDigital),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CameraCyanAccent
                )
            }
        }

        // Live Zoom Breakdown HUD Box
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            CameraDarkSurfaceVariant.copy(alpha = 0.92f),
                            CameraDarkBackground.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(1.dp, CameraBorder.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Total Active Zoom
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TOTAL",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                    color = CameraTextTertiary
                )
                Text(
                    text = String.format(Locale.US, "%.1fx", currentZoom),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    ),
                    color = if (currentZoom <= maxOptical) CameraAmberPrimary else CameraCyanAccent
                )
            }

            Divider(
                modifier = Modifier
                    .height(20.dp)
                    .width(1.dp),
                color = CameraBorder
            )

            // Optical portion
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PHYSIQUE (OPT)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                    color = CameraAmberPrimary
                )
                Text(
                    text = String.format(Locale.US, "%.1fx", opticalComp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = CameraTextPrimary
                )
            }

            Divider(
                modifier = Modifier
                    .height(20.dp)
                    .width(1.dp),
                color = CameraBorder
            )

            // Digital portion
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NUMÉRIQUE (CROP)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                    color = CameraCyanAccent
                )
                Text(
                    text = String.format(Locale.US, "%.1fx", digitalComp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = if (digitalComp > 1.05f) CameraCyanAccent else CameraTextSecondary
                )
            }
        }

        // Quick Lens Preset Pills (0.5x, 1x, 2x, 3x, 5x, 10x, 30x)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val presetList = zoomCapabilities.physicalLenses.ifEmpty {
                listOf(0.5f, 1.0f, 2.0f, 3.0f, 5.0f, 10.0f, 30.0f)
            }

            presetList.forEach { lensZoom ->
                val isSelected = kotlin.math.abs(currentZoom - lensZoom) < 0.15f
                val isOptical = lensZoom <= maxOptical

                Surface(
                    shape = CircleShape,
                    color = when {
                        isSelected -> if (isOptical) CameraAmberPrimary else CameraCyanAccent
                        else -> CameraDarkSurfaceVariant.copy(alpha = 0.85f)
                    },
                    border = if (!isSelected) {
                        BorderStroke(1.dp, if (isOptical) CameraAmberPrimary.copy(alpha = 0.4f) else CameraBorder)
                    } else null,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { onZoomSelected(lensZoom) }
                        .testTag("zoom_button_${lensZoom}x")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (lensZoom == 0.5f) ".5" else if (lensZoom % 1f == 0f) "${lensZoom.toInt()}" else "$lensZoom",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = if (isSelected) Color.Black else CameraTextPrimary
                        )
                        Text(
                            text = if (isOptical) "OPT" else "NUM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = if (isSelected) Color.Black.copy(alpha = 0.7f) else if (isOptical) CameraAmberPrimary else CameraCyanAccent
                        )
                    }
                }
            }
        }

        // Tactile Zoom Slider for continuous adjustment
        Slider(
            value = currentZoom,
            onValueChange = { onZoomSelected(it) },
            valueRange = zoomCapabilities.minZoom..zoomCapabilities.maxDigitalZoom,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(30.dp)
                .testTag("zoom_continuous_slider"),
            colors = SliderDefaults.colors(
                thumbColor = if (currentZoom <= maxOptical) CameraAmberPrimary else CameraCyanAccent,
                activeTrackColor = if (currentZoom <= maxOptical) CameraAmberPrimary else CameraCyanAccent,
                inactiveTrackColor = CameraDarkSurfaceVariant
            )
        )
    }
}
