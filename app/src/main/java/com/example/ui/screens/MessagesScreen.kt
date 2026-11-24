package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.CreatorAvatar
import com.example.ui.components.StrictShieldBanner
import com.example.ui.components.VerifiedHumanBadge
import com.example.ui.theme.BlockedCrimson
import com.example.ui.viewmodel.DmThreadSummary

@Composable
fun MessagesScreen(
    threads: List<DmThreadSummary>,
    onOpenThread: (peerUserId: String) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "dm_header") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.messages_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    StrictShieldBanner(
                        text = stringResource(R.string.messages_subtitle),
                        isWarning = false
                    )
                }
            }

            items(threads, key = { it.peer.userId }) { thread ->
                val peer = thread.peer
                val lastMsg = thread.lastMessage
                val previewText = when {
                    lastMsg == null -> "Start a verified conversation"
                    peer.isBlocked && lastMsg.isVideoAttachment -> "Video hidden (blocked) • Messaging active"
                    else -> lastMsg.text
                }

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenThread(peer.userId) }
                        .testTag("dm_thread_${peer.userId}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CreatorAvatar(
                            displayName = peer.displayName,
                            avatarKey = peer.avatarKey,
                            avatarUri = peer.avatarUri,
                            size = 52.dp,
                            hasUnseenStoryRing = !peer.isBlocked,
                            isBlocked = peer.isBlocked
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = peer.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                VerifiedHumanBadge()
                            }

                            if (peer.isBlocked) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = BlockedCrimson.copy(alpha = 0.14f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Block,
                                            contentDescription = null,
                                            tint = BlockedCrimson,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.chat_blocked_pill),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BlockedCrimson,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = previewText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        }
    }
}
