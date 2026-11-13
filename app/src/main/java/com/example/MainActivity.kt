package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ai.ChatMode
import com.example.ui.components.VikasTopBar
import com.example.ui.i18n.AppStrings
import com.example.ui.screens.*
import com.example.ui.theme.VikasAiTheme
import com.example.ui.viewmodel.VikasViewModel

enum class MainNavTab(
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector,
    val tag: String
) {
    HOME(Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    CHAT(Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_chat"),
    CREATE(Icons.Filled.Palette, Icons.Outlined.Palette, "nav_create"),
    TOOLS(Icons.Filled.Build, Icons.Outlined.Build, "nav_tools"),
    SETTINGS(Icons.Filled.Tune, Icons.Outlined.Tune, "nav_settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: VikasViewModel = viewModel(
                factory = VikasViewModel.provideFactory(applicationContext)
            )
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            VikasAiTheme(darkTheme = settings.isDarkTheme) {
                VikasAppRoot(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VikasAppRoot(viewModel: VikasViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val creations by viewModel.savedCreations.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val pendingAttachment by viewModel.pendingChatAttachment.collectAsStateWithLifecycle()

    val imageState by viewModel.imageState.collectAsStateWithLifecycle()
    val imageAnalysisState by viewModel.imageAnalysisState.collectAsStateWithLifecycle()
    val videoState by viewModel.videoState.collectAsStateWithLifecycle()
    val musicState by viewModel.musicState.collectAsStateWithLifecycle()
    val codeState by viewModel.codeState.collectAsStateWithLifecycle()
    val pickedDocument by viewModel.pickedDocument.collectAsStateWithLifecycle()
    val documentState by viewModel.documentState.collectAsStateWithLifecycle()
    val translationState by viewModel.translationState.collectAsStateWithLifecycle()
    val studyState by viewModel.studyState.collectAsStateWithLifecycle()

    val strings = AppStrings.forLanguage(settings.appLanguage)

    var currentTab by remember { mutableStateOf(MainNavTab.HOME) }
    var createTabIndex by remember { mutableIntStateOf(0) }
    var toolsTabIndex by remember { mutableIntStateOf(0) }
    var showHistorySheet by remember { mutableStateOf(false) }

    // Support predictive/system back returning to Home tab cleanly
    BackHandler(enabled = currentTab != MainNavTab.HOME) {
        currentTab = MainNavTab.HOME
    }

    fun tabLabel(tab: MainNavTab): String = when (tab) {
        MainNavTab.HOME -> strings.navHome
        MainNavTab.CHAT -> strings.navChat
        MainNavTab.CREATE -> strings.navCreate
        MainNavTab.TOOLS -> strings.navTools
        MainNavTab.SETTINGS -> strings.navSettings
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (isWideScreen) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start))
                ) {
                    Spacer(Modifier.height(12.dp))
                    MainNavTab.entries.forEach { tab ->
                        val selected = currentTab == tab
                        NavigationRailItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.iconSelected else tab.iconUnselected,
                                    contentDescription = tabLabel(tab)
                                )
                            },
                            label = { Text(tabLabel(tab)) },
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                topBar = {
                    VikasTopBar(
                        strings = strings,
                        currentLanguage = settings.appLanguage,
                        isDarkTheme = settings.isDarkTheme,
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onToggleTheme = { viewModel.toggleTheme() },
                        onNewChatClick = {
                            viewModel.startNewChat()
                            currentTab = MainNavTab.CHAT
                        },
                        onHistoryClick = { showHistorySheet = true }
                    )
                },
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            MainNavTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.iconSelected else tab.iconUnselected,
                                            contentDescription = tabLabel(tab)
                                        )
                                    },
                                    label = { Text(tabLabel(tab), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val contentModifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp)

                    when (currentTab) {
                        MainNavTab.HOME -> HomeScreen(
                            strings = strings,
                            appLanguage = settings.appLanguage,
                            sessions = sessions,
                            creations = creations,
                            onQuickAsk = { prompt ->
                                viewModel.startNewChat()
                                currentTab = MainNavTab.CHAT
                                viewModel.sendChatMessage(
                                    prompt = prompt,
                                    mode = ChatMode.GENERAL,
                                    replyLanguage = "Auto"
                                )
                            },
                            onNavigateSuite = { destination ->
                                when {
                                    destination == StudioDestination.CHAT -> currentTab = MainNavTab.CHAT
                                    destination.createTabIndex != null -> {
                                        createTabIndex = destination.createTabIndex
                                        currentTab = MainNavTab.CREATE
                                    }
                                    destination.toolsTabIndex != null -> {
                                        toolsTabIndex = destination.toolsTabIndex
                                        currentTab = MainNavTab.TOOLS
                                    }
                                }
                            },
                            onOpenSession = { sessionId ->
                                viewModel.selectChatSession(sessionId)
                                currentTab = MainNavTab.CHAT
                            },
                            modifier = contentModifier
                        )

                        MainNavTab.CHAT -> ChatScreen(
                            strings = strings,
                            appLanguage = settings.appLanguage,
                            messages = activeMessages,
                            isLoading = isChatLoading,
                            isSpeaking = isSpeaking,
                            pendingAttachment = pendingAttachment,
                            onAttachItem = viewModel::attachChatItem,
                            onClearAttachment = viewModel::clearChatAttachment,
                            onSendMessage = viewModel::sendChatMessage,
                            onSpeakText = { text, lang -> viewModel.speakText(text, lang) },
                            onStopSpeaking = viewModel::stopSpeaking,
                            onNewChat = viewModel::startNewChat,
                            modifier = contentModifier
                        )

                        MainNavTab.CREATE -> CreateStudioScreen(
                            strings = strings,
                            appLanguage = settings.appLanguage,
                            selectedTab = createTabIndex,
                            onSelectTab = { createTabIndex = it },
                            imageState = imageState,
                            imageAnalysisState = imageAnalysisState,
                            videoState = videoState,
                            musicState = musicState,
                            onGenerateImage = viewModel::generateImagePreview,
                            onAnalyzeImage = viewModel::analyzeImage,
                            onGenerateVideo = viewModel::generateVideoProject,
                            onGenerateMusic = viewModel::generateMusicTrack,
                            onSpeakText = { text, lang -> viewModel.speakText(text, lang) },
                            modifier = contentModifier
                        )

                        MainNavTab.TOOLS -> ToolsScreen(
                            strings = strings,
                            appLanguage = settings.appLanguage,
                            selectedTab = toolsTabIndex,
                            onSelectTab = { toolsTabIndex = it },
                            voiceGender = settings.voiceGender,
                            speechRate = settings.speechRate,
                            isSpeaking = isSpeaking,
                            onSetVoiceGender = viewModel::setVoiceGender,
                            onSetSpeechRate = viewModel::setSpeechRate,
                            onSpeakText = { text, lang -> viewModel.speakText(text, lang) },
                            onStopSpeaking = viewModel::stopSpeaking,
                            codeState = codeState,
                            onGenerateCode = viewModel::generateCode,
                            pickedDocument = pickedDocument,
                            documentState = documentState,
                            onLoadDocument = viewModel::loadPickedDocument,
                            onAnalyzeDocument = viewModel::analyzeDocument,
                            translationState = translationState,
                            onTranslate = viewModel::translate,
                            studyState = studyState,
                            onAssistStudy = viewModel::assistStudy,
                            modifier = contentModifier
                        )

                        MainNavTab.SETTINGS -> SettingsScreen(
                            strings = strings,
                            settings = settings,
                            onToggleTheme = viewModel::toggleTheme,
                            onSetLanguage = viewModel::setLanguage,
                            onSetVoiceGender = viewModel::setVoiceGender,
                            onSetSpeechRate = viewModel::setSpeechRate,
                            onSetPreferOpenModels = viewModel::setPreferOpenModels,
                            modifier = contentModifier
                        )
                    }
                }
            }
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.chatHistory, style = MaterialTheme.typography.titleLarge)
                    FilledTonalButton(
                        onClick = {
                            viewModel.startNewChat()
                            currentTab = MainNavTab.CHAT
                            showHistorySheet = false
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(strings.newChat)
                    }
                }
                if (sessions.isEmpty()) {
                    Text(
                        text = strings.noHistoryYet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sessions, key = { it.id }) { session ->
                            val isActive = session.id == activeSessionId
                            ListItem(
                                headlineContent = {
                                    Text(session.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                },
                                supportingContent = {
                                    Text("${session.mode} • ${session.language}")
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = if (isActive) Icons.Filled.Forum else Icons.Outlined.Forum,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = { viewModel.deleteChatSession(session.id) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete chat")
                                    }
                                },
                                modifier = Modifier.clickable {
                                    viewModel.selectChatSession(session.id)
                                    currentTab = MainNavTab.CHAT
                                    showHistorySheet = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
