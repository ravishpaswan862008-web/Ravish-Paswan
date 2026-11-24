package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PostEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.components.BlockedVideosShieldCard
import com.example.ui.components.CreatorAvatar
import com.example.ui.components.MediaContentFrame
import com.example.ui.components.VerifiedHumanBadge
import com.example.ui.theme.BlockedCrimson
import com.example.ui.theme.VerifiedEmerald

@Composable
fun ProfileScreen(
    currentUser: UserProfileEntity,
    ownPosts: List<PostEntity>,
    blockedUsers: List<UserProfileEntity>,
    onUnblockUser: (UserProfileEntity) -> Unit,
    onMessageBlockedUser: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "profile_card") {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CreatorAvatar(
                                displayName = currentUser.displayName,
                                avatarKey = currentUser.avatarKey,
                                avatarUri = currentUser.avatarUri,
                                size = 72.dp,
                                hasUnseenStoryRing = true
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = currentUser.displayName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    VerifiedHumanBadge()
                                }
                                Text(
                                    text = "@${currentUser.handle}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = VerifiedEmerald.copy(alpha = 0.14f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.VerifiedUser,
                                            contentDescription = null,
                                            tint = VerifiedEmerald,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Human ID Verified • ••••${currentUser.phoneLast4}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = VerifiedEmerald,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = currentUser.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStat("${ownPosts.size}", "Authentic Posts")
                            ProfileStat("${ownPosts.count { it.mediaType == "REEL_VIDEO" }}", "Verified Reels")
                            ProfileStat("${blockedUsers.size}", "Blocked")
                        }
                    }
                }
            }

            // Blocked Creators Manager
            if (blockedUsers.isNotEmpty()) {
                item(key = "blocked_header") {
                    Text(
                        text = "Blocked Creators (Videos Hidden • Chat Open)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BlockedCrimson
                    )
                }
                items(blockedUsers, key = { "blocked_${it.userId}" }) { blocked ->
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CreatorAvatar(
                                displayName = blocked.displayName,
                                avatarKey = blocked.avatarKey,
                                avatarUri = blocked.avatarUri,
                                size = 42.dp,
                                isBlocked = true
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "@${blocked.handle}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Videos hidden • DM still open",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { onMessageBlockedUser(blocked.userId) },
                                modifier = Modifier.testTag("profile_chat_blocked_${blocked.userId}")
                            ) {
                                Text("Chat")
                            }
                            Button(
                                onClick = { onUnblockUser(blocked) },
                                modifier = Modifier.testTag("profile_unblock_${blocked.userId}")
                            ) {
                                Text("Unblock")
                            }
                        }
                    }
                }
            }

            item(key = "own_posts_header") {
                Text(
                    text = "Your Authentic Captures",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (ownPosts.isEmpty()) {
                item(key = "no_own_posts") {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = VerifiedEmerald,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "No posts published yet",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Use the Create tab to capture an unedited photo or Reel.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(ownPosts.chunked(2), key = { row -> row.joinToString("_") { it.id.toString() } }) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { post ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.85f)
                                    .clip(MaterialTheme.shapes.medium)
                            ) {
                                MediaContentFrame(
                                    mediaKey = post.mediaKey,
                                    mediaUri = post.mediaUri,
                                    isVideo = post.mediaType == "REEL_VIDEO",
                                    contentDescription = post.caption,
                                    isPlaying = false,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (post.mediaType == "REEL_VIDEO") {
                                    Icon(
                                        imageVector = Icons.Filled.Videocam,
                                        contentDescription = "Reel",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(18.dp)
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorProfileScreen(
    creator: UserProfileEntity?,
    visiblePosts: List<PostEntity>,
    onBack: () -> Unit,
    onToggleBlock: (UserProfileEntity) -> Unit,
    onMessageCreator: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("creator_profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text(
                        text = creator?.let { "@${it.handle}" } ?: "Creator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        if (creator == null) return@Scaffold

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "creator_header") {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CreatorAvatar(
                                    displayName = creator.displayName,
                                    avatarKey = creator.avatarKey,
                                    avatarUri = creator.avatarUri,
                                    size = 68.dp,
                                    hasUnseenStoryRing = !creator.isBlocked,
                                    isBlocked = creator.isBlocked
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            text = creator.displayName,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        VerifiedHumanBadge()
                                    }
                                    Text(
                                        text = "@${creator.handle}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = creator.bio,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onMessageCreator(creator.userId) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("creator_message_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Message")
                                }

                                OutlinedButton(
                                    onClick = { onToggleBlock(creator) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (creator.isBlocked) VerifiedEmerald else BlockedCrimson
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("creator_block_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Block,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (creator.isBlocked) "Unblock" else "Block")
                                }
                            }
                        }
                    }
                }

                if (creator.isBlocked) {
                    item(key = "blocked_shield") {
                        BlockedVideosShieldCard(
                            creatorHandle = creator.handle,
                            onMessageClick = { onMessageCreator(creator.userId) },
                            onUnblockClick = { onToggleBlock(creator) }
                        )
                    }
                } else {
                    items(visiblePosts.chunked(2), key = { row -> row.joinToString("_") { it.id.toString() } }) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { post ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.85f)
                                        .clip(MaterialTheme.shapes.medium)
                                ) {
                                    MediaContentFrame(
                                        mediaKey = post.mediaKey,
                                        mediaUri = post.mediaUri,
                                        isVideo = post.mediaType == "REEL_VIDEO",
                                        contentDescription = post.caption,
                                        isPlaying = false,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
