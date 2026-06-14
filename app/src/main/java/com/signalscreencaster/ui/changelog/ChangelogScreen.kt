package com.castIRL.ui.changelog

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.castIRL.ui.icons.PhosphorIcons

// ─── Edit these links when ready ────────────────────────────────────────────
private const val DONATION_URL = "https://ko-fi.com/diments"   // ← swap for Ko-fi / PayPal
private const val FEEDBACK_URL = "https://diments.nl/discord"
// ────────────────────────────────────────────────────────────────────────────

private data class ChangelogEntry(
    val version: String,
    val date: String,
    val items: List<String>,
)

private val CHANGELOG = listOf(
    ChangelogEntry(
        version = "1.1.0",
        date    = "June 2026",
        items   = listOf(
            "Full Material 3 Expressive redesign with Material You dynamic color",
            "New floating navigation bar",
            "Predictive back gesture and contextual screen transitions",
            "Wavy, easier-to-grab bitrate slider",
            "Phosphor icon set throughout",
            "Stream URL removed from the home screen for privacy",
            "Screen now stays awake while streaming",
        ),
    ),
    ChangelogEntry(
        version = "1.0.0",
        date    = "May 2025",
        items   = listOf(
            "Initial release",
            "RTMP and SRT streaming support",
            "H.264 and H.265 hardware encoding",
            "Microphone and system audio capture",
            "Adaptive bitrate control",
            "Reconnect on disconnect",
            "Stream profiles",
            "Live stats: bitrate, FPS, dropped frames, duration",
            "Material 3 Expressive UI",
        ),
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen() {
    val context = LocalContext.current

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("What's new", style = MaterialTheme.typography.titleLarge) })
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                FilledTonalButton(
                    onClick  = { openUrl(DONATION_URL) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(PhosphorIcons.Heart, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Support CastIRL")
                }
            }

            items(CHANGELOG) { entry -> ChangelogCard(entry) }

            item {
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick  = { openUrl(FEEDBACK_URL) },
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor   = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Icon(PhosphorIcons.ChatCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Give feedback")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ChangelogCard(entry: ChangelogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape  = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text       = "v${entry.version}",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text  = entry.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(12.dp))

            entry.items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text  = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 10.dp),
                    )
                    Text(
                        text  = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
