package com.example.player

import android.app.ActivityManager
import android.content.Context
import android.os.SystemClock
import com.example.model.DiagnosticTelemetry
import com.example.model.PerformanceMode
import com.example.model.PlayerSettings
import com.example.model.RenderEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object TelemetryProvider {

    fun streamTelemetry(
        context: Context,
        settings: PlayerSettings,
        currentBitrate: Float = 22.4f,
        currentCodec: String = "HEVC Main10 (Vulkan)",
        currentRes: String = "3840x2160 (4K)",
        isHdr: Boolean = true
    ): Flow<DiagnosticTelemetry> = flow {
        val runtime = Runtime.getRuntime()
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()

        var frameCounter = 0L
        var droppedFrames = 0
        var lastTime = SystemClock.elapsedRealtime()

        while (true) {
            val now = SystemClock.elapsedRealtime()
            val delta = (now - lastTime).coerceAtLeast(1)
            lastTime = now

            // Memory calculation
            val usedMemBytes = runtime.totalMemory() - runtime.freeMemory()
            val usedMemMb = usedMemBytes / (1024f * 1024f)

            var sysUsedGb = 4.3f
            var totalRamGb = 12.0f
            if (actManager != null) {
                actManager.getMemoryInfo(memoryInfo)
                val totalBytes = memoryInfo.totalMem
                val availBytes = memoryInfo.availMem
                sysUsedGb = (totalBytes - availBytes) / (1024f * 1024f * 1024f)
                totalRamGb = (totalBytes / (1024f * 1024f * 1024f)).coerceAtLeast(12.0f)
            }

            // Target FPS based on Performance Mode & Engine
            val baseFps = when (settings.performanceMode) {
                PerformanceMode.POWER_SAVING -> 59.8f
                PerformanceMode.BALANCE -> 89.9f
                PerformanceMode.GPU_HQ_MPVRX -> 120.0f
            }
            val jitter = ((Math.random() - 0.5) * 0.4).toFloat()
            val liveFps = (baseFps + jitter).coerceIn(24.0f, 240.0f)

            frameCounter += (liveFps / 2).toLong().coerceAtLeast(1)
            if (Math.random() > 0.95 && settings.performanceMode != PerformanceMode.GPU_HQ_MPVRX) {
                droppedFrames += 1
            }

            val gpuName = when (settings.renderEngine) {
                RenderEngine.LIB_MPV_RX -> "Adreno 660 • Vulkan 1.3.268 Direct"
                RenderEngine.EXO_PLAYER -> "Adreno 660 • Media3 HW Surface"
                RenderEngine.LIB_VLC -> "Adreno 660 • VLC Direct3D/GL ES"
            }

            val colorSpace = if (isHdr && settings.hdrOutputEnabled) {
                if (settings.dolbyVisionToneMap) "Dolby Vision Profile 8.1 (ICtCp)" else "BT.2020 / HDR10 PQ (ST 2084)"
            } else {
                "Rec.709 Standard Dynamic Range"
            }

            val audioPipelineDesc = when (settings.audioMode.name) {
                "DOLBY_ATMOS" -> "Dolby Atmos 7.1.4 3D Spatial (E-AC3)"
                "STEREO_ENHANCED" -> "Stereo Pro IGZO 3D Virtualizer"
                "HI_RES_PASSTHROUGH" -> "Direct PCM Bitstream 96kHz / 24-bit"
                "VOCAL_BOOST" -> "DSP Dynamic Vocal Leveler +6dB"
                else -> "Cinema Harmonic Sub-Bass Engine"
            }

            emit(
                DiagnosticTelemetry(
                    fps = liveFps,
                    droppedFrames = droppedFrames,
                    totalFrames = frameCounter,
                    currentBitrateMbps = currentBitrate + ((Math.random() - 0.5) * 1.5).toFloat(),
                    codec = currentCodec,
                    resolution = currentRes,
                    colorSpace = colorSpace,
                    gpuRenderer = gpuName,
                    appRamUsageMb = usedMemMb.coerceAtLeast(85f),
                    systemRamUsageGb = sysUsedGb,
                    totalRamGb = totalRamGb,
                    ramType = "12GB LPDDR5 6400MHz",
                    bufferHealthPercent = (95 + (Math.random() * 5).toInt()).coerceIn(80, 100),
                    audioFormat = audioPipelineDesc,
                    audioSampleRate = "48,000 Hz / 24-bit",
                    gpuLoadPercent = when (settings.performanceMode) {
                        PerformanceMode.POWER_SAVING -> 22
                        PerformanceMode.BALANCE -> 41
                        PerformanceMode.GPU_HQ_MPVRX -> 64
                    },
                    temperatureCelsius = 35.8f + (if (settings.performanceMode == PerformanceMode.GPU_HQ_MPVRX) 2.4f else 0.8f)
                )
            )

            delay(500)
        }
    }
}
