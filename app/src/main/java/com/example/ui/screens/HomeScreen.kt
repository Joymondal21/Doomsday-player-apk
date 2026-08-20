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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DoomsdayObsidian,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch("video/*") },
                containerColor = DoomsdayEmerald,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("import_video_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = "Open File")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Video", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // TOP HEADER: Branding on top left + Quick Actions
            TopBrandingHeader(
                settings = settings,
                onRefresh = { viewModel.refreshVideos() },
                onTuningClick = { viewModel.setSelectedTab(3) }
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
    }
}

@Composable
private fun TopBrandingHeader(
    settings: PlayerSettings,
    onRefresh: () -> Unit,
    onTuningClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TOP LEFT: Doomsday Player name & branding
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DoomsdayEmerald.copy(alpha = 0.8f),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .border(1.5.dp, DoomsdayEmerald, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "DOOMSDAY PLAYER",
                    color = TitaniumWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "GPU VULKAN HQ",
                        color = DoomsdayEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(text = "•", color = TitaniumMuted, fontSize = 10.sp)
                    Text(
                        text = settings.renderEngine.tag,
                        color = DoomsdayCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(text = "•", color = TitaniumMuted, fontSize = 10.sp)
                    Text(
                        text = "SD888",
                        color = DoomsdayAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Action buttons
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Videos",
                    tint = TitaniumSilver
                )
            }

            IconButton(
                onClick = onTuningClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "GPU Settings",
                    tint = DoomsdayEmerald
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "STORAGE DIRECTORIES (${folders.size})",
                color = TitaniumMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        items(folders) { folder ->
            DoomsdayGlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (folder.videos.isNotEmpty()) {
                        onPlayVideo(folder.videos.first())
                    }
                }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = DoomsdayCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = folder.folderName,
                                color = TitaniumWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${folder.videos.size} videos • ${folder.totalSizeFormatted} • ${folder.totalDurationFormatted}",
                                color = TitaniumMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = folder.folderPath,
                                color = TitaniumMuted.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (folder.videos.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(folder.videos.take(6)) { vid ->
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DoomsdayObsidian)
                                        .border(1.dp, DoomsdayGlassBorder, RoundedCornerShape(8.dp))
                                        .clickable { onPlayVideo(vid) }
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = vid.title,
                                            color = TitaniumSilver,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = vid.durationFormatted,
                                            color = DoomsdayEmerald,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
