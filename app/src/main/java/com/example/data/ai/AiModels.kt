package com.example.data.ai

import com.example.data.local.AppLanguage

enum class ChatMode(val labelEn: String, val labelHi: String) {
    GENERAL("Smart Chat", "स्मार्ट चैट"),
    EXPLAIN("Explain", "समझाएँ"),
    SUMMARIZE("Summarize", "सारांश"),
    TEACH("Teach Me", "सिखाएँ")
}

enum class ImageStyle(val label: String) {
    REALISTIC("Realistic"),
    ANIME_2D("2D Anime"),
    RENDER_3D("3D Render"),
    CARTOON("Cartoon"),
    FANTASY("Fantasy Art")
}

enum class ImageTaskMode(val labelEn: String, val labelHi: String) {
    TEXT_TO_IMAGE("Text to Image", "टेक्स्ट से इमेज"),
    EDIT_PHOTO("Edit Photo", "फोटो एडिट"),
    REMOVE_BG("Remove BG", "बैकग्राउंड हटाएँ"),
    ENHANCE("Enhance HD", "HD एन्हांस")
}

enum class ImageResolution(val label: String, val pixels: String) {
    RES_1K("1K", "1024×1024"),
    RES_2K("2K", "2048×2048"),
    RES_4K("4K", "4096×4096")
}

enum class MediaAspectRatio(val label: String, val ratioText: String) {
    SQUARE("1:1", "1:1"),
    LANDSCAPE("16:9", "16:9 Landscape"),
    PORTRAIT("9:16", "9:16 Portrait")
}

enum class VideoStyle(val label: String) {
    CINEMATIC("Cinematic Live"),
    ANIME_MOVIE("Anime Movie"),
    ANIMATION_3D("3D Animation")
}

enum class MusicDurationMode(val labelEn: String, val labelHi: String, val targetModel: String) {
    CLIP_30S("30s Clip", "30 सेकंड क्लिप", "lyria-3-clip-preview"),
    FULL_TRACK("Full Track", "फुल ट्रैक", "lyria-3-pro-preview")
}

enum class CodeLanguage(val label: String) {
    HTML_CSS_JS("HTML/CSS/JS"),
    PYTHON("Python"),
    KOTLIN("Kotlin"),
    JAVASCRIPT("JavaScript"),
    SQL("SQL")
}

enum class CodeTask(val labelEn: String, val labelHi: String) {
    GENERATE("Generate Code", "कोड बनाएँ"),
    FIX_BUGS("Find & Fix Bugs", "एरर ठीक करें"),
    EXPLAIN("Explain Code", "कोड समझाएँ")
}

enum class DocumentTask(val labelEn: String, val labelHi: String) {
    SUMMARIZE("Summarize", "सारांश"),
    QA("Ask Question", "प्रश्न पूछें"),
    STORY("Write Story", "कहानी लिखें"),
    SCRIPT("Write Script", "स्क्रिप्ट लिखें"),
    ESSAY("Write Essay", "निबंध लिखें")
}

enum class StudySubject(val labelEn: String, val labelHi: String) {
    MATHEMATICS("Mathematics", "गणित"),
    SCIENCE("Science", "विज्ञान"),
    HISTORY("History", "इतिहास"),
    CODING("Computer Science", "कंप्यूटर विज्ञान")
}

enum class StudyMode(val labelEn: String, val labelHi: String) {
    STEP_BY_STEP("Step-by-Step Solve", "चरण-दर-चरण हल"),
    QUIZ("Quiz Practice", "क्विज़ अभ्यास"),
    NOTES("Study Notes", "अध्ययन नोट्स")
}

data class AiSampleNotice(
    val title: String,
    val message: String,
    val targetModel: String
)

data class ImageGenerationPreview(
    val prompt: String,
    val style: ImageStyle,
    val mode: ImageTaskMode,
    val resolution: ImageResolution,
    val aspectRatio: MediaAspectRatio,
    val sourcePhotoName: String?,
    val targetModel: String,
    val sceneBlueprint: String,
    val notice: AiSampleNotice
)

data class VideoScenePlan(
    val sceneNumber: Int,
    val timeRange: String,
    val cameraMovement: String,
    val visualDescription: String,
    val hindiDialogue: String?,
    val sfxAndMusic: String
)

data class VideoProjectPlan(
    val title: String,
    val durationMinutes: Int,
    val aspectRatio: MediaAspectRatio,
    val style: VideoStyle,
    val isImageToVideo: Boolean,
    val characterConsistency: Boolean,
    val targetModel: String,
    val exportFormat: String,
    val scenes: List<VideoScenePlan>,
    val notice: AiSampleNotice
)

data class MusicTrackPlan(
    val title: String,
    val genreAndMood: String,
    val durationLabel: String,
    val bpm: Int,
    val instrumentation: String,
    val sampleLyricsOrStructure: String,
    val targetModel: String,
    val notice: AiSampleNotice
)

data class CodeGenResult(
    val language: CodeLanguage,
    val task: CodeTask,
    val explanation: String,
    val codeSnippet: String,
    val targetModel: String,
    val notice: AiSampleNotice
)

data class TranslationResult(
    val detectedLanguage: String,
    val targetLanguage: String,
    val translatedText: String,
    val pronunciationNote: String,
    val notice: AiSampleNotice
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class StudyResult(
    val subject: StudySubject,
    val mode: StudyMode,
    val content: String,
    val quizQuestions: List<QuizQuestion>,
    val notice: AiSampleNotice
)

data class DocumentAnalysisResult(
    val fileName: String,
    val task: DocumentTask,
    val outputText: String,
    val notice: AiSampleNotice
)

data class ChatTurnContext(
    val role: String,
    val text: String
)

data class ChatReplyResult(
    val replyText: String,
    val targetModel: String,
    val notice: AiSampleNotice
)

data class ImageAnalysisResult(
    val summary: String,
    val targetModel: String,
    val notice: AiSampleNotice
)

data class AppLocaleStringConfig(
    val appLanguage: AppLanguage
)
