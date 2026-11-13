package com.example.data.ai

import com.example.data.local.AppLanguage

interface AiRepository {
    val isLiveModelConnected: Boolean

    suspend fun generateChatReply(
        history: List<ChatTurnContext>,
        prompt: String,
        mode: ChatMode,
        replyLanguage: String,
        attachmentSummary: String?,
        uiLanguage: AppLanguage
    ): ChatReplyResult

    suspend fun generateImagePreview(
        prompt: String,
        style: ImageStyle,
        mode: ImageTaskMode,
        resolution: ImageResolution,
        aspectRatio: MediaAspectRatio,
        sourcePhotoName: String?,
        uiLanguage: AppLanguage
    ): ImageGenerationPreview

    suspend fun analyzeImage(
        photoName: String,
        question: String,
        uiLanguage: AppLanguage
    ): ImageAnalysisResult

    suspend fun generateVideoProject(
        prompt: String,
        durationMinutes: Int,
        aspectRatio: MediaAspectRatio,
        style: VideoStyle,
        isImageToVideo: Boolean,
        characterConsistency: Boolean,
        includeHindiDialogue: Boolean,
        includeMusicAndSfx: Boolean,
        uiLanguage: AppLanguage
    ): VideoProjectPlan

    suspend fun generateMusicTrack(
        prompt: String,
        durationMode: MusicDurationMode,
        genre: String,
        uiLanguage: AppLanguage
    ): MusicTrackPlan

    suspend fun generateCode(
        prompt: String,
        language: CodeLanguage,
        task: CodeTask,
        uiLanguage: AppLanguage
    ): CodeGenResult

    suspend fun analyzeDocument(
        fileName: String,
        extractedPreview: String,
        question: String,
        task: DocumentTask,
        uiLanguage: AppLanguage
    ): DocumentAnalysisResult

    suspend fun translateText(
        sourceText: String,
        sourceLanguage: String,
        targetLanguage: String,
        uiLanguage: AppLanguage
    ): TranslationResult

    suspend fun assistStudy(
        subject: StudySubject,
        mode: StudyMode,
        topicOrQuestion: String,
        uiLanguage: AppLanguage
    ): StudyResult
}
