package com.example.aicalorietracker.ui.home.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.aicalorietracker.ui.Utils.bouncyClick
//import com.example.aicalorietracker.ui.home.AttachOptionChip
import com.example.aicalorietracker.ui.home.createTempImageUri
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputArea(
    onSubmit: (Uri?, String, LocalDate) -> Unit,
    isLoading: Boolean,
    targetDate: LocalDate,
    modifier: Modifier = Modifier,
    onOpenSavedMeals: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var attachMenuOpen by remember { mutableStateOf(false) }

    val isReadyToSend = text.isNotBlank() || selectedImageUri != null
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { selectedImageUri = it } }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success && tempCameraUri != null) selectedImageUri = tempCameraUri }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val attachIconRotation by animateFloatAsState(
        targetValue = if (attachMenuOpen) 45f else 0f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        ), label = "AttachRotation"
    )

    val addButtonColor by animateColorAsState(
        targetValue = if (attachMenuOpen) MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
        else MaterialTheme.colorScheme.primary, animationSpec = tween(200), label = "AddColor"
    )

    Column(
        modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.End
    ) {

        AnimatedVisibility(
            visible = selectedImageUri != null,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f)
        ) {
            Box(modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)) {
                selectedImageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-6).dp)
                            .bouncyClick { selectedImageUri = null }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.padding(5.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp, start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            AttachOptionChip(
                label = "Saved",
                icon = Icons.Rounded.Bookmark,
                visible = attachMenuOpen,
                delayMillis = 0,
                containerColor = Color(0xFF2D1A4A),
                iconBgColor = Color(0xFF6D28D9),
                iconTint = Color(0xFFD8B4FE),
                labelColor = Color(0xFFEDE9FE),
                onClick = {
                    attachMenuOpen = false
                    onOpenSavedMeals()
                })
            AttachOptionChip(
                label = "Gallery",
                icon = Icons.Rounded.Image,
                visible = attachMenuOpen,
                delayMillis = 60,
                containerColor = Color(0xFF1A3830),
                iconBgColor = Color(0xFF166534),
                iconTint = Color(0xFF86EFAC),
                labelColor = Color(0xFFBBF7D0),
                onClick = {
                    attachMenuOpen = false
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                })
            AttachOptionChip(
                label = "Camera",
                icon = Icons.Rounded.CameraAlt,
                visible = attachMenuOpen,
                delayMillis = 120,
                containerColor = Color(0xFF1A3A5C),
                iconBgColor = Color(0xFF2563A8),
                iconTint = Color(0xFF93C5FD),
                labelColor = Color(0xFFBFDBFE),
                onClick = {
                    attachMenuOpen = false
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        val uri = createTempImageUri(context)
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                })
        }

        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) {

                Surface(
                    shape = CircleShape,
                    color = addButtonColor,
                    shadowElevation = if (attachMenuOpen) 0.dp else 8.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .bouncyClick(scaleDown = 0.88f) {
                            attachMenuOpen = !attachMenuOpen
                        }) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Attach",
                            modifier = Modifier
                                .size(26.dp)
                                .rotate(attachIconRotation),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        if (it.isNotBlank()) attachMenuOpen = false
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        lineHeight = 24.sp
                    ),
                    maxLines = 5,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (text.isEmpty()) {
                                Text(
                                    text = "e.g. a slice of pizza",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.45f
                                        ), fontSize = 17.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    })

                Spacer(modifier = Modifier.width(8.dp))

                SendButton(
                    isLoading = isLoading, isActive = isReadyToSend, onClick = {
                        val currentText = text
                        val currentUri = selectedImageUri
                        text = ""
                        selectedImageUri = null
                        attachMenuOpen = false
                        onSubmit(currentUri, currentText, targetDate)
                    })
            }
        }
    }
}

@Composable
private fun SendButton(
    isLoading: Boolean, isActive: Boolean, onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(250),
        label = "SendBg"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
        animationSpec = tween(250),
        label = "SendIcon"
    )

    Surface(
        shape = CircleShape,
        color = bgColor,
        shadowElevation = if (isActive) 8.dp else 0.dp,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .bouncyClick(scaleDown = 0.88f) {
                if (isActive) onClick()
            }) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp), color = iconColor, strokeWidth = 2.5.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = "Send",
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}


@Composable
private fun AttachOptionChip(
    label: String,
    icon: ImageVector,
    visible: Boolean,
    delayMillis: Int,
    containerColor: Color,
    iconBgColor: Color,
    iconTint: Color,
    labelColor: Color,
    onClick: () -> Unit
) {
    val enterTransition = remember {
        fadeIn(
            animationSpec = tween(
                durationMillis = 200, delayMillis = delayMillis
            )
        ) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
            ), initialOffsetY = { it / 2 }) + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
            ), initialScale = 0.7f
        )
    }
    val exitTransition = remember {
        fadeOut(animationSpec = tween(150)) + slideOutVertically(
            animationSpec = tween(150),
            targetOffsetY = { it / 2 }) + scaleOut(animationSpec = tween(150), targetScale = 0.7f)
    }

    AnimatedVisibility(visible = visible, enter = enterTransition, exit = exitTransition) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = containerColor,
            shadowElevation = 6.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .bouncyClick(scaleDown = 0.93f, onClick = onClick)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconBgColor)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp),
                        tint = iconTint
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = labelColor,
                    fontSize = 15.sp
                )
            }
        }
    }
}
