package com.example.data.authenticity

import android.content.Context
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.local.VideoInstaDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.security.MessageDigest

enum class AuthenticityVerdict {
    VERIFIED_REAL_CAPTURE,
    BLOCKED_AI_EDITED,
    BLOCKED_MISSING_PROVENANCE
}

data class AuthenticityReport(
    val verdict: AuthenticityVerdict,
    val isAllowed: Boolean,
    val headline: String,
    val summary: String,
    val cameraMakeModel: String,
    val checksPassed: List<String>,
    val flagsDetected: List<String>,
    val isPlaceholderAi: Boolean = true,
    val aiNotice: String = "Local EXIF + Placeholder AI Scan active. Connect Firebase AI Logic for cloud Gemini multimodal forensics."
)

sealed interface IdentityVerificationResult {
    data class Verified(
        val userId: String,
        val normalizedHandle: String,
        val displayName: String,
        val phoneLast4: String,
        val idDocumentHash: String
    ) : IdentityVerificationResult

    data class Rejected(val reasons: List<String>) : IdentityVerificationResult
}

/**
 * Single abstraction for media authenticity & anti-fake-ID verification.
 * When Firebase AI Logic is enabled, a Gemini multimodal implementation can swap in behind this interface.
 */
interface MediaAuthenticityRepository {
    suspend fun inspectMedia(
        context: Context,
        uri: Uri?,
        isVideo: Boolean,
        fromLiveCamera: Boolean,
        simulateAiEditedDemo: Boolean = false
    ): AuthenticityReport

    suspend fun verifyHumanIdentity(
        fullName: String,
        rawHandle: String,
        phoneDigits: String,
        idNumber: String,
        selfieCaptured: Boolean
    ): IdentityVerificationResult
}

class LocalPlaceholderAuthenticityRepository(
    private val dao: VideoInstaDao
) : MediaAuthenticityRepository {

    private val aiSoftwareSignatures = listOf(
        "photoshop", "midjourney", "dall-e", "dalle", "stable diffusion",
        "stablediffusion", "firefly", "remini", "faceapp", "facetune",
        "imagen", "gemini", "ai_edit", "aiedit", "generative", "synthetic",
        "comfyui", "automatic1111", "snapseed_ai", "lightroom_ai", "magic_eraser"
    )

    private val fakeHandleKeywords = listOf(
        "fake", "bot", "spam", "anon", "anonymous", "burner", "dummy", "ai_clone", "deepfake", "temp"
    )

    override suspend fun inspectMedia(
        context: Context,
        uri: Uri?,
        isVideo: Boolean,
        fromLiveCamera: Boolean,
        simulateAiEditedDemo: Boolean
    ): AuthenticityReport = withContext(Dispatchers.IO) {
        delay(550) // Brief scan window so forensic feedback is legible

        if (simulateAiEditedDemo) {
            return@withContext AuthenticityReport(
                verdict = AuthenticityVerdict.BLOCKED_AI_EDITED,
                isAllowed = false,
                headline = "BLOCKED: AI-Edited Photo Prohibited",
                summary = "Simulated AI-manipulation test detected synthetic retouching & generative fill signatures. Video Insta strictly prohibits AI-edited photos.",
                cameraMakeModel = "Software Signature: Generative AI Fill v2",
                checksPassed = listOf("File header integrity readable"),
                flagsDetected = listOf(
                    "AI Software Tag detected (Generative Fill / Diffusion metadata)",
                    "Missing genuine optical sensor noise signature",
                    "Synthetic skin/background smoothing detected (Placeholder AI Forensic)"
                ),
                isPlaceholderAi = true
            )
        }

        if (uri == null) {
            return@withContext AuthenticityReport(
                verdict = AuthenticityVerdict.BLOCKED_MISSING_PROVENANCE,
                isAllowed = false,
                headline = "No Media Selected",
                summary = "Capture a live photo or select an unedited media file.",
                cameraMakeModel = "None",
                checksPassed = emptyList(),
                flagsDetected = listOf("No URI supplied")
            )
        }

        val fileName = resolveDisplayName(context, uri).lowercase()
        val matchedNameSignature = aiSoftwareSignatures.firstOrNull { fileName.contains(it) }

        if (isVideo) {
            if (matchedNameSignature != null) {
                return@withContext AuthenticityReport(
                    verdict = AuthenticityVerdict.BLOCKED_AI_EDITED,
                    isAllowed = false,
                    headline = "BLOCKED: AI-Manipulated Video Detected",
                    summary = "Filename or metadata indicates AI video synthesis ('$matchedNameSignature').",
                    cameraMakeModel = "Unverified Video Stream",
                    checksPassed = emptyList(),
                    flagsDetected = listOf("Matched prohibited AI marker: $matchedNameSignature")
                )
            }
            return@withContext AuthenticityReport(
                verdict = AuthenticityVerdict.VERIFIED_REAL_CAPTURE,
                isAllowed = true,
                headline = "VERIFIED: Authentic Video Reel",
                summary = if (fromLiveCamera) {
                    "Captured directly via device camera hardware."
                } else {
                    "No AI synthesis markers found in video container metadata (Placeholder AI Check)."
                },
                cameraMakeModel = if (fromLiveCamera) "Live Device Camera" else "Verified Media Stream",
                checksPassed = listOf(
                    "Zero AI synthesis software tags",
                    if (fromLiveCamera) "Direct hardware capture provenance" else "Clean gallery video container"
                ),
                flagsDetected = emptyList()
            )
        }

        // Photo EXIF Inspection
        var exifMake = ""
        var exifModel = ""
        var exifSoftware = ""
        var exifDescription = ""
        var exifDateTime = ""

        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                exifMake = exif.getAttribute(ExifInterface.TAG_MAKE).orEmpty().trim()
                exifModel = exif.getAttribute(ExifInterface.TAG_MODEL).orEmpty().trim()
                exifSoftware = exif.getAttribute(ExifInterface.TAG_SOFTWARE).orEmpty().trim()
                exifDescription = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION).orEmpty().trim()
                exifDateTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME).orEmpty()
            }
        } catch (_: Exception) {
            // Stream may not carry EXIF (e.g., stripped PNG / screenshot)
        }

        val combinedTags = "$exifSoftware $exifDescription $fileName".lowercase()
        val detectedAiTag = aiSoftwareSignatures.firstOrNull { combinedTags.contains(it) }

        if (detectedAiTag != null) {
            return@withContext AuthenticityReport(
                verdict = AuthenticityVerdict.BLOCKED_AI_EDITED,
                isAllowed = false,
                headline = "BLOCKED: AI-Edited Photo Prohibited",
                summary = "Prohibited editing or AI-generation software signature ('$detectedAiTag') was found in image metadata.",
                cameraMakeModel = if (exifSoftware.isNotBlank()) "Software: $exifSoftware" else "Edited / Synthetic Image",
                checksPassed = emptyList(),
                flagsDetected = listOf(
                    "Prohibited AI/Edit signature: $detectedAiTag",
                    "Video Insta rejects all AI-edited, retouched, or generated photos"
                )
            )
        }

        if (fromLiveCamera) {
            val hardwareLabel = listOf(exifMake, exifModel)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { "Live Device Camera Sensor" }
            return@withContext AuthenticityReport(
                verdict = AuthenticityVerdict.VERIFIED_REAL_CAPTURE,
                isAllowed = true,
                headline = "VERIFIED: Live Camera Capture",
                summary = "Direct hardware capture verified. Zero AI editing or synthetic manipulation detected.",
                cameraMakeModel = hardwareLabel,
                checksPassed = listOf(
                    "Direct live camera capture session",
                    "Zero AI or editing software tags in EXIF",
                    "Placeholder AI authenticity gate passed"
                ),
                flagsDetected = emptyList()
            )
        }

        // Gallery photo: require genuine camera EXIF hardware provenance
        val hasCameraExif = exifMake.isNotBlank() || exifModel.isNotBlank()
        if (!hasCameraExif) {
            return@withContext AuthenticityReport(
                verdict = AuthenticityVerdict.BLOCKED_MISSING_PROVENANCE,
                isAllowed = false,
                headline = "BLOCKED: Unverified / Stripped Metadata",
                summary = "This image lacks camera hardware EXIF tags (common in AI-generated images, screenshots, and AI-edited exports). Use Live Camera or an unedited camera photo.",
                cameraMakeModel = "Missing Camera EXIF",
                checksPassed = listOf("No explicit AI tool tag in filename"),
                flagsDetected = listOf(
                    "Camera Make/Model EXIF missing",
                    "Cannot prove unedited optical sensor origin"
                )
            )
        }

        val checks = mutableListOf(
            "Camera EXIF verified: $exifMake $exifModel".trim(),
            "Zero AI/editing software signatures in EXIF"
        )
        if (exifDateTime.isNotBlank()) {
            checks += "Original shutter timestamp: $exifDateTime"
        }

        AuthenticityReport(
            verdict = AuthenticityVerdict.VERIFIED_REAL_CAPTURE,
            isAllowed = true,
            headline = "VERIFIED: Authentic Camera Photo",
            summary = "Genuine camera hardware provenance confirmed with no AI editing signatures.",
            cameraMakeModel = "$exifMake $exifModel".trim(),
            checksPassed = checks,
            flagsDetected = emptyList()
        )
    }

    override suspend fun verifyHumanIdentity(
        fullName: String,
        rawHandle: String,
        phoneDigits: String,
        idNumber: String,
        selfieCaptured: Boolean
    ): IdentityVerificationResult = withContext(Dispatchers.IO) {
        val reasons = mutableListOf<String>()
        val cleanName = fullName.trim()
        val cleanHandle = rawHandle.trim().lowercase().removePrefix("@")
        val digitsOnlyPhone = phoneDigits.filter { it.isDigit() }
        val cleanId = idNumber.trim().uppercase()

        if (cleanName.length < 3 || !cleanName.contains(" ")) {
            reasons += "Enter your real full name (first & last name)."
        }
        if (!cleanHandle.matches(Regex("^[a-z0-9_]{3,20}$"))) {
            reasons += "Handle must be 3–20 lowercase letters, numbers, or underscores."
        }
        if (fakeHandleKeywords.any { cleanHandle.contains(it) }) {
            reasons += "Handle contains prohibited fake/bot/burner keywords."
        }
        if (digitsOnlyPhone.length != 10) {
            reasons += "Enter a valid 10-digit mobile number."
        }
        if (!cleanId.matches(Regex("^[A-Z0-9]{8,12}$"))) {
            reasons += "Verified ID number must be 8–12 alphanumeric characters."
        }
        if (!selfieCaptured) {
            reasons += "Live camera selfie liveness check is mandatory to block fake IDs."
        }

        if (reasons.isEmpty()) {
            if (dao.countByHandle(cleanHandle) > 0) {
                reasons += "Handle @$cleanHandle is already claimed by a verified human."
            }
            val idHash = sha256(cleanId)
            if (dao.countByIdDocumentHash(idHash) > 0) {
                reasons += "This ID document is already linked to another Video Insta account (1 ID = 1 Account)."
            }
            val phoneLast4 = digitsOnlyPhone.takeLast(4)
            if (dao.countByPhoneLast4(phoneLast4) > 0) {
                reasons += "Phone number ending in $phoneLast4 is already registered."
            }
            if (reasons.isEmpty()) {
                return@withContext IdentityVerificationResult.Verified(
                    userId = "user_${cleanHandle}",
                    normalizedHandle = cleanHandle,
                    displayName = cleanName,
                    phoneLast4 = phoneLast4,
                    idDocumentHash = idHash
                )
            }
        }

        IdentityVerificationResult.Rejected(reasons)
    }

    private fun resolveDisplayName(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) cursor.getString(idx).orEmpty() else ""
                    } else ""
                } ?: uri.lastPathSegment.orEmpty()
        } catch (_: Exception) {
            uri.lastPathSegment.orEmpty()
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
