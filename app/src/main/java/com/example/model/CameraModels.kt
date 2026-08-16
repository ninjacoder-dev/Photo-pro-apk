package com.example.model

enum class ResolutionMode(
    val label: String,
    val subtitle: String,
    val mpValue: Float,
    val targetWidth: Int,
    val targetHeight: Int,
    val description: String
) {
    MP_12(
        label = "12 MP",
        subtitle = "Pixel Binning Standard",
        mpValue = 12.2f,
        targetWidth = 4032,
        targetHeight = 3024,
        description = "Traitement rapide, plage dynamique étendue et excellente sensibilité en basse lumière."
    ),
    MP_50(
        label = "50 MP",
        subtitle = "Plein Capteur Ultra-HD",
        mpValue = 50.0f,
        targetWidth = 8160,
        targetHeight = 6120,
        description = "Résolution maximale du capteur pour un niveau de détail et un recadrage extrêmes."
    )
}

enum class ActiveParamTab(val label: String, val unitSuffix: String = "") {
    NONE("Auto"),
    ISO("ISO"),
    SHUTTER("Vitesse"),
    WB("WB"),
    EV("EV"),
    FOCUS("Focus"),
    ZOOM("Zoom"),
    RESOLUTION("Résolution")
}

enum class WhiteBalancePreset(val label: String, val kelvin: Int, val iconName: String) {
    AUTO("Auto", 0, "auto_mode"),
    INCANDESCENT("Incandescent", 2800, "lightbulb"),
    FLUORESCENT("Fluorescent", 4000, "fluorescent"),
    DAYLIGHT("Lumière du jour", 5500, "wb_sunny"),
    CLOUDY("Nuageux", 6500, "cloud"),
    SHADE("Ombre", 7500, "wb_twilight"),
    CUSTOM("Kelvin", 5000, "thermostat")
}

enum class FocusModeSetting(val label: String, val description: String) {
    AF_CONTINUOUS("AF-C", "Autofocus continu automatique"),
    AF_SINGLE("AF-S", "Autofocus ponctuel au toucher"),
    MANUAL("MF", "Mise au point manuelle de macro à l'infini")
}

enum class FlashModeSetting(val label: String, val iconName: String) {
    OFF("Désactivé", "flash_off"),
    AUTO("Auto", "flash_auto"),
    ON("Activé", "flash_on"),
    TORCH("Torche", "highlight")
}

enum class AspectRatioSetting(val label: String, val ratioValue: Float) {
    RATIO_4_3("4:3", 4f / 3f),
    RATIO_16_9("16:9", 16f / 9f),
    RATIO_1_1("1:1", 1f),
    RATIO_FULL("Plein", 19.5f / 9f)
}

enum class GridGuideSetting(val label: String) {
    OFF("Aucune"),
    RULE_OF_THIRDS("3x3 Tiers"),
    GOLDEN_RATIO("Nombre d'Or"),
    HORIZON_LEVEL("Niveau Horizon")
}

data class ZoomCapabilities(
    val currentZoom: Float = 1.0f,
    val minZoom: Float = 0.5f,
    val maxOpticalZoom: Float = 3.0f,
    val maxDigitalZoom: Float = 30.0f,
    val hasPhysicalUltraWide: Boolean = true,
    val hasPhysicalTelephoto: Boolean = true,
    val physicalLenses: List<Float> = listOf(0.5f, 1.0f, 2.0f, 3.0f, 5.0f)
) {
    val opticalZoomComponent: Float
        get() = if (currentZoom <= maxOpticalZoom) currentZoom else maxOpticalZoom

    val digitalZoomComponent: Float
        get() = if (currentZoom <= maxOpticalZoom) 1.0f else (currentZoom / maxOpticalZoom)

    val maxTotalZoom: Float
        get() = maxOpticalZoom * (maxDigitalZoom / maxOpticalZoom)
}

data class CapturedPhoto(
    val id: String,
    val filePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val resolutionMode: ResolutionMode,
    val width: Int,
    val height: Int,
    val megapixels: Float,
    val iso: String,
    val shutterSpeed: String,
    val whiteBalance: String,
    val totalZoom: Float,
    val opticalZoom: Float,
    val digitalZoom: Float,
    val focalLengthMm: Float = 24.0f,
    val aperture: String = "f/1.8",
    val fileSizeBytes: Long = 0L,
    val isRawSimulated: Boolean = false
)

data class ShutterSpeedOption(val display: String, val nanoseconds: Long) {
    companion object {
        val ALL = listOf(
            ShutterSpeedOption("Auto", 0L),
            ShutterSpeedOption("1/8000s", 125_000L),
            ShutterSpeedOption("1/4000s", 250_000L),
            ShutterSpeedOption("1/2000s", 500_000L),
            ShutterSpeedOption("1/1000s", 1_000_000L),
            ShutterSpeedOption("1/500s", 2_000_000L),
            ShutterSpeedOption("1/250s", 4_000_000L),
            ShutterSpeedOption("1/125s", 8_000_000L),
            ShutterSpeedOption("1/60s", 16_666_667L),
            ShutterSpeedOption("1/30s", 33_333_333L),
            ShutterSpeedOption("1/15s", 66_666_667L),
            ShutterSpeedOption("1/8s", 125_000_000L),
            ShutterSpeedOption("1/4s", 250_000_000L),
            ShutterSpeedOption("1/2s", 500_000_000L),
            ShutterSpeedOption("1s", 1_000_000_000L),
            ShutterSpeedOption("2s", 2_000_000_000L),
            ShutterSpeedOption("4s", 4_000_000_000L),
            ShutterSpeedOption("8s", 8_000_000_000L),
            ShutterSpeedOption("15s", 15_000_000_000L),
            ShutterSpeedOption("20s", 20_000_000_000L),
            ShutterSpeedOption("30s", 30_000_000_000L)
        )
    }
}
