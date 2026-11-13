package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.ai.ChatMode
import com.example.data.local.AppLanguage
import com.example.data.local.ChatMessageEntity
import com.example.ui.components.FirebaseNoticeBanner
import com.example.ui.components.SampleOutputTag
import com.example.ui.components.copyToClipboard
import com.example.ui.i18n.VikasStrings
import java.util.Locale

@Composable
fun ChatScreen(
    strings: VikasStrings,
    appLanguage: AppLanguage,
    messages: List<ChatMessageEntity>,
    isLoading: Boolean,
    isSpeaking: Boolean,
    pendingAttachment: Pair<String, String>?,
    onAttachItem: (String, String) -> Unit,
    onClearAttachment: () -> Unit,
    onSendMessage: (String, ChatMode, String) -> Unit,
    onSpeakText: (String, String) -> Unit,
    onStopSpeaking: () -> Unit,
    onNewChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(ChatMode.GENERAL) }
    var replyLanguage by remember { mutableStateOf("Auto") }
    val listState = rememberLazyListState()

    val replyLanguages = listOf("Auto", "English", "Hindi", "Japanese", "Chinese", "Korean", "Arabic", "Spanish", "French")

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "Selected_Photo.jpg"
            onAttachItem(name, "Image")
        }
    }

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "Document"
            onAttachItem(name, "Document")
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                inputText = if (inputText.isBlank()) spoken else "$inputText $spoken"
            }
        }
    }

    fun launchSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                if (appLanguage == AppLanguage.HINDI) "hi-IN" else Locale.getDefault().toLanguageTag()
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, strings.voiceInput)
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "Speech recognition service unavailable on this device.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchSpeechRecognizer()
        } else {
            Toast.makeText(
                context,
                "Microphone permission is required for voice input.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Mode & Language Selector Strip
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FirebaseNoticeBanner(
                title = strings.previewBannerTitle,
                body = if (appLanguage == AppLanguage.HINDI) {
                    "चैट इतिहास और वॉइस प्लेबैक डिवाइस पर काम करते हैं। लाइव Gemini उत्तरों के लिए Firebase आवश्यक है।"
                } else {
                    "Chat history & TTS voice playback run locally. Live Gemini answers require Firebase AI Logic."
                },
                modelTag = "gemini-3.8-flash"
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ChatMode.entries) { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { selectedMode = mode },
                        label = {
                            Text(if (appLanguage == AppLanguage.HINDI) mode.labelHi else mode.labelEn)
                        },
                        modifier = Modifier.testTag("chat_mode_${mode.name.lowercase()}")
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Icon(
                        imageVector = Icons.Outlined.Translate,
                        contentDescription = "Reply Language",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                items(replyLanguages) { lang ->
                    AssistChip(
                        onClick = { replyLanguage = lang },
                        label = { Text(lang) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (replyLanguage == lang) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_vikas_avatar),
                                contentDescription = "Vikas AI Avatar",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = if (appLanguage == AppLanguage.HINDI) {
                                    "नमस्ते! मैं विकास AI हूँ। कोई भी सवाल पूछें।"
                                } else {
                                    "Hello! I'm Vikas AI. Ask anything across languages."
                                },
                                style = MaterialTheme.typography.titleLarge
                            )
                            val suggestions = if (appLanguage == AppLanguage.HINDI) {
                                listOf(
                                    "क्वांटम कंप्यूटिंग को आसान भाषा में समझाएँ",
                                    "एक आधुनिक पोर्टफोलियो वेबसाइट का HTML कोड दें",
                                    "हिन्दी से जापानी में अभिवादन सिखाएँ"
                                )
                            } else {
                                listOf(
                                    "Explain quantum computing simply",
                                    "Build a responsive portfolio page in HTML & CSS",
                                    "Teach me Japanese travel greetings"
                                )
                            }
                            suggestions.forEach { prompt ->
                                OutlinedButton(
                                    onClick = { onSendMessage(prompt, selectedMode, replyLanguage) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(prompt, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        modifier = Modifier.widthIn(max = 330.dp),
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        ),
                        color = if (isUser) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        border = if (isUser) null else BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isUser && msg.isSampleResponse) {
                                SampleOutputTag(modelName = "gemini-3.8-flash")
                            }
                            if (!msg.attachmentName.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${msg.attachmentType ?: "File"}: ${msg.attachmentName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isUser) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (!isUser) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(
                                        onClick = {
                                            if (isSpeaking) onStopSpeaking() else onSpeakText(msg.text, replyLanguage)
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.Outlined.VolumeUp,
                                            contentDescription = strings.listen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (isSpeaking) strings.stopSpeaking else strings.listen)
                                    }
                                    TextButton(
                                        onClick = {
                                            copyToClipboard(context, "Vikas AI", msg.text, strings.copiedToast)
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ContentCopy,
                                            contentDescription = strings.copy,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(strings.copy)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(
                            text = if (appLanguage == AppLanguage.HINDI) "तैयार हो रहा है..." else "Preparing preview...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Composer Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pendingAttachment != null) {
                    InputChip(
                        selected = true,
                        onClick = onClearAttachment,
                        label = { Text("${pendingAttachment.second}: ${pendingAttachment.first}") },
                        trailingIcon = {
                            Icon(Icons.Filled.Close, contentDescription = "Remove attachment")
                        }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("chat_attach_photo_button")
                    ) {
                        Icon(Icons.Outlined.Image, contentDescription = strings.attachPhoto)
                    }
                    IconButton(
                        onClick = {
                            documentPicker.launch(arrayOf("application/pdf", "text/*"))
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("chat_attach_doc_button")
                    ) {
                        Icon(Icons.Outlined.AttachFile, contentDescription = strings.attachFile)
                    }
                    IconButton(
                        onClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                launchSpeechRecognizer()
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("chat_voice_input_button")
                    ) {
                        Icon(Icons.Outlined.Mic, contentDescription = strings.voiceInput)
                    }
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                        placeholder = { Text(strings.askAnythingHint, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        maxLines = 3
                    )
                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val text = inputText
                                inputText = ""
                                onSendMessage(text, selectedMode, replyLanguage)
                            }
                        },
                        enabled = !isLoading && inputText.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = strings.send)
                    }
                }
            }
        }
    }
}
