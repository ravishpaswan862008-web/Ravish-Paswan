package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val handle: String,
    val displayName: String,
    val phoneLast4: String,
    val idDocumentHash: String,
    val isVerifiedHuman: Boolean = true,
    val isCurrentUser: Boolean = false,
    val isBlocked: Boolean = false,
    val bio: String = "",
    val avatarKey: String = "",
    val avatarUri: String? = null,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorId: String,
    val authorHandle: String,
    val authorDisplayName: String,
    val authorAvatarKey: String = "",
    val authorAvatarUri: String? = null,
    val mediaType: String, // "PHOTO" or "REEL_VIDEO"
    val mediaKey: String = "",
    val mediaUri: String? = null,
    val caption: String,
    val locationTag: String = "",
    val aspectRatio: Float = 0.8f,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val authenticityLabel: String = "Authentic Capture ✓",
    val exifCameraModel: String = "Hardware Camera Verified",
    val reelAudioTitle: String = "",
    val durationSec: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorId: String,
    val authorHandle: String,
    val authorDisplayName: String,
    val authorAvatarKey: String = "",
    val authorAvatarUri: String? = null,
    val mediaKey: String = "",
    val mediaUri: String? = null,
    val caption: String = "",
    val isViewed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorHandle: String,
    val authorName: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "direct_messages")
data class DirectMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val peerUserId: String,
    val senderId: String,
    val text: String,
    val isVideoAttachment: Boolean = false,
    val attachmentTitle: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
