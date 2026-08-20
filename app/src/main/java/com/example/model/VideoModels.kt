package com.example.model

import android.net.Uri

enum class RenderEngine(val displayName: String, val description: String, val tag: String) {
    EXO_PLAYER("ExoPlayer 2.19", "Media3 Low-latency Hardware Accelerated Pipeline", "EXO"),
    LIB_VLC("libVLC Engine", "VLC Universal Direct Demuxer & Codec Suite", "VLC"),
    LIB_MPV_RX("libmpv-rx (Vulkan)", "Extreme GPU Custom Shader & Tone-Mapping Engine", "MPV-RX")
}

enum class GpuApi(val displayName: String, val spec: String) {
    VULKAN_1_3("Vulkan 1.3 API", "Direct Vulkan GPU Compute Pipeline (HDR10/Dolby Vision)"),
    OPENGL_ES_3_2("OpenGL ES 3.2", "High compatibility GLES Shader Program"),
    DIRECT_GPU("Direct GPU Surface", "Zero-copy Android Native Window Hardware Buffer")
}

enum class PerformanceMode(val displayName: String, val detail: String, val targetFps: Int) {
    POWER_SAVING("Power Saving", "Eco Mode 60Hz • Battery Conserving Decoder", 60),
    BALANCE("Balance", "Adaptive Dynamic Buffer • Optimal Thermal Profile", 90),
    GPU_HQ_MPVRX("GPU HQ (mpv-rx)", "Extreme Vulkan HDR • 120/240Hz Sharp Pro IGZO Uncapped", 120)
}

enum class DeviceProfile(val displayName: String, val hardwareSpec: String) {
    AQUOS_R6_SD888("Sharp AQUOS R6", "Snapdragon 888 5G • 12GB LPDDR5 • Pro IGZO OLED"),
    SD888_GENERIC("Snapdragon 888 Flagship", "Adreno 660 GPU • Vulkan Direct • High-Bandwidth"),
    GENERIC_HARDWARE("Universal GPU Profile", "Adaptive Hardware Codec Acceleration")
}

enum class AudioMode(val displayName: String, val detail: String) {
    DOLBY_ATMOS("Dolby Atmos Spatial", "Multichannel Object-based 3D Surround"),
    STEREO_ENHANCED("Stereo Speakers 3D", "Enhanced Stereo Separation & Virtualization"),
    HI_RES_PASSTHROUGH("Hi-Res Passthrough", "Bit-perfect Direct Audio Stream to DAC"),
    VOCAL_BOOST("Vocal Clarity Plus", "Dialog Enhancement & Background Leveling"),
    BASS_BEAST("Bass Cinema Beast", "Deep Sub-bass Harmonic Synthesis")
}

enum class AspectRatioMode(val displayName: String, val shortName: String) {
    FIT("Fit to Screen", "FIT"),
    FILL("Fill / Stretch (6.6\")", "FILL"),
    ZOOM("Crop Full Screen (6.6\")", "CROP"),
    PHONE_20_9("Phone 6.6\" (20:9)", "6.6\""),
    CINEMA_21_9("Cinema 21:9", "21:9"),
    RATIO_16_9("Standard 16:9", "16:9"),
    RATIO_4_3("Classic 4:3", "4:3"),
    ORIGINAL("100% Original", "100%"),
    CUSTOM_MANUAL("Manual Zoom & Pan", "MANUAL")
}

enum class MediaViewMode(val displayName: String) {
    TREE("Tree"),
    FOLDERS("Folders"),
    VIDEOS("Videos")
}

enum class MediaLayoutMode(val displayName: String) {
    LIST("List"),
    GRID("Grid")
}

enum class SortField(val displayName: String, val iconCode: String) {
    TITLE("Title", "T"),
    DURATION("Duration", "⏱"),
    DATE("Date", "📅"),
    SIZE("Size", "💾"),
    LOCATION("Location", "📍")
}

enum class SortOrder(val displayName: String) {
    ASCENDING("Ascending (A-Z)"),
    DESCENDING("Descending (Z-A)")
}

data class DisplayFieldsConfig(
    val showDuration: Boolean = true,
    val showFolderDuration: Boolean = true,
    val showExtension: Boolean = true,
    val showPath: Boolean = true,
    val showPlayedProgress: Boolean = true,
    val showResolution: Boolean = true,
    val showSize: Boolean = true,
    val showThumbnail: Boolean = true
)

data class QuickSettingsState(
    val mediaViewMode: MediaViewMode = MediaViewMode.FOLDERS,
    val mediaLayoutMode: MediaLayoutMode = MediaLayoutMode.LIST,
    val sortField: SortField = SortField.TITLE,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val displayFields: DisplayFieldsConfig = DisplayFieldsConfig()
)

enum class DecoderMode(val displayName: String, val badge: String, val description: String) {
    HW_PLUS("HW+ (Multi-Thread)", "HW+", "MediaCodec Multi-Threaded Low Latency Hardware Decoder"),
    HW("HW (MediaCodec)", "HW", "Direct GPU MediaCodec Hardware Decoder"),
    SW("SW (Software CPU)", "SW", "High-Compatibility Software / FFmpeg Core Decoder"),
    VULKAN_DIRECT("Vulkan Direct", "VULKAN", "Zero-Copy libmpv-rx Vulkan 1.3 Direct Surface")
}

enum class ScreenOrientationMode(val displayName: String, val shortLabel: String) {
    SENSOR("Auto Rotate", "AUTO"),
    PORTRAIT("Portrait", "PORTRAIT"),
    LANDSCAPE("Landscape", "LANDSCAPE"),
    REVERSE_LANDSCAPE("Reverse Land", "REV-LAND")
}

data class VideoFolder(
    val folderName: String,
    val folderPath: String,
    val videos: List<VideoItem>,
    val totalSizeBytes: Long = 0L,
    val totalDurationMs: Long = 0L,
    val firstThumbnailUri: String? = null
) {
    val totalSizeFormatted: String
        get() {
            val mb = totalSizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1024) {
                String.format("%.2f GB", mb / 1024.0)
            } else {
                String.format("%.1f MB", mb)
            }
        }

    val totalDurationFormatted: String
        get() {
            val totalSeconds = totalDurationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
}

data class VideoItem(
    val id: Long,
    val uri: String,
    val title: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val path: String,
    val resolution: String = "1080p",
    val mimeType: String = "video/mp4",
    val dateModified: Long = System.currentTimeMillis(),
    val isHdr: Boolean = false,
    val isDolbyVision: Boolean = false,
    val codec: String = "HEVC / H.265",
    val fps: Float = 60.0f,
    val thumbnailUri: String? = null
) {
    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    val sizeFormatted: String
        get() {
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1024) {
                String.format("%.2f GB", mb / 1024.0)
            } else {
                String.format("%.1f MB", mb)
            }
        }
}

data class SubtitleTrack(
    val id: String,
    val name: String,
    val language: String,
    val uri: String? = null,
    val isExternal: Boolean = false
)

data class AudioTrackInfo(
    val id: String,
    val name: String,
    val language: String,
    val channels: String = "Stereo 2.0",
    val sampleRate: String = "48 kHz",
    val codec: String = "AC3 / EAC3 / AAC",
    val isExternal: Boolean = false,
    val groupIndex: Int = -1,
    val trackIndex: Int = -1
)

data class DiagnosticTelemetry(
    val fps: Float = 60.0f,
    val droppedFrames: Int = 0,
    val totalFrames: Long = 0L,
    val currentBitrateMbps: Float = 18.5f,
    val codec: String = "HEVC Main10",
    val resolution: String = "3840x2160 (4K)",
    val colorSpace: String = "BT.2020 / HDR10 PQ",
    val gpuRenderer: String = "Adreno 660 (Vulkan 1.3)",
    val appRamUsageMb: Float = 142.4f,
    val systemRamUsageGb: Float = 4.2f,
    val totalRamGb: Float = 12.0f,
    val ramType: String = "LPDDR5 6400MHz",
    val bufferHealthPercent: Int = 98,
    val audioFormat: String = "Dolby Atmos 7.1 (E-AC3-JOC)",
    val audioSampleRate: String = "48,000 Hz / 24-bit",
    val gpuLoadPercent: Int = 38,
    val temperatureCelsius: Float = 36.5f
)

data class PlayerSettings(
    val renderEngine: RenderEngine = RenderEngine.LIB_MPV_RX,
    val gpuApi: GpuApi = GpuApi.VULKAN_1_3,
    val performanceMode: PerformanceMode = PerformanceMode.GPU_HQ_MPVRX,
    val deviceProfile: DeviceProfile = DeviceProfile.AQUOS_R6_SD888,
    val hdrOutputEnabled: Boolean = true,
    val dolbyVisionToneMap: Boolean = true,
    val audioMode: AudioMode = AudioMode.DOLBY_ATMOS,
    val ambientModeEnabled: Boolean = true,
    val subtitleBackgroundTransparent: Boolean = true,
    val subtitleFontSizeSp: Float = 18f,
    val subtitleTextColor: Long = 0xFFFFFFFF,
    val subtitleShadowEnabled: Boolean = true,
    val autoPlayNext: Boolean = true,
    val doubleTapSkipSeconds: Int = 10,
    val hardwareDecoderEnabled: Boolean = true,
    val debandFilterEnabled: Boolean = true,
    val anime4kUpscale: Boolean = false,
    val showDiagnosticHud: Boolean = true,
    val decoderMode: DecoderMode = DecoderMode.HW_PLUS,
    val rememberedBrightness: Float = 0.7f,
    val autoBrightnessDisabled: Boolean = true,
    val quickSettings: QuickSettingsState = QuickSettingsState()
)
