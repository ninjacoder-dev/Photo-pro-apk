package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun TopQuickSettingsBar(
    flashMode: FlashModeSetting,
    onToggleFlash: () -> Unit,
    timerSeconds: Int,
    onCycleTimer: () -> Unit,
    aspectRatio: AspectRatioSetting,
    onCycleAspectRatio: () -> Unit,
    gridGuide: GridGuideSetting,
    onCycleGrid: () -> Unit,
    resolutionMode: ResolutionMode,
    isRawEnabled: Boolean,
    onResolutionChanged: (ResolutionMode) -> Unit,
    onToggleRaw: () -> Unit,
    onInfoClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quick Action Icons Group (Flash, Timer, Ratio, Grid)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(CameraDarkBackground.copy(alpha = 0.8f))
                .border(1.dp, CameraBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            // Flash Button
            IconButton(
                onClick = onToggleFlash,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("top_flash_button")
            ) {
                Icon(
                    imageVector = when (flashMode) {
                        FlashModeSetting.OFF -> Icons.Default.FlashOff
                        FlashModeSetting.AUTO -> Icons.Default.FlashAuto
                        FlashModeSetting.ON -> Icons.Default.FlashOn
                        FlashModeSetting.TORCH -> Icons.Default.Highlight
                    },
                    contentDescription = "Flash ${flashMode.label}",
                    tint = if (flashMode != FlashModeSetting.OFF) CameraAmberPrimary else CameraTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Timer Button
            IconButton(
                onClick = onCycleTimer,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("top_timer_button")
            ) {
                if (timerSeconds > 0) {
                    Text(
                        text = "${timerSeconds}s",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = CameraAmberPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Minuteur",
                        tint = CameraTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Aspect Ratio Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CameraDarkSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCycleAspectRatio() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("top_aspect_ratio_button")
            ) {
                Text(
                    text = aspectRatio.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = CameraTextPrimary
                )
            }

            // Grid Guide Button
            IconButton(
                onClick = onCycleGrid,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("top_grid_button")
            ) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "Grille ${gridGuide.label}",
                    tint = if (gridGuide != GridGuideSetting.OFF) CameraCyanAccent else CameraTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Center / Right: 12MP vs 50MP Resolution Selector
        ResolutionSelectorBar(
            selectedResolution = resolutionMode,
            isRawEnabled = isRawEnabled,
            onResolutionChanged = onResolutionChanged,
            onToggleRaw = onToggleRaw
        )

        // Info Button
        IconButton(
            onClick = onInfoClicked,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(CameraDarkBackground.copy(alpha = 0.8f))
                .border(1.dp, CameraBorder.copy(alpha = 0.6f), CircleShape)
                .testTag("top_info_button")
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Informations",
                tint = CameraTextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
