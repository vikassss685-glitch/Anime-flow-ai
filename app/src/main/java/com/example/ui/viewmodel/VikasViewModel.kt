package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.ai.*
import com.example.data.device.DeviceSpeechService
import com.example.data.device.DocumentReaderHelper
import com.example.data.device.PickedDocumentInfo
import com.example.data.local.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface StudioUiState<out T> {
    data object Idle : StudioUiState<Nothing>
    data object Loading : StudioUiState<Nothing>
    data class Success<T>(val data: T) : StudioUiState<T>
    data class Error(val message: String) : StudioUiState<Nothing>
}

class VikasViewModel(
    private val dao: VikasDao,
    private val settingsStore: SettingsDataStore,
    private val aiRepository: AiRepository,
    private val speechService: DeviceSpeechService,
    private val appContext: Context
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserSettings())

    val sessions: StateFlow<List<ChatSessionEntity>> = dao.observeSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val savedCreations: StateFlow<List<SavedCreationEntity>> = dao.observeCreations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val isSpeaking: StateFlow<Boolean> = speechService.isSpeaking
    val ttsAvailable: StateFlow<Boolean> = speechService.ttsAvailable

    private val _activeSessionId = MutableStateFlow<Long?>(null)
    val activeSessionId: StateFlow<Long?> = _activeSessionId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<ChatMessageEntity>> = _activeSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else dao.observeMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _pendingChatAttachment = MutableStateFlow<Pair<String, String>?>(null)
    val pendingChatAttachment: StateFlow<Pair<String, String>?> = _pendingChatAttachment.asStateFlow()

    // Image Studio State
    private val _imageState = MutableStateFlow<StudioUiState<ImageGenerationPreview>>(StudioUiState.Idle)
    val imageState: StateFlow<StudioUiState<ImageGenerationPreview>> = _imageState.asStateFlow()

    private val _imageAnalysisState = MutableStateFlow<StudioUiState<ImageAnalysisResult>>(StudioUiState.Idle)
    val imageAnalysisState: StateFlow<StudioUiState<ImageAnalysisResult>> = _imageAnalysisState.asStateFlow()

    // Video Studio State
    private val _videoState = MutableStateFlow<StudioUiState<VideoProjectPlan>>(StudioUiState.Idle)
    val videoState: StateFlow<StudioUiState<VideoProjectPlan>> = _videoState.asStateFlow()

    // Music Studio State
    private val _musicState = MutableStateFlow<StudioUiState<MusicTrackPlan>>(StudioUiState.Idle)
    val musicState: StateFlow<StudioUiState<MusicTrackPlan>> = _musicState.asStateFlow()

    // Coding Studio State
    private val _codeState = MutableStateFlow<StudioUiState<CodeGenResult>>(StudioUiState.Idle)
    val codeState: StateFlow<StudioUiState<CodeGenResult>> = _codeState.asStateFlow()

    // Document Studio State
    private val _pickedDocument = MutableStateFlow<PickedDocumentInfo?>(null)
    val pickedDocument: StateFlow<PickedDocumentInfo?> = _pickedDocument.asStateFlow()

    private val _documentState = MutableStateFlow<StudioUiState<DocumentAnalysisResult>>(StudioUiState.Idle)
    val documentState: StateFlow<StudioUiState<DocumentAnalysisResult>> = _documentState.asStateFlow()

    // Translator State
    private val _translationState = MutableStateFlow<StudioUiState<TranslationResult>>(StudioUiState.Idle)
    val translationState: StateFlow<StudioUiState<TranslationResult>> = _translationState.asStateFlow()

    // Study Assistant State
    private val _studyState = MutableStateFlow<StudioUiState<StudyResult>>(StudioUiState.Idle)
    val studyState: StateFlow<StudioUiState<StudyResult>> = _studyState.asStateFlow()

    fun toggleTheme() {
        viewModelScope.launch {
            settingsStore.setDarkTheme(!settings.value.isDarkTheme)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsStore.setAppLanguage(language)
        }
    }

    fun toggleLanguage() {
        val next = if (settings.value.appLanguage == AppLanguage.ENGLISH) {
            AppLanguage.HINDI
        } else {
            AppLanguage.ENGLISH
        }
        setLanguage(next)
    }

    fun setVoiceGender(gender: VoiceGender) {
        viewModelScope.launch { settingsStore.setVoiceGender(gender) }
    }

    fun setSpeechRate(rate: Float) {
        viewModelScope.launch { settingsStore.setSpeechRate(rate) }
    }

    fun setPreferOpenModels(preferOpen: Boolean) {
        viewModelScope.launch { settingsStore.setPreferOpenModels(preferOpen) }
    }

    fun startNewChat() {
        _activeSessionId.value = null
        _pendingChatAttachment.value = null
    }

    fun selectChatSession(sessionId: Long) {
        _activeSessionId.value = sessionId
    }

    fun deleteChatSession(sessionId: Long) {
        viewModelScope.launch {
            dao.deleteSession(sessionId)
            if (_activeSessionId.value == sessionId) {
                _activeSessionId.value = null
            }
        }
    }

    fun attachChatItem(name: String, type: String) {
        _pendingChatAttachment.value = name to type
    }

    fun clearChatAttachment() {
        _pendingChatAttachment.value = null
    }

    fun sendChatMessage(
        prompt: String,
        mode: ChatMode,
        replyLanguage: String
    ) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty() || _isChatLoading.value) return

        viewModelScope.launch {
            _isChatLoading.value = true
            try {
                val now = System.currentTimeMillis()
                val sessionId = _activeSessionId.value ?: dao.insertSession(
                    ChatSessionEntity(
                        title = trimmed.take(36),
                        mode = mode.labelEn,
                        language = replyLanguage,
                        updatedAt = now
                    )
                ).also { _activeSessionId.value = it }

                val attachment = _pendingChatAttachment.value
                _pendingChatAttachment.value = null

                val priorHistory = activeMessages.value.map {
                    ChatTurnContext(role = it.role, text = it.text)
                }

                dao.insertMessage(
                    ChatMessageEntity(
                        sessionId = sessionId,
                        role = "user",
                        text = trimmed,
                        attachmentName = attachment?.first,
                        attachmentType = attachment?.second,
                        isSampleResponse = false,
                        createdAt = now
                    )
                )

                val result = aiRepository.generateChatReply(
                    history = priorHistory,
                    prompt = trimmed,
                    mode = mode,
                    replyLanguage = replyLanguage,
                    attachmentSummary = attachment?.let { "${it.second}: ${it.first}" },
                    uiLanguage = settings.value.appLanguage
                )

                dao.insertMessage(
                    ChatMessageEntity(
                        sessionId = sessionId,
                        role = "assistant",
                        text = result.replyText,
                        isSampleResponse = !aiRepository.isLiveModelConnected,
                        createdAt = System.currentTimeMillis()
                    )
                )
                dao.updateSessionHeader(
                    sessionId = sessionId,
                    title = if (priorHistory.isEmpty()) trimmed.take(36) else sessions.value.find { it.id == sessionId }?.title ?: trimmed.take(36),
                    updatedAt = System.currentTimeMillis()
                )
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun generateImagePreview(
        prompt: String,
        style: ImageStyle,
        mode: ImageTaskMode,
        resolution: ImageResolution,
        aspectRatio: MediaAspectRatio,
        sourcePhotoName: String?
    ) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _imageState.value = StudioUiState.Loading
            try {
                val preview = aiRepository.generateImagePreview(
                    prompt = prompt.trim(),
                    style = style,
                    mode = mode,
                    resolution = resolution,
                    aspectRatio = aspectRatio,
                    sourcePhotoName = sourcePhotoName,
                    uiLanguage = settings.value.appLanguage
                )
                _imageState.value = StudioUiState.Success(preview)
                dao.insertCreation(
                    SavedCreationEntity(
                        category = "IMAGE",
                        title = "${style.label} • ${resolution.label}",
                        prompt = prompt.trim(),
                        outputSummary = preview.sceneBlueprint,
                        modelTag = preview.targetModel,
                        isSampleResponse = true
                    )
                )
            } catch (e: Exception) {
                _imageState.value = StudioUiState.Error(e.localizedMessage ?: "Failed to prepare image preview")
            }
        }
    }

    fun analyzeImage(photoName: String, question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _imageAnalysisState.value = StudioUiState.Loading
            try {
                val res = aiRepository.analyzeImage(photoName, question.trim(), settings.value.appLanguage)
                _imageAnalysisState.value = StudioUiState.Success(res)
            } catch (e: Exception) {
                _imageAnalysisState.value = StudioUiState.Error(e.localizedMessage ?: "Image analysis failed")
            }
        }
    }

    fun generateVideoProject(
        prompt: String,
        durationMinutes: Int,
        aspectRatio: MediaAspectRatio,
        style: VideoStyle,
        isImageToVideo: Boolean,
        characterConsistency: Boolean,
        includeHindiDialogue: Boolean,
        includeMusicAndSfx: Boolean
    ) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _videoState.value = StudioUiState.Loading
            try {
                val plan = aiRepository.generateVideoProject(
                    prompt = prompt.trim(),
                    durationMinutes = durationMinutes,
                    aspectRatio = aspectRatio,
                    style = style,
                    isImageToVideo = isImageToVideo,
                    characterConsistency = characterConsistency,
                    includeHindiDialogue = includeHindiDialogue,
                    includeMusicAndSfx = includeMusicAndSfx,
                    uiLanguage = settings.value.appLanguage
                )
                _videoState.value = StudioUiState.Success(plan)
                dao.insertCreation(
                    SavedCreationEntity(
                        category = "VIDEO",
                        title = "${durationMinutes}m ${style.label} (${aspectRatio.label})",
                        prompt = prompt.trim(),
                        outputSummary = "${plan.scenes.size} scenes • ${plan.exportFormat}",
                        modelTag = plan.targetModel,
                        isSampleResponse = true
                    )
                )
            } catch (e: Exception) {
                _videoState.value = StudioUiState.Error(e.localizedMessage ?: "Video project build failed")
            }
        }
    }

    fun generateMusicTrack(
        prompt: String,
        durationMode: MusicDurationMode,
        genre: String
    ) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _musicState.value = StudioUiState.Loading
            try {
                val plan = aiRepository.generateMusicTrack(
                    prompt = prompt.trim(),
                    durationMode = durationMode,
                    genre = genre,
                    uiLanguage = settings.value.appLanguage
                )
                _musicState.value = StudioUiState.Success(plan)
                dao.insertCreation(
                    SavedCreationEntity(
                        category = "MUSIC",
                        title = "${plan.title} • ${plan.durationLabel}",
                        prompt = prompt.trim(),
                        outputSummary = plan.sampleLyricsOrStructure,
                        modelTag = plan.targetModel,
                        isSampleResponse = true
                    )
                )
            } catch (e: Exception) {
                _musicState.value = StudioUiState.Error(e.localizedMessage ?: "Music generation failed")
            }
        }
    }

    fun generateCode(prompt: String, language: CodeLanguage, task: CodeTask) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _codeState.value = StudioUiState.Loading
            try {
                val res = aiRepository.generateCode(prompt.trim(), language, task, settings.value.appLanguage)
                _codeState.value = StudioUiState.Success(res)
                dao.insertCreation(
                    SavedCreationEntity(
                        category = "CODE",
                        title = "${language.label} • ${task.labelEn}",
                        prompt = prompt.trim(),
                        outputSummary = res.codeSnippet.take(180),
                        modelTag = res.targetModel,
                        isSampleResponse = true
                    )
                )
            } catch (e: Exception) {
                _codeState.value = StudioUiState.Error(e.localizedMessage ?: "Code task failed")
            }
        }
    }

    fun loadPickedDocument(uri: Uri) {
        viewModelScope.launch {
            try {
                val info = DocumentReaderHelper.readDocument(appContext, uri)
                _pickedDocument.value = info
            } catch (e: Exception) {
                _documentState.value = StudioUiState.Error(e.localizedMessage ?: "Could not read document")
            }
        }
    }

    fun analyzeDocument(question: String, task: DocumentTask) {
        val doc = _pickedDocument.value
        val fileName = doc?.fileName ?: "Draft_Note.txt"
        val preview = doc?.extractedText ?: question
        if (fileName.isBlank() && question.isBlank()) return
        viewModelScope.launch {
            _documentState.value = StudioUiState.Loading
            try {
                val res = aiRepository.analyzeDocument(
                    fileName = fileName,
                    extractedPreview = preview,
                    question = question.trim(),
                    task = task,
                    uiLanguage = settings.value.appLanguage
                )
                _documentState.value = StudioUiState.Success(res)
            } catch (e: Exception) {
                _documentState.value = StudioUiState.Error(e.localizedMessage ?: "Document analysis failed")
            }
        }
    }

    fun translate(sourceText: String, sourceLanguage: String, targetLanguage: String) {
        if (sourceText.isBlank()) return
        viewModelScope.launch {
            _translationState.value = StudioUiState.Loading
            try {
                val res = aiRepository.translateText(
                    sourceText = sourceText.trim(),
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    uiLanguage = settings.value.appLanguage
                )
                _translationState.value = StudioUiState.Success(res)
            } catch (e: Exception) {
                _translationState.value = StudioUiState.Error(e.localizedMessage ?: "Translation failed")
            }
        }
    }

    fun assistStudy(subject: StudySubject, mode: StudyMode, topic: String) {
        if (topic.isBlank()) return
        viewModelScope.launch {
            _studyState.value = StudioUiState.Loading
            try {
                val res = aiRepository.assistStudy(subject, mode, topic.trim(), settings.value.appLanguage)
                _studyState.value = StudioUiState.Success(res)
            } catch (e: Exception) {
                _studyState.value = StudioUiState.Error(e.localizedMessage ?: "Study request failed")
            }
        }
    }

    fun speakText(text: String, languageLabel: String = settings.value.appLanguage.displayLabel): Boolean {
        val current = settings.value
        return speechService.speak(
            text = text,
            languageLabel = languageLabel,
            gender = current.voiceGender,
            speechRate = current.speechRate
        )
    }

    fun stopSpeaking() {
        speechService.stop()
    }

    fun deleteCreation(id: Long) {
        viewModelScope.launch { dao.deleteCreation(id) }
    }

    override fun onCleared() {
        super.onCleared()
        speechService.shutdown()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appCtx = context.applicationContext
                val db = VikasDatabase.getInstance(appCtx)
                VikasViewModel(
                    dao = db.vikasDao(),
                    settingsStore = SettingsDataStore(appCtx),
                    aiRepository = PlaceholderAiRepository(),
                    speechService = DeviceSpeechService(appCtx),
                    appContext = appCtx
                )
            }
        }
    }
}
