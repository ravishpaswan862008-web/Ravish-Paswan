package com.example.data.repository

import com.example.data.local.CommentEntity
import com.example.data.local.DirectMessageEntity
import com.example.data.local.PostEntity
import com.example.data.local.StoryEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.VideoInstaDao
import kotlinx.coroutines.flow.Flow

class VideoInstaRepository(private val dao: VideoInstaDao) {

    val currentUser: Flow<UserProfileEntity?> = dao.observeCurrentUser()
    val otherCreators: Flow<List<UserProfileEntity>> = dao.observeOtherCreators()
    val blockedUsers: Flow<List<UserProfileEntity>> = dao.observeBlockedUsers()
    val visibleFeedPosts: Flow<List<PostEntity>> = dao.observeVisibleFeedPosts()
    val visibleReels: Flow<List<PostEntity>> = dao.observeVisibleReels()
    val visibleStories: Flow<List<StoryEntity>> = dao.observeVisibleStories()
    val allMessages: Flow<List<DirectMessageEntity>> = dao.observeAllMessages()

    fun observeUser(userId: String): Flow<UserProfileEntity?> = dao.observeUserById(userId)

    fun observeVisiblePostsByAuthor(authorId: String): Flow<List<PostEntity>> =
        dao.observeVisiblePostsByAuthor(authorId)

    fun observeOwnPosts(authorId: String): Flow<List<PostEntity>> =
        dao.observeOwnPosts(authorId)

    fun observeComments(postId: Long): Flow<List<CommentEntity>> =
        dao.observeCommentsForPost(postId)

    fun observeMessagesForPeer(peerUserId: String): Flow<List<DirectMessageEntity>> =
        dao.observeMessagesForPeer(peerUserId)

    suspend fun saveVerifiedUser(user: UserProfileEntity) {
        dao.insertUser(user)
    }

    suspend fun toggleBlockUser(userId: String, currentlyBlocked: Boolean) {
        dao.setUserBlocked(userId, !currentlyBlocked)
    }

    suspend fun toggleLike(post: PostEntity) {
        val nextLiked = !post.isLiked
        val nextCount = if (nextLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
        dao.updatePostLike(post.id, nextLiked, nextCount)
    }

    suspend fun toggleBookmark(post: PostEntity) {
        dao.updatePostBookmark(post.id, !post.isBookmarked)
    }

    suspend fun addComment(postId: Long, authorHandle: String, authorName: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        dao.insertComment(
            CommentEntity(
                postId = postId,
                authorHandle = authorHandle,
                authorName = authorName,
                text = trimmed
            )
        )
    }

    suspend fun markStoryViewed(storyId: Long) {
        dao.markStoryViewed(storyId)
    }

    suspend fun publishPost(post: PostEntity) {
        dao.insertPost(post)
    }

    suspend fun publishStory(story: StoryEntity) {
        dao.insertStory(story)
    }

    suspend fun sendMessage(peerUserId: String, senderId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        dao.insertMessage(
            DirectMessageEntity(
                peerUserId = peerUserId,
                senderId = senderId,
                text = trimmed,
                isVideoAttachment = false
            )
        )
    }
}
