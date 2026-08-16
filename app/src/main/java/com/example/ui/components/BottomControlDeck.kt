package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ActiveParamTab
import com.example.model.CapturedPhoto
import com.example.ui.theme.*

@Composable
fun BottomControlDeck(
    activeTab: ActiveParamTab,
    onTabSelected: (ActiveParamTab) -> Unit,
    isoDisplay: String,
    shutterDisplay: String,
    wbDisplay: String,
    evDisplay: String,
    focusDisplay: String,
    zoomDisplay: String,
    isCapturing: Boolean,
    onCaptureClicked: () -> Unit,
    onFlipCamera: () -> Unit,
    lastPhoto: CapturedPhoto?,
    onGalleryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        CameraDarkBackground.copy(alpha = 0.85f),
                        CameraDarkBackground
                    )
                )
            )
            .padding(bottom = 20.dp, top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Horizontal Pro Parameter Badges Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProParamBadge(
                label = "ISO",
                value = isoDisplay,
                isActive = activeTab == ActiveParamTab.ISO,
                onClick = { onTabSelected(ActiveParamTab.ISO) }
            )
            ProParamBadge(
                label = "VITESSE",
                value = shutterDisplay,
                isActive = activeTab == ActiveParamTab.SHUTTER,
                onClick = { onTabSelected(ActiveParamTab.SHUTTER) }
            )
            ProParamBadge(
                label = "WB",
                value = wbDisplay,
                isActive = activeTab == ActiveParamTab.WB,
                onClick = { onTabSelected(ActiveParamTab.WB) }
            )
            ProParamBadge(
                label = "EV",
                value = evDisplay,
                isActive = activeTab == ActiveParamTab.EV,
                onClick = { onTabSelected(ActiveParamTab.EV) }
            )
            ProParamBadge(
                label = "FOCUS",
                value = focusDisplay,
                isActive = activeTab == ActiveParamTab.FOCUS,
                onClick = { onTabSelected(ActiveParamTab.FOCUS) }
            )
            ProParamBadge(
                label = "ZOOM",
                value = zoomDisplay,
                isActive = activeTab == ActiveParamTab.ZOOM,
                onClick = { onTabSelected(ActiveParamTab.ZOOM) }
            )
        }

        // Shutter & Primary Actions Deck
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery / Last Photo Thumbnail
            Surface(
                shape = CircleShape,
                color = CameraDarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(2.dp, CameraBorder),
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable { onGalleryClicked() }
                    .testTag("gallery_thumbnail_button")
            ) {
                if (lastPhoto != null) {
                    AsyncImage(
                        model = lastPhoto.filePath,
                        contentDescription = "Dernière photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Galerie",
                            tint = CameraTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Central Shutter Button
            ShutterButton(
                isCapturing = isCapturing,
                onClick = onCaptureClicked
            )

            // Switch Camera (Front/Back)
            Surface(
                shape = CircleShape,
                color = CameraDarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CameraBorder),
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable { onFlipCamera() }
                    .testTag("switch_camera_button")
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Changer de caméra",
                        tint = CameraTextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ShutterButton(
    isCapturing: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isCapturing) 0.88f else 1.0f,
        label = "shutter_scale"
    )

    Box(
        modifier = Modifier
            .size(86.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        CameraDarkSurfaceVariant,
                        CameraDarkBackground
                    )
                )
            )
            .border(
                2.5.dp,
                Brush.sweepGradient(
                    listOf(
                        CameraAmberPrimary,
                        CameraGoldGradientEnd,
                        CameraAmberPrimary,
                        CameraGoldGradientStart,
                        CameraAmberPrimary
                    )
                ),
                CircleShape
            )
            .padding(6.dp)
            .clip(CircleShape)
            .background(
                if (isCapturing)
                    Brush.radialGradient(listOf(CameraShutterRed, Color(0xFF9E1B28)))
                else
                    Brush.radialGradient(listOf(Color(0xFFFFFFFF), Color(0xFFDCDFE5)))
            )
            .clickable(enabled = !isCapturing) { onClick() }
            .testTag("camera_shutter_button"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(
                    if (isCapturing)
                        Brush.radialGradient(listOf(CameraShutterRed, Color(0xFF9E1B28)))
                    else
                        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE8EBF0)))
                )
                .border(1.5.dp, Color(0xFF101216).copy(alpha = 0.25f), CircleShape)
        )
    }
}

@Composable
private fun ProParamBadge(
    label: String,
    value: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) CameraAmberPrimary else CameraDarkSurfaceVariant.copy(alpha = 0.85f),
        border = if (!isActive) androidx.compose.foundation.BorderStroke(1.dp, CameraBorder) else androidx.compose.foundation.BorderStroke(1.dp, CameraGoldGradientStart),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .testTag("param_badge_$label")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                ),
                color = if (isActive) Color.Black.copy(alpha = 0.75f) else CameraAmberPrimary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = if (isActive) Color.Black else CameraTextPrimary
            )
        }
    }
}
