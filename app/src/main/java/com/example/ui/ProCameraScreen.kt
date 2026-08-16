package com.example.ui

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.CameraUiState
import com.example.viewmodel.ProCameraViewModel
import java.util.Locale

@Composable
fun ProCameraScreen(
    viewModel: ProCameraViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var activeCameraRef by remember { mutableStateOf<Camera?>(null) }

    // Re-bind camera when core parameters change
    LaunchedEffect(
        uiState.lensFacing,
        uiState.resolutionMode,
        uiState.aspectRatio,
        uiState.selectedIsoIndex,
        uiState.selectedShutterIndex,
        uiState.selectedWbPreset,
        uiState.customKelvin,
        uiState.evStep,
        uiState.focusMode,
        uiState.manualFocusDistance,
        uiState.flashMode,
        previewViewRef
    ) {
        val pv = previewViewRef ?: return@LaunchedEffect
        val isoVal = if (uiState.selectedIsoIndex == 0) 0 else viewModel.isoOptions[uiState.selectedIsoIndex].toIntOrNull() ?: 0
        val shutterNs = ShutterSpeedOption.ALL[uiState.selectedShutterIndex].nanoseconds

        viewModel.cameraHelper.bindCamera(
            lifecycleOwner = lifecycleOwner,
            previewView = pv,
            lensFacing = uiState.lensFacing,
            resolutionMode = uiState.resolutionMode,
            aspectRatio = uiState.aspectRatio,
            isoValue = isoVal,
            shutterNs = shutterNs,
            wbPreset = uiState.selectedWbPreset,
            customKelvin = uiState.customKelvin,
            evIndex = uiState.evStep,
            focusMode = uiState.focusMode,
            manualFocusDistance = uiState.manualFocusDistance,
            flashMode = uiState.flashMode,
            onReady = { cam ->
                activeCameraRef = cam
                cam.cameraControl.setZoomRatio(uiState.currentZoom)
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CameraDarkBackground)
            .testTag("pro_camera_screen")
    ) {
        // Camera Viewfinder Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoomChange, _ ->
                        val newZoom = uiState.currentZoom * zoomChange
                        viewModel.setZoom(newZoom)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val pv = previewViewRef
                        if (pv != null && size.width > 0 && size.height > 0) {
                            val normX = offset.x / size.width.toFloat()
                            val normY = offset.y / size.height.toFloat()
                            viewModel.onTouchFocus(normX, normY)
                            viewModel.cameraHelper.focusOnPoint(offset.x, offset.y, pv)
                        }
                    }
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Grid & Horizon Guides & Focus Box
            ViewfinderOverlay(
                gridGuide = uiState.gridGuide,
                horizonTiltDegrees = uiState.horizonTiltDegrees,
                isLevel = uiState.isLevel,
                touchFocusPoint = uiState.touchFocusPoint
            )
        }

        // Live Histogram HUD Overlay (Top Right)
        LiveHistogramView(
            bins = uiState.histogramBins,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 64.dp, end = 12.dp)
                .width(130.dp)
        )

        // Shutter Flash Animation Overlay
        if (uiState.isCapturing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.85f))
            )
        }

        // Countdown Timer Overlay
        if (uiState.countdownRemaining != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = CameraOverlayDark.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(3.dp, CameraAmberPrimary),
                    modifier = Modifier.size(110.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${uiState.countdownRemaining}",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = CameraAmberPrimary
                            )
                        )
                    }
                }
            }
        }

        // Top Quick Controls (Status Bars Padding)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            TopQuickSettingsBar(
                flashMode = uiState.flashMode,
                onToggleFlash = { viewModel.toggleFlash() },
                timerSeconds = uiState.timerSeconds,
                onCycleTimer = { viewModel.cycleTimer() },
                aspectRatio = uiState.aspectRatio,
                onCycleAspectRatio = { viewModel.cycleAspectRatio() },
                gridGuide = uiState.gridGuide,
                onCycleGrid = { viewModel.cycleGridGuide() },
                resolutionMode = uiState.resolutionMode,
                isRawEnabled = uiState.isRawModeEnabled,
                onResolutionChanged = { viewModel.setResolutionMode(it) },
                onToggleRaw = { viewModel.toggleRawMode() },
                onInfoClicked = { viewModel.setShowInfo(true) }
            )
        }

        // Bottom Controls Container
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Zoom Control Bar (with Max Optical & Max Digital Zoom Badges)
            ZoomControlBar(
                zoomCapabilities = uiState.zoomCapabilities,
                currentZoom = uiState.currentZoom,
                onZoomSelected = { viewModel.setZoom(it) }
            )

            // Pro Parameter Active Adjustment Wheel (ISO, Vitesse, WB Kelvin, EV, MF Distance)
            AnimatedVisibility(
                visible = uiState.activeParamTab != ActiveParamTab.NONE && uiState.activeParamTab != ActiveParamTab.ZOOM,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ProParamControlWheel(
                    activeTab = uiState.activeParamTab,
                    selectedIsoIndex = uiState.selectedIsoIndex,
                    isoOptions = viewModel.isoOptions,
                    onIsoSelected = { viewModel.setIsoIndex(it) },
                    selectedShutterIndex = uiState.selectedShutterIndex,
                    onShutterSelected = { viewModel.setShutterIndex(it) },
                    selectedWbPreset = uiState.selectedWbPreset,
                    customKelvin = uiState.customKelvin,
                    onWbPresetSelected = { viewModel.setWbPreset(it) },
                    onKelvinChanged = { viewModel.setCustomKelvin(it) },
                    evStep = uiState.evStep,
                    onEvStepChanged = { viewModel.setEvStep(it) },
                    focusMode = uiState.focusMode,
                    manualFocusDistance = uiState.manualFocusDistance,
                    onFocusModeChanged = { viewModel.setFocusMode(it) },
                    onManualFocusDistanceChanged = { viewModel.setManualFocusDistance(it) }
                )
            }

            // Bottom Shutter & Tab Navigation Deck
            BottomControlDeck(
                activeTab = uiState.activeParamTab,
                onTabSelected = { viewModel.selectParamTab(it) },
                isoDisplay = viewModel.isoOptions[uiState.selectedIsoIndex],
                shutterDisplay = ShutterSpeedOption.ALL[uiState.selectedShutterIndex].display,
                wbDisplay = if (uiState.selectedWbPreset == WhiteBalancePreset.CUSTOM) "${uiState.customKelvin}K" else uiState.selectedWbPreset.label,
                evDisplay = String.format(Locale.US, "%+.1f", uiState.evStep * 0.33f),
                focusDisplay = uiState.focusMode.label,
                zoomDisplay = String.format(Locale.US, "%.1fx", uiState.currentZoom),
                isCapturing = uiState.isCapturing,
                onCaptureClicked = { viewModel.takePicture() },
                onFlipCamera = { viewModel.toggleLensFacing() },
                lastPhoto = uiState.lastCapturedPhoto,
                onGalleryClicked = { viewModel.setShowGallery(true) }
            )
        }

        // Full Photo Viewer Modal
        if (uiState.activePhotoDetail != null) {
            PhotoViewerDialog(
                photo = uiState.activePhotoDetail!!,
                onDismiss = { viewModel.closePhotoDetail() },
                onDelete = { viewModel.deletePhoto(it) }
            )
        }

        // Photo Gallery Bottom Sheet
        if (uiState.showGallerySheet) {
            GallerySheet(
                photos = uiState.capturedPhotos,
                onPhotoSelected = { viewModel.openPhotoDetail(it) },
                onDismiss = { viewModel.setShowGallery(false) }
            )
        }

        // Info / Guide Dialog
        if (uiState.showInfoDialog) {
            CameraInfoDialog(
                onDismiss = { viewModel.setShowInfo(false) }
            )
        }
    }
}
