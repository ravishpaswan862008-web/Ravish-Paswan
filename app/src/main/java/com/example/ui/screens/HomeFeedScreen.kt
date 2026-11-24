package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.CommentEntity
import com.example.data.local.PostEntity
import com.example.data.local.StoryEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.components.AuthenticCapturePill
import com.example.ui.components.CreatorAvatar
import com.example.ui.components.MediaContentFrame
import com.example.ui.components.VerifiedHumanBadge
import com.example.ui.theme.AmberFlare
import com.example.ui.theme.BlockedCrimson
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.VerifiedEmerald
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFeedScreen(
    currentUser: UserProfileEntity,
    stories: List<StoryEntity>,
    posts: List<PostEntity>,
    blockedUsersCount: Int,
    onOpenStory: (StoryEntity) -> Unit,
    onAddStoryClick: () -> Unit,
    onToggleLike: (PostEntity) -> Unit,
    onToggleBookmark: (PostEntity) -> Unit,
    onOpenCreatorProfile: (String) -> Unit,
    onOpenMessages: () -> Unit,
    onOpenChatWithCreator: (String) -> Unit,
    onBlockCreator: (userId: String, handle: String) -> Unit,
    observeComments: (Long) -> Flow<List<CommentEntity>>,
    onAddComment: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeCommentsPost by remember { mutableStateOf<PostEntity?>(null) }

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
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Top Editorial Header
            item(key = "feed_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                brush = Brush.linearGradient(listOf(CoralPulse, AmberFlare))
                            ),
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = VerifiedEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (blockedUsersCount > 0) {
                                    "Strict Shield • $blockedUsersCount creator(s) videos hidden"
                                } else {
                                    "Verified Humans • Zero AI Edits"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenMessages,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("feed_messages_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Open Direct Messages",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Stories Tray
            item(key = "stories_tray") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item(key = "your_story") {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(72.dp)
                                .clickable(onClick = onAddStoryClick)
                                .testTag("add_story_button")
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                CreatorAvatar(
                                    displayName = currentUser.displayName,
                                    avatarKey = currentUser.avatarKey,
                                    avatarUri = currentUser.avatarUri,
                                    size = 64.dp,
                                    hasUnseenStoryRing = false
                                )
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Add Story",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.stories_your_story),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(stories, key = { it.id }) { story ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(72.dp)
                                .clickable { onOpenStory(story) }
                                .testTag("story_item_${story.id}")
                        ) {
                            CreatorAvatar(
                                displayName = story.authorDisplayName,
                                avatarKey = story.authorAvatarKey,
                                avatarUri = story.authorAvatarUri,
                                size = 64.dp,
                                hasUnseenStoryRing = !story.isViewed
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = story.authorHandle,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (posts.isEmpty()) {
                item(key = "empty_feed") {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = VerifiedEmerald,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "All creators' videos are currently hidden",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "You blocked all creators in the feed, or no posts have been shared yet. Remember: your Direct Messages remain open!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(posts, key = { it.id }) { post ->
                    FeedPostCard(
                        post = post,
                        isOwnPost = post.authorId == currentUser.userId,
                        onToggleLike = { onToggleLike(post) },
                        onToggleBookmark = { onToggleBookmark(post) },
                        onOpenComments = { activeCommentsPost = post },
                        onOpenCreatorProfile = { onOpenCreatorProfile(post.authorId) },
                        onMessageCreator = { onOpenChatWithCreator(post.authorId) },
                        onBlockCreator = { onBlockCreator(post.authorId, post.authorHandle) }
                    )
                }
            }
        }
    }

    activeCommentsPost?.let { post ->
        val comments by observeComments(post.id).collectAsStateWithLifecycle(initialValue = emptyList())
        CommentsBottomSheet(
            post = post,
            comments = comments,
            onDismiss = { activeCommentsPost = null },
            onSendComment = { text -> onAddComment(post.id, text) }
        )
    }
}

@Composable
private fun FeedPostCard(
    post: PostEntity,
    isOwnPost: Boolean,
    onToggleLike: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenComments: () -> Unit,
    onOpenCreatorProfile: () -> Unit,
    onMessageCreator: () -> Unit,
    onBlockCreator: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDoubleTapHeart by remember { mutableStateOf(false) }

    LaunchedEffect(showDoubleTapHeart) {
        if (showDoubleTapHeart) {
            delay(700)
            showDoubleTapHeart = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("post_card_${post.id}")
    ) {
        // Creator Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenCreatorProfile),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CreatorAvatar(
                    displayName = post.authorDisplayName,
                    avatarKey = post.authorAvatarKey,
                    avatarUri = post.authorAvatarUri,
                    size = 40.dp,
                    hasUnseenStoryRing = true
                )
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "@${post.authorHandle}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        VerifiedHumanBadge()
                    }
                    val subtitle = listOf(post.locationTag, post.exifCameraModel)
                        .filter { it.isNotBlank() }
                        .joinToString(" • ")
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("post_menu_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Post options"
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    if (!isOwnPost) {
                        DropdownMenuItem(
                            text = { Text("Message @${post.authorHandle}") },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onMessageCreator()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Block @${post.authorHandle} (Hide Videos, Keep Chat)",
                                    color = BlockedCrimson
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Block,
                                    contentDescription = null,
                                    tint = BlockedCrimson
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onBlockCreator()
                            },
                            modifier = Modifier.testTag("block_creator_menu_${post.id}")
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("View Creator Profile") },
                        onClick = {
                            menuExpanded = false
                            onOpenCreatorProfile()
                        }
                    )
                }
            }
        }

        // Media Frame
        val isVideo = post.mediaType == "REEL_VIDEO"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (isVideo) 0.85f else post.aspectRatio.coerceIn(0.75f, 1.35f))
                .pointerInput(post.id) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (!post.isLiked) onToggleLike()
                            showDoubleTapHeart = true
                        }
                    )
                }
        ) {
            MediaContentFrame(
                mediaKey = post.mediaKey,
                mediaUri = post.mediaUri,
                isVideo = isVideo,
                contentDescription = post.caption,
                isPlaying = true,
                modifier = Modifier.fillMaxSize()
            )

            // Top-left authentic capture pill
            AuthenticCapturePill(
                label = post.authenticityLabel,
                isVideo = isVideo,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )

            if (isVideo) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CoralPulse.copy(alpha = 0.88f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            contentDescription = "Reel",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "REEL",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showDoubleTapHeart,
                enter = scaleIn(animationSpec = spring(dampingRatio = 0.5f)) + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = CoralPulse,
                    modifier = Modifier.size(86.dp)
                )
            }
        }

        // Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleLike,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("like_button_${post.id}")
            ) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like post",
                    tint = if (post.isLiked) CoralPulse else MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${post.likesCount}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = onOpenComments,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("comment_button_${post.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.ChatBubbleOutline,
                    contentDescription = "Comments"
                )
            }

            if (!isOwnPost) {
                IconButton(
                    onClick = onMessageCreator,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Direct message creator"
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = onToggleBookmark,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("bookmark_button_${post.id}")
            ) {
                Icon(
                    imageVector = if (post.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Save post",
                    tint = if (post.isBookmarked) AmberFlare else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Caption
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("@${post.authorHandle} ")
                }
                append(post.caption)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text(
            text = "View comments",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clickable(onClick = onOpenComments)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    post: PostEntity,
    comments: List<CommentEntity>,
    onDismiss: () -> Unit,
    onSendComment: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.comments_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (comments.isEmpty()) {
                Text(
                    text = "No comments yet. Be the first verified human to reply!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "@${comment.authorHandle}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                VerifiedHumanBadge()
                            }
                            Text(
                                text = comment.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text(stringResource(R.string.comments_input_placeholder)) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input_field")
                )
                IconButton(
                    onClick = {
                        if (draft.isNotBlank()) {
                            onSendComment(draft)
                            draft = ""
                        }
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("comment_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send comment",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
