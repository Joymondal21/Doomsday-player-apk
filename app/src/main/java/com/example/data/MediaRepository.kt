package com.example.data

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.model.AudioMode
import com.example.model.DeviceProfile
import com.example.model.GpuApi
import com.example.model.PerformanceMode
import com.example.model.PlayerSettings
import com.example.model.RenderEngine
import com.example.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class MediaRepository(private val context: Context, private val dao: AppDao) {

    val playbackHistory: Flow<List<PlaybackHistoryEntity>> = dao.getAllHistory()
    val userSettingsFlow: Flow<PlayerSettings> = dao.getUserSettings().map { entity ->
        if (entity == null) {
            PlayerSettings()
        } else {
            PlayerSettings(
                renderEngine = runCatching { RenderEngine.valueOf(entity.renderEngine) }.getOrDefault(RenderEngine.LIB_MPV_RX),
                gpuApi = runCatching { GpuApi.valueOf(entity.gpuApi) }.getOrDefault(GpuApi.VULKAN_1_3),
                performanceMode = runCatching { PerformanceMode.valueOf(entity.performanceMode) }.getOrDefault(PerformanceMode.GPU_HQ_MPVRX),
                deviceProfile = runCatching { DeviceProfile.valueOf(entity.deviceProfile) }.getOrDefault(DeviceProfile.AQUOS_R6_SD888),
                hdrOutputEnabled = entity.hdrOutputEnabled,
                dolbyVisionToneMap = entity.dolbyVisionToneMap,
                audioMode = runCatching { AudioMode.valueOf(entity.audioMode) }.getOrDefault(AudioMode.DOLBY_ATMOS),
                ambientModeEnabled = entity.ambientModeEnabled,
                subtitleBackgroundTransparent = entity.subtitleBackgroundTransparent,
                subtitleFontSizeSp = entity.subtitleFontSizeSp,
                autoPlayNext = entity.autoPlayNext,
                doubleTapSkipSeconds = entity.doubleTapSkipSeconds,
                showDiagnosticHud = entity.showDiagnosticHud
            )
        }
    }

    suspend fun saveSettings(settings: PlayerSettings) {
        dao.saveUserSettings(
            UserSettingsEntity(
                id = 1,
                renderEngine = settings.renderEngine.name,
                gpuApi = settings.gpuApi.name,
                performanceMode = settings.performanceMode.name,
                deviceProfile = settings.deviceProfile.name,
                hdrOutputEnabled = settings.hdrOutputEnabled,
                dolbyVisionToneMap = settings.dolbyVisionToneMap,
                audioMode = settings.audioMode.name,
                ambientModeEnabled = settings.ambientModeEnabled,
                subtitleBackgroundTransparent = settings.subtitleBackgroundTransparent,
                subtitleFontSizeSp = settings.subtitleFontSizeSp,
                autoPlayNext = settings.autoPlayNext,
                doubleTapSkipSeconds = settings.doubleTapSkipSeconds,
                showDiagnosticHud = settings.showDiagnosticHud
            )
        )
    }

    suspend fun savePlaybackProgress(
        video: VideoItem,
        positionMs: Long,
        completed: Boolean,
        engineUsed: String
    ) {
        dao.insertOrUpdateHistory(
            PlaybackHistoryEntity(
                videoUri = video.uri,
                title = video.title,
                lastPositionMs = positionMs,
                durationMs = video.durationMs,
                resolution = video.resolution,
                codec = video.codec,
                isHdr = video.isHdr,
                thumbnailUri = video.thumbnailUri,
                lastPlayedTimestamp = System.currentTimeMillis(),
                completed = completed,
                engineUsed = engineUsed
            )
        )
    }

    suspend fun getHistoryForUri(uri: String): PlaybackHistoryEntity? {
        return dao.getHistoryForVideo(uri)
    }

    suspend fun deleteHistoryItem(uri: String) {
        dao.deleteHistory(uri)
    }

    suspend fun clearAllHistory() {
        dao.clearAllHistory()
    }

    private val prefs = context.getSharedPreferences("doomsday_media_prefs", Context.MODE_PRIVATE)

    companion object {
        const val AVENGERS_DOOMSDAY_ID = 9000L
        private const val PREF_KEY_CLEAN_UI = "clean_ui_local_only"
        private const val PREF_KEY_HIDDEN_DEMOS = "hidden_demo_ids"
    }

    fun isCleanUiMode(): Boolean {
        return prefs.getBoolean(PREF_KEY_CLEAN_UI, false)
    }

    fun setCleanUiMode(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_CLEAN_UI, enabled).apply()
    }

    private fun getHiddenDemoIds(): Set<String> {
        return prefs.getStringSet(PREF_KEY_HIDDEN_DEMOS, emptySet()) ?: emptySet()
    }

    private fun saveHiddenDemoIds(set: Set<String>) {
        prefs.edit().putStringSet(PREF_KEY_HIDDEN_DEMOS, set).apply()
    }

    fun isDemoVideo(uri: String): Boolean {
        return uri.startsWith("https://commondatastorage.googleapis.com/") || uri.contains("sample")
    }

    fun isAvengersDemoPresent(): Boolean {
        if (isCleanUiMode()) return false
        val hidden = getHiddenDemoIds()
        return !hidden.contains(AVENGERS_DOOMSDAY_ID.toString())
    }

    fun removeDemoVideoById(id: Long) {
        val current = getHiddenDemoIds().toMutableSet()
        current.add(id.toString())
        saveHiddenDemoIds(current)
    }

    fun removeAllDemoVideos() {
        val allDemoIds = getSampleCinemaTrailers().map { it.id.toString() }.toSet()
        val current = getHiddenDemoIds().toMutableSet()
        current.addAll(allDemoIds)
        saveHiddenDemoIds(current)
        setCleanUiMode(true)
    }

    fun addAvengersDoomsdayDemo() {
        val current = getHiddenDemoIds().toMutableSet()
        current.remove(AVENGERS_DOOMSDAY_ID.toString())
        saveHiddenDemoIds(current)
        setCleanUiMode(false)
    }

    fun restoreAllDemoVideos() {
        saveHiddenDemoIds(emptySet())
        setCleanUiMode(false)
    }

    suspend fun scanDeviceVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<VideoItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        try {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                val mimeColumn = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                val dateColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                val widthColumn = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightColumn = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Untitled Video"
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val path = if (dataColumn != -1) cursor.getString(dataColumn) ?: "" else ""
                    val mimeType = if (mimeColumn != -1) cursor.getString(mimeColumn) ?: "video/mp4" else "video/mp4"
                    val dateModified = if (dateColumn != -1) cursor.getLong(dateColumn) * 1000 else System.currentTimeMillis()
                    val width = if (widthColumn != -1) cursor.getInt(widthColumn) else 1920
                    val height = if (heightColumn != -1) cursor.getInt(heightColumn) else 1080

                    val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                    val resolution = if (width > 0 && height > 0) "${width}x${height}" else "1080p"
                    val is4k = width >= 3840 || height >= 2160
                    val isHdr = is4k || name.contains("HDR", ignoreCase = true) || name.contains("10bit", ignoreCase = true)
                    val isDv = name.contains("Dolby", ignoreCase = true) || name.contains("DV", ignoreCase = true)

                    videoList.add(
                        VideoItem(
                            id = id,
                            uri = contentUri.toString(),
                            title = name,
                            durationMs = if (duration > 0) duration else 0L,
                            sizeBytes = size,
                            path = path,
                            resolution = resolution,
                            mimeType = mimeType,
                            dateModified = dateModified,
                            isHdr = isHdr,
                            isDolbyVision = isDv,
                            codec = if (is4k) "HEVC Main10 (HDR10)" else "H.264 High@L4.2",
                            fps = if (is4k) 60.0f else 30.0f,
                            thumbnailUri = contentUri.toString()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Bundle high-quality cinema sample demo clips (filtering out removed ones or if Clean UI mode is on)
        if (isCleanUiMode()) {
            videoList
        } else {
            val hidden = getHiddenDemoIds()
            val sampleCinemas = getSampleCinemaTrailers().filter { !hidden.contains(it.id.toString()) }
            (sampleCinemas + videoList)
        }
    }

    fun getSampleCinemaTrailers(): List<VideoItem> {
        return listOf(
            VideoItem(
                id = AVENGERS_DOOMSDAY_ID,
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                title = "Avengers: Doomsday - Official 4K IMAX Vulkan Direct Teaser",
                durationMs = 480000L,
                sizeBytes = 210000000L,
                path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                resolution = "3840x2160 (4K IMAX Enhanced)",
                mimeType = "video/mp4",
                dateModified = System.currentTimeMillis() + 86400000L,
                isHdr = true,
                isDolbyVision = true,
                codec = "HEVC / H.265 Main 10 (BT.2020 PQ / 60fps)",
                fps = 60.0f,
                thumbnailUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/WeAreGoingOnBullrun.jpg"
            ),
            VideoItem(
                id = 9001L,
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                title = "Doomsday: Ultra 4K HDR60 Cinema Demo",
                durationMs = 596000L,
                sizeBytes = 158000000L,
                path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                resolution = "3840x2160 (4K UHD)",
                mimeType = "video/mp4",
                dateModified = System.currentTimeMillis(),
                isHdr = true,
                isDolbyVision = true,
                codec = "HEVC / H.265 Main 10 (BT.2020 PQ)",
                fps = 60.0f,
                thumbnailUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg"
            ),
            VideoItem(
                id = 9002L,
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                title = "Tears of Steel: Sci-Fi Dolby Atmos & Vulkan Benchmark",
                durationMs = 734000L,
                sizeBytes = 168000000L,
                path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                resolution = "3840x2160 (4K)",
                mimeType = "video/mp4",
                dateModified = System.currentTimeMillis() - 86400000L,
                isHdr = true,
                isDolbyVision = true,
                codec = "AV1 / HEVC Main10 (Dolby Vision)",
                fps = 60.0f,
                thumbnailUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/TearsOfSteel.jpg"
            ),
            VideoItem(
                id = 9003L,
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                title = "Sintel: 4K HDR10 Master Cinema Track",
                durationMs = 888000L,
                sizeBytes = 129000000L,
                path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                resolution = "1920x1080 (1080p 60fps)",
                mimeType = "video/mp4",
                dateModified = System.currentTimeMillis() - 172800000L,
                isHdr = true,
                isDolbyVision = false,
                codec = "H.264 High Profile / 10-Bit Color",
                fps = 60.0f,
                thumbnailUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg"
            ),
            VideoItem(
                id = 9004L,
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                title = "Elephant's Dream: 3D Spatial Audio & IGZO Display Demo",
                durationMs = 653000L,
                sizeBytes = 94000000L,
                path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                resolution = "1920x1080 (FHD)",
                mimeType = "video/mp4",
                dateModified = System.currentTimeMillis() - 259200000L,
                isHdr = false,
                isDolbyVision = false,
                codec = "H.264 / AAC 5.1 Surround",
                fps = 30.0f,
                thumbnailUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg"
            )
        )
    }

    suspend fun deleteVideo(video: VideoItem): Boolean = withContext(Dispatchers.IO) {
        try {
            if (video.id == AVENGERS_DOOMSDAY_ID || video.id in 9000L..9010L) {
                removeDemoVideoById(video.id)
                dao.deleteHistory(video.uri)
                true
            } else if (video.uri.startsWith("content://")) {
                val uri = Uri.parse(video.uri)
                val rows = context.contentResolver.delete(uri, null, null)
                dao.deleteHistory(video.uri)
                rows > 0
            } else if (video.path.isNotEmpty() && !video.path.startsWith("http")) {
                val file = File(video.path)
                if (file.exists()) {
                    file.delete()
                }
                dao.deleteHistory(video.uri)
                true
            } else {
                dao.deleteHistory(video.uri)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareVideo(video: VideoItem) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                if (video.uri.startsWith("content://") || video.uri.startsWith("file://")) {
                    type = video.mimeType
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(video.uri))
                } else {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Watching \"${video.title}\" on Doomsday Player:\n${video.uri}")
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share \"${video.title}\" via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun extractMetadata(uriString: String): Map<String, String> = withContext(Dispatchers.IO) {
        val details = mutableMapOf<String, String>()
        val retriever = MediaMetadataRetriever()
        try {
            if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                retriever.setDataSource(uriString, HashMap())
            } else {
                retriever.setDataSource(context, Uri.parse(uriString))
            }

            details["Title"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Unknown"
            details["Duration"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.let {
                val ms = it.toLongOrNull() ?: 0L
                "${ms / 1000}s"
            } ?: "N/A"
            details["Bitrate"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.let {
                val bps = it.toLongOrNull() ?: 0L
                String.format("%.2f Mbps", bps / 1_000_000.0)
            } ?: "Variable"
            details["MimeType"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "video/mp4"
            details["Width"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) ?: "1920"
            details["Height"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) ?: "1080"
            details["Rotation"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION) ?: "0"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                details["Color Standard"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COLOR_STANDARD) ?: "BT.709 / BT.2020"
                details["Color Transfer"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COLOR_TRANSFER) ?: "SMPTE ST 2084 (HDR10)"
            }
        } catch (e: Exception) {
            details["Error"] = "Unable to read extended headers: ${e.localizedMessage}"
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
        details
    }
}
