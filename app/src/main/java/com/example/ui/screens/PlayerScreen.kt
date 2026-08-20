@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.media3.common.util.UnstableApi::class)

package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.AspectRatioMode
import com.example.model.AudioMode
import com.example.model.DecoderMode
import com.example.model.GpuApi
import com.example.model.PerformanceMode
import com.example.model.PlayerSettings
import com.example.model.RenderEngine
import com.example.model.ScreenOrientationMode
import com.example.model.VideoItem
import com.example.ui.components.AmbientGlow
import com.example.ui.components.DiagnosticHudOverlay
import com.example.ui.components.DoomsdayGlowingBadge
import com.example.ui.components.EngineBadge
import com.example.ui.components.GestureCinemaController
import com.example.ui.components.HdrBadge
import com.example.ui.components.SubtitleOverlay
import com.example.ui.theme.DolbyVisionPurple
import com.example.ui.theme.DoomsdayAmber
import com.example.ui.theme.DoomsdayCyan
import com.example.ui.theme.DoomsdayCrimson
import com.example.ui.theme.DoomsdayEmerald
import com.example.ui.theme.DoomsdayEmeraldDark
import com.example.ui.theme.DoomsdayGlassBorder
import com.example.ui.theme.DoomsdayObsidian
import com.example.ui.theme.DoomsdaySurface
import com.example.ui.theme.DoomsdaySurfaceVariant
import com.example.ui.theme.HdrGold
import com.example.ui.theme.TitaniumMuted
import com.example.ui.theme.TitaniumSilver
import com.example.ui.theme.TitaniumWhite
import com.example.ui.theme.VulkanRed
import com.example.ui.viewmodel.PlayerViewModel

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    isInPipMode: Boolean = false,
    onEnterPip: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentVideo by viewModel.currentVideo.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val bufferedPercentage by viewModel.bufferedPercentage.collectAsState()
    val isControlsVisible by viewModel.isControlsVisible.collectAsState()
    val aspectRatioMode by viewModel.aspectRatioMode.collectAsState()
    val manualZoomScale by viewModel.manualZoomScale.collectAsState()
    val panOffsetX by viewModel.panOffsetX.collectAsState()
    val panOffsetY by viewModel.panOffsetY.collectAsState()
    val activeDecoder by viewModel.activeDecoder.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val isLoopEnabled by viewModel.isLoopEnabled.collectAsState()
    val isScreenLocked by viewModel.isScreenLocked.collectAsState()
    val currentBrightness by viewModel.currentBrightness.collectAsState()
    val currentVolume by viewModel.currentVolume.collectAsState()
    val volumeBoostPercent by viewModel.volumeBoostPercent.collectAsState()
    val subtitleText by viewModel.subtitleText.collectAsState()
    val availableSubtitles by viewModel.availableSubtitles.collectAsState()
    val selectedSubtitle by viewModel.selectedSubtitle.collectAsState()
    val availableAudioTracks by viewModel.availableAudioTracks.collectAsState()
    val selectedAudioTrack by viewModel.selectedAudioTrack.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val screenOrientationMode by viewModel.screenOrientationMode.collectAsState()
    val orientationToastMessage by viewModel.orientationToastMessage.collectAsState()

    var showTrackSelectionSheet by remember { mutableStateOf(false) }
    var showEngineTuningSheet by remember { mutableStateOf(false) }
    var showZoomCropControls by remember { mutableStateOf(false) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableStateOf(0L) }

    // Full screen immersion & disable auto brightness in player mode
    LaunchedEffect(Unit) {
        val window = (context as? Activity)?.window
        if (window != null) {
            val controller = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
    }

    // Synchronize Screen Orientation Mode with Android Window
    LaunchedEffect(screenOrientationMode) {
        val activity = context as? Activity
        if (activity != null) {
            activity.requestedOrientation = when (screenOrientationMode) {
                ScreenOrientationMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                ScreenOrientationMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                ScreenOrientationMode.REVERSE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                ScreenOrientationMode.SENSOR -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
        }
    }

    // Reset orientation, system bars, and screen brightness on exit
    DisposableEffect(Unit) {
        onDispose {
            val activity = context as? Activity
            if (activity != null) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                activity.window?.let { window ->
                    val lp = window.attributes
                    lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    window.attributes = lp
                    val controller = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                    controller.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }

    // External file loaders
    val subPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment ?: "External Subtitle"
            viewModel.loadExternalSubtitle(uri, name)
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment ?: "External Audio"
            viewModel.loadExternalAudioTrack(uri, name)
        }
    }

    // Handle back button
    BackHandler {
        if (showZoomCropControls) {
            showZoomCropControls = false
        } else if (isScreenLocked) {
            viewModel.toggleScreenLock()
        } else {
            onBack()
        }
    }

    // Apply brightness to window (remembers brightness & disables auto-brightness)
    LaunchedEffect(currentBrightness) {
        val window = (context as? Activity)?.window
        if (window != null) {
            val lp = window.attributes
            lp.screenBrightness = currentBrightness
            window.attributes = lp
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DoomsdayObsidian)
    ) {
        // 1. YouTube-Style Ambient Glow Background (Disabled in PiP for performance)
        if (!isInPipMode) {
            AmbientGlow(
                enabled = settings.ambientModeEnabled,
                isPlaying = isPlaying,
                primaryGlowColor = if (currentVideo?.isHdr == true) DoomsdayEmerald else DoomsdayCyan,
                secondaryGlowColor = if (currentVideo?.isDolbyVision == true) DolbyVisionPurple else DoomsdayEmerald
            )
        }

        // 2. Video Player View (Media3 ExoPlayer with Hardware Acceleration & Custom Manual Zoom/Pan)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (aspectRatioMode == AspectRatioMode.CUSTOM_MANUAL || manualZoomScale != 1.0f) {
                        scaleX = manualZoomScale
                        scaleY = manualZoomScale
                        translationX = panOffsetX
                        translationY = panOffsetY
                    }
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        useController = false
                        player = viewModel.exoPlayer
                        resizeMode = when (aspectRatioMode) {
                            AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                            AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            AspectRatioMode.PHONE_20_9 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            AspectRatioMode.CINEMA_21_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                            AspectRatioMode.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                            AspectRatioMode.RATIO_4_3 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                            AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            AspectRatioMode.CUSTOM_MANUAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    }
                },
                update = { playerView ->
                    playerView.player = viewModel.exoPlayer
                    playerView.resizeMode = when (aspectRatioMode) {
                        AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioMode.PHONE_20_9 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioMode.CINEMA_21_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                        AspectRatioMode.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                        AspectRatioMode.RATIO_4_3 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                        AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.CUSTOM_MANUAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. Transparent Subtitles Engine Layer (Hidden in PiP)
        if (!isInPipMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isControlsVisible) 100.dp else 24.dp)
            ) {
                SubtitleOverlay(
                    text = subtitleText,
                    settings = settings,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        // 4. Gestures Controller (Volume up to 200%, Brightness, Seek, Double Tap, Pinch-to-Zoom)
        if (!isInPipMode) {
            if (!isScreenLocked) {
                GestureCinemaController(
                    onToggleControls = { viewModel.toggleControls() },
                    onSeekRelative = { viewModel.seekRelative(it) },
                    onScrubSeek = { viewModel.seekRelativePercent(it) },
                    onBrightnessChange = { viewModel.setBrightness(it) },
                    onVolumeChange = { viewModel.setVolume(it) },
                    currentBrightness = currentBrightness,
                    currentVolume = currentVolume,
                    skipStepSeconds = settings.doubleTapSkipSeconds,
                    onPinchZoom = { zoomChange, panChange ->
                        val newZoom = (manualZoomScale * zoomChange).coerceIn(0.5f, 3.5f)
                        viewModel.setManualZoomScale(newZoom)
                        viewModel.setPanOffset(panOffsetX + panChange.x, panOffsetY + panChange.y)
                    }
                )
            } else {
                // Locked screen click catcher
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { viewModel.toggleControls() }
                )
            }
        }

        // 5. Diagnostic OSD Telemetry HUD (Live Engine & Performance Stats)
        if (!isInPipMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 14.dp, top = if (isControlsVisible) 70.dp else 20.dp)
            ) {
                DiagnosticHudOverlay(
                    telemetry = telemetry,
                    settings = settings,
                    visible = settings.showDiagnosticHud && isControlsVisible
                )
            }
        }

        // 6. Cinema Glass UI Overlay (Controls) - Hidden when in PiP Mode
        if (!isInPipMode) {
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x55000000))
                ) {
                    // TOP BAR
                    CinemaTopBar(
                        video = currentVideo,
                        settings = settings,
                        activeDecoder = activeDecoder,
                        orientationMode = screenOrientationMode,
                        isLocked = isScreenLocked,
                        onBack = onBack,
                        onCycleDecoder = { viewModel.cycleDecoderMode() },
                        onToggleOrientation = { viewModel.toggleOrientation() },
                        onCycleOrientation = { viewModel.cycleOrientation() },
                        onToggleHud = { viewModel.updateSettings { it.copy(showDiagnosticHud = !it.showDiagnosticHud) } },
                        onToggleHdr = { viewModel.updateSettings { it.copy(hdrOutputEnabled = !it.hdrOutputEnabled) } },
                        onOpenTracks = { showTrackSelectionSheet = true },
                        onOpenTuning = { showEngineTuningSheet = true },
                        onEnterPip = onEnterPip,
                        onOpenZoomCrop = { showZoomCropControls = !showZoomCropControls },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                    // ON-SCREEN ORIENTATION TOAST HUD
                    AnimatedVisibility(
                        visible = orientationToastMessage != null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 70.dp)
                    ) {
                        orientationToastMessage?.let { msg ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = DoomsdaySurfaceVariant.copy(alpha = 0.95f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DoomsdayCyan),
                                shadowElevation = 8.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ScreenRotation,
                                        contentDescription = null,
                                        tint = DoomsdayCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = msg,
                                        color = TitaniumWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // CENTER PLAY/PAUSE (When paused)
                    if (!isPlaying && !isScreenLocked) {
                        Surface(
                            shape = CircleShape,
                            color = DoomsdayEmerald.copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(2.dp, TitaniumWhite),
                            modifier = Modifier
                                .size(72.dp)
                                .align(Alignment.Center)
                                .clickable { viewModel.togglePlayPause() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.Black,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp)
                            )
                        }
                    }

                    // SCREEN LOCK BUTTON (Always accessible)
                    Surface(
                        shape = CircleShape,
                        color = if (isScreenLocked) DoomsdayCrimson else DoomsdaySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isScreenLocked) DoomsdayCrimson else DoomsdayGlassBorder
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                            .size(44.dp)
                            .clickable { viewModel.toggleScreenLock() }
                    ) {
                        Icon(
                            imageVector = if (isScreenLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Lock Screen",
                            tint = if (isScreenLocked) TitaniumWhite else TitaniumSilver,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    // MANUAL ZOOM & CROP FLOATING PANEL
                    if (showZoomCropControls && !isScreenLocked) {
                        ManualZoomCropCard(
                            aspectRatioMode = aspectRatioMode,
                            zoomScale = manualZoomScale,
                            onSelectAspectMode = { viewModel.setAspectRatio(it) },
                            onZoomChange = { viewModel.setManualZoomScale(it) },
                            onReset = { viewModel.resetZoomAndPan() },
                            onClose = { showZoomCropControls = false },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp)
                        )
                    }

                    // BOTTOM BAR CONTROLS
                    if (!isScreenLocked) {
                        CinemaBottomControls(
                            isPlaying = isPlaying,
                            currentPositionMs = if (isScrubbing) scrubPositionMs else currentPositionMs,
                            durationMs = durationMs,
                            bufferedPercentage = bufferedPercentage,
                            playbackSpeed = playbackSpeed,
                            aspectRatioMode = aspectRatioMode,
                            orientationMode = screenOrientationMode,
                            isLoopEnabled = isLoopEnabled,
                            volumeBoostPercent = volumeBoostPercent,
                            settings = settings,
                            onPlayPause = { viewModel.togglePlayPause() },
                            onSeekTo = { viewModel.seekTo(it) },
                            onSeekRelative = { viewModel.seekRelative(it) },
                            onNext = { viewModel.playNext() },
                            onPrevious = { viewModel.playPrevious() },
                            onCycleSpeed = { viewModel.cyclePlaybackSpeed() },
                            onCycleAspectRatio = { viewModel.cycleAspectRatio() },
                            onOpenZoomCrop = { showZoomCropControls = !showZoomCropControls },
                            onToggleLoop = { viewModel.toggleLoop() },
                            onToggleOrientation = { viewModel.toggleOrientation() },
                            onScrubStart = {
                                isScrubbing = true
                                scrubPositionMs = currentPositionMs
                            },
                            onScrubProgress = { scrubPositionMs = it },
                            onScrubEnd = {
                                isScrubbing = false
                                viewModel.seekTo(scrubPositionMs)
                            },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }

        // Track Selection Bottom Sheet (Subtitles, Audio & 200% Volume Boost)
        if (showTrackSelectionSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTrackSelectionSheet = false },
                containerColor = DoomsdaySurface,
                scrimColor = Color(0x99000000)
            ) {
                TrackSelectionBottomSheetContent(
                    availableSubtitles = availableSubtitles,
                    selectedSubtitle = selectedSubtitle,
                    availableAudioTracks = availableAudioTracks,
                    selectedAudioTrack = selectedAudioTrack,
                    volumeBoostPercent = volumeBoostPercent,
                    settings = settings,
                    onSelectSubtitle = { viewModel.selectSubtitle(it) },
                    onSelectAudioTrack = { viewModel.selectAudioTrack(it) },
                    onLoadExternalSubtitle = { subPickerLauncher.launch("*/*") },
                    onLoadExternalAudio = { audioPickerLauncher.launch("audio/*") },
                    onToggleSubtitleTransparency = {
                        viewModel.updateSettings { it.copy(subtitleBackgroundTransparent = !it.subtitleBackgroundTransparent) }
                    },
                    onSelectAudioMode = { viewModel.updateSettings { s -> s.copy(audioMode = it) } },
                    onSetVolumeBoost = { viewModel.setVolumeBoostPercent(it) }
                )
            }
        }

        // Engine Tuning Sheet (Decoders, Vulkan, Engines, Performance, Orientation)
        if (showEngineTuningSheet) {
            ModalBottomSheet(
                onDismissRequest = { showEngineTuningSheet = false },
                containerColor = DoomsdaySurface,
                scrimColor = Color(0x99000000)
            ) {
                EngineQuickTuningSheetContent(
                    settings = settings,
                    activeDecoder = activeDecoder,
                    orientationMode = screenOrientationMode,
                    onSelectDecoder = { viewModel.setDecoderMode(it) },
                    onSelectOrientation = { viewModel.setScreenOrientation(it) },
                    onSelectEngine = { viewModel.updateSettings { s -> s.copy(renderEngine = it) } },
                    onSelectPerformanceMode = { viewModel.updateSettings { s -> s.copy(performanceMode = it) } },
                    onSelectGpuApi = { viewModel.updateSettings { s -> s.copy(gpuApi = it) } },
                    onToggleHdr = { viewModel.updateSettings { s -> s.copy(hdrOutputEnabled = !s.hdrOutputEnabled) } },
                    onToggleDolbyVision = { viewModel.updateSettings { s -> s.copy(dolbyVisionToneMap = !s.dolbyVisionToneMap) } },
                    onToggleAmbient = { viewModel.updateSettings { s -> s.copy(ambientModeEnabled = !s.ambientModeEnabled) } }
                )
            }
        }
    }
}

@Composable
private fun CinemaTopBar(
    video: VideoItem?,
    settings: PlayerSettings,
    activeDecoder: DecoderMode,
    orientationMode: ScreenOrientationMode,
    isLocked: Boolean,
    onBack: () -> Unit,
    onCycleDecoder: () -> Unit,
    onToggleOrientation: () -> Unit,
    onCycleOrientation: () -> Unit,
    onToggleHud: () -> Unit,
    onToggleHdr: () -> Unit,
    onOpenTracks: () -> Unit,
    onOpenTuning: () -> Unit,
    onEnterPip: () -> Unit,
    onOpenZoomCrop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xF0070A11),
                        Color(0xAA0B101D),
                        Color.Transparent
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DoomsdaySurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.NavigateBefore,
                        contentDescription = "Back",
                        tint = TitaniumWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = video?.title ?: "Playing Video",
                        color = TitaniumWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EngineBadge(engine = settings.renderEngine)

                        // On-Screen Decoder Switch Button
                        val decoderColor = when (activeDecoder) {
                            DecoderMode.HW_PLUS -> DoomsdayEmerald
                            DecoderMode.HW -> DoomsdayCyan
                            DecoderMode.SW -> VulkanRed
                            DecoderMode.VULKAN_DIRECT -> HdrGold
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = decoderColor.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, decoderColor),
                            modifier = Modifier.clickable { onCycleDecoder() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = decoderColor,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = activeDecoder.badge,
                                    color = TitaniumWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // On-Screen Orientation Selector Badge
                        val orientationIcon = when (orientationMode) {
                            ScreenOrientationMode.PORTRAIT -> Icons.Default.StayCurrentPortrait
                            ScreenOrientationMode.LANDSCAPE, ScreenOrientationMode.REVERSE_LANDSCAPE -> Icons.Default.StayCurrentLandscape
                            ScreenOrientationMode.SENSOR -> Icons.Default.ScreenRotation
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DoomsdayCyan.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DoomsdayCyan),
                            modifier = Modifier
                                .testTag("topbar_orientation_badge")
                                .clickable { onCycleOrientation() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = orientationIcon,
                                    contentDescription = "Screen Orientation: ${orientationMode.displayName}",
                                    tint = DoomsdayCyan,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = orientationMode.shortLabel,
                                    color = TitaniumWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (video?.isHdr == true || settings.hdrOutputEnabled) {
                            HdrBadge(isDolbyVision = video?.isDolbyVision == true)
                        }
                    }
                }
            }

            if (!isLocked) {
                // Top Right Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // On-Screen Orientation Switch Button
                    IconButton(
                        onClick = onToggleOrientation,
                        modifier = Modifier.testTag("topbar_orientation_btn")
                    ) {
                        Icon(
                            imageVector = if (orientationMode == ScreenOrientationMode.PORTRAIT) Icons.Default.StayCurrentLandscape else Icons.Default.StayCurrentPortrait,
                            contentDescription = "Toggle Orientation (Portrait / Landscape)",
                            tint = DoomsdayCyan
                        )
                    }

                    // Manual Zoom & Aspect Ratio Button
                    IconButton(onClick = onOpenZoomCrop) {
                        Icon(
                            imageVector = Icons.Default.CropFree,
                            contentDescription = "Manual Zoom & Crop",
                            tint = DoomsdayEmerald
                        )
                    }

                    // Picture-in-Picture Mode Button
                    IconButton(onClick = onEnterPip) {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = "Picture in Picture",
                            tint = DoomsdayCyan
                        )
                    }

                    // HDR Output Button
                    IconButton(onClick = onToggleHdr) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Toggle HDR",
                            tint = if (settings.hdrOutputEnabled) HdrGold else TitaniumMuted
                        )
                    }

                    // Telemetry HUD Toggle Button
                    IconButton(onClick = onToggleHud) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Telemetry HUD",
                            tint = if (settings.showDiagnosticHud) DoomsdayEmerald else TitaniumMuted
                        )
                    }

                    // Audio Track Button
                    IconButton(onClick = onOpenTracks) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = "Audio Track & Codecs",
                            tint = DoomsdayEmerald
                        )
                    }

                    // Subtitles Button
                    IconButton(onClick = onOpenTracks) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Subtitles & Studio",
                            tint = DoomsdayCyan
                        )
                    }

                    // Engine Quick Tuning Button
                    IconButton(onClick = onOpenTuning) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Engine Tuning",
                            tint = DoomsdayEmerald
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualZoomCropCard(
    aspectRatioMode: AspectRatioMode,
    zoomScale: Float,
    onSelectAspectMode: (AspectRatioMode) -> Unit,
    onZoomChange: (Float) -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DoomsdaySurface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DoomsdayEmerald.copy(alpha = 0.7f)),
        modifier = modifier
            .width(260.dp)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = null,
                        tint = DoomsdayEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ZOOM & ASPECT RATIO",
                        color = TitaniumWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TitaniumMuted)
                }
            }

            // Aspect Presets Grid
            Text(text = "PRESET RATIOS", color = TitaniumMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            val presets = listOf(
                AspectRatioMode.FIT,
                AspectRatioMode.ZOOM,
                AspectRatioMode.FILL,
                AspectRatioMode.CINEMA_21_9,
                AspectRatioMode.RATIO_16_9,
                AspectRatioMode.RATIO_4_3
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { mode ->
                    val isSelected = aspectRatioMode == mode
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) DoomsdayEmerald.copy(alpha = 0.25f) else DoomsdaySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) DoomsdayEmerald else DoomsdayGlassBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSelectAspectMode(mode) }
                    ) {
                        Text(
                            text = mode.displayName,
                            color = if (isSelected) DoomsdayEmerald else TitaniumSilver,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Manual Scale Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "MANUAL SCALE", color = TitaniumMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = String.format("%.2fx", zoomScale),
                    color = DoomsdayEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Slider(
                value = zoomScale,
                onValueChange = onZoomChange,
                valueRange = 0.5f..3.0f,
                colors = SliderDefaults.colors(
                    thumbColor = DoomsdayEmerald,
                    activeTrackColor = DoomsdayEmerald,
                    inactiveTrackColor = Color(0x40FFFFFF)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Reset & Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pinch screen to zoom/pan",
                    color = TitaniumMuted,
                    fontSize = 9.sp
                )
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(containerColor = DoomsdaySurfaceVariant),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = TitaniumWhite, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", color = TitaniumWhite, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun CinemaBottomControls(
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    bufferedPercentage: Int,
    playbackSpeed: Float,
    aspectRatioMode: AspectRatioMode,
    orientationMode: ScreenOrientationMode,
    isLoopEnabled: Boolean,
    volumeBoostPercent: Int,
    settings: PlayerSettings,
    onPlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekRelative: (Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onCycleSpeed: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onOpenZoomCrop: () -> Unit,
    onToggleLoop: () -> Unit,
    onToggleOrientation: () -> Unit,
    onScrubStart: () -> Unit,
    onScrubProgress: (Long) -> Unit,
    onScrubEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = durationMs / 1000
    val currentSeconds = currentPositionMs / 1000

    val posFormatted = formatDurationSeconds(currentSeconds)
    val durFormatted = formatDurationSeconds(totalSeconds)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xCC070A11),
                        Color(0xFA05080E)
                    )
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Timecode & Buffer Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$posFormatted / $durFormatted",
                    color = TitaniumWhite,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (volumeBoostPercent > 100) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFF5252).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252))
                        ) {
                            Text(
                                text = "BOOST ${volumeBoostPercent}%",
                                color = Color(0xFFFF5252),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Buffer: $bufferedPercentage%",
                        color = DoomsdayCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${settings.performanceMode.targetFps}Hz Pro IGZO",
                        color = DoomsdayEmerald,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Scrubber Slider
            Slider(
                value = if (durationMs > 0) currentPositionMs.toFloat() else 0f,
                onValueChange = {
                    onScrubStart()
                    onScrubProgress(it.toLong())
                },
                onValueChangeFinished = { onScrubEnd() },
                valueRange = 0f..(durationMs.toFloat().coerceAtLeast(1f)),
                colors = SliderDefaults.colors(
                    thumbColor = DoomsdayEmerald,
                    activeTrackColor = DoomsdayEmerald,
                    inactiveTrackColor = Color(0x44FFFFFF)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Transport Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left auxiliary controls: Speed, Aspect Ratio, Loop
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speed Button
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DoomsdaySurfaceVariant,
                        modifier = Modifier.clickable { onCycleSpeed() }
                    ) {
                        Text(
                            text = "${playbackSpeed}x",
                            color = if (playbackSpeed != 1.0f) DoomsdayEmerald else TitaniumSilver,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    // Aspect Ratio Button
                    IconButton(onClick = onCycleAspectRatio, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = "Aspect Ratio",
                            tint = TitaniumSilver,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Loop Button
                    IconButton(onClick = onToggleLoop, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isLoopEnabled) Icons.Default.RepeatOne else Icons.Default.Repeat,
                            contentDescription = "Loop",
                            tint = if (isLoopEnabled) DoomsdayEmerald else TitaniumMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Center Primary Transport: Prev, Rewind 10s, Play/Pause, Forward 10s, Next
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevious, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.NavigateBefore, "Previous", tint = TitaniumWhite)
                    }

                    IconButton(onClick = { onSeekRelative(-10) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.FastRewind, "Rewind 10s", tint = TitaniumSilver)
                    }

                    // Main Play/Pause Button
                    Surface(
                        shape = CircleShape,
                        color = DoomsdayEmerald,
                        modifier = Modifier
                            .size(46.dp)
                            .clickable { onPlayPause() }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    IconButton(onClick = { onSeekRelative(10) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.FastForward, "Forward 10s", tint = TitaniumSilver)
                    }

                    IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.NavigateNext, "Next", tint = TitaniumWhite)
                    }
                }

                // Right auxiliary controls: Zoom & Orientation
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(onClick = onOpenZoomCrop, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.CropFree,
                            contentDescription = "Zoom & Crop",
                            tint = DoomsdayEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // On-screen button: Toggle Portrait / Landscape / Auto
                    val bottomOrientationIcon = when (orientationMode) {
                        ScreenOrientationMode.PORTRAIT -> Icons.Default.StayCurrentLandscape
                        ScreenOrientationMode.LANDSCAPE, ScreenOrientationMode.REVERSE_LANDSCAPE -> Icons.Default.StayCurrentPortrait
                        ScreenOrientationMode.SENSOR -> Icons.Default.ScreenRotation
                    }

                    IconButton(
                        onClick = onToggleOrientation,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("video_orientation_bottom_btn")
                    ) {
                        Icon(
                            imageVector = bottomOrientationIcon,
                            contentDescription = "Toggle Orientation (Current: ${orientationMode.displayName})",
                            tint = DoomsdayCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackSelectionBottomSheetContent(
    availableSubtitles: List<com.example.model.SubtitleTrack>,
    selectedSubtitle: com.example.model.SubtitleTrack?,
    availableAudioTracks: List<com.example.model.AudioTrackInfo>,
    selectedAudioTrack: com.example.model.AudioTrackInfo?,
    volumeBoostPercent: Int,
    settings: PlayerSettings,
    onSelectSubtitle: (com.example.model.SubtitleTrack) -> Unit,
    onSelectAudioTrack: (com.example.model.AudioTrackInfo) -> Unit,
    onLoadExternalSubtitle: () -> Unit,
    onLoadExternalAudio: () -> Unit,
    onToggleSubtitleTransparency: () -> Unit,
    onSelectAudioMode: (AudioMode) -> Unit,
    onSetVolumeBoost: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "AUDIO & SUBTITLES STUDIO",
            color = DoomsdayEmerald,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // Volume Boost Slider (up to 200%)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (volumeBoostPercent > 100) Color(0xFFFF5252) else DoomsdayCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VOLUME BOOST (+15dB LOUDNESS)",
                        color = TitaniumWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "$volumeBoostPercent%",
                    color = if (volumeBoostPercent > 100) Color(0xFFFF5252) else DoomsdayCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Slider(
                value = volumeBoostPercent.toFloat(),
                onValueChange = { onSetVolumeBoost(it.toInt()) },
                valueRange = 100f..200f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = if (volumeBoostPercent > 100) Color(0xFFFF5252) else DoomsdayCyan,
                    activeTrackColor = if (volumeBoostPercent > 100) Color(0xFFFF5252) else DoomsdayCyan,
                    inactiveTrackColor = Color(0x40FFFFFF)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(100, 125, 150, 175, 200).forEach { boostLevel ->
                    val isSel = volumeBoostPercent == boostLevel
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSel) {
                            if (boostLevel > 100) Color(0xFFFF5252).copy(alpha = 0.3f) else DoomsdayCyan.copy(alpha = 0.3f)
                        } else DoomsdaySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSel) {
                                if (boostLevel > 100) Color(0xFFFF5252) else DoomsdayCyan
                            } else DoomsdayGlassBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSetVolumeBoost(boostLevel) }
                    ) {
                        Text(
                            text = "$boostLevel%",
                            color = if (boostLevel > 100) Color(0xFFFF5252) else TitaniumWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Audio Track Selection & External Audio Loading Section
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "AUDIO TRACKS & CODECS (AC3/EAC3/AAC/DTS)", color = TitaniumWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = onLoadExternalAudio,
                    colors = ButtonDefaults.buttonColors(containerColor = DoomsdaySurfaceVariant),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("+ Load External Audio (.m4a/.mp3)", color = DoomsdayCyan, fontSize = 10.sp)
                }
            }

            if (availableAudioTracks.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DoomsdaySurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Default Primary Audio Stream (Dolby Digital AC3/EAC3 Fallback Active)",
                        color = TitaniumSilver,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                availableAudioTracks.forEach { track ->
                    val isSelected = selectedAudioTrack?.id == track.id
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) DoomsdayCyan.copy(alpha = 0.2f) else DoomsdaySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) DoomsdayCyan else DoomsdayGlassBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectAudioTrack(track) }
                            .padding(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = track.name,
                                    color = if (isSelected) DoomsdayCyan else TitaniumWhite,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "${track.codec} • ${track.channels} • ${track.sampleRate}",
                                    color = TitaniumMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (isSelected) {
                                DoomsdayGlowingBadge(text = "ACTIVE", accentColor = DoomsdayCyan)
                            }
                        }
                    }
                }
            }
        }

        // Subtitles section
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "SUBTITLE TRACKS", color = TitaniumWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = onLoadExternalSubtitle,
                    colors = ButtonDefaults.buttonColors(containerColor = DoomsdaySurfaceVariant),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("+ Load External (.srt/.vtt)", color = DoomsdayCyan, fontSize = 10.sp)
                }
            }

            availableSubtitles.forEach { sub ->
                val isSelected = selectedSubtitle?.id == sub.id
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) DoomsdayEmerald.copy(alpha = 0.2f) else DoomsdaySurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) DoomsdayEmerald else DoomsdayGlassBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectSubtitle(sub) }
                        .padding(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sub.name,
                            color = if (isSelected) DoomsdayEmerald else TitaniumWhite,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            DoomsdayGlowingBadge(text = "ACTIVE", accentColor = DoomsdayEmerald)
                        }
                    }
                }
            }
        }

        // Audio Modes
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "DOLBY & SPATIAL AUDIO MODE", color = TitaniumWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            AudioMode.values().forEach { mode ->
                val isSelected = settings.audioMode == mode
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) DoomsdayCyan.copy(alpha = 0.2f) else DoomsdaySurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) DoomsdayCyan else DoomsdayGlassBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectAudioMode(mode) }
                        .padding(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mode.displayName,
                            color = if (isSelected) DoomsdayCyan else TitaniumWhite,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            DoomsdayGlowingBadge(text = "ENGAGED", accentColor = DoomsdayCyan)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun EngineQuickTuningSheetContent(
    settings: PlayerSettings,
    activeDecoder: DecoderMode,
    orientationMode: ScreenOrientationMode,
    onSelectDecoder: (DecoderMode) -> Unit,
    onSelectOrientation: (ScreenOrientationMode) -> Unit,
    onSelectEngine: (RenderEngine) -> Unit,
    onSelectPerformanceMode: (PerformanceMode) -> Unit,
    onSelectGpuApi: (GpuApi) -> Unit,
    onToggleHdr: () -> Unit,
    onToggleDolbyVision: () -> Unit,
    onToggleAmbient: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "DOOMSDAY GPU & ENGINE TUNING",
            color = DoomsdayEmerald,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // Screen Orientation Mode Selector
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "SCREEN / VIDEO ORIENTATION", color = TitaniumMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ScreenOrientationMode.values().forEach { mode ->
                    val isSelected = orientationMode == mode
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) DoomsdayCyan.copy(alpha = 0.25f) else DoomsdaySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) DoomsdayCyan else DoomsdayGlassBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectOrientation(mode) }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = mode.displayName,
                            color = if (isSelected) DoomsdayCyan else TitaniumSilver,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Hardware / Software Decoders
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "HARDWARE / SOFTWARE DECODER", color = TitaniumMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DecoderMode.values().forEach { dec ->
                    val isSelected = activeDecoder == dec
                    val decColor = when (dec) {
                        DecoderMode.HW_PLUS -> DoomsdayEmerald
                        DecoderMode.HW -> DoomsdayCyan
                        DecoderMode.SW -> VulkanRed
                        DecoderMode.VULKAN_DIRECT -> HdrGold
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) decColor.copy(alpha = 0.2f) else DoomsdaySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) decColor else DoomsdayGlassBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectDecoder(dec) }
                            .padding(vertical = 8.dp),
                        content = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = dec.displayName,
                                    color = if (isSelected) decColor else TitaniumSilver,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    )
                }
            }
        }

        // 3 Engine Selector
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "RENDER ENGINE", color = TitaniumMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RenderEngine.values().forEach { eng ->
                    val isSelected = settings.renderEngine == eng
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) DoomsdayEmerald.copy(alpha = 0.2f) else DoomsdaySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) DoomsdayEmerald else DoomsdayGlassBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectEngine(eng) }
                            .padding(vertical = 8.dp),
                        content = {
                            Text(
                                text = eng.displayName,
                                color = if (isSelected) DoomsdayEmerald else TitaniumSilver,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }
            }
        }

        // 3 Performance Modes
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "PERFORMANCE MODE", color = TitaniumMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PerformanceMode.values().forEach { mode ->
                    val isSelected = settings.performanceMode == mode
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) DoomsdayCyan.copy(alpha = 0.2f) else DoomsdaySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) DoomsdayCyan else DoomsdayGlassBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectPerformanceMode(mode) }
                            .padding(vertical = 8.dp),
                        content = {
                            Text(
                                text = mode.displayName,
                                color = if (isSelected) DoomsdayCyan else TitaniumSilver,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }
            }
        }

        // Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Vulkan Direct HDR Output", color = TitaniumWhite, fontSize = 13.sp)
            Switch(
                checked = settings.hdrOutputEnabled,
                onCheckedChange = { onToggleHdr() },
                colors = SwitchDefaults.colors(checkedThumbColor = HdrGold)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Dolby Vision PQ Tone-Mapping", color = TitaniumWhite, fontSize = 13.sp)
            Switch(
                checked = settings.dolbyVisionToneMap,
                onCheckedChange = { onToggleDolbyVision() },
                colors = SwitchDefaults.colors(checkedThumbColor = DolbyVisionPurple)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Ambient Glow Lighting", color = TitaniumWhite, fontSize = 13.sp)
            Switch(
                checked = settings.ambientModeEnabled,
                onCheckedChange = { onToggleAmbient() },
                colors = SwitchDefaults.colors(checkedThumbColor = DoomsdayEmerald)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun formatDurationSeconds(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}
