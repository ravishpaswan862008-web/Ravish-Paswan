package com.example.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.ui.theme.AmberFlare
import com.example.ui.theme.BlockedCrimson
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.VerifiedEmerald
import com.example.ui.theme.VioletPrism

val StoryRingBrush = Brush.linearGradient(
    colors = listOf(CoralPulse, AmberFlare, VioletPrism)
)

@Composable
fun VerifiedHumanBadge(
    modifier: Modifier = Modifier,
    showText: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.VerifiedUser,
            contentDescription = "Verified Human Identity",
            tint = VerifiedEmerald,
            modifier = Modifier.size(16.dp)
        )
        if (showText) {
            Text(
                text = "Human Verified",
                style = MaterialTheme.typography.labelSmall,
                color = VerifiedEmerald,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AuthenticCapturePill(
    label: String,
    isVideo: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = Color.Black.copy(alpha = 0.62f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = if (isVideo) Icons.Filled.Videocam else Icons.Filled.CameraAlt,
                contentDescription = "Authentic Capture",
                tint = VerifiedEmerald,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CreatorAvatar(
    displayName: String,
    avatarKey: String,
    avatarUri: String?,
    size: Dp = 44.dp,
    hasUnseenStoryRing: Boolean = false,
    isBlocked: Boolean = false,
    modifier: Modifier = Modifier
) {
    val ringModifier = when {
        isBlocked -> Modifier.border(2.dp, BlockedCrimson, CircleShape)
        hasUnseenStoryRing -> Modifier.border(2.5.dp, StoryRingBrush, CircleShape)
        else -> Modifier.border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(ringModifier)
            .padding(3.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            !avatarUri.isNullOrBlank() -> {
                AsyncImage(
                    model = Uri.parse(avatarUri),
                    contentDescription = "$displayName avatar",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            avatarKey.isNotBlank() -> {
                Image(
                    painter = painterResource(id = resolveDrawableRes(avatarKey)),
                    contentDescription = "$displayName avatar",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                Text(
                    text = displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MediaContentFrame(
    mediaKey: String,
    mediaUri: String?,
    isVideo: Boolean,
    contentDescription: String,
    isPlaying: Boolean = true,
    onTogglePlay: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val hasLocalVideoUri = isVideo && !mediaUri.isNullOrBlank()
    var showPauseOverlay by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(Color(0xFF0B0E14))
            .then(
                if (onTogglePlay != null) {
                    Modifier.clickable {
                        onTogglePlay()
                        showPauseOverlay = !isPlaying
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (hasLocalVideoUri) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(Uri.parse(mediaUri))
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            if (isPlaying) start()
                        }
                    }
                },
                update = { view ->
                    if (isPlaying && !view.isPlaying) {
                        view.start()
                    } else if (!isPlaying && view.isPlaying) {
                        view.pause()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (!mediaUri.isNullOrBlank()) {
            AsyncImage(
                model = Uri.parse(mediaUri),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Bundled authentic showcase photo or animated cinematic Reel preview
            val transition = rememberInfiniteTransition(label = "reelMotion")
            val zoom by transition.animateFloat(
                initialValue = 1.0f,
                targetValue = if (isVideo && isPlaying) 1.08f else 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 7500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "reelZoom"
            )
            Image(
                painter = painterResource(id = resolveDrawableRes(mediaKey)),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (isVideo) {
                            scaleX = zoom
                            scaleY = zoom
                        }
                    },
                contentScale = ContentScale.Crop
            )
        }

        AnimatedVisibility(
            visible = isVideo && !isPlaying,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause Reel" else "Play Reel",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BlockedVideosShieldCard(
    creatorHandle: String,
    onMessageClick: () -> Unit,
    onUnblockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(BlockedCrimson.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Videos hidden",
                    tint = BlockedCrimson,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = "Videos & Reels Hidden",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "You blocked @$creatorHandle. Their videos, Reels, and stories are hidden across Video Insta, but direct messaging remains open.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Surface(
                    onClick = onMessageClick,
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("blocked_profile_message_button")
                ) {
                    Text(
                        text = "Open Chat",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
                Surface(
                    onClick = onUnblockClick,
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("blocked_profile_unblock_button")
                ) {
                    Text(
                        text = "Unblock",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StrictShieldBanner(
    text: String,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isWarning) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
    }
    val tint = if (isWarning) AmberFlare else VerifiedEmerald

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (isWarning) Icons.Filled.Shield else Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
