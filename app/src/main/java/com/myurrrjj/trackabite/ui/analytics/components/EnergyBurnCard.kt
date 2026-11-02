package com.myurrrjj.trackabite.ui.home.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.myurrrjj.trackabite.health.HealthConnectManager
import com.myurrrjj.trackabite.ui.Utils.bouncyClick
import kotlinx.coroutines.launch


@Composable
fun EnergyBurnCard(
    activeCalories: Int,
    basalCalories: Int,
    isBasalEstimated: Boolean,
    healthConnectManager: HealthConnectManager,
    onPermissionsGranted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val availabilityStatus = healthConnectManager.availabilityStatus

    var hasPermissions by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    LaunchedEffect(availabilityStatus) {
        if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
            hasPermissions = healthConnectManager.hasPermissions()
            if (hasPermissions) onPermissionsGranted()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.containsAll(healthConnectManager.requiredPermissions)) {
            hasPermissions = true
            onPermissionsGranted()
        }
    }

    if (showInfo) {
        BurnInfoDialog(onDismiss = { showInfo = false })
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .heightIn(min = 182.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ENERGY BURNED",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                IconButton(onClick = { showInfo = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Information about energy burned",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            when (availabilityStatus) {
                HealthConnectClient.SDK_UNAVAILABLE -> {
                    MessageState("Health Connect is unavailable on this device")
                }

                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    ActionState(
                        "Health Connect app is missing",
                        "Install"
                    ) {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                "market://details?id=com.google.android.apps.healthdata".toUri()
                            )
                        )
                    }
                }

                HealthConnectClient.SDK_AVAILABLE -> {
                    if (hasPermissions) {
                        EnergyBurnSummary(
                            activeCalories = activeCalories,
                            basalCalories = basalCalories,
                            isBasalEstimated = isBasalEstimated
                        )
                    } else {
                        ActionState(
                            "Connect Health Connect to see your burn",
                            "Grant permission"
                        ) {
                            permissionLauncher.launch(
                                healthConnectManager.requiredPermissions
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnergyBurnSummary(
    activeCalories: Int,
    basalCalories: Int,
    isBasalEstimated: Boolean
) {
    val activeColor = MaterialTheme.colorScheme.tertiary
    val basalColor = MaterialTheme.colorScheme.primary

    val safeActive = activeCalories.coerceAtLeast(0)
    val safeBasal = basalCalories.coerceAtLeast(0)
    val total = safeActive + safeBasal

    val animatedTotal by animateIntAsState(
        targetValue = total,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    val animatedActive by animateIntAsState(
        targetValue = safeActive,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    val animatedBasal by animateIntAsState(
        targetValue = safeBasal,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    val activeFraction = if (total > 0) {
        safeActive.toFloat() / total
    } else {
        0f
    }

    val animatedActiveFraction by animateFloatAsState(
        targetValue = activeFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "$animatedTotal",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = "kcal burned",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(
                    if (total > 0) basalColor
                    else MaterialTheme.colorScheme.surfaceVariant
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedActiveFraction.coerceIn(0f, 1f))
                    .background(activeColor)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            EnergyBreakdownRow(
                label = "Active",
                value = animatedActive,
                color = activeColor
            )

            EnergyBreakdownRow(
                label = if (isBasalEstimated) "Basal · est." else "Basal · BMR",
                value = animatedBasal,
                color = basalColor
            )
        }
    }
}

@Composable
private fun EnergyBreakdownRow(
    label: String,
    value: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(Modifier.width(7.dp))

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )

        Text(
            text = "$value kcal",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}


@Composable
private fun EnergyBurnBody(activeCalories: Int, basalCalories: Int, isBasalEstimated: Boolean) {
    val activeColor = MaterialTheme.colorScheme.tertiary
    val basalColor = MaterialTheme.colorScheme.primary
    val total = activeCalories + basalCalories

    val load = remember { Animatable(0f) }
    LaunchedEffect(activeCalories, basalCalories) {
        load.snapTo(0f)
        load.animateTo(1f, tween(durationMillis = 1000, easing = FastOutSlowInEasing))
    }

    val animatedTotal by animateIntAsState(
        targetValue = total,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    val animatedActive by animateIntAsState(
        targetValue = activeCalories,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    val animatedBasal by animateIntAsState(
        targetValue = basalCalories,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$animatedTotal",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "kcal today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 5.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(load.value)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(activeCalories.coerceAtLeast(1).toFloat())
                        .background(activeColor)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(basalCalories.coerceAtLeast(1).toFloat())
                        .background(basalColor)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LegendItem(dotColor = activeColor, label = "Active", value = "$animatedActive kcal")
            LegendItem(
                dotColor = basalColor,
                label = if (isBasalEstimated) "Basal (est.)" else "Basal (BMR)",
                value = "$animatedBasal kcal",
                alignEnd = true
            )
        }
    }
}

@Composable
private fun LegendItem(dotColor: Color, label: String, value: String, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BurnInfoDialog(onDismiss: () -> Unit) {
    val scale = remember { Animatable(0.88f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch { alpha.animateTo(1f, tween(durationMillis = 220)) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale.value; scaleY = scale.value; this.alpha = alpha.value
                }
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "How your burn adds up",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(18.dp))
                InfoItem(
                    dotColor = MaterialTheme.colorScheme.tertiary,
                    title = "Active burn",
                    body = "The energy you spend moving."
                )
                Spacer(Modifier.height(14.dp))
                InfoItem(
                    dotColor = MaterialTheme.colorScheme.primary,
                    title = "Basal burn (BMR)",
                    body = "The energy your body spends just staying alive."
                )
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InfoItem(dotColor: Color, title: String, body: String) {
    Row {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MessageState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActionState(title: String, actionLabel: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onAction,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Text(actionLabel, fontSize = 12.sp)
        }
    }
}