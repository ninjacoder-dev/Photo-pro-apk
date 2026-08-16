package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.camera.CameraHardwareHelper
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.atan2
import kotlin.math.roundToInt

data class CameraUiState(
    val hasCameraPermission: Boolean = false,
    val activeParamTab: ActiveParamTab = ActiveParamTab.NONE,
    
    // Pro Exposure Parameters
    val selectedIsoIndex: Int = 0, // 0 = Auto, 1=50, 2=100, 3=200, 4=400, 5=800, 6=1600, 7=3200, 8=6400
    val selectedShutterIndex: Int = 0, // 0 = Auto, index in ShutterSpeedOption.ALL
    val selectedWbPreset: WhiteBalancePreset = WhiteBalancePreset.AUTO,
    val customKelvin: Int = 5500, // 2000K to 10000K
    val evStep: Int = 0, // -6 to +6 (i.e. -2.0 EV to +2.0 EV)
    val focusMode: FocusModeSetting = FocusModeSetting.AF_CONTINUOUS,
    val manualFocusDistance: Float = 0.0f, // 0.0f (Infini) to 10.0f (Macro 10cm)
    
    // Resolution Mode: 12MP vs 50MP
    val resolutionMode: ResolutionMode = ResolutionMode.MP_12,
    val isRawModeEnabled: Boolean = false,
    
    // Zoom System (Optical & Digital)
    val zoomCapabilities: ZoomCapabilities = ZoomCapabilities(),
    val currentZoom: Float = 1.0f,
    
    // Auxiliary Controls
    val flashMode: FlashModeSetting = FlashModeSetting.OFF,
    val timerSeconds: Int = 0, // 0 = Off, 3, 5, 10
    val aspectRatio: AspectRatioSetting = AspectRatioSetting.RATIO_4_3,
    val gridGuide: GridGuideSetting = GridGuideSetting.RULE_OF_THIRDS,
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    
    // Live Viewfinder telemetry
    val histogramBins: IntArray = IntArray(32),
    val horizonTiltDegrees: Float = 0f,
    val isLevel: Boolean = false,
    
    // Focus Ring animation
    val touchFocusPoint: Pair<Float, Float>? = null,
    
    // Capture state & feedback
    val isCapturing: Boolean = false,
    val countdownRemaining: Int? = null,
    val capturedPhotos: List<CapturedPhoto> = emptyList(),
    val lastCapturedPhoto: CapturedPhoto? = null,
    val activePhotoDetail: CapturedPhoto? = null,
    val showGallerySheet: Boolean = false,
    val showInfoDialog: Boolean = false
)

class ProCameraViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    val cameraHelper = CameraHardwareHelper(application)
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    val isoOptions = listOf("Auto", "50", "100", "200", "400", "800", "1600", "3200", "6400")

    private var timerJob: Job? = null

    init {
        initSensor()
        refreshZoomCapabilities()
        loadExistingPhotos()

        cameraHelper.onHistogramUpdated = { bins ->
            _uiState.update { it.copy(histogramBins = bins) }
        }
    }

    private fun initSensor() {
        val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accel != null) {
            sensorManager?.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
        if (granted) {
            refreshZoomCapabilities()
        }
    }

    fun refreshZoomCapabilities() {
        val caps = cameraHelper.getHardwareZoomCapabilities(_uiState.value.lensFacing)
        _uiState.update {
            it.copy(
                zoomCapabilities = caps,
                currentZoom = caps.currentZoom.coerceIn(caps.minZoom, caps.maxDigitalZoom)
            )
        }
    }

    fun selectParamTab(tab: ActiveParamTab) {
        _uiState.update {
            it.copy(activeParamTab = if (it.activeParamTab == tab) ActiveParamTab.NONE else tab)
        }
        triggerHaptic(VibrationEffect.EFFECT_CLICK)
    }

    fun setIsoIndex(index: Int) {
        _uiState.update { it.copy(selectedIsoIndex = index.coerceIn(0, isoOptions.lastIndex)) }
        triggerHaptic(VibrationEffect.EFFECT_TICK)
    }

    fun setShutterIndex(index: Int) {
        _uiState.update { it.copy(selectedShutterIndex = index.coerceIn(0, ShutterSpeedOption.ALL.lastIndex)) }
        triggerHaptic(VibrationEffect.EFFECT_TICK)
    }

    fun setWbPreset(preset: WhiteBalancePreset) {
        _uiState.update { it.copy(selectedWbPreset = preset) }
        triggerHaptic(VibrationEffect.EFFECT_CLICK)
    }

    fun setCustomKelvin(kelvin: Int) {
        _uiState.update { it.copy(customKelvin = kelvin.coerceIn(2000, 10000)) }
        triggerHaptic(VibrationEffect.EFFECT_TICK)
    }

    fun setEvStep(step: Int) {
        _uiState.update { it.copy(evStep = step.coerceIn(-6, 6)) }
        triggerHaptic(VibrationEffect.EFFECT_TICK)
    }

    fun setFocusMode(mode: FocusModeSetting) {
        _uiState.update { it.copy(focusMode = mode) }
        triggerHaptic(VibrationEffect.EFFECT_CLICK)
    }

    fun setManualFocusDistance(distance: Float) {
        _uiState.update { it.copy(manualFocusDistance = distance.coerceIn(0.0f, 10.0f)) }
        triggerHaptic(VibrationEffect.EFFECT_TICK)
    }

    fun setResolutionMode(mode: ResolutionMode) {
        _uiState.update { it.copy(resolutionMode = mode) }
        triggerHaptic(VibrationEffect.EFFECT_HEAVY_CLICK)
    }

    fun toggleRawMode() {
        _uiState.update { it.copy(isRawModeEnabled = !it.isRawModeEnabled) }
        triggerHaptic(VibrationEffect.EFFECT_CLICK)
    }

    fun setZoom(zoom: Float) {
        val caps = _uiState.value.zoomCapabilities
        val clamped = zoom.coerceIn(caps.minZoom, caps.maxDigitalZoom)
        val rounded = (clamped * 10f).roundToInt() / 10f
        _uiState.update {
            it.copy(
                currentZoom = rounded,
                zoomCapabilities = caps.copy(currentZoom = rounded)
            )
        }
        cameraHelper.setZoomRatio(rounded)
        triggerHaptic(VibrationEffect.EFFECT_TICK)
    }

    fun toggleFlash() {
        val next = when (_uiState.value.flashMode) {
            FlashModeSetting.OFF -> FlashModeSetting.AUTO
            FlashModeSetting.AUTO -> FlashModeSetting.ON
            FlashModeSetting.ON -> FlashModeSetting.TORCH
            FlashModeSetting.TORCH -> FlashModeSetting.OFF
        }
        _uiState.update { it.copy(flashMode = next) }
        triggerHaptic(VibrationEffect.EFFECT_CLICK)
    }

    fun cycleTimer() {
        val next = when (_uiState.value.timerSeconds) {
            0 -> 3
            3 -> 5
            5 -> 10
            else -> 0
        }
        _uiState.update { it.copy(timerSeconds = next) }
        triggerHaptic(VibrationEffect.EFFECT_CLICK)
    }

    fun cycleAspectRatio() {
        val next = when (_uiState.value.aspectRatio) {
            AspectRatioSetting.RATIO_4_3 -> AspectRatioSetting.RATIO_16_9
            AspectRatioSetting.RATIO_16_9 -> AspectRatioSetting.RATIO_1_1
            AspectRatioSetting.RATIO_1_1 -> AspectRatioSetting.RATIO_FULL
            AspectRatioSetting.RATIO_FULL -> AspectRatioSetting.RATIO_4_3
        }
        _uiState.update { it.copy(aspectRatio = next) }
        triggerHaptic(VibrationEffect.EFFECT_CLICK)
    }

    fun cycleGridGuide() {
        val next = when (_uiState.value.gridGuide) {
            GridGuideSetting.OFF -> GridGuideSetting.RULE_OF_THIRDS
            GridGuideSetting.RULE_OF_THIRDS -> GridGuideSetting.GOLDEN_RATIO
            GridGuideSetting.GOLDEN_RATIO -> GridGuideSetting.HORIZON_LEVEL
            GridGuideSetting.HORIZON_LEVEL -> GridGuideSetting.OFF
        }
        _uiState.update { it.copy(gridGuide = next) }
        triggerHaptic(VibrationEffect.EFFECT_CLICK)
    }

    fun toggleLensFacing() {
        val next = if (_uiState.value.lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        _uiState.update { it.copy(lensFacing = next) }
        refreshZoomCapabilities()
        triggerHaptic(VibrationEffect.EFFECT_CLICK)
    }

    fun onTouchFocus(x: Float, y: Float) {
        _uiState.update { it.copy(touchFocusPoint = Pair(x, y)) }
        viewModelScope.launch {
            delay(2000)
            _uiState.update { it.copy(touchFocusPoint = null) }
        }
    }

    fun takePicture() {
        if (_uiState.value.isCapturing) return

        val timer = _uiState.value.timerSeconds
        if (timer > 0) {
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                for (sec in timer downTo 1) {
                    _uiState.update { it.copy(countdownRemaining = sec) }
                    triggerHaptic(VibrationEffect.EFFECT_TICK)
                    delay(1000)
                }
                _uiState.update { it.copy(countdownRemaining = null) }
                executeCapture()
            }
        } else {
            executeCapture()
        }
    }

    private fun executeCapture() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturing = true) }
            try {
                audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK)
            } catch (e: Exception) {
                Log.w("ProCameraViewModel", "Could not play sound effect", e)
            }
            triggerHaptic(VibrationEffect.EFFECT_HEAVY_CLICK)

            val state = _uiState.value
            val isoStr = isoOptions[state.selectedIsoIndex]
            val shutterStr = ShutterSpeedOption.ALL[state.selectedShutterIndex].display
            val wbStr = if (state.selectedWbPreset == WhiteBalancePreset.CUSTOM) {
                "${state.customKelvin}K"
            } else {
                state.selectedWbPreset.label
            }

            val caps = state.zoomCapabilities
            val result = cameraHelper.capturePhoto(
                resolutionMode = state.resolutionMode,
                isoDisplay = isoStr,
                shutterDisplay = shutterStr,
                wbDisplay = wbStr,
                zoomRatio = state.currentZoom,
                opticalZoom = caps.opticalZoomComponent,
                digitalZoom = caps.digitalZoomComponent,
                isRawSimulated = state.isRawModeEnabled
            )

            result.onSuccess { photo ->
                _uiState.update {
                    it.copy(
                        isCapturing = false,
                        lastCapturedPhoto = photo,
                        capturedPhotos = listOf(photo) + it.capturedPhotos
                    )
                }
            }.onFailure { err ->
                Log.e("ProCameraViewModel", "Capture failed", err)
                _uiState.update { it.copy(isCapturing = false) }
            }
        }
    }

    fun openPhotoDetail(photo: CapturedPhoto) {
        _uiState.update { it.copy(activePhotoDetail = photo) }
    }

    fun closePhotoDetail() {
        _uiState.update { it.copy(activePhotoDetail = null) }
    }

    fun setShowGallery(show: Boolean) {
        _uiState.update { it.copy(showGallerySheet = show) }
    }

    fun setShowInfo(show: Boolean) {
        _uiState.update { it.copy(showInfoDialog = show) }
    }

    fun deletePhoto(photo: CapturedPhoto) {
        try {
            val f = File(photo.filePath)
            if (f.exists()) f.delete()
        } catch (e: Exception) {
            Log.e("ProCameraViewModel", "Error deleting photo", e)
        }
        _uiState.update {
            val updated = it.capturedPhotos.filter { p -> p.id != photo.id }
            it.copy(
                capturedPhotos = updated,
                activePhotoDetail = if (it.activePhotoDetail?.id == photo.id) null else it.activePhotoDetail,
                lastCapturedPhoto = updated.firstOrNull()
            )
        }
    }

    private fun loadExistingPhotos() {
        viewModelScope.launch {
            try {
                val photoDir = File(getApplication<Application>().filesDir, "photos")
                if (photoDir.exists()) {
                    val files = photoDir.listFiles { f -> f.extension.equals("jpg", true) }
                        ?.sortedByDescending { it.lastModified() }
                    val loaded = files?.map { f ->
                        val is50 = f.name.contains("MP_50")
                        val res = if (is50) ResolutionMode.MP_50 else ResolutionMode.MP_12
                        CapturedPhoto(
                            id = f.name,
                            filePath = f.absolutePath,
                            timestamp = f.lastModified(),
                            resolutionMode = res,
                            width = res.targetWidth,
                            height = res.targetHeight,
                            megapixels = res.mpValue,
                            iso = "Auto",
                            shutterSpeed = "Auto",
                            whiteBalance = "Auto",
                            totalZoom = 1.0f,
                            opticalZoom = 1.0f,
                            digitalZoom = 1.0f,
                            fileSizeBytes = f.length()
                        )
                    } ?: emptyList()

                    _uiState.update {
                        it.copy(
                            capturedPhotos = loaded,
                            lastCapturedPhoto = loaded.firstOrNull()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ProCameraViewModel", "Error loading existing photos", e)
            }
        }
    }

    private fun triggerHaptic(effectId: Int) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(effectId))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20L)
            }
        } catch (_: Exception) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val angle = Math.toDegrees(atan2(x.toDouble(), y.toDouble())).toFloat()
            val normalized = if (angle > 0) angle - 90 else angle + 90
            val isLevel = kotlin.math.abs(normalized) < 1.0f
            _uiState.update { it.copy(horizonTiltDegrees = normalized, isLevel = isLevel) }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {}
        try {
            cameraHelper.release()
        } catch (_: Exception) {}
    }
}
