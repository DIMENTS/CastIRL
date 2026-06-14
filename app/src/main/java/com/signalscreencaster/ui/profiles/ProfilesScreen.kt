package com.castIRL.ui.profiles

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.castIRL.data.model.StreamProfile
import com.castIRL.ui.icons.PhosphorIcons
import com.castIRL.ui.theme.MotionTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    viewModel: ProfilesViewModel = hiltViewModel(),
) {
    val profiles by viewModel.profiles.collectAsState()
    var showSaveDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profiles", style = MaterialTheme.typography.titleLarge) })
        },
    ) { padding ->
      Box(Modifier.fillMaxSize().padding(padding)) {
        if (profiles.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(96.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                ) {
                    Icon(
                        PhosphorIcons.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp),
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text("No profiles yet", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tap + to save your current settings as a profile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 12.dp, bottom = 110.dp),
            ) {
                items(profiles, key = { it.id }) { profile ->
                    ProfileCard(
                        profile  = profile,
                        onLoad   = { viewModel.loadProfile(profile) },
                        onDelete = { viewModel.deleteProfile(profile.id) },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick        = { showSaveDialog = true; newProfileName = "" },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier       = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 96.dp),
        ) {
            Icon(PhosphorIcons.Plus, contentDescription = "Save current settings as profile")
        }
      }

        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Save profile") },
                text = {
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Profile name") },
                        singleLine = true,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newProfileName.isNotBlank()) {
                                viewModel.saveCurrentAsProfile(newProfileName.trim())
                                showSaveDialog = false
                            }
                        },
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
                },
            )
        }
    }
}

@Composable
private fun ProfileCard(
    profile: StreamProfile,
    onLoad: () -> Unit,
    onDelete: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = MotionTokens.spatialFast(),
        label         = "profilePress",
    )

    Card(
        onClick = onLoad,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .scale(pressScale),
        shape  = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            ) {
                Icon(
                    PhosphorIcons.VideoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    profileSummary(profile),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    PhosphorIcons.Trash,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun profileSummary(profile: StreamProfile): String {
    val proto = profile.connection.protocol.name
    val res   = "${profile.video.width}×${profile.video.height}"
    val fps   = "${profile.video.fps}fps"
    val br    = if (profile.video.bitrateBps >= 1_000_000)
        "%.1fM".format(profile.video.bitrateBps / 1_000_000.0)
    else "${profile.video.bitrateBps / 1_000}k"
    return "$proto · $res $fps · $br"
}
