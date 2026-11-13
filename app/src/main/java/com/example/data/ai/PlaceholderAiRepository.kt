package com.example.data.ai

import com.example.data.local.AppLanguage
import kotlinx.coroutines.delay

/**
 * Placeholder implementation used when Firebase setup is declined.
 * Clearly labels every response as a sample preview and never pretends
 * disconnected cloud models are live.
 */
class PlaceholderAiRepository : AiRepository {
    override val isLiveModelConnected: Boolean = false

    private fun notice(targetModel: String, uiLanguage: AppLanguage): AiSampleNotice {
        return if (uiLanguage == AppLanguage.HINDI) {
            AiSampleNotice(
                title = "सैंपल प्रीव्यू मोड (Firebase आवश्यक)",
                message = "AI Studio में Android ऐप के लिए लाइव Gemini / मीडिया AI मॉडल Firebase AI Logic के माध्यम से चलते हैं। अभी यह ऑफ़लाइन सैंपल आउटपुट दिखा रहा है।",
                targetModel = targetModel
            )
        } else {
            AiSampleNotice(
                title = "Sample Preview Mode (Firebase Required)",
                message = "Live Gemini, Image, Video, and Music AI models in AI Studio Android apps require Firebase AI Logic. Showing clearly labeled sample output.",
                targetModel = targetModel
            )
        }
    }

    override suspend fun generateChatReply(
        history: List<ChatTurnContext>,
        prompt: String,
        mode: ChatMode,
        replyLanguage: String,
        attachmentSummary: String?,
        uiLanguage: AppLanguage
    ): ChatReplyResult {
        delay(450)
        val contextCount = history.size
        val attachmentNote = attachmentSummary?.let { "\n• Attached context: $it" } ?: ""
        val reply = if (replyLanguage.equals("Hindi", ignoreCase = true) ||
            replyLanguage.equals("हिन्दी", ignoreCase = true) ||
            uiLanguage == AppLanguage.HINDI && replyLanguage == "Auto"
        ) {
            """
            [सैंपल प्रीव्यू उत्तर — मॉडल: gemini-3.8-flash (कनेक्टेड नहीं)]
            
            आपका प्रश्न: "$prompt"
            मोड: ${mode.labelHi} | भाषा: $replyLanguage | पिछला संदर्भ: $contextCount संदेश$attachmentNote
            
            सैंपल संरचना:
            1. मुख्य सार: यह एक स्पष्ट सैंपल उत्तर है जो दिखाता है कि चैट थ्रेड आपके पिछले संदेशों का संदर्भ और संलग्न फ़ाइलों को कैसे संभालता है।
            2. अगले कदम: आप नीचे दिए 'सुनें' बटन से असली Android Text-to-Speech (हिन्दी/अंग्रेज़ी) द्वारा इसे सुन सकते हैं।
            3. लाइव AI: वास्तविक Gemini उत्तरों के लिए Firebase AI Logic सक्षम करना आवश्यक है।
            """.trimIndent()
        } else {
            """
            [Sample Preview Response — Target: gemini-3.8-flash (Not Connected)]
            
            Prompt: "$prompt"
            Mode: ${mode.labelEn} | Reply Language: $replyLanguage | Context Turns: $contextCount$attachmentNote
            
            Sample Response Outline:
            • Direct Answer: Your conversation thread retains $contextCount prior turn(s) locally in Room so follow-up questions keep context.
            • Voice Playback: Tap "Listen" on this bubble to hear this text spoken aloud using the real on-device Android TextToSpeech engine.
            • Cloud Connection Status: Live generative inference requires enabling Firebase AI Logic; this preview does not call an external LLM.
            """.trimIndent()
        }

        return ChatReplyResult(
            replyText = reply,
            targetModel = "gemini-3.8-flash",
            notice = notice("gemini-3.8-flash", uiLanguage)
        )
    }

    override suspend fun generateImagePreview(
        prompt: String,
        style: ImageStyle,
        mode: ImageTaskMode,
        resolution: ImageResolution,
        aspectRatio: MediaAspectRatio,
        sourcePhotoName: String?,
        uiLanguage: AppLanguage
    ): ImageGenerationPreview {
        delay(550)
        val targetModel = if (resolution == ImageResolution.RES_4K || resolution == ImageResolution.RES_2K) {
            "gemini-3-pro-image-preview"
        } else {
            "gemini-nano-banana-2.1"
        }
        val sourceLine = sourcePhotoName?.let { "Source Photo: $it\n" } ?: ""
        val blueprint = if (uiLanguage == AppLanguage.HINDI) {
            "[सैंपल इमेज प्रीव्यू — मॉडल कनेक्टेड नहीं]\n" +
                "${sourceLine}प्रॉम्प्ट: $prompt\n" +
                "स्टाइल: ${style.label} | मोड: ${mode.labelHi}\n" +
                "रिज़ॉल्यूशन: ${resolution.label} (${resolution.pixels}) | अनुपात: ${aspectRatio.ratioText}\n" +
                "नोट: वास्तविक पिक्सेल जनरेशन/एडिटिंग के लिए Firebase AI Logic आवश्यक है।"
        } else {
            "[Sample Image Specification — Model Not Connected]\n" +
                "${sourceLine}Prompt: $prompt\n" +
                "Style: ${style.label} | Task: ${mode.labelEn}\n" +
                "Output Spec: ${resolution.label} (${resolution.pixels}) • ${aspectRatio.ratioText}\n" +
                "Note: Real pixel synthesis & background removal require Firebase AI Logic."
        }
        return ImageGenerationPreview(
            prompt = prompt,
            style = style,
            mode = mode,
            resolution = resolution,
            aspectRatio = aspectRatio,
            sourcePhotoName = sourcePhotoName,
            targetModel = targetModel,
            sceneBlueprint = blueprint,
            notice = notice(targetModel, uiLanguage)
        )
    }

    override suspend fun analyzeImage(
        photoName: String,
        question: String,
        uiLanguage: AppLanguage
    ): ImageAnalysisResult {
        delay(400)
        val text = if (uiLanguage == AppLanguage.HINDI) {
            "[सैंपल इमेज विश्लेषण — लक्षित मॉडल: gemini-4-argon]\nफ़ोटो: $photoName\nप्रश्न: $question\n\nयह एक सैंपल प्रीव्यू है। वास्तविक विज़न विश्लेषण के लिए Firebase AI Logic आवश्यक है।"
        } else {
            "[Sample Image Analysis — Target Model: gemini-4-argon]\nSelected Photo: $photoName\nQuestion: $question\n\nPhoto selected safely via Android Photo Picker without broad storage permissions. Live multimodal vision Q&A requires Firebase AI Logic."
        }
        return ImageAnalysisResult(
            summary = text,
            targetModel = "gemini-4-argon",
            notice = notice("gemini-4-argon", uiLanguage)
        )
    }

    override suspend fun generateVideoProject(
        prompt: String,
        durationMinutes: Int,
        aspectRatio: MediaAspectRatio,
        style: VideoStyle,
        isImageToVideo: Boolean,
        characterConsistency: Boolean,
        includeHindiDialogue: Boolean,
        includeMusicAndSfx: Boolean,
        uiLanguage: AppLanguage
    ): VideoProjectPlan {
        delay(600)
        val sceneCount = when (durationMinutes) {
            1 -> 3
            5 -> 5
            15 -> 7
            30 -> 9
            else -> 12
        }
        val secondsPerScene = (durationMinutes * 60) / sceneCount
        val cameraMoves = listOf(
            "Slow Dolly Push-In",
            "Aerial Drone Orbit",
            "Tracking Steadicam Shot",
            "Whip Pan Transition",
            "Wide Establishing Crane"
        )
        val scenes = (1..sceneCount).map { idx ->
            val startSec = (idx - 1) * secondsPerScene
            val endSec = idx * secondsPerScene
            val startMin = "%02d:%02d".format(startSec / 60, startSec % 60)
            val endMin = "%02d:%02d".format(endSec / 60, endSec % 60)
            VideoScenePlan(
                sceneNumber = idx,
                timeRange = "$startMin – $endMin",
                cameraMovement = cameraMoves[(idx - 1) % cameraMoves.size],
                visualDescription = "[Sample Scene $idx] ${style.label} sequence for \"$prompt\"" +
                    if (characterConsistency) " (Consistent lead character seed locked)" else "",
                hindiDialogue = if (includeHindiDialogue) {
                    "\"सीन $idx: नई यात्रा की शुरुआत यहीं से होती है।\""
                } else null,
                sfxAndMusic = if (includeMusicAndSfx) {
                    "Ambient synth pad + cinematic riser + subtle foley"
                } else {
                    "Muted audio track"
                }
            )
        }
        return VideoProjectPlan(
            title = prompt.take(42).ifBlank { "Untitled Video Project" },
            durationMinutes = durationMinutes,
            aspectRatio = aspectRatio,
            style = style,
            isImageToVideo = isImageToVideo,
            characterConsistency = characterConsistency,
            targetModel = "gemini-omni-1.2-flash (Interactions API)",
            exportFormat = "MP4 (H.264 / AAC — Preview Plan Only)",
            scenes = scenes,
            notice = notice("gemini-omni-1.2-flash", uiLanguage)
        )
    }

    override suspend fun generateMusicTrack(
        prompt: String,
        durationMode: MusicDurationMode,
        genre: String,
        uiLanguage: AppLanguage
    ): MusicTrackPlan {
        delay(500)
        val isClip = durationMode == MusicDurationMode.CLIP_30S
        return MusicTrackPlan(
            title = if (prompt.isNotBlank()) "${prompt.take(28)} (Sample)" else "Cosmic Dawn (Sample)",
            genreAndMood = genre,
            durationLabel = if (isClip) "0:30 Clip" else "3:15 Full Track",
            bpm = if (genre.contains("Lo-Fi", true)) 82 else 118,
            instrumentation = "Warm analog synth, tabla/percussion groove, airy vocal pads, deep sub-bass",
            sampleLyricsOrStructure = "[Sample Arrangement — ${durationMode.targetModel} Not Connected]\n" +
                "00:00 Intro — Soft atmospheric pad\n" +
                "00:15 Verse — Rhythm enters with melodic hook\n" +
                (if (isClip) "00:25 Outro — Clean loop fade" else "01:05 Chorus — Full harmonic lift\n02:15 Bridge — Acoustic solo\n03:00 Outro — Ambient resolve"),
            targetModel = durationMode.targetModel,
            notice = notice(durationMode.targetModel, uiLanguage)
        )
    }

    override suspend fun generateCode(
        prompt: String,
        language: CodeLanguage,
        task: CodeTask,
        uiLanguage: AppLanguage
    ): CodeGenResult {
        delay(450)
        val snippet = when (language) {
            CodeLanguage.HTML_CSS_JS -> """
                <!-- Sample Preview Snippet (Not Live AI) -->
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <style>
                    body { background: #090c12; color: #f1f5f9; font-family: sans-serif; padding: 24px; }
                    .card { background: #111622; border: 1px solid #22d3ee; border-radius: 16px; padding: 20px; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h2>Vikas AI Starter</h2>
                    <p>Prompt: ${prompt.take(60)}</p>
                    <button onclick="alert('Sample action ready!')">Run</button>
                  </div>
                </body>
                </html>
            """.trimIndent()

            CodeLanguage.PYTHON -> """
                # Sample Preview Snippet (Target: gemini-4-argon)
                def solve_task(query: str) -> dict:
                    return {
                        "query": query,
                        "status": "sample_preview",
                        "note": "Connect Firebase AI Logic for live code synthesis."
                    }

                if __name__ == "__main__":
                    print(solve_task("${prompt.take(50).replace("\"", "'")}"))
            """.trimIndent()

            CodeLanguage.KOTLIN -> """
                // Sample Preview Snippet (Target: gemini-4-argon)
                data class VikasTask(val prompt: String, val isLive: Boolean = false)

                fun main() {
                    val task = VikasTask("${prompt.take(50).replace("\"", "'")}")
                    println("Preview ready: ${'$'}task")
                }
            """.trimIndent()

            CodeLanguage.JAVASCRIPT -> """
                // Sample Preview Snippet
                export function createAppPreview(prompt) {
                  return {
                    prompt,
                    mode: "sample_preview",
                    timestamp: new Date().toISOString(),
                  };
                }
                console.log(createAppPreview("${prompt.take(50).replace("\"", "'")}"));
            """.trimIndent()

            CodeLanguage.SQL -> """
                -- Sample Preview SQL Query
                SELECT id, title, mode, updated_at
                FROM chat_sessions
                WHERE title LIKE '%${prompt.take(24).replace("'", "")}%'
                ORDER BY updated_at DESC
                LIMIT 20;
            """.trimIndent()
        }

        val explanation = if (uiLanguage == AppLanguage.HINDI) {
            "[सैंपल कोड प्रीव्यू — ${task.labelHi}]\nयह ${language.label} के लिए एक सैंपल टेम्पलेट है। वास्तविक बग फिक्सिंग और कस्टम कोड जनरेशन के लिए Firebase AI Logic आवश्यक है।"
        } else {
            "[Sample Code Preview — ${task.labelEn}]\nGenerated a clean ${language.label} template for \"$prompt\". Live debugging and custom multi-file generation require Firebase AI Logic."
        }

        return CodeGenResult(
            language = language,
            task = task,
            explanation = explanation,
            codeSnippet = snippet,
            targetModel = "gemini-4-argon",
            notice = notice("gemini-4-argon", uiLanguage)
        )
    }

    override suspend fun analyzeDocument(
        fileName: String,
        extractedPreview: String,
        question: String,
        task: DocumentTask,
        uiLanguage: AppLanguage
    ): DocumentAnalysisResult {
        delay(450)
        val wordCount = extractedPreview.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val excerpt = extractedPreview.take(360).ifBlank { "No plain UTF-8 text extracted from file." }
        val output = if (uiLanguage == AppLanguage.HINDI) {
            "[सैंपल दस्तावेज़ विश्लेषण — ${task.labelHi}]\n" +
                "फ़ाइल: $fileName (लगभग $wordCount शब्द पढ़े गए)\n" +
                (if (question.isNotBlank()) "प्रश्न: $question\n\n" else "\n") +
                "स्थानीय फ़ाइल अंश:\n\"$excerpt\"\n\n" +
                "नोट: पूरा AI सारांश, निबंध और PDF प्रश्न-उत्तर Firebase AI Logic कनेक्शन के बाद उपलब्ध होंगे।"
        } else {
            "[Sample Document Output — ${task.labelEn}]\n" +
                "File: $fileName ($wordCount local words inspected)\n" +
                (if (question.isNotBlank()) "Question: $question\n\n" else "\n") +
                "Extracted Local Excerpt:\n\"$excerpt\"\n\n" +
                "Preview Summary: Document loaded cleanly via Android Storage Access Framework. Full AI summarization, essay/script drafting, and deep Q&A require Firebase AI Logic."
        }
        return DocumentAnalysisResult(
            fileName = fileName,
            task = task,
            outputText = output,
            notice = notice("gemini-3.8-flash", uiLanguage)
        )
    }

    override suspend fun translateText(
        sourceText: String,
        sourceLanguage: String,
        targetLanguage: String,
        uiLanguage: AppLanguage
    ): TranslationResult {
        delay(350)
        val detected = if (sourceLanguage == "Auto-Detect") {
            when {
                sourceText.any { it in '\u0900'..'\u097F' } -> "Hindi (Devanagari detected)"
                sourceText.any { it in '\u3040'..'\u30FF' } -> "Japanese (Kana detected)"
                sourceText.any { it in '\uAC00'..'\uD7AF' } -> "Korean (Hangul detected)"
                sourceText.any { it in '\u0600'..'\u06FF' } -> "Arabic (Script detected)"
                sourceText.any { it in '\u4E00'..'\u9FFF' } -> "Chinese (Hanzi detected)"
                else -> "English / Latin Script"
            }
        } else {
            sourceLanguage
        }

        val sampleTranslation = when (targetLanguage) {
            "Hindi" -> "[सैंपल अनुवाद -> हिन्दी] \"$sourceText\" — (लाइव न्यूरल अनुवाद के लिए Firebase AI Logic आवश्यक है)"
            "Japanese" -> "[サンプル翻訳 -> 日本語] \"$sourceText\" (Firebase AI Logic が必要です)"
            "Korean" -> "[샘플 번역 -> 한국어] \"$sourceText\" (Firebase AI Logic 필요)"
            "Arabic" -> "[ترجمة تجريبية -> العربية] \"$sourceText\""
            "Chinese" -> "[示例翻译 -> 中文] \"$sourceText\""
            "Spanish" -> "[Traducción de muestra -> Español] \"$sourceText\""
            "French" -> "[Traduction exemple -> Français] \"$sourceText\""
            else -> "[Sample Translation -> $targetLanguage] \"$sourceText\" (Live neural translation requires Firebase AI Logic)"
        }

        return TranslationResult(
            detectedLanguage = detected,
            targetLanguage = targetLanguage,
            translatedText = sampleTranslation,
            pronunciationNote = "Tap Speak to hear on-device Android TTS pronunciation in $targetLanguage.",
            notice = notice("gemini-3.8-flash", uiLanguage)
        )
    }

    override suspend fun assistStudy(
        subject: StudySubject,
        mode: StudyMode,
        topicOrQuestion: String,
        uiLanguage: AppLanguage
    ): StudyResult {
        delay(450)
        val quiz = listOf(
            QuizQuestion(
                question = "Sample ${subject.labelEn} Check: Which step comes first when breaking down \"$topicOrQuestion\"?",
                options = listOf(
                    "Identify given values and target unknown",
                    "Skip units and guess final answer",
                    "Ignore core formulas"
                ),
                correctIndex = 0,
                explanation = "Structured problem solving always begins by listing known inputs and constraints."
            ),
            QuizQuestion(
                question = "Why is verifying units/assumptions important in ${subject.labelEn}?",
                options = listOf(
                    "It slows down learning",
                    "It catches sign and scale errors early",
                    "It is optional"
                ),
                correctIndex = 1,
                explanation = "Checking units and boundary conditions prevents calculation mistakes."
            )
        )

        val content = if (uiLanguage == AppLanguage.HINDI) {
            "[सैंपल अध्ययन सहायता — ${subject.labelHi} (${mode.labelHi})]\n" +
                "विषय: $topicOrQuestion\n\n" +
                "चरण 1: प्रश्न के मुख्य तथ्यों और सूत्रों को पहचानें।\n" +
                "चरण 2: समस्या को छोटे तार्किक भागों में विभाजित करें।\n" +
                "चरण 3: उत्तर की इकाइयों और तर्क की जाँच करें।\n\n" +
                "नोट: कस्टम AI ट्यूटरिंग और अनंत प्रश्न-हल के लिए Firebase AI Logic आवश्यक है।"
        } else {
            "[Sample Study Output — ${subject.labelEn} • ${mode.labelEn}]\n" +
                "Topic: $topicOrQuestion\n\n" +
                "Step 1 — Understand the Concept: Break \"$topicOrQuestion\" into known inputs and desired outcome.\n" +
                "Step 2 — Apply Core Principle: Select the relevant formula, law, or historical timeline.\n" +
                "Step 3 — Verify Result: Double-check edge cases and units.\n\n" +
                "Note: Live adaptive tutoring & custom problem solving require Firebase AI Logic."
        }

        return StudyResult(
            subject = subject,
            mode = mode,
            content = content,
            quizQuestions = if (mode == StudyMode.QUIZ) quiz else emptyList(),
            notice = notice("gemini-4-argon", uiLanguage)
        )
    }
}
