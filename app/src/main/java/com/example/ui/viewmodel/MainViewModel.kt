package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MediaRepository
import com.example.data.PlaybackHistoryEntity
import com.example.model.AudioMode
import com.example.model.DeviceProfile
import com.example.model.GpuApi
import com.example.model.PerformanceMode
import com.example.model.PlayerSettings
import com.example.model.RenderEngine
import com.example.model.VideoFolder
import com.example.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = MediaRepository(application, db.appDao())

    val settings: StateFlow<PlayerSettings> = repository.userSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerSettings()
    )

    val historyList: StateFlow<List<PlaybackHistoryEntity>> = repository.playbackHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _videoList = MutableStateFlow<List<VideoItem>>(emptyList())
    val videoList: StateFlow<List<VideoItem>> = _videoList.asStateFlow()

    private val _isLoadingVideos = MutableStateFlow(false)
    val isLoadingVideos: StateFlow<Boolean> = _isLoadingVideos.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: All Videos, 1: Folders, 2: History, 3: Engine Tuning, 4: Audio & Subtitles
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedFilterHdrOnly = MutableStateFlow(false)
    val selectedFilterHdrOnly: StateFlow<Boolean> = _selectedFilterHdrOnly.asStateFlow()

    private val _isCleanUiMode = MutableStateFlow(repository.isCleanUiMode())
    val isCleanUiMode: StateFlow<Boolean> = _isCleanUiMode.asStateFlow()

    private val _selectedFolder = MutableStateFlow<VideoFolder?>(null)
    val selectedFolder: StateFlow<VideoFolder?> = _selectedFolder.asStateFlow()

    private val _folderSearchQuery = MutableStateFlow("")
    val folderSearchQuery: StateFlow<String> = _folderSearchQuery.asStateFlow()

    private val _selectedVideoForInfo = MutableStateFlow<VideoItem?>(null)
    val selectedVideoForInfo: StateFlow<VideoItem?> = _selectedVideoForInfo.asStateFlow()

    private val _videoMetadataDetails = MutableStateFlow<Map<String, String>>(emptyMap())
    val videoMetadataDetails: StateFlow<Map<String, String>> = _videoMetadataDetails.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        refreshVideos()
    }

    fun refreshVideos() {
        viewModelScope.launch {
            _isLoadingVideos.value = true
            try {
                val videos = repository.scanDeviceVideos()
                _videoList.value = videos
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingVideos.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun toggleHdrFilter() {
        _selectedFilterHdrOnly.value = !_selectedFilterHdrOnly.value
    }

    fun updateSettings(transform: (PlayerSettings) -> PlayerSettings) {
        viewModelScope.launch {
            val updated = transform(settings.value)
            repository.saveSettings(updated)
        }
    }

    fun setRenderEngine(engine: RenderEngine) {
        updateSettings { it.copy(renderEngine = engine) }
    }

    fun setGpuApi(gpuApi: GpuApi) {
        updateSettings { it.copy(gpuApi = gpuApi) }
    }

    fun setPerformanceMode(mode: PerformanceMode) {
        updateSettings { it.copy(performanceMode = mode) }
    }

    fun setDeviceProfile(profile: DeviceProfile) {
        updateSettings { it.copy(deviceProfile = profile) }
    }

    fun setAudioMode(mode: AudioMode) {
        updateSettings { it.copy(audioMode = mode) }
    }

    fun toggleHdrOutput() {
        updateSettings { it.copy(hdrOutputEnabled = !it.hdrOutputEnabled) }
    }

    fun toggleDolbyVisionToneMap() {
        updateSettings { it.copy(dolbyVisionToneMap = !it.dolbyVisionToneMap) }
    }

    fun toggleAmbientMode() {
        updateSettings { it.copy(ambientModeEnabled = !it.ambientModeEnabled) }
    }

    fun toggleDiagnosticHud() {
        updateSettings { it.copy(showDiagnosticHud = !it.showDiagnosticHud) }
    }

    fun toggleSubtitleTransparency() {
        updateSettings { it.copy(subtitleBackgroundTransparent = !it.subtitleBackgroundTransparent) }
    }

    fun setSubtitleFontSize(sizeSp: Float) {
        updateSettings { it.copy(subtitleFontSizeSp = sizeSp) }
    }

    fun addImportedVideo(uri: Uri, displayName: String) {
        val newVideo = VideoItem(
            id = System.currentTimeMillis(),
            uri = uri.toString(),
            title = displayName.ifBlank { "External Media" },
            durationMs = 0L,
            sizeBytes = 0L,
            path = uri.toString(),
            resolution = "4K / Master Source",
            mimeType = "video/*",
            dateModified = System.currentTimeMillis(),
            isHdr = true,
            isDolbyVision = true,
            codec = "Direct Vulkan Native Buffer",
            fps = 60.0f
        )
        _videoList.value = listOf(newVideo) + _videoList.value
        _toastMessage.value = "Imported: $displayName"
    }

    fun inspectVideoDetails(video: VideoItem) {
        _selectedVideoForInfo.value = video
        viewModelScope.launch {
            _videoMetadataDetails.value = repository.extractMetadata(video.uri)
        }
    }

    fun closeVideoDetails() {
        _selectedVideoForInfo.value = null
        _videoMetadataDetails.value = emptyMap()
    }

    fun deleteVideo(video: VideoItem) {
        viewModelScope.launch {
            val success = repository.deleteVideo(video)
            if (success) {
                _videoList.value = _videoList.value.filter { it.uri != video.uri }
                _toastMessage.value = "Removed \"${video.title}\""
            } else {
                _toastMessage.value = "Could not delete video (Read-only source)"
            }
        }
    }

    fun toggleCleanUiMode() {
        val newMode = !_isCleanUiMode.value
        repository.setCleanUiMode(newMode)
        _isCleanUiMode.value = newMode
        refreshVideos()
        _toastMessage.value = if (newMode) "Clean UI: Local files only" else "Standard UI: Demo videos enabled"
    }

    fun removeAllDemoVideos() {
        repository.removeAllDemoVideos()
        _isCleanUiMode.value = true
        refreshVideos()
        _toastMessage.value = "All demo videos removed (Clean UI Active)"
    }

    fun isAvengersDemoPresent(): Boolean {
        return repository.isAvengersDemoPresent()
    }

    fun addAvengersDemoVideo() {
        repository.addAvengersDoomsdayDemo()
        _isCleanUiMode.value = false
        refreshVideos()
        _toastMessage.value = "Added Avengers: Doomsday 4K IMAX Demo"
    }

    fun restoreAllDemoVideos() {
        repository.restoreAllDemoVideos()
        _isCleanUiMode.value = false
        refreshVideos()
        _toastMessage.value = "Restored all Cinema Demo videos"
    }

    fun removeAvengersDemoVideo() {
        repository.removeDemoVideoById(MediaRepository.AVENGERS_DOOMSDAY_ID)
        refreshVideos()
        _toastMessage.value = "Removed Avengers: Doomsday Demo video"
    }

    fun selectFolder(folder: VideoFolder?) {
        _selectedFolder.value = folder
        _folderSearchQuery.value = ""
    }

    fun updateFolderSearchQuery(query: String) {
        _folderSearchQuery.value = query
    }

    fun shareVideo(video: VideoItem) {
        repository.shareVideo(video)
    }

    fun deleteHistoryItem(uri: String) {
        viewModelScope.launch {
            repository.deleteHistoryItem(uri)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _toastMessage.value = "Playback history cleared"
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun getFolders(): List<VideoFolder> {
        val map = mutableMapOf<String, Triple<String, String, MutableList<VideoItem>>>()
        _videoList.value.forEach { item ->
            val (folderName, folderPath) = if (item.path.startsWith("http")) {
                "Cinema 4K Benchmarks" to "Web Stream / Demo Storage"
            } else {
                val f = File(item.path).parentFile
                val name = f?.name ?: "Internal Storage"
                val path = f?.absolutePath ?: "/storage/emulated/0"
                name to path
            }
            val key = folderPath
            val existing = map.getOrPut(key) { Triple(folderName, folderPath, mutableListOf()) }
            existing.third.add(item)
        }

        return map.map { (_, triple) ->
            val (name, path, videos) = triple
            val totalSize = videos.sumOf { it.sizeBytes }
            val totalDuration = videos.sumOf { it.durationMs }
            val firstThumb = videos.firstOrNull { it.thumbnailUri != null }?.thumbnailUri
            VideoFolder(
                folderName = name,
                folderPath = path,
                videos = videos,
                totalSizeBytes = totalSize,
                totalDurationMs = totalDuration,
                firstThumbnailUri = firstThumb
            )
        }.sortedByDescending { it.videos.size }
    }

    fun getTotalLocalStorageBytes(): Long {
        return _videoList.value.filter { !it.path.startsWith("http") }.sumOf { it.sizeBytes }
    }

    fun getLocalVideosCount(): Int {
        return _videoList.value.count { !it.path.startsWith("http") }
    }
}
