package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DirectMessageEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.components.CreatorAvatar
import com.example.ui.components.StrictShieldBanner
import com.example.ui.components.VerifiedHumanBadge
import com.example.ui.theme.BlockedCrimson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadScreen(
    currentUser: UserProfileEntity,
    peer: UserProfileEntity?,
    messages: List<DirectMessageEntity>,
    onBack: () -> Unit,
    onToggleBlock: (UserProfileEntity) -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var draft by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

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
                            .testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    if (peer != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CreatorAvatar(
                                displayName = peer.displayName,
                                avatarKey = peer.avatarKey,
                                avatarUri = peer.avatarUri,
                                size = 36.dp,
                                hasUnseenStoryRing = !peer.isBlocked,
                                isBlocked = peer.isBlocked
                            )
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "@${peer.handle}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    VerifiedHumanBadge()
                                }
                                if (peer.isBlocked) {
                                    Text(
                                        text = "Blocked • Videos Hidden • Chat Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BlockedCrimson
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    if (peer != null) {
                        TextButton(
                            onClick = { onToggleBlock(peer) },
                            modifier = Modifier.testTag("chat_block_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Block,
                                contentDescription = null,
                                tint = if (peer.isBlocked) MaterialTheme.colorScheme.primary else BlockedCrimson,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (peer.isBlocked) "Unblock" else "Block",
                                color = if (peer.isBlocked) MaterialTheme.colorScheme.primary else BlockedCrimson
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = { Text(stringResource(R.string.chat_input_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input")
                    )
                    IconButton(
                        onClick = {
                            if (draft.isNotBlank()) {
                                onSendMessage(draft)
                                draft = ""
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
            ) {
                if (peer?.isBlocked == true) {
                    StrictShieldBanner(
                        text = stringResource(R.string.blocked_policy_banner),
                        isWarning = true,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isMine = msg.senderId == currentUser.userId
                        val isBlockedVideoBubble = (peer?.isBlocked == true) && msg.isVideoAttachment && !isMine

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (isMine) 18.dp else 4.dp,
                                    bottomEnd = if (isMine) 4.dp else 18.dp
                                ),
                                color = when {
                                    isBlockedVideoBubble -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                                    isMine -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier.widthIn(max = 290.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isBlockedVideoBubble) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Lock,
                                                contentDescription = "Hidden video",
                                                tint = BlockedCrimson,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.chat_hidden_video_attachment),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    } else {
                                        if (msg.isVideoAttachment && !msg.attachmentTitle.isNullOrBlank()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Videocam,
                                                    contentDescription = null,
                                                    tint = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = msg.attachmentTitle,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Text(
                                            text = msg.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
