package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.ai.*
import com.example.data.device.PickedDocumentInfo
import com.example.data.local.AppLanguage
import com.example.data.local.VoiceGender
import com.example.ui.components.FirebaseNoticeBanner
import com.example.ui.components.SampleOutputTag
import com.example.ui.components.StudioSectionCard
import com.example.ui.components.copyToClipboard
import com.example.ui.i18n.VikasStrings
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.viewmodel.StudioUiState
import java.util.Locale

@Composable
fun ToolsScreen(
    strings: VikasStrings,
    appLanguage: AppLanguage,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    voiceGender: VoiceGender,
    speechRate: Float,
    isSpeaking: Boolean,
    onSetVoiceGender: (VoiceGender) -> Unit,
    onSetSpeechRate: (Float) -> Unit,
    onSpeakText: (String, String) -> Unit,
    onStopSpeaking: () -> Unit,
    codeState: StudioUiState<CodeGenResult>,
    onGenerateCode: (String, CodeLanguage, CodeTask) -> Unit,
    pickedDocument: PickedDocumentInfo?,
    documentState: StudioUiState<DocumentAnalysisResult>,
    onLoadDocument: (android.net.Uri) -> Unit,
    onAnalyzeDocument: (String, DocumentTask) -> Unit,
    translationState: StudioUiState<TranslationResult>,
    onTranslate: (String, String, String) -> Unit,
    studyState: StudioUiState<StudyResult>,
    onAssistStudy: (StudySubject, StudyMode, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = if (appLanguage == AppLanguage.HINDI) {
        listOf("वॉइस", "कोडिंग", "दस्तावेज़", "अनुवाद", "पढ़ाई")
    } else {
        listOf("Voice", "Coding", "Docs", "Translate", "Study")
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab.coerceIn(0, 4),
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onSelectTab(index) },
                    text = { Text(title) },
                    modifier = Modifier.testTag("tools_tab_$index")
                )
            }
        }

        when (selectedTab.coerceIn(0, 4)) {
            0 -> VoiceLabTab(
                appLanguage = appLanguage,
                voiceGender = voiceGender,
                speechRate = speechRate,
                isSpeaking = isSpeaking,
                onSetVoiceGender = onSetVoiceGender,
                onSetSpeechRate = onSetSpeechRate,
                onSpeakText = onSpeakText,
                onStopSpeaking = onStopSpeaking
            )
            1 -> CodingTab(
                strings = strings,
                appLanguage = appLanguage,
                codeState = codeState,
                onGenerateCode = onGenerateCode
            )
            2 -> DocumentsTab(
                strings = strings,
                appLanguage = appLanguage,
                pickedDocument = pickedDocument,
                documentState = documentState,
                onLoadDocument = onLoadDocument,
                onAnalyzeDocument = onAnalyzeDocument
            )
            3 -> TranslatorTab(
                strings = strings,
                appLanguage = appLanguage,
                translationState = translationState,
                onTranslate = onTranslate,
                onSpeakText = onSpeakText
            )
            else -> StudyTab(
                strings = strings,
                appLanguage = appLanguage,
                studyState = studyState,
                onAssistStudy = onAssistStudy
            )
        }
    }
}

@Composable
private fun VoiceLabTab(
    appLanguage: AppLanguage,
    voiceGender: VoiceGender,
    speechRate: Float,
    isSpeaking: Boolean,
    onSetVoiceGender: (VoiceGender) -> Unit,
    onSetSpeechRate: (Float) -> Unit,
    onSpeakText: (String, String) -> Unit,
    onStopSpeaking: () -> Unit
) {
    val context = LocalContext.current
    var textToSpeak by remember {
        mutableStateOf(
            if (appLanguage == AppLanguage.HINDI) {
                "नमस्ते! मैं विकास AI हूँ। यह असली एंड्रॉइड वॉइस इंजन है।"
            } else {
                "Hello! I am Vikas AI speaking via the native Android Text-to-Speech engine."
            }
        )
    }
    var speakLang by remember { mutableStateOf("Hindi") }
    var voiceCloneConsent by remember { mutableStateOf(false) }
    val langs = listOf("Hindi", "English", "Japanese", "Chinese", "Korean", "Arabic", "Spanish", "French")

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) textToSpeak = spoken
        }
    }

    fun startStt() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (speakLang == "Hindi") "hi-IN" else Locale.US.toLanguageTag())
        }
        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Speech recognition service unavailable on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startStt() else Toast.makeText(context, "Microphone permission denied.", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = if (appLanguage == AppLanguage.HINDI) {
                            "लाइव ऑन-डिवाइस इंजन: Android Text-to-Speech और Speech-to-Text बिना किसी पेड API के मुफ़्त काम करते हैं।"
                        } else {
                            "Live On-Device Engine: Android Text-to-Speech & Speech-to-Text work free without cloud API keys."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            StudioSectionCard {
                Text("Voice Profile & Language", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VoiceGender.entries.forEach { gender ->
                        FilterChip(
                            selected = voiceGender == gender,
                            onClick = { onSetVoiceGender(gender) },
                            label = { Text("${gender.label} Voice") }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(langs) { lang ->
                        FilterChip(
                            selected = speakLang == lang,
                            onClick = { speakLang = lang },
                            label = { Text(lang) }
                        )
                    }
                }
                Text("Speech Speed: ${"%.1fx".format(speechRate)}", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = speechRate,
                    onValueChange = onSetSpeechRate,
                    valueRange = 0.7f..1.5f
                )

                OutlinedTextField(
                    value = textToSpeak,
                    onValueChange = { textToSpeak = it },
                    modifier = Modifier.fillMaxWidth().testTag("voice_tts_input"),
                    label = { Text("Text to Speak or Dictate") },
                    minLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (isSpeaking) onStopSpeaking() else onSpeakText(textToSpeak, speakLang)
                        },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("voice_speak_button")
                    ) {
                        Icon(if (isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (isSpeaking) "Stop" else "Speak TTS")
                    }
                    OutlinedButton(
                        onClick = {
                            val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            if (ok) startStt() else permLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("voice_stt_button")
                    ) {
                        Icon(Icons.Outlined.Mic, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Dictate (STT)")
                    }
                }
            }
        }

        item {
            StudioSectionCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Text("Voice Cloning Permission Policy", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = "Voice cloning is strictly gated behind explicit speaker ownership consent and requires a live cloud voice-cloning backend (not active in preview mode).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = voiceCloneConsent, onCheckedChange = { voiceCloneConsent = it })
                    Text(
                        text = "I confirm I own or have written permission for any voice sample.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (voiceCloneConsent) {
                    Text(
                        text = "Consent recorded locally. Cloud custom-voice synthesis is unavailable while Firebase/cloud models are disconnected.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun CodingTab(
    strings: VikasStrings,
    appLanguage: AppLanguage,
    codeState: StudioUiState<CodeGenResult>,
    onGenerateCode: (String, CodeLanguage, CodeTask) -> Unit
) {
    val context = LocalContext.current
    var prompt by remember { mutableStateOf("") }
    var selectedLang by remember { mutableStateOf(CodeLanguage.HTML_CSS_JS) }
    var selectedTask by remember { mutableStateOf(CodeTask.GENERATE) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FirebaseNoticeBanner(
                title = strings.previewBannerTitle,
                body = "Code generation & error fixing return sample templates until Firebase AI Logic (gemini-4-argon) is enabled.",
                modelTag = "gemini-4-argon"
            )
        }
        item {
            StudioSectionCard {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CodeTask.entries) { task ->
                        FilterChip(
                            selected = selectedTask == task,
                            onClick = { selectedTask = task },
                            label = { Text(if (appLanguage == AppLanguage.HINDI) task.labelHi else task.labelEn) }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CodeLanguage.entries) { lang ->
                        FilterChip(
                            selected = selectedLang == lang,
                            onClick = { selectedLang = lang },
                            label = { Text(lang.label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier.fillMaxWidth().testTag("code_prompt_input"),
                    label = { Text("Describe app, website, or paste buggy code") },
                    minLines = 3
                )
                Button(
                    onClick = { onGenerateCode(prompt, selectedLang, selectedTask) },
                    enabled = prompt.isNotBlank() && codeState !is StudioUiState.Loading,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("code_generate_button")
                ) {
                    Icon(Icons.Filled.Code, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Run AI Code Preview")
                }
            }
        }
        if (codeState is StudioUiState.Success) {
            val res = codeState.data
            item {
                StudioSectionCard {
                    SampleOutputTag(modelName = res.targetModel)
                    Text(res.explanation, style = MaterialTheme.typography.bodyMedium)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = res.codeSnippet,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(12.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = { copyToClipboard(context, "Code", res.codeSnippet, strings.copiedToast) },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(strings.copy)
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentsTab(
    strings: VikasStrings,
    appLanguage: AppLanguage,
    pickedDocument: PickedDocumentInfo?,
    documentState: StudioUiState<DocumentAnalysisResult>,
    onLoadDocument: (android.net.Uri) -> Unit,
    onAnalyzeDocument: (String, DocumentTask) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf(DocumentTask.SUMMARIZE) }

    val docPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) onLoadDocument(uri)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FirebaseNoticeBanner(
                title = strings.previewBannerTitle,
                body = "Local PDF page inspection and TXT/MD reading run natively via Storage Access Framework. AI Q&A and essays require Firebase AI Logic.",
                modelTag = "gemini-3.8-flash"
            )
        }
        item {
            StudioSectionCard {
                OutlinedButton(
                    onClick = { docPicker.launch(arrayOf("application/pdf", "text/*")) },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("docs_pick_button")
                ) {
                    Icon(Icons.Outlined.UploadFile, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (pickedDocument == null) "Pick PDF or Text Document" else "Loaded: ${pickedDocument.fileName}")
                }
                if (pickedDocument != null) {
                    Text(
                        text = "${pickedDocument.mimeType} • ${pickedDocument.sizeBytes / 1024} KB" +
                            (pickedDocument.pageCount?.let { " • $it pages" } ?: ""),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DocumentTask.entries) { task ->
                        FilterChip(
                            selected = selectedTask == task,
                            onClick = { selectedTask = task },
                            label = { Text(if (appLanguage == AppLanguage.HINDI) task.labelHi else task.labelEn) }
                        )
                    }
                }
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier.fillMaxWidth().testTag("docs_question_input"),
                    label = { Text("Ask a question or provide writing topic") },
                    minLines = 2
                )
                Button(
                    onClick = { onAnalyzeDocument(question, selectedTask) },
                    enabled = (pickedDocument != null || question.isNotBlank()) && documentState !is StudioUiState.Loading,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("docs_run_button")
                ) {
                    Icon(Icons.Filled.Description, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Run Document Preview")
                }
            }
        }
        if (documentState is StudioUiState.Success) {
            item {
                StudioSectionCard {
                    SampleOutputTag(modelName = "gemini-3.8-flash")
                    Text(documentState.data.outputText, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun TranslatorTab(
    strings: VikasStrings,
    appLanguage: AppLanguage,
    translationState: StudioUiState<TranslationResult>,
    onTranslate: (String, String, String) -> Unit,
    onSpeakText: (String, String) -> Unit
) {
    var sourceText by remember { mutableStateOf("") }
    var sourceLang by remember { mutableStateOf("Auto-Detect") }
    var targetLang by remember { mutableStateOf(if (appLanguage == AppLanguage.HINDI) "English" else "Hindi") }
    val langs = listOf("Auto-Detect", "English", "Hindi", "Japanese", "Chinese", "Korean", "Arabic", "Spanish", "French")
    val targets = langs.filter { it != "Auto-Detect" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FirebaseNoticeBanner(
                title = strings.previewBannerTitle,
                body = "Script detection & TTS pronunciation work on-device; full neural translation requires Firebase AI Logic.",
                modelTag = "gemini-3.8-flash"
            )
        }
        item {
            StudioSectionCard {
                Text("Source: $sourceLang -> Target: $targetLang", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(targets) { lang ->
                        FilterChip(
                            selected = targetLang == lang,
                            onClick = { targetLang = lang },
                            label = { Text(lang) }
                        )
                    }
                }
                OutlinedTextField(
                    value = sourceText,
                    onValueChange = { sourceText = it },
                    modifier = Modifier.fillMaxWidth().testTag("translate_input"),
                    label = { Text("Enter text to translate") },
                    minLines = 3
                )
                Button(
                    onClick = { onTranslate(sourceText, sourceLang, targetLang) },
                    enabled = sourceText.isNotBlank() && translationState !is StudioUiState.Loading,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("translate_button")
                ) {
                    Icon(Icons.Filled.Translate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Translate Preview")
                }
            }
        }
        if (translationState is StudioUiState.Success) {
            val res = translationState.data
            item {
                StudioSectionCard {
                    SampleOutputTag(modelName = "gemini-3.8-flash")
                    Text("Detected: ${res.detectedLanguage}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(res.translatedText, style = MaterialTheme.typography.bodyLarge)
                    OutlinedButton(
                        onClick = { onSpeakText(sourceText, targetLang) },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Outlined.VolumeUp, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Speak with Device TTS")
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyTab(
    strings: VikasStrings,
    appLanguage: AppLanguage,
    studyState: StudioUiState<StudyResult>,
    onAssistStudy: (StudySubject, StudyMode, String) -> Unit
) {
    var topic by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(StudySubject.MATHEMATICS) }
    var mode by remember { mutableStateOf(StudyMode.STEP_BY_STEP) }
    val selectedQuizAnswers = remember { mutableStateMapOf<Int, Int>() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FirebaseNoticeBanner(
                title = strings.previewBannerTitle,
                body = "Interactive quiz UI works offline; custom AI step-by-step math & science tutoring requires Firebase AI Logic.",
                modelTag = "gemini-4-argon"
            )
        }
        item {
            StudioSectionCard {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StudySubject.entries) { sub ->
                        FilterChip(
                            selected = subject == sub,
                            onClick = { subject = sub },
                            label = { Text(if (appLanguage == AppLanguage.HINDI) sub.labelHi else sub.labelEn) }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StudyMode.entries) { m ->
                        FilterChip(
                            selected = mode == m,
                            onClick = { mode = m },
                            label = { Text(if (appLanguage == AppLanguage.HINDI) m.labelHi else m.labelEn) }
                        )
                    }
                }
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    modifier = Modifier.fillMaxWidth().testTag("study_topic_input"),
                    label = { Text("Enter math problem, science question, or history topic") },
                    minLines = 2
                )
                Button(
                    onClick = {
                        selectedQuizAnswers.clear()
                        onAssistStudy(subject, mode, topic)
                    },
                    enabled = topic.isNotBlank() && studyState !is StudioUiState.Loading,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("study_run_button")
                ) {
                    Icon(Icons.Filled.School, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Study Preview")
                }
            }
        }
        if (studyState is StudioUiState.Success) {
            val res = studyState.data
            item {
                StudioSectionCard {
                    SampleOutputTag(modelName = "gemini-4-argon")
                    Text(res.content, style = MaterialTheme.typography.bodyLarge)
                }
            }
            items(res.quizQuestions.withIndex().toList()) { (qIdx, quiz) ->
                StudioSectionCard {
                    Text("Q${qIdx + 1}: ${quiz.question}", style = MaterialTheme.typography.titleMedium)
                    quiz.options.forEachIndexed { oIdx, opt ->
                        val picked = selectedQuizAnswers[qIdx]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = picked == oIdx,
                                onClick = { selectedQuizAnswers[qIdx] = oIdx }
                            )
                            Text(opt, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    val chosen = selectedQuizAnswers[qIdx]
                    if (chosen != null) {
                        val correct = chosen == quiz.correctIndex
                        Text(
                            text = if (correct) "Correct! ${quiz.explanation}" else "Not quite. ${quiz.explanation}",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
