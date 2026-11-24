package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.authenticity.AuthenticityReport
import com.example.ui.components.MediaContentFrame
import com.example.ui.components.StrictShieldBanner
import com.example.ui.components.createCameraCaptureUri
import com.example.ui.theme.BlockedCrimson
import com.example.ui.theme.VerifiedEmerald
import com.example.ui.viewmodel.MediaScanState

@Composable
fun CreatePostScreen(
    scanState: MediaScanState,
    initialMode: String = "PHOTO",
    onInspectMedia: (uri: Uri?, isVideo: Boolean, fromLiveCamera: Boolean, simulateAiDemo: Boolean) -> Unit,
    onResetScan: () -> Unit,
    onPublish: (uri: Uri?, mode: String, caption: String, location: String, report: AuthenticityReport) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var publishMode by rememberSaveable(initialMode) { mutableStateOf(initialMode) } // PHOTO, REEL_VIDEO, STORY
    var selectedUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var caption by rememberSaveable { mutableStateOf("") }
    var locationTag by rememberSaveable { mutableStateOf("") }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var permissionNotice by rememberSaveable { mutableStateOf<String?>(null) }

    val selectedUri = remember(selectedUriString) {
        selectedUriString?.let { Uri.parse(it) }
    }
    val isVideoMode = publishMode == "REEL_VIDEO"

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            selectedUriString = uri.toString()
            permissionNotice = null
            onInspectMedia(uri, false, true, false)
        }
    }

    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            selectedUriString = uri.toString()
            permissionNotice = null
            onInspectMedia(uri, true, true, false)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createCameraCaptureUri(context, prefix = if (isVideoMode) "reel_cam" else "photo_cam")
            pendingCameraUri = uri
            if (isVideoMode) {
                captureVideoLauncher.launch(uri)
            } else {
                takePictureLauncher.launch(uri)
            }
        } else {
            permissionNotice = "Camera permission is required for direct live capture. You can still test with Photo Picker or the AI-Edit Block Simulator."
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUriString = uri.toString()
            val mime = context.contentResolver.getType(uri).orEmpty()
            val pickedIsVideo = isVideoMode || mime.startsWith("video/")
            onInspectMedia(uri, pickedIsVideo, false, false)
        }
    }

    fun launchCameraCapture() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            val uri = createCameraCaptureUri(context, prefix = if (isVideoMode) "reel_cam" else "photo_cam")
            pendingCameraUri = uri
            if (isVideoMode) {
                captureVideoLauncher.launch(uri)
            } else {
                takePictureLauncher.launch(uri)
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = VerifiedEmerald
                )
                Text(
                    text = stringResource(R.string.create_screen_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            StrictShieldBanner(
                text = stringResource(R.string.create_policy_notice),
                isWarning = false
            )

            StrictShieldBanner(
                text = stringResource(R.string.create_ai_placeholder_notice),
                isWarning = true
            )

            // Format Selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "PHOTO" to "Photo Post",
                    "REEL_VIDEO" to "Reel Video",
                    "STORY" to "Story"
                ).forEach { (mode, label) ->
                    FilterChip(
                        selected = publishMode == mode,
                        onClick = {
                            if (publishMode != mode) {
                                publishMode = mode
                                selectedUriString = null
                                onResetScan()
                            }
                        },
                        label = { Text(label) },
                        modifier = Modifier.testTag("mode_chip_$mode")
                    )
                }
            }

            // Capture / Pick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { launchCameraCapture() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("create_live_camera_button")
                ) {
                    Icon(
                        imageVector = if (isVideoMode) Icons.Filled.Videocam else Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.create_capture_camera))
                }

                OutlinedButton(
                    onClick = {
                        val visualType = if (isVideoMode) {
                            ActivityResultContracts.PickVisualMedia.VideoOnly
                        } else {
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        }
                        photoPickerLauncher.launch(PickVisualMediaRequest(visualType))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("create_photo_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.create_pick_gallery))
                }
            }

            OutlinedButton(
                onClick = {
                    selectedUriString = null
                    onInspectMedia(null, false, false, true)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("simulate_ai_photo_block_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Science,
                    contentDescription = null,
                    tint = BlockedCrimson,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.create_test_ai_block))
            }

            if (!permissionNotice.isNullOrBlank()) {
                StrictShieldBanner(text = permissionNotice!!, isWarning = true)
            }

            // Preview Frame if media chosen
            if (selectedUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (isVideoMode) 0.85f else 1.15f)
                        .clip(MaterialTheme.shapes.large)
                ) {
                    MediaContentFrame(
                        mediaKey = "",
                        mediaUri = selectedUri.toString(),
                        isVideo = isVideoMode,
                        contentDescription = "Selected media preview",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Forensic Scan Output
            AnimatedVisibility(visible = scanState is MediaScanState.Scanning) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                        Column {
                            Text(
                                text = "Running Authenticity & AI-Edit Forensics...",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Inspecting EXIF hardware tags & synthetic edit signatures",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (scanState is MediaScanState.Completed) {
                val report = scanState.report
                val containerColor = if (report.isAllowed) {
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
                } else {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f)
                }
                val accentColor = if (report.isAllowed) VerifiedEmerald else BlockedCrimson

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = containerColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("authenticity_report_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (report.isAllowed) Icons.Filled.CheckCircle else Icons.Filled.Block,
                                contentDescription = report.headline,
                                tint = accentColor
                            )
                            Text(
                                text = report.headline,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                        Text(
                            text = report.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Source: ${report.cameraMakeModel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        report.checksPassed.forEach { check ->
                            Text(
                                text = "✓ $check",
                                style = MaterialTheme.typography.bodySmall,
                                color = VerifiedEmerald
                            )
                        }
                        report.flagsDetected.forEach { flag ->
                            Text(
                                text = "✕ $flag",
                                style = MaterialTheme.typography.bodySmall,
                                color = BlockedCrimson
                            )
                        }
                    }
                }

                if (report.isAllowed && selectedUri != null) {
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text(stringResource(R.string.create_caption_label)) },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_caption_input")
                    )

                    if (publishMode != "STORY") {
                        OutlinedTextField(
                            value = locationTag,
                            onValueChange = { locationTag = it },
                            label = { Text(stringResource(R.string.create_location_label)) },
                            leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_location_input")
                        )
                    }

                    Button(
                        onClick = {
                            onPublish(selectedUri, publishMode, caption, locationTag, report)
                            caption = ""
                            locationTag = ""
                            selectedUriString = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VerifiedEmerald),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("publish_authentic_button")
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.create_publish_button),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
