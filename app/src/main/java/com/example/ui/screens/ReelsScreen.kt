package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CommentEntity
import com.example.data.local.PostEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.components.AuthenticCapturePill
import com.example.ui.components.CreatorAvatar
import com.example.ui.components.MediaContentFrame
import com.example.ui.components.VerifiedHumanBadge
import com.example.ui.theme.AmberFlare
import com.example.ui.theme.BlockedCrimson
import com.example.ui.theme.CoralPulse
import kotlinx.coroutines.flow.Flow

@Composable
fun ReelsScreen(
    currentUser: UserProfileEntity,
    reels: List<PostEntity>,
    onToggleLike: (PostEntity) -> Unit,
    onToggleBookmark: (PostEntity) -> Unit,
    onOpenCreatorProfile: (String) -> Unit,
    onOpenChatWithCreator: (String) -> Unit,
    onBlockCreator: (userId: String, handle: String) -> Unit,
    onOpenCreateReel: () -> Unit,
    onOpenMessages: () -> Unit,
    observeComments: (Long) -> Flow<List<CommentEntity>>,
    onAddComment: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeCommentsReel by remember { mutableStateOf<PostEntity?>(null) }

    if (reels.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(BlockedCrimson.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VideocamOff,
                            contentDescription = "No visible Reels",
                            tint = BlockedCrimson,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = "No Visible Reels",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "All blocked creators' Reels and videos are hidden automatically. Your direct messages with them are still active!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onOpenCreateReel,
                            modifier = Modifier.testTag("empty_reels_create_button")
                        ) {
                            Text("Create Reel")
                        }
                        Surface(
                            onClick = onOpenMessages,
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.testTag("empty_reels_messages_button")
                        ) {
                            Text(
                                text = "Open Messages",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { reels.size })

    VerticalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("reels_vertical_pager")
    ) { page ->
        val reel = reels[page]
        val isOwnReel = reel.authorId == currentUser.userId
        var isPlaying by remember(reel.id) { mutableStateOf(true) }

        Box(modifier = Modifier.fillMaxSize()) {
            MediaContentFrame(
                mediaKey = reel.mediaKey,
                mediaUri = reel.mediaUri,
                isVideo = true,
                contentDescription = reel.caption,
                isPlaying = isPlaying && pagerState.currentPage == page,
                onTogglePlay = { isPlaying = !isPlaying },
                modifier = Modifier.fillMaxSize()
            )

            // Top Header Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Reels",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                AuthenticCapturePill(
                    label = reel.authenticityLabel,
                    isVideo = true
                )
            }

            // Bottom Info + Right Action Rail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 18.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Creator & Caption Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.clickable { onOpenCreatorProfile(reel.authorId) }
                        ) {
                            CreatorAvatar(
                                displayName = reel.authorDisplayName,
                                avatarKey = reel.authorAvatarKey,
                                avatarUri = reel.authorAvatarUri,
                                size = 40.dp,
                                hasUnseenStoryRing = true
                            )
                            Text(
                                text = "@${reel.authorHandle}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            VerifiedHumanBadge()
                        }

                        Text(
                            text = reel.caption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.94f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = AmberFlare,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = reel.reelAudioTitle.ifBlank { "Original Camera Audio • @${reel.authorHandle}" },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (reel.exifCameraModel.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${reel.exifCameraModel} • No AI Manipulation",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.78f)
                                )
                            }
                        }
                    }

                    // Right Action Rail
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReelActionItem(
                            icon = if (reel.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            label = "${reel.likesCount}",
                            tint = if (reel.isLiked) CoralPulse else Color.White,
                            onClick = { onToggleLike(reel) },
                            testTag = "reel_like_${reel.id}"
                        )
                        ReelActionItem(
                            icon = Icons.Filled.ChatBubbleOutline,
                            label = "Reply",
                            tint = Color.White,
                            onClick = { activeCommentsReel = reel },
                            testTag = "reel_comment_${reel.id}"
                        )
                        ReelActionItem(
                            icon = if (reel.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            label = "Save",
                            tint = if (reel.isBookmarked) AmberFlare else Color.White,
                            onClick = { onToggleBookmark(reel) },
                            testTag = "reel_save_${reel.id}"
                        )
                        if (!isOwnReel) {
                            ReelActionItem(
                                icon = Icons.AutoMirrored.Filled.Chat,
                                label = "DM",
                                tint = Color.White,
                                onClick = { onOpenChatWithCreator(reel.authorId) },
                                testTag = "reel_dm_${reel.id}"
                            )
                            ReelActionItem(
                                icon = Icons.Filled.Block,
                                label = "Block",
                                tint = BlockedCrimson,
                                onClick = { onBlockCreator(reel.authorId, reel.authorHandle) },
                                testTag = "reel_block_${reel.id}"
                            )
                        }
                    }
                }
            }
        }
    }

    activeCommentsReel?.let { reel ->
        val comments by observeComments(reel.id).collectAsStateWithLifecycle(initialValue = emptyList())
        CommentsBottomSheet(
            post = reel,
            comments = comments,
            onDismiss = { activeCommentsReel = null },
            onSendComment = { text -> onAddComment(reel.id, text) }
        )
    }
}

@Composable
private fun ReelActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.36f))
                .testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}
