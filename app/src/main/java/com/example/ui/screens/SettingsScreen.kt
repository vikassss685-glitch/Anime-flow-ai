package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.AppLanguage
import com.example.data.local.UserSettings
import com.example.data.local.VoiceGender
import com.example.ui.components.FirebaseNoticeBanner
import com.example.ui.components.StudioSectionCard
import com.example.ui.i18n.VikasStrings

@Composable
fun SettingsScreen(
    strings: VikasStrings,
    settings: UserSettings,
    onToggleTheme: () -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetVoiceGender: (VoiceGender) -> Unit,
    onSetSpeechRate: (Float) -> Unit,
    onSetPreferOpenModels: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isHindi = settings.appLanguage == AppLanguage.HINDI

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FirebaseNoticeBanner(
                title = strings.previewBannerTitle,
                body = strings.previewBannerBody
            )
        }

        item {
            StudioSectionCard {
                Text(
                    text = if (isHindi) "भाषा और थीम" else "Language & Appearance",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = settings.appLanguage == AppLanguage.ENGLISH,
                        onClick = { onSetLanguage(AppLanguage.ENGLISH) },
                        label = { Text("English") },
                        modifier = Modifier.testTag("settings_lang_en")
                    )
                    FilterChip(
                        selected = settings.appLanguage == AppLanguage.HINDI,
                        onClick = { onSetLanguage(AppLanguage.HINDI) },
                        label = { Text("हिन्दी (Hindi)") },
                        modifier = Modifier.testTag("settings_lang_hi")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isHindi) "डार्क मोड" else "Dark Obsidian Theme",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = if (isHindi) "डार्क या लाइट इंटरफ़ेस चुनें" else "Switch between dark studio and daylight UI",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.isDarkTheme,
                        onCheckedChange = { onToggleTheme() },
                        modifier = Modifier.testTag("settings_theme_switch")
                    )
                }
            }
        }

        item {
            StudioSectionCard {
                Text(
                    text = if (isHindi) "वॉइस और स्पीच सेटिंग्स" else "Voice & Speech Preferences",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VoiceGender.entries.forEach { gender ->
                        FilterChip(
                            selected = settings.voiceGender == gender,
                            onClick = { onSetVoiceGender(gender) },
                            label = { Text("${gender.label} Voice") }
                        )
                    }
                }
                Text("TTS Speed: ${"%.1fx".format(settings.speechRate)}", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = settings.speechRate,
                    onValueChange = onSetSpeechRate,
                    valueRange = 0.7f..1.5f
                )
            }
        }

        item {
            StudioSectionCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Text(
                        text = if (isHindi) "AI मॉडल कनेक्शन और निःशुल्क सीमाएँ" else "Model Connection & Free Usage Limits",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Prefer Free / Open-Tier Models", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Prioritize zero-cost on-device tools and free-tier models whenever available.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.preferOpenModels,
                        onCheckedChange = onSetPreferOpenModels
                    )
                }
                HorizontalDivider()
                Text(
                    text = "Live On-Device (100% Free, No Account Needed):\n" +
                        "• Android Text-to-Speech (Hindi, English & multilingual voices)\n" +
                        "• Speech-to-Text voice dictation (with mic permission)\n" +
                        "• Zero-permission Photo Picker & PDF/TXT Document Reader\n" +
                        "• Local Room Chat History & Saved Studio Creations",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Cloud AI Status (Currently Sample Preview Mode):\n" +
                        "• Chat, Code, Docs, Translation, Study: gemini-3.8-flash / gemini-4-argon (Not connected — requires Firebase AI Logic)\n" +
                        "• Image Studio: gemini-nano-banana-2.1 / gemini-3-pro-image-preview (Not connected — requires Firebase AI Logic + Blaze plan)\n" +
                        "• Video & Music: gemini-omni-1.2-flash & lyria-3-clip/pro-preview (Not connected — requires Firebase / Interactions API)\n\n" +
                        "Security Note: In AI Studio Android apps, cloud Gemini models authenticate securely via Firebase App Check rather than embedding raw API keys in the APK. Enable Firebase when prompted to connect live models.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
