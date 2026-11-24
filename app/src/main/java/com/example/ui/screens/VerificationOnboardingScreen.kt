package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.StrictShieldBanner
import com.example.ui.components.createCameraCaptureUri
import com.example.ui.components.persistMediaCopy
import com.example.ui.theme.AmberFlare
import com.example.ui.theme.BlockedCrimson
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.VerifiedEmerald
import com.example.ui.viewmodel.VerificationUiState

@Composable
fun VerificationOnboardingScreen(
    verificationState: VerificationUiState,
    onSubmitVerification: (
        fullName: String,
        handle: String,
        phoneDigits: String,
        idNumber: String,
        selfieUri: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fullName by rememberSaveable { mutableStateOf("") }
    var handle by rememberSaveable { mutableStateOf("") }
    var phoneDigits by rememberSaveable { mutableStateOf("") }
    var idNumber by rememberSaveable { mutableStateOf("") }
    var capturedSelfieUri by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var cameraNotice by rememberSaveable { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            capturedSelfieUri = persistMediaCopy(context, uri, prefix = "selfie")
            cameraNotice = null
        } else {
            cameraNotice = "Live selfie capture was cancelled or camera returned no frame. Please try again."
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createCameraCaptureUri(context, prefix = "liveness_selfie")
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            cameraNotice = context.getString(R.string.verify_camera_permission_rationale)
        }
    }

    fun launchLiveSelfie() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            val uri = createCameraCaptureUri(context, prefix = "liveness_selfie")
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Brand Header Card
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        CoralPulse.copy(alpha = 0.18f),
                                        AmberFlare.copy(alpha = 0.12f),
                                        VerifiedEmerald.copy(alpha = 0.14f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(listOf(CoralPulse, AmberFlare))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.VerifiedUser,
                                        contentDescription = "Video Insta Shield",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.app_name),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.tagline_strict_shield),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = VerifiedEmerald
                                    )
                                }
                            }

                            Text(
                                text = stringResource(R.string.verify_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.verify_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PolicyRulePill(
                                    label = "No Fake IDs",
                                    modifier = Modifier.weight(1f)
                                )
                                PolicyRulePill(
                                    label = "No AI Photos",
                                    modifier = Modifier.weight(1f)
                                )
                                PolicyRulePill(
                                    label = "Block = DM Open",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Identity Verification Form
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text(stringResource(R.string.verify_full_name_label)) },
                            placeholder = { Text("e.g. Ravish Paswan") },
                            leadingIcon = {
                                Icon(Icons.Filled.Person, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verify_fullname_input")
                        )

                        OutlinedTextField(
                            value = handle,
                            onValueChange = { handle = it.lowercase().replace(" ", "") },
                            label = { Text(stringResource(R.string.verify_handle_label)) },
                            placeholder = { Text("e.g. ravish_real") },
                            prefix = { Text("@") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verify_handle_input")
                        )

                        OutlinedTextField(
                            value = phoneDigits,
                            onValueChange = { input ->
                                phoneDigits = input.filter { it.isDigit() }.take(10)
                            },
                            label = { Text(stringResource(R.string.verify_phone_label)) },
                            placeholder = { Text("10-digit mobile number") },
                            leadingIcon = {
                                Icon(Icons.Filled.Phone, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verify_phone_input")
                        )

                        OutlinedTextField(
                            value = idNumber,
                            onValueChange = { idNumber = it.uppercase().take(12) },
                            label = { Text(stringResource(R.string.verify_id_code_label)) },
                            placeholder = { Text("e.g. IND90428174") },
                            leadingIcon = {
                                Icon(Icons.Filled.Badge, contentDescription = null)
                            },
                            supportingText = {
                                Text("1 verified ID document = 1 account. Duplicates & bot patterns are rejected.")
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verify_id_input")
                        )

                        // Live Liveness Selfie Capture
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 2.dp,
                                            color = if (capturedSelfieUri != null) VerifiedEmerald else MaterialTheme.colorScheme.outline,
                                            shape = CircleShape
                                        )
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (capturedSelfieUri != null) {
                                        AsyncImage(
                                            model = Uri.parse(capturedSelfieUri),
                                            contentDescription = "Captured live selfie",
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.CameraAlt,
                                            contentDescription = "Selfie placeholder",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (capturedSelfieUri != null) {
                                            stringResource(R.string.verify_selfie_captured)
                                        } else {
                                            "Live Camera Liveness Check"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (capturedSelfieUri != null) VerifiedEmerald else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Gallery uploads are disabled for identity check to prevent fake or AI-generated profile photos.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    OutlinedButton(
                                        onClick = { launchLiveSelfie() },
                                        modifier = Modifier.testTag("capture_liveness_selfie_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CameraAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.size(6.dp))
                                        Text(
                                            if (capturedSelfieUri != null) "Retake Selfie"
                                            else stringResource(R.string.verify_take_selfie)
                                        )
                                    }
                                }
                            }
                        }

                        if (!cameraNotice.isNullOrBlank()) {
                            StrictShieldBanner(
                                text = cameraNotice!!,
                                isWarning = true
                            )
                        }

                        AnimatedVisibility(visible = verificationState is VerificationUiState.Failed) {
                            val reasons = (verificationState as? VerificationUiState.Failed)?.reasons.orEmpty()
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ErrorOutline,
                                            contentDescription = "Verification rejected",
                                            tint = BlockedCrimson
                                        )
                                        Text(
                                            text = "Verification Rejected (Anti-Fake ID Guard)",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = BlockedCrimson
                                        )
                                    }
                                    reasons.forEach { reason ->
                                        Text(
                                            text = "• $reason",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                onSubmitVerification(
                                    fullName,
                                    handle,
                                    phoneDigits,
                                    idNumber,
                                    capturedSelfieUri
                                )
                            },
                            enabled = verificationState !is VerificationUiState.Checking,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("activate_verified_account_button")
                        ) {
                            if (verificationState is VerificationUiState.Checking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    text = stringResource(R.string.verify_submit_button),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicyRulePill(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = VerifiedEmerald,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
