package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.StoryEntity
import com.example.ui.components.AuthenticCapturePill
import com.example.ui.components.CreatorAvatar
import com.example.ui.components.MediaContentFrame
import com.example.ui.components.VerifiedHumanBadge
import kotlinx.coroutines.delay

@Composable
fun StoryViewerOverlay(
    stories: List<StoryEntity>,
    initialStoryId: Long,
    onMarkViewed: (Long) -> Unit,
    onSendReply: (peerUserId: String, text: String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val startIndex = remember(stories, initialStoryId) {
        stories.indexOfFirst { it.id == initialStoryId }.coerceAtLeast(0)
    }
    var currentIndex by remember { mutableIntStateOf(startIndex) }
    var replyText by remember { mutableStateOf("") }

    if (stories.isEmpty() || currentIndex !in stories.indices) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    val story = stories[currentIndex]

    LaunchedEffect(story.id) {
        onMarkViewed(story.id)
        delay(5500)
        if (currentIndex < stories.lastIndex) {
            currentIndex++
        } else {
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding()
            .pointerInput(currentIndex, stories.size) {
                detectTapGestures { offset ->
                    if (offset.x < size.width * 0.35f) {
                        if (currentIndex > 0) currentIndex--
                    } else {
                        if (currentIndex < stories.lastIndex) currentIndex++ else onClose()
                    }
                }
            }
    ) {
        MediaContentFrame(
            mediaKey = story.mediaKey,
            mediaUri = story.mediaUri,
            isVideo = false,
            contentDescription = story.caption,
            modifier = Modifier.fillMaxSize()
        )

        // Top Scrim + Progress Bars + Author Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.72f), Color.Transparent)
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .align(Alignment.TopCenter)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stories.forEachIndexed { idx, _ ->
                        val targetProgress = when {
                            idx < currentIndex -> 1f
                            idx == currentIndex -> 1f
                            else -> 0f
                        }
                        val animatedProgress by animateFloatAsState(
                            targetValue = if (idx == currentIndex) 1f else targetProgress,
                            animationSpec = if (idx == currentIndex) tween(5500, easing = LinearEasing) else tween(0),
                            label = "storyProgress_$idx"
                        )
                        LinearProgressIndicator(
                            progress = { if (idx < currentIndex) 1f else if (idx > currentIndex) 0f else animatedProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(50)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.32f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CreatorAvatar(
                        displayName = story.authorDisplayName,
                        avatarKey = story.authorAvatarKey,
                        avatarUri = story.authorAvatarUri,
                        size = 38.dp,
                        hasUnseenStoryRing = false
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "@${story.authorHandle}",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            VerifiedHumanBadge()
                        }
                        AuthenticCapturePill(
                            label = "Verified Real Story",
                            isVideo = false
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("close_story_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close story",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Bottom Caption + Direct Message Reply Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (story.caption.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.45f)
                    ) {
                        Text(
                            text = story.caption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = {
                            Text(
                                "Reply to @${story.authorHandle}...",
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("story_reply_input")
                    )
                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onSendReply(story.authorId, "Replied to story: $replyText")
                                replyText = ""
                                onClose()
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("story_reply_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send story reply",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
