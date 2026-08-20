package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import android.view.WindowManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.example.data.AppDatabase
import com.example.data.MediaRepository
import com.example.model.AspectRatioMode
import com.example.model.AudioMode
import com.example.model.AudioTrackInfo
import com.example.model.DecoderMode
import com.example.model.DiagnosticTelemetry
import com.example.model.PlayerSettings
import com.example.model.ScreenOrientationMode
import com.example.model.SubtitleTrack
import com.example.model.VideoItem
import com.example.player.DoomsdayAudioProcessor
import com.example.player.TelemetryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = MediaRepository(application, db.appDao())

    var exoPlayer: ExoPlayer? = null
        private set

    private var audioProcessor = DoomsdayAudioProcessor(application)
    private var trackSelector = DefaultTrackSelector(application)

    private val _currentVideo = MutableStateFlow<VideoItem?>(null)
    val currentVideo: StateFlow<VideoItem?> = _currentVideo.asStateFlow()

    private val _playlist = MutableStateFlow<List<VideoItem>>(emptyList())
    val playlist: StateFlow<List<VideoItem>> = _playlist.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _bufferedPercentage = MutableStateFlow(0)
    val bufferedPercentage: StateFlow<Int> = _bufferedPercentage.asStateFlow()

    private val _isControlsVisible = MutableStateFlow(true)
    val isControlsVisible: StateFlow<Boolean> = _isControlsVisible.asStateFlow()

    private val _aspectRatioMode = MutableStateFlow(AspectRatioMode.FIT)
    val aspectRatioMode: StateFlow<AspectRatioMode> = _aspectRatioMode.asStateFlow()

    private val _manualZoomScale = MutableStateFlow(1.0f)
    val manualZoomScale: StateFlow<Float> = _manualZoomScale.asStateFlow()

    private val _panOffsetX = MutableStateFlow(0f)
    val panOffsetX: StateFlow<Float> = _panOffsetX.asStateFlow()

    private val _panOffsetY = MutableStateFlow(0f)
    val panOffsetY: StateFlow<Float> = _panOffsetY.asStateFlow()

    private val _activeDecoder = MutableStateFlow(DecoderMode.HW_PLUS)
    val activeDecoder: StateFlow<DecoderMode> = _activeDecoder.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isLoopEnabled = MutableStateFlow(false)
    val isLoopEnabled: StateFlow<Boolean> = _isLoopEnabled.asStateFlow()

    private val _isScreenLocked = MutableStateFlow(false)
    val isScreenLocked: StateFlow<Boolean> = _isScreenLocked.asStateFlow()

    private val _currentBrightness = MutableStateFlow(0.7f)
    val currentBrightness: StateFlow<Float> = _currentBrightness.asStateFlow()

    // Volume range: 0.0f to 2.0f (0% to 200%)
    private val _currentVolume = MutableStateFlow(0.8f)
    val currentVolume: StateFlow<Float> = _currentVolume.asStateFlow()

    private val _volumeBoostPercent = MutableStateFlow(100)
    val volumeBoostPercent: StateFlow<Int> = _volumeBoostPercent.asStateFlow()

    private val _subtitleText = MutableStateFlow("")
    val subtitleText: StateFlow<String> = _subtitleText.asStateFlow()

    private val _availableSubtitles = MutableStateFlow<List<SubtitleTrack>>(emptyList())
    val availableSubtitles: StateFlow<List<SubtitleTrack>> = _availableSubtitles.asStateFlow()

    private val _selectedSubtitle = MutableStateFlow<SubtitleTrack?>(null)
    val selectedSubtitle: StateFlow<SubtitleTrack?> = _selectedSubtitle.asStateFlow()

    private val _availableAudioTracks = MutableStateFlow<List<AudioTrackInfo>>(emptyList())
    val availableAudioTracks: StateFlow<List<AudioTrackInfo>> = _availableAudioTracks.asStateFlow()

    private val _telemetry = MutableStateFlow(DiagnosticTelemetry())
    val telemetry: StateFlow<DiagnosticTelemetry> = _telemetry.asStateFlow()

    private val _screenOrientationMode = MutableStateFlow(ScreenOrientationMode.SENSOR)
    val screenOrientationMode: StateFlow<ScreenOrientationMode> = _screenOrientationMode.asStateFlow()

    private val _orientationToastMessage = MutableStateFlow<String?>(null)
    val orientationToastMessage: StateFlow<String?> = _orientationToastMessage.asStateFlow()

    val settings: StateFlow<PlayerSettings> = repository.userSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerSettings()
    )

    private var progressTrackingJob: Job? = null
    private var telemetryJob: Job? = null
    private var autoHideControlsJob: Job? = null

    init {
        initExoPlayer()
        initSystemVolumeAndBrightness()
    }

    private fun initSystemVolumeAndBrightness() {
        val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audioManager != null) {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
            val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
            _currentVolume.value = (curVol / maxVol.coerceAtLeast(1f)).coerceIn(0f, 1f)
        }
    }

    private fun initExoPlayer() {
        if (exoPlayer != null) return

        exoPlayer = ExoPlayer.Builder(getApplication())
            .setTrackSelector(trackSelector)
            .build()
            .apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF

                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                        if (playing) {
                            scheduleAutoHideControls()
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                _durationMs.value = duration.coerceAtLeast(0L)
                                audioProcessor.attachAudioSession(audioSessionId)
                                audioProcessor.applyMode(settings.value.audioMode)
                            }
                            Player.STATE_ENDED -> {
                                onVideoEnded()
                            }
                            else -> Unit
                        }
                    }
                })
            }
    }

    fun playVideo(video: VideoItem, playlist: List<VideoItem> = listOf(video), resumeTimestamp: Long? = null) {
        _currentVideo.value = video
        _playlist.value = playlist
        initExoPlayer()

        viewModelScope.launch {
            val history = repository.getHistoryForUri(video.uri)
            val startPosition = resumeTimestamp ?: history?.lastPositionMs ?: 0L

            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(video.uri))
                .setMediaId(video.id.toString())
                .build()

            exoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
                if (startPosition > 1000L && (video.durationMs <= 0 || startPosition < video.durationMs - 5000L)) {
                    seekTo(startPosition)
                }
                play()
            }

            _isPlaying.value = true
            startProgressTracking()
            startTelemetryStream(video)
            populateDefaultTracks(video)
            scheduleAutoHideControls()
        }
    }

    private fun populateDefaultTracks(video: VideoItem) {
        _availableSubtitles.value = listOf(
            SubtitleTrack("none", "Off (Subtitles Disabled)", "none"),
            SubtitleTrack("en_trans", "English (Transparent Subtitle Engine)", "en"),
            SubtitleTrack("es_trans", "Spanish (Dolby Vision Synced)", "es"),
            SubtitleTrack("jp_trans", "Japanese (Vulkan Direct Font)", "ja")
        )
        _selectedSubtitle.value = _availableSubtitles.value[1]

        _availableAudioTracks.value = listOf(
            AudioTrackInfo("track_1", "Dolby Atmos 7.1.4 (English)", "en", "7.1 Surround", "48 kHz / 24-bit"),
            AudioTrackInfo("track_2", "DTS-HD Master 5.1 (Direct)", "en", "5.1 Surround", "96 kHz / 24-bit"),
            AudioTrackInfo("track_3", "Hi-Res Stereo Pro IGZO", "en", "2.0 Stereo", "192 kHz / 32-bit")
        )
    }

    private fun startProgressTracking() {
        progressTrackingJob?.cancel()
        progressTrackingJob = viewModelScope.launch {
            while (true) {
                exoPlayer?.let { player ->
                    _currentPositionMs.value = player.currentPosition.coerceAtLeast(0L)
                    _durationMs.value = player.duration.coerceAtLeast(0L)
                    _bufferedPercentage.value = player.bufferedPercentage

                    // Simulate transparent subtitle engine lines
                    val posSec = _currentPositionMs.value / 1000
                    if (_selectedSubtitle.value?.id != "none") {
                        val sampleSub = when {
                            posSec in 2..7 -> "▶ DOOMSDAY CINEMA: High Dynamic Range Output Initialized."
                            posSec in 9..16 -> "Qualcomm Snapdragon 888 • Vulkan Direct Pipeline Engaged."
                            posSec in 18..25 -> "Dolby Atmos 7.1.4 3D Spatial Audio processing bitstream."
                            posSec in 28..36 -> "Sharp AQUOS R6 Pro IGZO Display 120Hz Ultra-Low Latency."
                            posSec in 40..48 -> "Lossless transparent subtitle rendering with zero frame drop."
                            else -> ""
                        }
                        _subtitleText.value = sampleSub
                    } else {
                        _subtitleText.value = ""
                    }

                    // Save progress periodically to Room database
                    _currentVideo.value?.let { video ->
                        if (player.currentPosition > 2000L) {
                            val isCompleted = player.duration > 0 && player.currentPosition >= player.duration - 3000L
                            repository.savePlaybackProgress(
                                video = video,
                                positionMs = player.currentPosition,
                                completed = isCompleted,
                                engineUsed = settings.value.renderEngine.tag
                            )
                        }
                    }
                }
                delay(400)
            }
        }
    }

    private fun startTelemetryStream(video: VideoItem) {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            TelemetryProvider.streamTelemetry(
                context = getApplication(),
                settings = settings.value,
                currentBitrate = if (video.isHdr) 24.5f else 12.0f,
                currentCodec = video.codec,
                currentRes = video.resolution,
                isHdr = video.isHdr
            ).collectLatest {
                _telemetry.value = it
            }
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
            } else {
                player.play()
                _isPlaying.value = true
                scheduleAutoHideControls()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs.coerceIn(0L, _durationMs.value))
        _currentPositionMs.value = positionMs
    }

    fun seekRelative(seconds: Int) {
        val newPos = (_currentPositionMs.value + seconds * 1000L).coerceIn(0L, _durationMs.value)
        seekTo(newPos)
    }

    fun seekRelativePercent(percentDelta: Float) {
        if (_durationMs.value <= 0) return
        val currentMs = _currentPositionMs.value
        val deltaMs = (percentDelta * _durationMs.value).toLong()
        val target = (currentMs + deltaMs).coerceIn(0L, _durationMs.value)
        seekTo(target)
    }

    fun playNext() {
        val list = _playlist.value
        if (list.isEmpty()) return
        val curIndex = list.indexOfFirst { it.uri == _currentVideo.value?.uri }
        if (curIndex >= 0 && curIndex + 1 < list.size) {
            playVideo(list[curIndex + 1], list)
        } else if (list.isNotEmpty()) {
            playVideo(list.first(), list)
        }
    }

    fun playPrevious() {
        val list = _playlist.value
        if (list.isEmpty()) return
        val curIndex = list.indexOfFirst { it.uri == _currentVideo.value?.uri }
        if (curIndex > 0) {
            playVideo(list[curIndex - 1], list)
        } else if (_currentPositionMs.value > 3000L) {
            seekTo(0L)
        }
    }

    private fun onVideoEnded() {
        if (_isLoopEnabled.value) {
            seekTo(0L)
            exoPlayer?.play()
        } else if (settings.value.autoPlayNext) {
            playNext()
        }
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        _aspectRatioMode.value = mode
        if (mode != AspectRatioMode.CUSTOM_MANUAL) {
            _manualZoomScale.value = 1.0f
            _panOffsetX.value = 0f
            _panOffsetY.value = 0f
        }
    }

    fun setManualZoomScale(scale: Float) {
        _manualZoomScale.value = scale.coerceIn(0.5f, 3.5f)
        _aspectRatioMode.value = AspectRatioMode.CUSTOM_MANUAL
    }

    fun setPanOffset(x: Float, y: Float) {
        _panOffsetX.value = x
        _panOffsetY.value = y
    }

    fun resetZoomAndPan() {
        _manualZoomScale.value = 1.0f
        _panOffsetX.value = 0f
        _panOffsetY.value = 0f
        _aspectRatioMode.value = AspectRatioMode.FIT
    }

    fun cycleAspectRatio() {
        val modes = AspectRatioMode.values()
        val nextIndex = (modes.indexOf(_aspectRatioMode.value) + 1) % modes.size
        setAspectRatio(modes[nextIndex])
    }

    fun setDecoderMode(mode: DecoderMode) {
        _activeDecoder.value = mode
        updateSettings { it.copy(decoderMode = mode) }
    }

    fun cycleDecoderMode() {
        val decoders = DecoderMode.values()
        val nextIndex = (decoders.indexOf(_activeDecoder.value) + 1) % decoders.size
        setDecoderMode(decoders[nextIndex])
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer?.playbackParameters = PlaybackParameters(speed)
    }

    fun cyclePlaybackSpeed() {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f)
        val nextIndex = (speeds.indexOf(_playbackSpeed.value) + 1) % speeds.size
        setPlaybackSpeed(speeds[nextIndex])
    }

    fun toggleLoop() {
        _isLoopEnabled.value = !_isLoopEnabled.value
        exoPlayer?.repeatMode = if (_isLoopEnabled.value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    fun toggleScreenLock() {
        _isScreenLocked.value = !_isScreenLocked.value
    }

    fun toggleControls() {
        if (_isScreenLocked.value) {
            _isControlsVisible.value = !_isControlsVisible.value
            return
        }
        _isControlsVisible.value = !_isControlsVisible.value
        if (_isControlsVisible.value && _isPlaying.value) {
            scheduleAutoHideControls()
        }
    }

    private fun scheduleAutoHideControls() {
        autoHideControlsJob?.cancel()
        autoHideControlsJob = viewModelScope.launch {
            delay(4000)
            if (_isPlaying.value && !_isScreenLocked.value) {
                _isControlsVisible.value = false
            }
        }
    }

    fun setBrightness(value: Float) {
        _currentBrightness.value = value.coerceIn(0.05f, 1.0f)
    }

    fun setVolume(value: Float) {
        // Value: 0.0f to 2.0f
        val clamped = value.coerceIn(0f, 2.0f)
        _currentVolume.value = clamped

        val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audioManager != null) {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val baseVolRatio = clamped.coerceIn(0f, 1.0f)
            val target = (baseVolRatio * maxVol).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        }

        if (clamped > 1.0f) {
            val boost = 100 + ((clamped - 1.0f) * 100).toInt()
            _volumeBoostPercent.value = boost
            audioProcessor.setVolumeBoost(boost)
        } else {
            _volumeBoostPercent.value = 100
            audioProcessor.setVolumeBoost(100)
        }
    }

    fun setVolumeBoostPercent(percent: Int) {
        val clamped = percent.coerceIn(100, 200)
        _volumeBoostPercent.value = clamped
        _currentVolume.value = clamped / 100f
        audioProcessor.setVolumeBoost(clamped)
    }

    fun selectSubtitle(subtitle: SubtitleTrack) {
        _selectedSubtitle.value = subtitle
    }

    fun loadExternalSubtitle(uri: Uri, fileName: String) {
        val externalTrack = SubtitleTrack(
            id = "ext_${System.currentTimeMillis()}",
            name = "External: $fileName",
            language = "custom",
            uri = uri.toString(),
            isExternal = true
        )
        _availableSubtitles.value = _availableSubtitles.value + externalTrack
        _selectedSubtitle.value = externalTrack
    }

    fun loadExternalAudioTrack(uri: Uri, fileName: String) {
        val externalTrack = AudioTrackInfo(
            id = "ext_aud_${System.currentTimeMillis()}",
            name = "External: $fileName",
            language = "custom",
            channels = "Dolby Stereo Passthrough",
            sampleRate = "48 kHz",
            isExternal = true
        )
        _availableAudioTracks.value = _availableAudioTracks.value + externalTrack
    }

    fun setScreenOrientation(mode: ScreenOrientationMode) {
        _screenOrientationMode.value = mode
        _orientationToastMessage.value = "Orientation: ${mode.displayName}"
        viewModelScope.launch {
            delay(1800)
            if (_orientationToastMessage.value?.startsWith("Orientation: ${mode.displayName}") == true) {
                _orientationToastMessage.value = null
            }
        }
    }

    fun toggleOrientation() {
        val newMode = when (_screenOrientationMode.value) {
            ScreenOrientationMode.PORTRAIT -> ScreenOrientationMode.LANDSCAPE
            ScreenOrientationMode.LANDSCAPE -> ScreenOrientationMode.PORTRAIT
            ScreenOrientationMode.SENSOR -> ScreenOrientationMode.LANDSCAPE
            ScreenOrientationMode.REVERSE_LANDSCAPE -> ScreenOrientationMode.PORTRAIT
        }
        setScreenOrientation(newMode)
    }

    fun cycleOrientation() {
        val modes = ScreenOrientationMode.values()
        val nextIndex = (modes.indexOf(_screenOrientationMode.value) + 1) % modes.size
        setScreenOrientation(modes[nextIndex])
    }

    fun clearOrientationToast() {
        _orientationToastMessage.value = null
    }

    fun updateSettings(transform: (PlayerSettings) -> PlayerSettings) {
        viewModelScope.launch {
            val updated = transform(settings.value)
            repository.saveSettings(updated)
            audioProcessor.applyMode(updated.audioMode)
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressTrackingJob?.cancel()
        telemetryJob?.cancel()
        autoHideControlsJob?.cancel()
        audioProcessor.release()
        exoPlayer?.release()
        exoPlayer = null
    }
}
