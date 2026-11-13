package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.ai.*
import com.example.data.local.AppLanguage
import com.example.ui.components.FirebaseNoticeBanner
import com.example.ui.components.SampleOutputTag
import com.example.ui.components.StudioSectionCard
import com.example.ui.i18n.VikasStrings
import com.example.ui.viewmodel.StudioUiState

@Composable
fun CreateStudioScreen(
    strings: VikasStrings,
    appLanguage: AppLanguage,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    imageState: StudioUiState<ImageGenerationPreview>,
    imageAnalysisState: StudioUiState<ImageAnalysisResult>,
    videoState: StudioUiState<VideoProjectPlan>,
    musicState: StudioUiState<MusicTrackPlan>,
    onGenerateImage: (String, ImageStyle, ImageTaskMode, ImageResolution, MediaAspectRatio, String?) -> Unit,
    onAnalyzeImage: (String, String) -> Unit,
    onGenerateVideo: (String, Int, MediaAspectRatio, VideoStyle, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onGenerateMusic: (String, MusicDurationMode, String) -> Unit,
    onSpeakText: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = if (appLanguage == AppLanguage.HINDI) {
        listOf("इमेज स्टूडियो", "वीडियो स्टूडियो", "म्यूज़िक लैब")
    } else {
        listOf("Image Studio", "Video Studio", "Music Lab")
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab.coerceIn(0, 2)) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { onSelectTab(idx) },
                    text = { Text(title) },
                    modifier = Modifier.testTag("create_tab_$idx")
                )
            }
        }

        when (selectedTab.coerceIn(0, 2)) {
            0 -> ImageStudioTab(
                strings = strings,
                appLanguage = appLanguage,
                imageState = imageState,
                imageAnalysisState = imageAnalysisState,
                onGenerateImage = onGenerateImage,
                onAnalyzeImage = onAnalyzeImage
            )
            1 -> VideoStudioTab(
                strings = strings,
                appLanguage = appLanguage,
                videoState = videoState,
                onGenerateVideo = onGenerateVideo,
                onSpeakText = onSpeakText
            )
            else -> MusicStudioTab(
                strings = strings,
                appLanguage = appLanguage,
                musicState = musicState,
                onGenerateMusic = onGenerateMusic
            )
        }
    }
}

@Composable
private fun ImageStudioTab(
    strings: VikasStrings,
    appLanguage: AppLanguage,
    imageState: StudioUiState<ImageGenerationPreview>,
    imageAnalysisState: StudioUiState<ImageAnalysisResult>,
    onGenerateImage: (String, ImageStyle, ImageTaskMode, ImageResolution, MediaAspectRatio, String?) -> Unit,
    onAnalyzeImage: (String, String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(ImageStyle.REALISTIC) }
    var selectedMode by remember { mutableStateOf(ImageTaskMode.TEXT_TO_IMAGE) }
    var selectedResolution by remember { mutableStateOf(ImageResolution.RES_1K) }
    var selectedRatio by remember { mutableStateOf(MediaAspectRatio.SQUARE) }
    var pickedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var analysisQuestion by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> pickedPhotoUri = uri }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FirebaseNoticeBanner(
                title = strings.previewBannerTitle,
                body = if (appLanguage == AppLanguage.HINDI) {
                    "इमेज जनरेशन और फोटो एडिटिंग के लिए Firebase AI Logic आवश्यक है। अभी सैंपल स्पेसिफिकेशन कार्ड दिखता है।"
                } else {
                    "Live image generation, editing & vision Q&A require Firebase AI Logic. Photo Picker runs natively; AI outputs show labeled sample specs."
                },
                modelTag = if (selectedResolution == ImageResolution.RES_1K) "gemini-nano-banana-2.1" else "gemini-3-pro-image-preview"
            )
        }

        item {
            StudioSectionCard {
                Text(
                    text = if (appLanguage == AppLanguage.HINDI) "इमेज मोड और स्टाइल" else "Task Mode & Art Style",
                    style = MaterialTheme.typography.titleMedium
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ImageTaskMode.entries) { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(if (appLanguage == AppLanguage.HINDI) mode.labelHi else mode.labelEn) }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ImageStyle.entries) { style ->
                        FilterChip(
                            selected = selectedStyle == style,
                            onClick = { selectedStyle = style },
                            label = { Text(style.label) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ImageResolution.entries.forEach { res ->
                        FilterChip(
                            selected = selectedResolution == res,
                            onClick = { selectedResolution = res },
                            label = { Text(res.label) },
                            modifier = Modifier.testTag("image_res_${res.label.lowercase()}")
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MediaAspectRatio.entries.forEach { ratio ->
                        FilterChip(
                            selected = selectedRatio == ratio,
                            onClick = { selectedRatio = ratio },
                            label = { Text(ratio.label) }
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("image_pick_photo_button")
                ) {
                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (pickedPhotoUri == null) {
                            if (appLanguage == AppLanguage.HINDI) "फ़ोटो अपलोड करें (Photo Picker)" else "Upload Photo (Optional for Edit / Analysis)"
                        } else {
                            "Change Selected Photo"
                        }
                    )
                }

                if (pickedPhotoUri != null) {
                    AsyncImage(
                        model = pickedPhotoUri,
                        contentDescription = "Selected User Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("image_prompt_input"),
                    label = { Text(if (appLanguage == AppLanguage.HINDI) "इमेज प्रॉम्प्ट लिखें" else "Describe the image or edit") },
                    minLines = 2
                )

                Button(
                    onClick = {
                        onGenerateImage(
                            prompt,
                            selectedStyle,
                            selectedMode,
                            selectedResolution,
                            selectedRatio,
                            pickedPhotoUri?.lastPathSegment
                        )
                    },
                    enabled = prompt.isNotBlank() && imageState !is StudioUiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("image_generate_button")
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (appLanguage == AppLanguage.HINDI) "इमेज प्रीव्यू बनाएँ" else "Create Image Preview")
                }
            }
        }

        if (imageState is StudioUiState.Loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (imageState is StudioUiState.Success) {
            val preview = imageState.data
            item {
                StudioSectionCard {
                    SampleOutputTag(modelName = preview.targetModel)
                    Text(text = preview.sceneBlueprint, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        if (pickedPhotoUri != null) {
            item {
                StudioSectionCard {
                    Text(
                        text = if (appLanguage == AppLanguage.HINDI) "फ़ोटो विश्लेषण (Vision Q&A)" else "Analyze Uploaded Photo (Vision Q&A)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        value = analysisQuestion,
                        onValueChange = { analysisQuestion = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Ask about objects, text, or details in this photo") }
                    )
                    FilledTonalButton(
                        onClick = {
                            onAnalyzeImage(
                                pickedPhotoUri?.lastPathSegment ?: "Photo",
                                analysisQuestion.ifBlank { "Describe everything in this image." }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("image_analyze_button")
                    ) {
                        Icon(Icons.Outlined.DocumentScanner, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Analyze Photo (gemini-4-argon)")
                    }
                    if (imageAnalysisState is StudioUiState.Success) {
                        SampleOutputTag(modelName = imageAnalysisState.data.targetModel)
                        Text(imageAnalysisState.data.summary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoStudioTab(
    strings: VikasStrings,
    appLanguage: AppLanguage,
    videoState: StudioUiState<VideoProjectPlan>,
    onGenerateVideo: (String, Int, MediaAspectRatio, VideoStyle, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onSpeakText: (String, String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var durationMin by remember { mutableIntStateOf(5) }
    var aspectRatio by remember { mutableStateOf(MediaAspectRatio.LANDSCAPE) }
    var style by remember { mutableStateOf(VideoStyle.ANIME_MOVIE) }
    var isImageToVideo by remember { mutableStateOf(false) }
    var charConsistency by remember { mutableStateOf(true) }
    var hindiDialogue by remember { mutableStateOf(true) }
    var musicAndSfx by remember { mutableStateOf(true) }
    var sourcePhotoUri by remember { mutableStateOf<Uri?>(null) }

    val durations = listOf(1, 5, 15, 30, 60)
    val videoRatios = listOf(MediaAspectRatio.LANDSCAPE, MediaAspectRatio.PORTRAIT)

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> sourcePhotoUri = uri }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FirebaseNoticeBanner(
                title = strings.previewBannerTitle,
                body = if (appLanguage == AppLanguage.HINDI) {
                    "MP4 वीडियो रेंडरिंग के लिए gemini-omni-1.2-flash (Firebase / Interactions API) आवश्यक है। अभी सीन-बाय-सीन स्टोरीबोर्ड और हिन्दी संवाद प्रीव्यू बनता है।"
                } else {
                    "Live video rendering & MP4 synthesis require Firebase / Interactions API (gemini-omni-1.2-flash). Generates a complete multi-scene storyboard preview."
                },
                modelTag = "gemini-omni-1.2-flash"
            )
        }

        item {
            StudioSectionCard {
                Text(
                    text = if (appLanguage == AppLanguage.HINDI) "वीडियो प्रोजेक्ट सेटिंग्स" else "Video Project Configuration",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isImageToVideo,
                        onClick = { isImageToVideo = false },
                        label = { Text("Text-to-Video") }
                    )
                    FilterChip(
                        selected = isImageToVideo,
                        onClick = { isImageToVideo = true },
                        label = { Text("Image-to-Video") }
                    )
                }

                if (isImageToVideo) {
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Outlined.Movie, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (sourcePhotoUri == null) "Pick Start Image to Animate" else "Start Image Selected ✓")
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(VideoStyle.entries) { st ->
                        FilterChip(
                            selected = style == st,
                            onClick = { style = st },
                            label = { Text(st.label) }
                        )
                    }
                }

                Text("Project Duration", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(durations) { mins ->
                        FilterChip(
                            selected = durationMin == mins,
                            onClick = { durationMin = mins },
                            label = { Text("${mins}m") },
                            modifier = Modifier.testTag("video_duration_$mins")
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    videoRatios.forEach { ratio ->
                        FilterChip(
                            selected = aspectRatio == ratio,
                            onClick = { aspectRatio = ratio },
                            label = { Text(ratio.ratioText) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Character Consistency", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = charConsistency, onCheckedChange = { charConsistency = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hindi Voice Dialogues", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = hindiDialogue, onCheckedChange = { hindiDialogue = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Background Music & SFX", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = musicAndSfx, onCheckedChange = { musicAndSfx = it })
                }

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_prompt_input"),
                    label = { Text("Story, anime plot, or scene description") },
                    minLines = 2
                )

                Button(
                    onClick = {
                        onGenerateVideo(
                            prompt,
                            durationMin,
                            aspectRatio,
                            style,
                            isImageToVideo,
                            charConsistency,
                            hindiDialogue,
                            musicAndSfx
                        )
                    },
                    enabled = prompt.isNotBlank() && videoState !is StudioUiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("video_generate_button")
                ) {
                    Icon(Icons.Filled.MovieCreation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Build ${durationMin}m Scene Plan")
                }
            }
        }

        if (videoState is StudioUiState.Loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (videoState is StudioUiState.Success) {
            val plan = videoState.data
            item {
                StudioSectionCard {
                    SampleOutputTag(modelName = plan.targetModel)
                    Text(plan.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${plan.durationMinutes} min • ${plan.style.label} • ${plan.aspectRatio.ratioText} • ${plan.exportFormat}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            items(plan.scenes) { scene ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Scene ${scene.sceneNumber} (${scene.timeRange})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                scene.cameraMovement,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(scene.visualDescription, style = MaterialTheme.typography.bodyMedium)
                        if (!scene.hindiDialogue.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = scene.hindiDialogue,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onSpeakText(scene.hindiDialogue, "Hindi") },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Outlined.VolumeUp, contentDescription = "Speak Hindi Dialogue")
                                }
                            }
                        }
                        Text(
                            text = "Audio: ${scene.sfxAndMusic}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MusicStudioTab(
    strings: VikasStrings,
    appLanguage: AppLanguage,
    musicState: StudioUiState<MusicTrackPlan>,
    onGenerateMusic: (String, MusicDurationMode, String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var durationMode by remember { mutableStateOf(MusicDurationMode.CLIP_30S) }
    var selectedGenre by remember { mutableStateOf("Cinematic Synth") }
    val genres = listOf("Cinematic Synth", "Lo-Fi Chill", "Indian Fusion", "Electronic Pop", "Acoustic")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FirebaseNoticeBanner(
                title = strings.previewBannerTitle,
                body = if (appLanguage == AppLanguage.HINDI) {
                    "ऑडियो म्यूज़िक सिंथेसिस के लिए Lyria मॉडल कनेक्शन आवश्यक है। अभी यह ट्रैक संरचना और बीट्स का सैंपल प्रीव्यू बनाता है।"
                } else {
                    "Live music audio synthesis requires ${durationMode.targetModel} connection. Generates a clearly labeled arrangement & BPM preview."
                },
                modelTag = durationMode.targetModel
            )
        }

        item {
            StudioSectionCard {
                Text("Music Track Length", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MusicDurationMode.entries.forEach { mode ->
                        FilterChip(
                            selected = durationMode == mode,
                            onClick = { durationMode = mode },
                            label = { Text(if (appLanguage == AppLanguage.HINDI) mode.labelHi else mode.labelEn) }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(genres) { genre ->
                        FilterChip(
                            selected = selectedGenre == genre,
                            onClick = { selectedGenre = genre },
                            label = { Text(genre) }
                        )
                    }
                }
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("music_prompt_input"),
                    label = { Text("Describe mood, instruments, or song theme") },
                    minLines = 2
                )
                Button(
                    onClick = { onGenerateMusic(prompt, durationMode, selectedGenre) },
                    enabled = prompt.isNotBlank() && musicState !is StudioUiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("music_generate_button")
                ) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Compose Track Preview")
                }
            }
        }

        if (musicState is StudioUiState.Success) {
            val track = musicState.data
            item {
                StudioSectionCard {
                    SampleOutputTag(modelName = track.targetModel)
                    Text(track.title, style = MaterialTheme.typography.titleLarge)
                    Text("${track.genreAndMood} • ${track.durationLabel} • ${track.bpm} BPM", color = MaterialTheme.colorScheme.primary)
                    Text("Instruments: ${track.instrumentation}", style = MaterialTheme.typography.bodyMedium)
                    Text(track.sampleLyricsOrStructure, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
