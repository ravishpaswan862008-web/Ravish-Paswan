package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoInstaDao {

    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 1 LIMIT 1")
    fun observeCurrentUser(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserSync(): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 0 ORDER BY displayName ASC")
    fun observeOtherCreators(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun observeUserById(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE isBlocked = 1 ORDER BY displayName ASC")
    fun observeBlockedUsers(): Flow<List<UserProfileEntity>>

    @Query("SELECT COUNT(*) FROM user_profiles WHERE LOWER(handle) = LOWER(:handle)")
    suspend fun countByHandle(handle: String): Int

    @Query("SELECT COUNT(*) FROM user_profiles WHERE idDocumentHash = :idHash")
    suspend fun countByIdDocumentHash(idHash: String): Int

    @Query("SELECT COUNT(*) FROM user_profiles WHERE phoneLast4 = :phoneLast4 AND isCurrentUser = 0")
    suspend fun countByPhoneLast4(phoneLast4: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserProfileEntity>)

    @Query("UPDATE user_profiles SET isBlocked = :blocked WHERE userId = :userId")
    suspend fun setUserBlocked(userId: String, blocked: Boolean)

    // Feed Posts: strictly hide all posts & videos from blocked creators
    @Query(
        """
        SELECT p.* FROM posts p
        INNER JOIN user_profiles u ON p.authorId = u.userId
        WHERE u.isBlocked = 0
        ORDER BY p.createdAt DESC
        """
    )
    fun observeVisibleFeedPosts(): Flow<List<PostEntity>>

    // Reels: strictly hide all Reels/videos from blocked creators
    @Query(
        """
        SELECT p.* FROM posts p
        INNER JOIN user_profiles u ON p.authorId = u.userId
        WHERE p.mediaType = 'REEL_VIDEO' AND u.isBlocked = 0
        ORDER BY p.createdAt DESC
        """
    )
    fun observeVisibleReels(): Flow<List<PostEntity>>

    // Profile grid for a specific creator (only returns if not blocked, or returns empty if blocked)
    @Query(
        """
        SELECT p.* FROM posts p
        INNER JOIN user_profiles u ON p.authorId = u.userId
        WHERE p.authorId = :authorId AND u.isBlocked = 0
        ORDER BY p.createdAt DESC
        """
    )
    fun observeVisiblePostsByAuthor(authorId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun observeOwnPosts(authorId: String): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("UPDATE posts SET isLiked = :liked, likesCount = :likesCount WHERE id = :postId")
    suspend fun updatePostLike(postId: Long, liked: Boolean, likesCount: Int)

    @Query("UPDATE posts SET isBookmarked = :bookmarked WHERE id = :postId")
    suspend fun updatePostBookmark(postId: Long, bookmarked: Boolean)

    // Stories: strictly hide stories from blocked creators
    @Query(
        """
        SELECT s.* FROM stories s
        INNER JOIN user_profiles u ON s.authorId = u.userId
        WHERE u.isBlocked = 0
        ORDER BY s.createdAt DESC
        """
    )
    fun observeVisibleStories(): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Query("UPDATE stories SET isViewed = 1 WHERE id = :storyId")
    suspend fun markStoryViewed(storyId: Long)

    // Comments
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun observeCommentsForPost(postId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    // Direct Messages: Messaging ALWAYS stays active even when user is blocked!
    @Query("SELECT * FROM direct_messages ORDER BY createdAt DESC")
    fun observeAllMessages(): Flow<List<DirectMessageEntity>>

    @Query("SELECT * FROM direct_messages WHERE peerUserId = :peerUserId ORDER BY createdAt ASC")
    fun observeMessagesForPeer(peerUserId: String): Flow<List<DirectMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DirectMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<DirectMessageEntity>)
}
