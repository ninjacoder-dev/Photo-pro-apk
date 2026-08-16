package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RawOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ResolutionMode
import com.example.ui.theme.*

@Composable
fun ResolutionSelectorBar(
    selectedResolution: ResolutionMode,
    isRawEnabled: Boolean,
    onResolutionChanged: (ResolutionMode) -> Unit,
    onToggleRaw: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(CameraDarkBackground.copy(alpha = 0.85f))
            .border(1.dp, CameraBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 12 MP Pill
        ResolutionPill(
            resolution = ResolutionMode.MP_12,
            isSelected = selectedResolution == ResolutionMode.MP_12,
            onClick = { onResolutionChanged(ResolutionMode.MP_12) }
        )

        // 50 MP Pill
        ResolutionPill(
            resolution = ResolutionMode.MP_50,
            isSelected = selectedResolution == ResolutionMode.MP_50,
            onClick = { onResolutionChanged(ResolutionMode.MP_50) }
        )

        Divider(
            modifier = Modifier
                .height(20.dp)
                .width(1.dp),
            color = CameraBorder
        )

        // RAW / DNG Toggle
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isRawEnabled) CameraAmberPrimary else Color.Transparent,
            border = if (!isRawEnabled) BorderStroke(1.dp, CameraBorder) else null,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { onToggleRaw() }
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("raw_toggle_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "RAW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isRawEnabled) Color.Black else CameraTextSecondary
                )
            }
        }
    }
}

@Composable
private fun ResolutionPill(
    resolution: ResolutionMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected && resolution == ResolutionMode.MP_50 -> CameraCyanAccent
            isSelected -> CameraAmberPrimary
            else -> Color.Transparent
        },
        label = "res_pill_bg"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("resolution_mode_${resolution.name}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (resolution == ResolutionMode.MP_50) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = if (isSelected) Color.Black else CameraCyanAccent
                )
            }
            Text(
                text = resolution.label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = if (isSelected) Color.Black else CameraTextPrimary
            )
        }
    }
}
