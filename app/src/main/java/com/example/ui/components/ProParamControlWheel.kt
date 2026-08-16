package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun ProParamControlWheel(
    activeTab: ActiveParamTab,
    selectedIsoIndex: Int,
    isoOptions: List<String>,
    onIsoSelected: (Int) -> Unit,
    selectedShutterIndex: Int,
    onShutterSelected: (Int) -> Unit,
    selectedWbPreset: WhiteBalancePreset,
    customKelvin: Int,
    onWbPresetSelected: (WhiteBalancePreset) -> Unit,
    onKelvinChanged: (Int) -> Unit,
    evStep: Int,
    onEvStepChanged: (Int) -> Unit,
    focusMode: FocusModeSetting,
    manualFocusDistance: Float,
    onFocusModeChanged: (FocusModeSetting) -> Unit,
    onManualFocusDistanceChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeTab == ActiveParamTab.NONE || activeTab == ActiveParamTab.ZOOM || activeTab == ActiveParamTab.RESOLUTION) {
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        CameraDarkSurfaceVariant.copy(alpha = 0.96f),
                        CameraDarkBackground.copy(alpha = 0.98f)
                    )
                )
            )
            .border(1.dp, CameraBorder.copy(alpha = 0.7f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(vertical = 10.dp)
    ) {
        when (activeTab) {
            ActiveParamTab.ISO -> {
                IsoControlRow(
                    options = isoOptions,
                    selectedIndex = selectedIsoIndex,
                    onSelected = onIsoSelected
                )
            }
            ActiveParamTab.SHUTTER -> {
                ShutterControlRow(
                    selectedIndex = selectedShutterIndex,
                    onSelected = onShutterSelected
                )
            }
            ActiveParamTab.WB -> {
                WbControlRow(
                    selectedPreset = selectedWbPreset,
                    customKelvin = customKelvin,
                    onPresetSelected = onWbPresetSelected,
                    onKelvinChanged = onKelvinChanged
                )
            }
            ActiveParamTab.EV -> {
                EvControlRow(
                    evStep = evStep,
                    onEvChanged = onEvStepChanged
                )
            }
            ActiveParamTab.FOCUS -> {
                FocusControlRow(
                    focusMode = focusMode,
                    focusDistance = manualFocusDistance,
                    onFocusModeChanged = onFocusModeChanged,
                    onDistanceChanged = onManualFocusDistanceChanged
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun IsoControlRow(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "SENSIBILITÉ DU CAPTEUR (ISO)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = CameraAmberPrimary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, iso ->
                val isSelected = index == selectedIndex
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) CameraAmberPrimary else CameraDarkSurface,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, CameraBorder) else null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelected(index) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("iso_option_$iso")
                ) {
                    Text(
                        text = iso,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.Black else CameraTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ShutterControlRow(
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "TEMPS DE CAPTATION / VITESSE D'OBTURATION",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = CameraAmberPrimary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShutterSpeedOption.ALL.forEachIndexed { index, opt ->
                val isSelected = index == selectedIndex
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) CameraAmberPrimary else CameraDarkSurface,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, CameraBorder) else null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelected(index) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("shutter_option_${opt.display}")
                ) {
                    Text(
                        text = opt.display,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.Black else CameraTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun WbControlRow(
    selectedPreset: WhiteBalancePreset,
    customKelvin: Int,
    onPresetSelected: (WhiteBalancePreset) -> Unit,
    onKelvinChanged: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "BALANCE DES BLANCS (TEMPÉRATURE COULEUR)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = CameraAmberPrimary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WhiteBalancePreset.values().forEach { preset ->
                val isSelected = preset == selectedPreset
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) CameraAmberPrimary else CameraDarkSurface,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, CameraBorder) else null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onPresetSelected(preset) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("wb_preset_${preset.name}")
                ) {
                    Text(
                        text = preset.label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.Black else CameraTextPrimary
                    )
                }
            }
        }

        if (selectedPreset == WhiteBalancePreset.CUSTOM) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${customKelvin}K",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CameraCyanAccent,
                    modifier = Modifier.width(64.dp)
                )
                Slider(
                    value = customKelvin.toFloat(),
                    onValueChange = { onKelvinChanged(it.toInt()) },
                    valueRange = 2000f..10000f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = CameraCyanAccent,
                        activeTrackColor = CameraCyanAccent,
                        inactiveTrackColor = CameraDarkSurface
                    )
                )
            }
        }
    }
}

@Composable
private fun EvControlRow(
    evStep: Int,
    onEvChanged: (Int) -> Unit
) {
    val evValue = evStep * 0.33f
    val displayStr = String.format(Locale.US, "%+.1f EV", evValue)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COMPENSATION D'EXPOSITION (EV)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = CameraAmberPrimary
            )
            Text(
                text = displayStr,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                ),
                color = if (evStep == 0) CameraTextPrimary else if (evStep > 0) CameraAmberPrimary else CameraCyanAccent
            )
        }

        Slider(
            value = evStep.toFloat(),
            onValueChange = { onEvChanged(it.toInt()) },
            valueRange = -6f..6f,
            steps = 11,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = CameraAmberPrimary,
                activeTrackColor = CameraAmberPrimary,
                inactiveTrackColor = CameraDarkSurface
            )
        )
    }
}

@Composable
private fun FocusControlRow(
    focusMode: FocusModeSetting,
    focusDistance: Float,
    onFocusModeChanged: (FocusModeSetting) -> Unit,
    onDistanceChanged: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "SYSTÈME DE MISE AU POINT (FOCUS)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = CameraAmberPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FocusModeSetting.values().forEach { mode ->
                val isSelected = mode == focusMode
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) CameraAmberPrimary else CameraDarkSurface,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, CameraBorder) else null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onFocusModeChanged(mode) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("focus_mode_${mode.name}")
                ) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.Black else CameraTextPrimary
                    )
                }
            }
        }

        if (focusMode == FocusModeSetting.MANUAL) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (focusDistance <= 0.1f) "∞ Infini" else String.format(Locale.US, "%.1f dioptrie", focusDistance),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = CameraCyanAccent,
                    modifier = Modifier.width(90.dp)
                )
                Slider(
                    value = focusDistance,
                    onValueChange = { onDistanceChanged(it) },
                    valueRange = 0.0f..10.0f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = CameraCyanAccent,
                        activeTrackColor = CameraCyanAccent,
                        inactiveTrackColor = CameraDarkSurface
                    )
                )
            }
        }
    }
}
