package com.myurrrjj.trackabite.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myurrrjj.trackabite.local.UserProfile
import com.myurrrjj.trackabite.ui.MealViewModel
import com.myurrrjj.trackabite.ui.Utils.ExpressiveTextField
import com.myurrrjj.trackabite.ui.profile.ProfileForm

private enum class SettingSection { PROFILE, API_KEY, MODEL }

private fun isValidKey(raw: String): Boolean {
    val t = raw.trim()
    return t.length in 20..400 &&
            t.none { it.isWhitespace() } &&
            (t.startsWith("AIza") || t.startsWith("AQ."))
}

private fun toggleSection(current: SettingSection?, target: SettingSection): SettingSection? =
    if (current == target) null else target

private fun formatNum(v: Float): String =
    if (v % 1f == 0f) v.toInt().toString() else v.toString()

@Composable
fun SettingsScreen(
    viewModel: MealViewModel,
    initialProfile: UserProfile?,
    onSaveProfile: (UserProfile) -> Unit,
    onNavigateToApiGuide: () -> Unit,
    onBack: () -> Unit
) {
    val models by viewModel.availableModels.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()

    var keyInput by remember { mutableStateOf(viewModel.getApiKey() ?: "") }
    var keyVisible by remember { mutableStateOf(false) }
    var justSaved by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf<SettingSection?>(null) }

    val savedKey = viewModel.getApiKey().orEmpty()
    val keyValid = isValidKey(keyInput)

    val profileSubtitle = initialProfile?.let {
        "${formatNum(it.weightKg)} kg · ${formatNum(it.heightCm)} cm · ${it.age} yrs"
    } ?: "Add your weight, height, age and sex"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 12.dp, end = 24.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Tap a card to set it up.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(22.dp))

            SettingTile(
                icon = Icons.Rounded.Person,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                iconContainer = MaterialTheme.colorScheme.primaryContainer,
                title = "Profile",
                subtitle = profileSubtitle,
                expanded = expandedSection == SettingSection.PROFILE,
                onToggle = { expandedSection = toggleSection(expandedSection, SettingSection.PROFILE) }
            ) {
                Text(
                    text = "Used to estimate your basal burn when Health Connect has nothing to read.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                ProfileForm(
                    initial = initialProfile,
                    submitLabel = "Save profile",
                    onSubmit = onSaveProfile
                )
            }

            Spacer(Modifier.height(14.dp))

            SettingTile(
                icon = Icons.Rounded.VpnKey,
                iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                iconContainer = MaterialTheme.colorScheme.tertiaryContainer,
                title = "Gemini API key",
                subtitle = if (savedKey.isBlank()) "Not set — meal analysis is off" else "Saved on this device",
                expanded = expandedSection == SettingSection.API_KEY,
                onToggle = { expandedSection = toggleSection(expandedSection, SettingSection.API_KEY) }
            ) {
                ExpressiveTextField(
                    value = keyInput,
                    onValueChange = {
                        keyInput = it
                        justSaved = false
                    },
                    label = "API key",
                    leadingIcon = Icons.Rounded.VpnKey,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (keyVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    isError = keyInput.isNotBlank() && !keyValid,
                    trailingIcon = {
                        IconButton(onClick = { keyVisible = !keyVisible }) {
                            Icon(
                                imageVector = if (keyVisible) Icons.Outlined.VisibilityOff
                                else Icons.Outlined.Visibility,
                                contentDescription = if (keyVisible) "Hide key" else "Show key"
                            )
                        }
                    }
                )

                if (keyInput.isNotBlank() && !keyValid) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "That doesn't look like a Gemini key — it should start with AIza or AQ.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onNavigateToApiGuide) {
                        Icon(
                            imageVector = Icons.Rounded.HelpOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("How to get this?")
                    }
                    Button(
                        onClick = {
                            viewModel.saveApiKey(keyInput.trim())
                            viewModel.loadAvailableModels()
                            justSaved = true
                        },
                        enabled = keyValid,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(if (justSaved) "Saved" else "Save", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            SettingTile(
                icon = Icons.Rounded.AutoAwesome,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                iconContainer = MaterialTheme.colorScheme.secondaryContainer,
                title = "AI model",
                subtitle = selectedModel?.displayName ?: "Choose a model",
                expanded = expandedSection == SettingSection.MODEL,
                onToggle = { expandedSection = toggleSection(expandedSection, SettingSection.MODEL) }
            ) {
                if (models.isEmpty()) {
                    Text(
                        text = "No models loaded yet. Add a valid API key above and the list appears here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    models.forEach { model ->
                        val selected = model.name == selectedModel?.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.secondaryContainer
                                    else Color.Transparent
                                )
                                .clickable { viewModel.selectModel(model) }
                                .padding(horizontal = 10.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = { viewModel.selectModel(model) }
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = model.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SettingTile(
    icon: ImageVector,
    iconTint: Color,
    iconContainer: Color,
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(iconContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val chevronRotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "chevronRotation"
                )
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.graphicsLayer { rotationZ = chevronRotation }
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 18.dp)
                ) {
                    content()
                }
            }
        }
    }
}
