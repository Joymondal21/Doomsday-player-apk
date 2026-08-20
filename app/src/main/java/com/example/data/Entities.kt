package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey val videoUri: String,
    val title: String,
    val lastPositionMs: Long,
    val durationMs: Long,
    val resolution: String,
    val codec: String,
    val isHdr: Boolean,
    val thumbnailUri: String?,
    val lastPlayedTimestamp: Long = System.currentTimeMillis(),
    val completed: Boolean = false,
    val engineUsed: String = "libmpv-rx"
)

@Entity(tableName = "video_bookmarks")
data class VideoBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoUri: String,
    val title: String,
    val positionMs: Long,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val renderEngine: String = "LIB_MPV_RX",
    val gpuApi: String = "VULKAN_1_3",
    val performanceMode: String = "GPU_HQ_MPVRX",
    val deviceProfile: String = "AQUOS_R6_SD888",
    val hdrOutputEnabled: Boolean = true,
    val dolbyVisionToneMap: Boolean = true,
    val audioMode: String = "DOLBY_ATMOS",
    val ambientModeEnabled: Boolean = true,
    val subtitleBackgroundTransparent: Boolean = true,
    val subtitleFontSizeSp: Float = 18f,
    val autoPlayNext: Boolean = true,
    val doubleTapSkipSeconds: Int = 10,
    val showDiagnosticHud: Boolean = true
)
