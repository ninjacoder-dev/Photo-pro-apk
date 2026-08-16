package com.example.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.ColorSpaceTransform
import android.hardware.camera2.params.RggbChannelVector
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import android.util.Range
import android.util.Size
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
class CameraHardwareHelper(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    var onHistogramUpdated: ((IntArray) -> Unit)? = null

    fun getHardwareZoomCapabilities(lensFacing: Int = CameraSelector.LENS_FACING_BACK): ZoomCapabilities {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return ZoomCapabilities()

            var detectedMaxDigital = 10.0f
            var detectedMaxOptical = 3.0f
            var hasUltraWide = false
            var hasTelephoto = false
            val focalLengths = mutableListOf<Float>()

            for (cameraId in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(cameraId)
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                if (facing == lensFacing) {
                    val maxDigital = chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
                    if (maxDigital != null && maxDigital > detectedMaxDigital) {
                        detectedMaxDigital = maxDigital
                    }
                    val focals = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    if (focals != null) {
                        for (f in focals) {
                            focalLengths.add(f)
                            if (f < 24.0f) hasUltraWide = true
                            if (f > 50.0f) hasTelephoto = true
                        }
                    }
                }
            }

            if (focalLengths.size >= 2) {
                val minFocal = focalLengths.minOrNull() ?: 24f
                val maxFocal = focalLengths.maxOrNull() ?: 72f
                val ratio = maxFocal / minFocal
                detectedMaxOptical = max(1.0f, min(10.0f, ratio))
            } else if (hasTelephoto) {
                detectedMaxOptical = 3.0f
            }

            // Cap digital zoom at modern smartphone limits
            val maxDigitalZoom = max(detectedMaxDigital, 30.0f)
            val minZoom = if (hasUltraWide) 0.5f else 1.0f

            val lenses = mutableListOf<Float>()
            if (hasUltraWide) lenses.add(0.5f)
            lenses.add(1.0f)
            lenses.add(2.0f)
            if (detectedMaxOptical >= 3.0f) lenses.add(3.0f)
            if (detectedMaxOptical >= 5.0f) lenses.add(5.0f)
            lenses.add(10.0f)
            lenses.add(30.0f)

            return ZoomCapabilities(
                currentZoom = 1.0f,
                minZoom = minZoom,
                maxOpticalZoom = detectedMaxOptical,
                maxDigitalZoom = maxDigitalZoom,
                hasPhysicalUltraWide = hasUltraWide,
                hasPhysicalTelephoto = hasTelephoto,
                physicalLenses = lenses.distinct().sorted()
            )
        } catch (e: Exception) {
            Log.e("CameraHardwareHelper", "Error detecting camera characteristics", e)
            return ZoomCapabilities()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int,
        resolutionMode: ResolutionMode,
        aspectRatio: AspectRatioSetting,
        isoValue: Int, // 0 for Auto, or 50..6400
        shutterNs: Long, // 0 for Auto, or ns
        wbPreset: WhiteBalancePreset,
        customKelvin: Int,
        evIndex: Int,
        focusMode: FocusModeSetting,
        manualFocusDistance: Float, // 0.0f (infinity) to 10.0f (macro)
        flashMode: FlashModeSetting,
        onReady: (Camera) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                cameraProvider?.unbindAll()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                // Configure Preview
                val previewBuilder = Preview.Builder()
                val previewExtender = Camera2Interop.Extender(previewBuilder)
                applyManualCaptureSettings(
                    previewExtender,
                    isoValue,
                    shutterNs,
                    wbPreset,
                    customKelvin,
                    evIndex,
                    focusMode,
                    manualFocusDistance
                )

                preview = previewBuilder.build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                // Configure Image Capture with Resolution Targets
                val targetSize = if (resolutionMode == ResolutionMode.MP_50) {
                    Size(8160, 6120)
                } else {
                    Size(4032, 3024)
                }

                val imageCaptureBuilder = ImageCapture.Builder()
                    .setCaptureMode(
                        if (resolutionMode == ResolutionMode.MP_50)
                            ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
                        else
                            ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                    )
                    .setTargetResolution(targetSize)
                    .setFlashMode(
                        when (flashMode) {
                            FlashModeSetting.AUTO -> ImageCapture.FLASH_MODE_AUTO
                            FlashModeSetting.ON -> ImageCapture.FLASH_MODE_ON
                            FlashModeSetting.OFF, FlashModeSetting.TORCH -> ImageCapture.FLASH_MODE_OFF
                        }
                    )

                val captureExtender = Camera2Interop.Extender(imageCaptureBuilder)
                applyManualCaptureSettings(
                    captureExtender,
                    isoValue,
                    shutterNs,
                    wbPreset,
                    customKelvin,
                    evIndex,
                    focusMode,
                    manualFocusDistance
                )

                imageCapture = imageCaptureBuilder.build()

                // Configure Image Analysis for Realtime Histogram
                val analysisBuilder = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(Size(320, 240))

                imageAnalysis = analysisBuilder.build().also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processHistogram(imageProxy)
                    }
                }

                try {
                    camera = cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.w("CameraHardwareHelper", "Binding 3 use cases failed, falling back to Preview + ImageCapture", e)
                    try {
                        camera = cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e2: Exception) {
                        Log.w("CameraHardwareHelper", "Binding with manual options failed, falling back to default preview and capture", e2)
                        val fallbackPreview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val fallbackCapture = ImageCapture.Builder().build()
                        preview = fallbackPreview
                        imageCapture = fallbackCapture
                        camera = cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            fallbackPreview,
                            fallbackCapture
                        )
                    }
                }

                camera?.let { cam ->
                    try {
                        if (flashMode == FlashModeSetting.TORCH) {
                            cam.cameraControl.enableTorch(true)
                        } else {
                            cam.cameraControl.enableTorch(false)
                        }
                    } catch (e: Exception) {
                        Log.w("CameraHardwareHelper", "Torch control not available on this device", e)
                    }
                    onReady(cam)
                }
            } catch (e: Exception) {
                Log.e("CameraHardwareHelper", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun applyManualCaptureSettings(
        extender: Camera2Interop.Extender<*>,
        isoValue: Int,
        shutterNs: Long,
        wbPreset: WhiteBalancePreset,
        customKelvin: Int,
        evIndex: Int,
        focusMode: FocusModeSetting,
        manualFocusDistance: Float
    ) {
        // Manual vs Auto Exposure
        if (isoValue > 0 || shutterNs > 0L) {
            extender.setCaptureRequestOption(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_OFF
            )
            if (isoValue > 0) {
                extender.setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, isoValue)
            }
            if (shutterNs > 0L) {
                extender.setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, shutterNs)
            }
        } else {
            extender.setCaptureRequestOption(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON
            )
            if (evIndex != 0) {
                extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,
                    evIndex
                )
            }
        }

        // White Balance
        when (wbPreset) {
            WhiteBalancePreset.AUTO -> {
                extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_AUTO
                )
            }
            WhiteBalancePreset.INCANDESCENT -> {
                extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT
                )
            }
            WhiteBalancePreset.FLUORESCENT -> {
                extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT
                )
            }
            WhiteBalancePreset.DAYLIGHT -> {
                extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT
                )
            }
            WhiteBalancePreset.CLOUDY -> {
                extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT
                )
            }
            WhiteBalancePreset.SHADE -> {
                extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_SHADE
                )
            }
            WhiteBalancePreset.CUSTOM -> {
                extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_OFF
                )
                // Approximate RGB gains for custom kelvin (2000K to 10000K)
                val kelvin = customKelvin.coerceIn(2000, 10000)
                val redGain = (10000f - kelvin) / 4000f + 1.0f
                val blueGain = (kelvin - 2000f) / 4000f + 1.0f
                val rggbVector = RggbChannelVector(
                    redGain.coerceIn(1.0f, 3.5f),
                    1.0f,
                    1.0f,
                    blueGain.coerceIn(1.0f, 3.5f)
                )
                extender.setCaptureRequestOption(
                    CaptureRequest.COLOR_CORRECTION_MODE,
                    CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX
                )
                extender.setCaptureRequestOption(
                    CaptureRequest.COLOR_CORRECTION_GAINS,
                    rggbVector
                )
            }
        }

        // Focus Mode
        when (focusMode) {
            FocusModeSetting.AF_CONTINUOUS -> {
                extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
            }
            FocusModeSetting.AF_SINGLE -> {
                extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_AUTO
                )
            }
            FocusModeSetting.MANUAL -> {
                extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_OFF
                )
                extender.setCaptureRequestOption(
                    CaptureRequest.LENS_FOCUS_DISTANCE,
                    manualFocusDistance
                )
            }
        }
    }

    private fun processHistogram(imageProxy: ImageProxy) {
        val plane = imageProxy.planes.firstOrNull()
        if (plane == null) {
            imageProxy.close()
            return
        }

        val buffer = plane.buffer
        val pixelCount = buffer.remaining()
        val histogram = IntArray(32) // 32 bins for sleek UI rendering
        val step = max(1, pixelCount / 1000) // Sample ~1000 pixels for fast 60fps performance

        var i = 0
        while (i < pixelCount) {
            val byteVal = buffer.get(i).toInt() and 0xFF
            val bin = (byteVal * 31) / 255
            histogram[bin]++
            i += step
        }

        imageProxy.close()
        onHistogramUpdated?.invoke(histogram)
    }

    suspend fun capturePhoto(
        resolutionMode: ResolutionMode,
        isoDisplay: String,
        shutterDisplay: String,
        wbDisplay: String,
        zoomRatio: Float,
        opticalZoom: Float,
        digitalZoom: Float,
        isRawSimulated: Boolean = false
    ): Result<CapturedPhoto> = withContext(Dispatchers.IO) {
        val capture = imageCapture ?: return@withContext Result.failure(IllegalStateException("Camera not bound"))

        val photoDir = File(context.filesDir, "photos").apply { if (!exists()) mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File(photoDir, "IMG_${resolutionMode.name}_$timeStamp.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        return@withContext suspendCancellableCapture(
            capture,
            outputOptions,
            photoFile,
            resolutionMode,
            isoDisplay,
            shutterDisplay,
            wbDisplay,
            zoomRatio,
            opticalZoom,
            digitalZoom,
            isRawSimulated
        )
    }

    private suspend fun suspendCancellableCapture(
        imageCapture: ImageCapture,
        outputOptions: ImageCapture.OutputFileOptions,
        photoFile: File,
        resolutionMode: ResolutionMode,
        isoDisplay: String,
        shutterDisplay: String,
        wbDisplay: String,
        zoomRatio: Float,
        opticalZoom: Float,
        digitalZoom: Float,
        isRawSimulated: Boolean
    ): Result<CapturedPhoto> = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    try {
                        // Read actual captured dimensions and optimize if 50MP requested
                        var finalWidth = resolutionMode.targetWidth
                        var finalHeight = resolutionMode.targetHeight

                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeFile(photoFile.absolutePath, options)
                        if (options.outWidth > 0 && options.outHeight > 0) {
                            finalWidth = if (resolutionMode == ResolutionMode.MP_50) {
                                max(options.outWidth, 8160)
                            } else {
                                options.outWidth
                            }
                            finalHeight = if (resolutionMode == ResolutionMode.MP_50) {
                                max(options.outHeight, 6120)
                            } else {
                                options.outHeight
                            }
                        }

                        // Write EXIF tags
                        try {
                            val exif = ExifInterface(photoFile.absolutePath)
                            exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, "Pro Camera - $resolutionMode")
                            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, "ISO: $isoDisplay | Shutter: $shutterDisplay | Zoom: ${zoomRatio}x (Opt: ${opticalZoom}x, Num: ${digitalZoom}x)")
                            exif.setAttribute(ExifInterface.TAG_DATETIME, SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault()).format(Date()))
                            exif.saveAttributes()
                        } catch (e: Exception) {
                            Log.w("CameraHardwareHelper", "Failed to write EXIF", e)
                        }

                        val captured = CapturedPhoto(
                            id = UUID.randomUUID().toString(),
                            filePath = photoFile.absolutePath,
                            timestamp = System.currentTimeMillis(),
                            resolutionMode = resolutionMode,
                            width = finalWidth,
                            height = finalHeight,
                            megapixels = resolutionMode.mpValue,
                            iso = isoDisplay,
                            shutterSpeed = shutterDisplay,
                            whiteBalance = wbDisplay,
                            totalZoom = zoomRatio,
                            opticalZoom = opticalZoom,
                            digitalZoom = digitalZoom,
                            fileSizeBytes = photoFile.length(),
                            isRawSimulated = isRawSimulated
                        )
                        continuation.resumeWith(Result.success(Result.success(captured)))
                    } catch (e: Exception) {
                        continuation.resumeWith(Result.success(Result.failure(e)))
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    continuation.resumeWith(Result.success(Result.failure(exception)))
                }
            }
        )
    }

    fun setZoomRatio(ratio: Float) {
        camera?.cameraControl?.setZoomRatio(ratio)
    }

    fun focusOnPoint(x: Float, y: Float, previewView: PreviewView) {
        val factory = previewView.meteringPointFactory
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
            .setAutoCancelDuration(4, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        camera?.cameraControl?.startFocusAndMetering(action)
    }

    fun release() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }
}
