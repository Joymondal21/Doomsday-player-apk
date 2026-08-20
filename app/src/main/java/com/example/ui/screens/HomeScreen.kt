package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DisplayFieldsConfig
import com.example.model.MediaLayoutMode
import com.example.model.MediaViewMode
import com.example.model.QuickSettingsState
import com.example.model.SortField
import com.example.model.SortOrder
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.PlaybackHistoryEntity
import com.example.model.AudioMode
import com.example.model.DeviceProfile
import com.example.model.GpuApi
import com.example.model.PerformanceMode
import com.example.model.PlayerSettings
import com.example.model.RenderEngine
import com.example.model.VideoFolder
import com.example.model.VideoItem
import com.example.ui.components.DoomsdayGlassCard
import com.example.ui.components.DoomsdayGlowingBadge
import com.example.ui.components.EngineBadge
import com.example.ui.components.HdrBadge
import com.example.ui.theme.DolbyVisionPurple
import com.example.ui.theme.DoomsdayAmber
import com.example.ui.theme.DoomsdayCyan
import com.example.ui.theme.DoomsdayCrimson
import com.example.ui.theme.DoomsdayEmerald
import com.example.ui.theme.DoomsdayEmeraldDark
import com.example.ui.theme.DoomsdayGlassBg
import com.example.ui.theme.DoomsdayGlassBorder
import com.example.ui.theme.DoomsdayObsidian
import com.example.ui.theme.DoomsdaySurface
import com.example.ui.theme.DoomsdaySurfaceVariant
import com.example.ui.theme.HdrGold
import com.example.ui.theme.TitaniumMuted
import com.example.ui.theme.TitaniumSilver
import com.example.ui.theme.TitaniumWhite
import com.example.ui.theme.VulkanRed
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onPlayVideo: (video: VideoItem, playlist: List<VideoItem>, resumeTimestamp: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoList by viewModel.videoList.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isLoading by viewModel.isLoadingVideos.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val hdrOnlyFilter by viewModel.selectedFilterHdrOnly.collectAsState()
    val selectedVideoForInfo by viewModel.selectedVideoForInfo.collectAsState()
    val videoMetadata by viewModel.videoMetadataDetails.collectAsState()

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "External Cinema Video"
            viewModel.addImportedVideo(uri, fileName)
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.refreshVideos()
    }

    var showQuickSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch("video/*") },
                containerColor = Color(0xFF22C55E),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("import_video_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        bottomBar = {
            NextPlayerBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tabIndex -> viewModel.setSelectedTab(tabIndex) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // TOP HEADER: Next Player branding on top left + Search, Grid & Settings
            TopBrandingHeader(
                settings = settings,
                onSearchClick = { viewModel.setSelectedTab(0) },
                onQuickSettingsClick = { showQuickSettingsDialog = true },
                onTuningClick = { showQuickSettingsDialog = true }
            )

            // Quick Hardware Mode Switcher Bar
            QuickHardwareModeBar(
                settings = settings,
                onHdrToggle = { viewModel.toggleHdrOutput() },
                onPerformanceModeChange = { viewModel.setPerformanceMode(it) },
                onEngineChange = { viewModel.setRenderEngine(it) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Navigation Tabs
            val tabs = listOf("Videos", "Folders", "History", "Engine & GPU", "Audio & Subs")
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DoomsdaySurface,
                contentColor = DoomsdayEmerald,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = DoomsdayEmerald,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) DoomsdayEmerald else TitaniumMuted,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> AllVideosTabContent(
                    videoList = videoList,
                    historyList = historyList,
                    searchQuery = searchQuery,
                    hdrOnlyFilter = hdrOnlyFilter,
                    isLoading = isLoading,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onToggleHdrFilter = { viewModel.toggleHdrFilter() },
                    onPlayVideo = { video, resume -> onPlayVideo(video, videoList, resume) },
                    onInspectVideo = { viewModel.inspectVideoDetails(it) },
                    onShareVideo = { viewModel.shareVideo(it) },
                    onDeleteVideo = { viewModel.deleteVideo(it) }
                )
                1 -> FoldersTabContent(
                    folders = viewModel.getFolders(),
                    onPlayVideo = { video -> onPlayVideo(video, videoList, null) }
                )
                2 -> HistoryTabContent(
                    historyList = historyList,
                    videoList = videoList,
                    onPlayHistory = { uri, pos ->
                        val video = videoList.find { it.uri == uri }
                            ?: VideoItem(
                                id = System.currentTimeMillis(),
                                uri = uri,
                                title = "Resumed Video",
                                durationMs = 0L,
                                sizeBytes = 0L,
                                path = uri
                            )
                        onPlayVideo(video, videoList, pos)
                    },
                    onDeleteHistoryItem = { viewModel.deleteHistoryItem(it) },
                    onClearAllHistory = { viewModel.clearAllHistory() }
                )
                3 -> EngineGpuTuningTabContent(
                    settings = settings,
                    viewModel = viewModel
                )
                4 -> AudioSubtitleStudioTabContent(
                    settings = settings,
                    viewModel = viewModel
                )
            }
        }

        // Detailed Metadata Dialog
        selectedVideoForInfo?.let { video ->
            VideoInfoDialog(
                video = video,
                metadata = videoMetadata,
                onDismiss = { viewModel.closeVideoDetails() },
                onPlay = { onPlayVideo(video, videoList, null) },
                onShare = { viewModel.shareVideo(video) },
                onDelete = { viewModel.deleteVideo(video) }
            )
        }

        // Quick Settings Dialog
        if (showQuickSettingsDialog) {
            QuickSettingsDialog(
                quickSettings = settings.quickSettings,
                onDismiss = { showQuickSettingsDialog = false },
                onSave = { updatedQs ->
                    viewModel.updateSettings { it.copy(quickSettings = updatedQs) }
                    showQuickSettingsDialog = false
                    when (updatedQs.mediaViewMode) {
                        MediaViewMode.VIDEOS -> viewModel.setSelectedTab(0)
                        MediaViewMode.FOLDERS, MediaViewMode.TREE -> viewModel.setSelectedTab(1)
                    }
                }
            )
        }
    }
}

@Composable
private fun TopBrandingHeader(
    settings: PlayerSettings,
    onSearchClick: () -> Unit,
    onQuickSettingsClick: () -> Unit,
    onTuningClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TOP LEFT: Next Player branding title
        Text(
            text = "Next Player",
            color = TitaniumWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )

        // Right Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TitaniumWhite,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onQuickSettingsClick,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("quick_settings_top_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "Layout View",
                    tint = TitaniumWhite,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onTuningClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TitaniumWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickHardwareModeBar(
    settings: PlayerSettings,
    onHdrToggle: () -> Unit,
    onPerformanceModeChange: (PerformanceMode) -> Unit,
    onEngineChange: (RenderEngine) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // HDR Toggle Chip
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (settings.hdrOutputEnabled) HdrGold.copy(alpha = 0.25f) else DoomsdaySurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (settings.hdrOutputEnabled) HdrGold else DoomsdayGlassBorder
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onHdrToggle() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (settings.hdrOutputEnabled) HdrGold else TitaniumMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (settings.hdrOutputEnabled) "VULKAN HDR: ON" else "HDR: OFF",
                    color = if (settings.hdrOutputEnabled) HdrGold else TitaniumMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Performance Mode Selector Chips
        PerformanceMode.values().forEach { mode ->
            val isSelected = settings.performanceMode == mode
            val color = when (mode) {
                PerformanceMode.POWER_SAVING -> DoomsdayCyan
                PerformanceMode.BALANCE -> DoomsdayAmber
                PerformanceMode.GPU_HQ_MPVRX -> DoomsdayEmerald
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) color.copy(alpha = 0.22f) else DoomsdaySurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) color else DoomsdayGlassBorder.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onPerformanceModeChange(mode) }
            ) {
                Text(
                    text = mode.displayName,
                    color = if (isSelected) color else TitaniumSilver,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Sharp AQUOS R6 Badge
        DoomsdayGlowingBadge(
            text = "Sharp AQUOS R6 12GB",
            accentColor = DoomsdayCyan,
            icon = Icons.Default.Memory
        )
    }
}

@Composable
private fun AllVideosTabContent(
    videoList: List<VideoItem>,
    historyList: List<PlaybackHistoryEntity>,
    searchQuery: String,
    hdrOnlyFilter: Boolean,
    isLoading: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onToggleHdrFilter: () -> Unit,
    onPlayVideo: (VideoItem, Long?) -> Unit,
    onInspectVideo: (VideoItem) -> Unit,
    onShareVideo: (VideoItem) -> Unit,
    onDeleteVideo: (VideoItem) -> Unit
) {
    val filteredVideos = remember(videoList, searchQuery, hdrOnlyFilter) {
        videoList.filter { video ->
            val matchesQuery = searchQuery.isBlank() ||
                video.title.contains(searchQuery, ignoreCase = true) ||
                video.path.contains(searchQuery, ignoreCase = true)
            val matchesHdr = !hdrOnlyFilter || video.isHdr
            matchesQuery && matchesHdr
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Featured Hero Cinema Banner
        item {
            FeaturedCinemaHeroCard(
                onPlayFeatured = {
                    if (videoList.isNotEmpty()) {
                        onPlayVideo(videoList.first(), null)
                    }
                }
            )
        }

        // Continue Watching / Left to Watch History Carousel
        if (historyList.isNotEmpty()) {
            item {
                ContinueWatchingSection(
                    historyList = historyList,
                    videoList = videoList,
                    onPlay = { uri, pos ->
                        val item = videoList.find { it.uri == uri }
                            ?: VideoItem(
                                id = System.currentTimeMillis(),
                                uri = uri,
                                title = "Resumed Video",
                                durationMs = 0L,
                                sizeBytes = 0L,
                                path = uri
                            )
                        onPlayVideo(item, pos)
                    }
                )
            }
        }

        // Search & Filter Row
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search 4K / HDR videos or storage paths...", color = TitaniumMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TitaniumSilver)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TitaniumSilver)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DoomsdayEmerald,
                        unfocusedBorderColor = DoomsdayGlassBorder,
                        focusedContainerColor = DoomsdaySurfaceVariant,
                        unfocusedContainerColor = DoomsdaySurfaceVariant,
                        focusedTextColor = TitaniumWhite,
                        unfocusedTextColor = TitaniumWhite
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIBRARY (${filteredVideos.size} VIDEOS)",
                        color = TitaniumMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    FilterChip(
                        selected = hdrOnlyFilter,
                        onClick = onToggleHdrFilter,
                        label = {
                            Text(
                                text = "HDR10 & DV Only",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (hdrOnlyFilter) HdrGold else TitaniumMuted
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HdrGold.copy(alpha = 0.2f),
                            selectedLabelColor = HdrGold,
                            containerColor = DoomsdaySurfaceVariant,
                            labelColor = TitaniumSilver
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = hdrOnlyFilter,
                            selectedBorderColor = HdrGold,
                            borderColor = DoomsdayGlassBorder
                        )
                    )
                }
            }
        }

        // Loading or Empty or Video List
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DoomsdayEmerald)
                }
            }
        } else if (filteredVideos.isEmpty()) {
            item {
                EmptyVideosPlaceholder(onImport = { onSearchQueryChange("") })
            }
        } else {
            items(filteredVideos, key = { it.id }) { video ->
                VideoCardItem(
                    video = video,
                    onPlay = { onPlayVideo(video, null) },
                    onInspect = { onInspectVideo(video) },
                    onShare = { onShareVideo(video) },
                    onDelete = { onDeleteVideo(video) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun FeaturedCinemaHeroCard(onPlayFeatured: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(DoomsdaySurfaceVariant)
            .border(1.dp, DoomsdayEmerald.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .clickable { onPlayFeatured() }
    ) {
        // Hero Image
        Image(
            painter = painterResource(id = R.drawable.img_cinema_hero),
            contentDescription = "Doomsday Cinema Engine",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x99000000),
                            Color(0xFA070A11)
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HdrBadge(isDolbyVision = true)
                    DoomsdayGlowingBadge(text = "8K VULKAN", accentColor = DoomsdayEmerald)
                }

                Surface(
                    shape = CircleShape,
                    color = DoomsdayEmerald,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Column {
                Text(
                    text = "Doomsday Ultra 4K HDR60 Benchmark",
                    color = TitaniumWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Snapdragon 888 • Direct Vulkan 1.3 • Dolby Atmos 7.1 Spatializer",
                    color = DoomsdayCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ContinueWatchingSection(
    historyList: List<PlaybackHistoryEntity>,
    videoList: List<VideoItem>,
    onPlay: (String, Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = DoomsdayEmerald,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CONTINUE WATCHING",
                    color = TitaniumWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(historyList.take(6)) { history ->
                val progress = if (history.durationMs > 0) {
                    (history.lastPositionMs.toFloat() / history.durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                val posMin = (history.lastPositionMs / 1000) / 60
                val posSec = (history.lastPositionMs / 1000) % 60

                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DoomsdaySurfaceVariant)
                        .border(1.dp, DoomsdayGlassBorder, RoundedCornerShape(12.dp))
                        .clickable { onPlay(history.videoUri, history.lastPositionMs) }
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = history.title,
                                color = TitaniumWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                tint = DoomsdayEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Left at ${String.format("%02d:%02d", posMin, posSec)} • ${history.engineUsed}",
                            color = DoomsdayCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = DoomsdayEmerald,
                            trackColor = Color(0x40FFFFFF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoCardItem(
    video: VideoItem,
    onPlay: () -> Unit,
    onInspect: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    DoomsdayGlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onPlay
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail or Icon
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DoomsdaySurfaceVariant)
                    .border(1.dp, DoomsdayGlassBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (video.thumbnailUri != null) {
                    AsyncImage(
                        model = video.thumbnailUri,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = DoomsdayEmerald,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Duration badge on thumbnail
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = video.durationFormatted,
                        color = TitaniumWhite,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = TitaniumWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (video.isHdr) {
                        HdrBadge(isDolbyVision = video.isDolbyVision)
                    }
                    DoomsdayGlowingBadge(text = video.resolution, accentColor = DoomsdayCyan)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${video.sizeFormatted} • ${video.codec}",
                    color = TitaniumMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Options",
                        tint = TitaniumSilver,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(DoomsdaySurfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play Video", color = DoomsdayEmerald) },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = DoomsdayEmerald) },
                        onClick = {
                            showMenu = false
                            onPlay()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Media Specifications", color = TitaniumWhite) },
                        leadingIcon = { Icon(Icons.Default.Info, null, tint = DoomsdayCyan) },
                        onClick = {
                            showMenu = false
                            onInspect()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share Video", color = TitaniumWhite) },
                        leadingIcon = { Icon(Icons.Default.Share, null, tint = TitaniumSilver) },
                        onClick = {
                            showMenu = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = DoomsdayCrimson) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = DoomsdayCrimson) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FoldersTabContent(
    folders: List<VideoFolder>,
    onPlayVideo: (VideoItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(folders) { folder ->
            NextPlayerFolderCard(
                folder = folder,
                onClick = {
                    if (folder.videos.isNotEmpty()) {
                        onPlayVideo(folder.videos.first())
                    }
                }
            )
        }
    }
}

@Composable
private fun NextPlayerFolderCard(
    folder: VideoFolder,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail box with folder icon and duration overlay
        Box(
            modifier = Modifier
                .size(width = 92.dp, height = 66.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E222B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = Color(0xFF2D3748),
                modifier = Modifier.size(42.dp)
            )

            // Duration badge in bottom-right corner
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xE6000000),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            ) {
                Text(
                    text = folder.totalDurationFormatted,
                    color = TitaniumWhite,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title, Path, and Badges Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Green Folder Title
            Text(
                text = folder.folderName,
                color = Color(0xFF22C55E),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Grey Path Subtext
            Text(
                text = folder.folderPath,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Badges row: "X Videos" & "X.XX GB"
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = "${folder.videos.size} Videos",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = folder.totalSizeFormatted,
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NextPlayerBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Tab
            val isHome = selectedTab == 0 || selectedTab == 1
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(0) }
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isHome) Color(0xFF4A2B38) else Color.Transparent)
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (isHome) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Home",
                    color = if (isHome) Color.White else Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Network Tab
            val isNetwork = selectedTab == 3 || selectedTab == 4
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(3) }
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isNetwork) Color(0xFF4A2B38) else Color.Transparent)
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = "Network",
                        tint = if (isNetwork) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Network",
                    color = if (isNetwork) Color.White else Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = if (isNetwork) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun HistoryTabContent(
    historyList: List<PlaybackHistoryEntity>,
    videoList: List<VideoItem>,
    onPlayHistory: (String, Long) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearAllHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PLAYBACK HISTORY (${historyList.size})",
                    color = TitaniumMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                if (historyList.isNotEmpty()) {
                    Button(
                        onClick = onClearAllHistory,
                        colors = ButtonDefaults.buttonColors(containerColor = DoomsdayCrimson.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Clear All", color = DoomsdayCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (historyList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No playback history recorded yet.", color = TitaniumMuted, fontSize = 13.sp)
                }
            }
        } else {
            items(historyList) { item ->
                DoomsdayGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onPlayHistory(item.videoUri, item.lastPositionMs) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = DoomsdayEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = TitaniumWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val posMin = (item.lastPositionMs / 1000) / 60
                            val posSec = (item.lastPositionMs / 1000) % 60
                            Text(
                                text = "Paused at ${String.format("%02d:%02d", posMin, posSec)} • Engine: ${item.engineUsed}",
                                color = DoomsdayCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(onClick = { onDeleteHistoryItem(item.videoUri) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TitaniumMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EngineGpuTuningTabContent(
    settings: PlayerSettings,
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: 3 Render Engines
        DoomsdayGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "CORE PLAYBACK ENGINE",
                    color = DoomsdayEmerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                RenderEngine.values().forEach { engine ->
                    val isSelected = settings.renderEngine == engine
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) DoomsdayEmerald.copy(alpha = 0.18f) else DoomsdaySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) DoomsdayEmerald else DoomsdayGlassBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.setRenderEngine(engine) }
                            .padding(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = engine.displayName,
                                    color = if (isSelected) DoomsdayEmerald else TitaniumWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = engine.description,
                                    color = TitaniumMuted,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                DoomsdayGlowingBadge(text = "ACTIVE", accentColor = DoomsdayEmerald)
                            }
                        }
                    }
                }
            }
        }

        // Section: 3 Performance Modes
        DoomsdayGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "PERFORMANCE & THERMAL PROFILE",
                    color = DoomsdayCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                PerformanceMode.values().forEach { mode ->
                    val isSelected = settings.performanceMode == mode
                    val color = when (mode) {
                        PerformanceMode.POWER_SAVING -> DoomsdayCyan
                        PerformanceMode.BALANCE -> DoomsdayAmber
                        PerformanceMode.GPU_HQ_MPVRX -> DoomsdayEmerald
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) color.copy(alpha = 0.18f) else DoomsdaySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) color else DoomsdayGlassBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.setPerformanceMode(mode) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.displayName,
                                    color = if (isSelected) color else TitaniumWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = mode.detail,
                                    color = TitaniumMuted,
                                    fontSize = 11.sp
                                )
                            }
                            DoomsdayGlowingBadge(text = "${mode.targetFps} FPS", accentColor = color)
                        }
                    }
                }
            }
        }

        // Section: GPU API & Hardware
        DoomsdayGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "GPU API & DRIVER PIPELINE",
                    color = VulkanRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                GpuApi.values().forEach { api ->
                    val isSelected = settings.gpuApi == api
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) VulkanRed.copy(alpha = 0.18f) else DoomsdaySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) VulkanRed else DoomsdayGlassBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.setGpuApi(api) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = api.displayName,
                                color = if (isSelected) VulkanRed else TitaniumWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = api.spec,
                                color = TitaniumMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Section: Device Optimization & Features Toggles
        DoomsdayGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "CINEMA ENGINE FEATURES",
                    color = HdrGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                TuningToggleRow(
                    title = "Vulkan Direct HDR Output",
                    desc = "BT.2020 Color Space with SMPTE ST 2084 PQ Curve",
                    checked = settings.hdrOutputEnabled,
                    onCheckedChange = { viewModel.toggleHdrOutput() }
                )

                TuningToggleRow(
                    title = "Dolby Vision PQ Tone-Mapper",
                    desc = "Dynamic frame-by-frame ICtCp gamut mapping",
                    checked = settings.dolbyVisionToneMap,
                    onCheckedChange = { viewModel.toggleDolbyVisionToneMap() }
                )

                TuningToggleRow(
                    title = "YouTube-Style Ambient Mode",
                    desc = "Dynamic glowing ambient perimeter light around video",
                    checked = settings.ambientModeEnabled,
                    onCheckedChange = { viewModel.toggleAmbientMode() }
                )

                TuningToggleRow(
                    title = "Real-Time Diagnostic OSD HUD",
                    desc = "Live FPS, RAM Heap MB, GPU Load, Dropped Frames meter",
                    checked = settings.showDiagnosticHud,
                    onCheckedChange = { viewModel.toggleDiagnosticHud() }
                )
            }
        }

        // Section: Avengers Doomsday Demo & Benchmark Media
        DoomsdayGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "DEMO & BENCHMARK MEDIA",
                    color = DoomsdayAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "Manage high-bitrate 4K HDR benchmark files including the Avengers: Doomsday IMAX demo video.",
                    color = TitaniumMuted,
                    fontSize = 11.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (viewModel.isAvengersDemoPresent()) {
                                viewModel.removeAvengersDemoVideo()
                            } else {
                                viewModel.addAvengersDemoVideo()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.isAvengersDemoPresent()) DoomsdayCrimson.copy(alpha = 0.8f) else DoomsdayEmerald.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (viewModel.isAvengersDemoPresent()) "Remove Avengers Demo" else "Add Avengers Demo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TitaniumWhite
                        )
                    }

                    Button(
                        onClick = { viewModel.restoreAllDemoVideos() },
                        colors = ButtonDefaults.buttonColors(containerColor = DoomsdaySurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Restore All Demos",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TitaniumSilver
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioSubtitleStudioTabContent(
    settings: PlayerSettings,
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Audio Modes
        DoomsdayGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SPECIAL AUDIO MODES",
                    color = DoomsdayEmerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                AudioMode.values().forEach { mode ->
                    val isSelected = settings.audioMode == mode
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) DoomsdayEmerald.copy(alpha = 0.18f) else DoomsdaySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) DoomsdayEmerald else DoomsdayGlassBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.setAudioMode(mode) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.displayName,
                                    color = if (isSelected) DoomsdayEmerald else TitaniumWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = mode.detail,
                                    color = TitaniumMuted,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                DoomsdayGlowingBadge(text = "ENGAGED", accentColor = DoomsdayEmerald)
                            }
                        }
                    }
                }
            }
        }

        // Subtitle Settings
        DoomsdayGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "TRANSPARENT SUBTITLE ENGINE",
                    color = DoomsdayCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                TuningToggleRow(
                    title = "Transparent Subtitle Background",
                    desc = "Zero-box background with high-contrast text shadow",
                    checked = settings.subtitleBackgroundTransparent,
                    onCheckedChange = { viewModel.toggleSubtitleTransparency() }
                )

                Column {
                    Text(
                        text = "Subtitle Font Size: ${settings.subtitleFontSizeSp.toInt()} sp",
                        color = TitaniumWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = settings.subtitleFontSizeSp,
                        onValueChange = { viewModel.setSubtitleFontSize(it) },
                        valueRange = 12f..32f,
                        colors = SliderDefaults.colors(
                            thumbColor = DoomsdayCyan,
                            activeTrackColor = DoomsdayCyan,
                            inactiveTrackColor = DoomsdaySurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TuningToggleRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TitaniumWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = TitaniumMuted, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DoomsdayEmerald,
                checkedTrackColor = DoomsdayEmeraldDark,
                uncheckedThumbColor = TitaniumMuted,
                uncheckedTrackColor = DoomsdaySurfaceVariant
            )
        )
    }
}

@Composable
private fun EmptyVideosPlaceholder(onImport: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = TitaniumMuted,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No videos matching criteria",
                color = TitaniumSilver,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun QuickSettingsDialog(
    quickSettings: QuickSettingsState,
    onDismiss: () -> Unit,
    onSave: (QuickSettingsState) -> Unit
) {
    var state by remember { mutableStateOf(quickSettings) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = DoomsdaySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DoomsdayGlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Quick Settings",
                    color = TitaniumWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                HorizontalDivider(color = DoomsdayGlassBorder)

                // 1. Media View Mode
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Media view mode", color = TitaniumMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DoomsdaySurfaceVariant, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MediaViewMode.values().forEach { mode ->
                            val isSel = state.mediaViewMode == mode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) DoomsdayEmerald else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { state = state.copy(mediaViewMode = mode) }
                            ) {
                                Text(
                                    text = mode.displayName,
                                    color = if (isSel) Color.Black else TitaniumWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Media Layout
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Media Layout", color = TitaniumMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DoomsdaySurfaceVariant, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MediaLayoutMode.values().forEach { mode ->
                            val isSel = state.mediaLayoutMode == mode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) DoomsdayEmerald else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { state = state.copy(mediaLayoutMode = mode) }
                            ) {
                                Text(
                                    text = mode.displayName,
                                    color = if (isSel) Color.Black else TitaniumWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Sort
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Sort", color = TitaniumMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SortField.values().forEach { field ->
                            val isSel = state.sortField == field
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) DoomsdayEmerald.copy(alpha = 0.25f) else DoomsdaySurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) DoomsdayEmerald else DoomsdayGlassBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { state = state.copy(sortField = field) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = field.iconCode, fontSize = 12.sp)
                                    Text(
                                        text = field.displayName,
                                        color = if (isSel) DoomsdayEmerald else TitaniumSilver,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DoomsdaySurfaceVariant, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SortOrder.values().forEach { order ->
                            val isSel = state.sortOrder == order
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) DoomsdayCyan else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { state = state.copy(sortOrder = order) }
                            ) {
                                Text(
                                    text = order.displayName,
                                    color = if (isSel) Color.Black else TitaniumWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // 4. Fields Checklist
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Fields", color = TitaniumMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    val df = state.displayFields
                    val fieldItems = listOf(
                        "Duration" to df.showDuration,
                        "Folder duration" to df.showFolderDuration,
                        "Extension" to df.showExtension,
                        "Path" to df.showPath,
                        "Played progress" to df.showPlayedProgress,
                        "Resolution" to df.showResolution,
                        "Size" to df.showSize,
                        "Thumbnail" to df.showThumbnail
                    )

                    val rows = fieldItems.chunked(2)
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { (label, isChecked) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isChecked) DoomsdayEmerald.copy(alpha = 0.2f) else DoomsdaySurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isChecked) DoomsdayEmerald else DoomsdayGlassBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val newDf = when (label) {
                                                "Duration" -> df.copy(showDuration = !df.showDuration)
                                                "Folder duration" -> df.copy(showFolderDuration = !df.showFolderDuration)
                                                "Extension" -> df.copy(showExtension = !df.showExtension)
                                                "Path" -> df.copy(showPath = !df.showPath)
                                                "Played progress" -> df.copy(showPlayedProgress = !df.showPlayedProgress)
                                                "Resolution" -> df.copy(showResolution = !df.showResolution)
                                                "Size" -> df.copy(showSize = !df.showSize)
                                                "Thumbnail" -> df.copy(showThumbnail = !df.showThumbnail)
                                                else -> df
                                            }
                                            state = state.copy(displayFields = newDf)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isChecked) "✓ " else "  ",
                                            color = DoomsdayEmerald,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = label,
                                            color = if (isChecked) TitaniumWhite else TitaniumMuted,
                                            fontSize = 11.sp,
                                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                            if (rowItems.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                HorizontalDivider(color = DoomsdayGlassBorder)

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = DoomsdaySurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TitaniumWhite)
                    }

                    Button(
                        onClick = { onSave(state) },
                        colors = ButtonDefaults.buttonColors(containerColor = DoomsdayEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
