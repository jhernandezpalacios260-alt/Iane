package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AiStudioScreen
import com.example.ui.screens.AvatarStudioScreen
import com.example.ui.screens.EditorStudioScreen
import com.example.ui.screens.PostProductionScreen
import com.example.ui.theme.*
import com.example.ui.voice.VoiceCommandModal
import com.example.viewmodel.CineGenViewModel

enum class CineTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    EDITOR("Estudio", Icons.Default.Movie),
    AI_GEN("Generador IA", Icons.Default.AutoAwesome),
    AVATARS("Avatares 100% Gratis", Icons.Default.Face),
    POST("Post 4K", Icons.Default.Tune)
}

class MainActivity : ComponentActivity() {

    private val viewModel: CineGenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CineGenTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: CineGenViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(CineTab.EDITOR) }
    var showVoiceModal by remember { mutableStateOf(false) }

    val project by viewModel.project.collectAsStateWithLifecycle()
    val currentPlaybackSeconds by viewModel.currentPlaybackSeconds.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val selectedShotIndex by viewModel.selectedShotIndex.collectAsStateWithLifecycle()
    val lastVoiceCommand by viewModel.lastVoiceCommand.collectAsStateWithLifecycle()

    val isListening by viewModel.voiceEngine.isListening.collectAsStateWithLifecycle()
    val liveSpokenText by viewModel.voiceEngine.liveSpokenText.collectAsStateWithLifecycle()
    val audioRms by viewModel.voiceEngine.audioRms.collectAsStateWithLifecycle()

    // Permission launcher for speech recognition (RECORD_AUDIO)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showVoiceModal = true
            viewModel.voiceEngine.startListening()
        } else {
            // Still allow voice command modal with text typing & quick chips
            showVoiceModal = true
            Toast.makeText(
                context,
                "Permiso de micrófono no otorgado. Puedes usar los comandos sugeridos o escribir.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val requestVoiceStart = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            showVoiceModal = true
            viewModel.voiceEngine.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Handle back button when not on main Editor tab
    BackHandler(enabled = selectedTab != CineTab.EDITOR) {
        selectedTab = CineTab.EDITOR
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CineVoid,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // App Logo icon
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    brush = Brush.radialGradient(listOf(CineGold, CineSurfaceElevated)),
                                    shape = CircleShape
                                )
                                .border(1.dp, CineGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "CineGen Logo",
                                tint = CineVoid,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "CineGen AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CineTextPrimary
                                )
                                Text(
                                    text = "PRO 4K",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CineGold,
                                    modifier = Modifier
                                        .background(CineGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = project.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = CineTextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    // Quick Voice Command Trigger in Top Bar
                    FilledTonalButton(
                        onClick = { requestVoiceStart() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CineCyan.copy(alpha = 0.2f),
                            contentColor = CineCyan
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("topbar_voice_button")
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Comando de voz", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Comando", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CineSurface,
                    titleContentColor = CineTextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CineSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                CineTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = when (tab) {
                                    CineTab.EDITOR -> "Estudio"
                                    CineTab.AI_GEN -> "Generador"
                                    CineTab.AVATARS -> "Avatares"
                                    CineTab.POST -> "Post 4K"
                                },
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CineVoid,
                            selectedTextColor = CineGold,
                            indicatorColor = CineGold,
                            unselectedIconColor = CineTextSecondary,
                            unselectedTextColor = CineTextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                CineTab.EDITOR -> {
                    EditorStudioScreen(
                        project = project,
                        currentPlaybackSeconds = currentPlaybackSeconds,
                        isPlaying = isPlaying,
                        selectedShotIndex = selectedShotIndex,
                        lastVoiceCommandResult = lastVoiceCommand,
                        onTogglePlay = { viewModel.togglePlay() },
                        onSeekSeconds = { viewModel.seekSeconds(it) },
                        onSelectShot = { viewModel.selectShot(it) },
                        onUpdateProject = { viewModel.updateProject(it) },
                        onOpenVoiceModal = { requestVoiceStart() },
                        onNavigateToAvatars = { selectedTab = CineTab.AVATARS },
                        onNavigateToPostproduction = { selectedTab = CineTab.POST }
                    )
                }

                CineTab.AI_GEN -> {
                    AiStudioScreen(
                        currentProject = project,
                        onGenerateNewSequence = { prompt, genre, count ->
                            viewModel.generateNewSequence(prompt, genre, count)
                        },
                        onOpenEditorTab = { selectedTab = CineTab.EDITOR }
                    )
                }

                CineTab.AVATARS -> {
                    AvatarStudioScreen(
                        currentAvatarConfig = project.avatarConfig,
                        ttsController = viewModel.ttsController,
                        onUpdateAvatarConfig = { viewModel.updateAvatarConfig(it) }
                    )
                }

                CineTab.POST -> {
                    PostProductionScreen(
                        currentProject = project,
                        onUpdateProject = { viewModel.updateProject(it) }
                    )
                }
            }

            // Voice Command Modal Sheet
            if (showVoiceModal) {
                VoiceCommandModal(
                    isListening = isListening,
                    liveSpokenText = liveSpokenText,
                    audioRms = audioRms,
                    lastResult = lastVoiceCommand,
                    onStartListening = { viewModel.voiceEngine.startListening() },
                    onStopListening = { viewModel.voiceEngine.stopListening() },
                    onSubmitCommand = { cmd ->
                        viewModel.voiceEngine.stopListening()
                        viewModel.processVoiceCommand(cmd)
                        Toast.makeText(context, "Comando procesado: $cmd", Toast.LENGTH_SHORT).show()
                    },
                    onDismiss = {
                        viewModel.voiceEngine.stopListening()
                        showVoiceModal = false
                    }
                )
            }
        }
    }
}
