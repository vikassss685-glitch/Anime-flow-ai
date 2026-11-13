package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AppLanguage
import com.example.data.local.ChatSessionEntity
import com.example.data.local.SavedCreationEntity
import com.example.ui.components.FirebaseNoticeBanner
import com.example.ui.i18n.VikasStrings

enum class StudioDestination(val createTabIndex: Int? = null, val toolsTabIndex: Int? = null) {
    CHAT,
    IMAGE(createTabIndex = 0),
    VIDEO(createTabIndex = 1),
    MUSIC(createTabIndex = 2),
    VOICE(toolsTabIndex = 0),
    CODE(toolsTabIndex = 1),
    DOCUMENTS(toolsTabIndex = 2),
    TRANSLATOR(toolsTabIndex = 3),
    STUDY(toolsTabIndex = 4)
}

private data class FeatureTile(
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val accentColor: Color,
    val destination: StudioDestination,
    val tag: String
)

@Composable
fun HomeScreen(
    strings: VikasStrings,
    appLanguage: AppLanguage,
    sessions: List<ChatSessionEntity>,
    creations: List<SavedCreationEntity>,
    onQuickAsk: (String) -> Unit,
    onNavigateSuite: (StudioDestination) -> Unit,
    onOpenSession: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var quickPrompt by remember { mutableStateOf("") }

    val tiles = listOf(
        FeatureTile(
            strings.featureChatTitle,
            strings.featureChatDesc,
            Icons.Filled.AutoAwesome,
            MaterialTheme.colorScheme.primary,
            StudioDestination.CHAT,
            "tile_chat"
        ),
        FeatureTile(
            strings.featureImageTitle,
            strings.featureImageDesc,
            Icons.Filled.Image,
            MaterialTheme.colorScheme.secondary,
            StudioDestination.IMAGE,
            "tile_image"
        ),
        FeatureTile(
            strings.featureVideoTitle,
            strings.featureVideoDesc,
            Icons.Filled.MovieCreation,
            MaterialTheme.colorScheme.tertiary,
            StudioDestination.VIDEO,
            "tile_video"
        ),
        FeatureTile(
            strings.featureMusicTitle,
            strings.featureMusicDesc,
            Icons.Filled.MusicNote,
            MaterialTheme.colorScheme.primary,
            StudioDestination.MUSIC,
            "tile_music"
        ),
        FeatureTile(
            strings.featureVoiceTitle,
            strings.featureVoiceDesc,
            Icons.Filled.RecordVoiceOver,
            MaterialTheme.colorScheme.secondary,
            StudioDestination.VOICE,
            "tile_voice"
        ),
        FeatureTile(
            strings.featureCodeTitle,
            strings.featureCodeDesc,
            Icons.Filled.Code,
            MaterialTheme.colorScheme.tertiary,
            StudioDestination.CODE,
            "tile_code"
        ),
        FeatureTile(
            strings.featureDocsTitle,
            strings.featureDocsDesc,
            Icons.Filled.Description,
            MaterialTheme.colorScheme.primary,
            StudioDestination.DOCUMENTS,
            "tile_docs"
        ),
        FeatureTile(
            strings.featureTranslateTitle,
            strings.featureTranslateDesc,
            Icons.Filled.Translate,
            MaterialTheme.colorScheme.secondary,
            StudioDestination.TRANSLATOR,
            "tile_translate"
        ),
        FeatureTile(
            strings.featureStudyTitle,
            strings.featureStudyDesc,
            Icons.Filled.School,
            MaterialTheme.colorScheme.tertiary,
            StudioDestination.STUDY,
            "tile_study"
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Hero Studio Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_studio),
                    contentDescription = "Vikas AI Studio Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = if (appLanguage == AppLanguage.HINDI) "हिन्दी + विश्व भाषाएँ" else "MULTILINGUAL SUPER AI",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (appLanguage == AppLanguage.HINDI) "एक ही ऐप में सभी AI टूल्स" else "Every Major AI Studio, Unified.",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (appLanguage == AppLanguage.HINDI) {
                                "चैट, इमेज, वीडियो, म्यूज़िक, आवाज़, कोडिंग, दस्तावेज़, अनुवाद और पढ़ाई"
                            } else {
                                "Chat • Image • Video • Music • Voice • Code • Docs • Translate • Study"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.86f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        item {
            FirebaseNoticeBanner(
                title = strings.previewBannerTitle,
                body = strings.previewBannerBody
            )
        }

        item {
            // Quick Ask Composer Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quickPrompt,
                        onValueChange = { quickPrompt = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_quick_ask_input"),
                        placeholder = {
                            Text(
                                text = strings.askAnythingHint,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    FilledIconButton(
                        onClick = {
                            if (quickPrompt.isNotBlank()) {
                                val text = quickPrompt
                                quickPrompt = ""
                                onQuickAsk(text)
                            } else {
                                onNavigateSuite(StudioDestination.CHAT)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("home_quick_ask_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = strings.send
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = strings.allSuitesTitle,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 3-column or 2-column responsive grid rows (chunked by 2 for crisp mobile readability)
        items(tiles.chunked(2)) { rowTiles ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowTiles.forEach { tile ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 112.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onNavigateSuite(tile.destination) }
                            .testTag(tile.tag),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(tile.accentColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tile.icon,
                                    contentDescription = tile.title,
                                    tint = tile.accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = tile.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = tile.desc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                if (rowTiles.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            // Free System & Usage Limits Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = strings.freeSystemTitle,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = strings.freeSystemTitle,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = strings.freeSystemDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = strings.recentHistoryTitle,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (sessions.isEmpty() && creations.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = strings.noHistoryYet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(sessions.take(4)) { session ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOpenSession(session.id) },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Forum,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${session.mode} • ${session.language}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Open chat"
                        )
                    }
                }
            }
            items(creations.take(3)) { item ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.prompt,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
