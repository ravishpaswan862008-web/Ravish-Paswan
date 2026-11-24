package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        PostEntity::class,
        StoryEntity::class,
        CommentEntity::class,
        DirectMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VideoInstaDatabase : RoomDatabase() {
    abstract fun videoInstaDao(): VideoInstaDao

    companion object {
        @Volatile
        private var INSTANCE: VideoInstaDatabase? = null

        fun getInstance(context: Context): VideoInstaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context): VideoInstaDatabase {
            return Room.databaseBuilder(
                appContext,
                VideoInstaDatabase::class.java,
                "video_insta_db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialVerifiedCommunity(getInstance(appContext).videoInstaDao())
                        }
                    }
                })
                .build()
        }

        private suspend fun seedInitialVerifiedCommunity(dao: VideoInstaDao) {
            val now = System.currentTimeMillis()
            val creators = listOf(
                UserProfileEntity(
                    userId = "creator_arjun",
                    handle = "arjun_lens",
                    displayName = "Arjun Malhotra",
                    phoneLast4 = "4821",
                    idDocumentHash = "VERIFIED_HASH_ARJUN_01",
                    isVerifiedHuman = true,
                    isCurrentUser = false,
                    isBlocked = false,
                    bio = "Documentary filmmaker • 100% raw camera stories from Himachal & Ladakh.",
                    avatarKey = "avatar_arjun",
                    followersCount = 18400,
                    followingCount = 312,
                    createdAt = now - 86_400_000L * 14
                ),
                UserProfileEntity(
                    userId = "creator_meera",
                    handle = "meera_streets",
                    displayName = "Meera Nair",
                    phoneLast4 = "9034",
                    idDocumentHash = "VERIFIED_HASH_MEERA_02",
                    isVerifiedHuman = true,
                    isCurrentUser = false,
                    isBlocked = false,
                    bio = "Street photographer & Reel storyteller in Jaipur. Zero AI filters, real light only.",
                    avatarKey = "avatar_meera",
                    followersCount = 24900,
                    followingCount = 418,
                    createdAt = now - 86_400_000L * 10
                )
            )
            dao.insertUsers(creators)

            val stories = listOf(
                StoryEntity(
                    authorId = "creator_arjun",
                    authorHandle = "arjun_lens",
                    authorDisplayName = "Arjun Malhotra",
                    authorAvatarKey = "avatar_arjun",
                    mediaKey = "post_mountain",
                    caption = "Dawn mist rolling across the upper trail — raw 35mm lens capture.",
                    isViewed = false,
                    createdAt = now - 3_600_000L * 2
                ),
                StoryEntity(
                    authorId = "creator_meera",
                    authorHandle = "meera_streets",
                    authorDisplayName = "Meera Nair",
                    authorAvatarKey = "avatar_meera",
                    mediaKey = "post_street",
                    caption = "Lantern hour in old Jaipur market — unedited candid frames.",
                    isViewed = false,
                    createdAt = now - 3_600_000L
                )
            )
            dao.insertStories(stories)

            val posts = listOf(
                PostEntity(
                    id = 1L,
                    authorId = "creator_arjun",
                    authorHandle = "arjun_lens",
                    authorDisplayName = "Arjun Malhotra",
                    authorAvatarKey = "avatar_arjun",
                    mediaType = "REEL_VIDEO",
                    mediaKey = "post_mountain",
                    caption = "Morning ridge climb above 11,000 ft. Captured straight on handheld camera—no AI sky replacement.",
                    locationTag = "Spiti Valley, India",
                    aspectRatio = 0.65f,
                    likesCount = 1284,
                    authenticityLabel = "Verified Real Video ✓",
                    exifCameraModel = "Canon EOS R6 • 24fps RAW",
                    reelAudioTitle = "Original Trail Wind & Footsteps • arjun_lens",
                    durationSec = 18,
                    createdAt = now - 7_200_000L
                ),
                PostEntity(
                    id = 2L,
                    authorId = "creator_meera",
                    authorHandle = "meera_streets",
                    authorDisplayName = "Meera Nair",
                    authorAvatarKey = "avatar_meera",
                    mediaType = "PHOTO",
                    mediaKey = "post_street",
                    caption = "Spice stalls and brass lanterns coming alive at 7 PM. Real motion blur, zero synthetic touch-ups.",
                    locationTag = "Johari Bazaar, Jaipur",
                    aspectRatio = 1.25f,
                    likesCount = 942,
                    authenticityLabel = "Authentic Capture ✓",
                    exifCameraModel = "Sony A7C • f/2.8 • 1/60s",
                    reelAudioTitle = "",
                    durationSec = 0,
                    createdAt = now - 14_400_000L
                ),
                PostEntity(
                    id = 3L,
                    authorId = "creator_meera",
                    authorHandle = "meera_streets",
                    authorDisplayName = "Meera Nair",
                    authorAvatarKey = "avatar_meera",
                    mediaType = "REEL_VIDEO",
                    mediaKey = "post_street",
                    caption = "60-second walk through Jaipur's evening spice alley. Verified real-world footage.",
                    locationTag = "Jaipur Old City",
                    aspectRatio = 0.65f,
                    likesCount = 2110,
                    authenticityLabel = "Verified Real Video ✓",
                    exifCameraModel = "Sony A7C • 4K 30fps",
                    reelAudioTitle = "Bazaar Ambience (Live Mic) • meera_streets",
                    durationSec = 24,
                    createdAt = now - 21_600_000L
                ),
                PostEntity(
                    id = 4L,
                    authorId = "creator_arjun",
                    authorHandle = "arjun_lens",
                    authorDisplayName = "Arjun Malhotra",
                    authorAvatarKey = "avatar_arjun",
                    mediaType = "PHOTO",
                    mediaKey = "post_mountain",
                    caption = "Prayer flags catching the first warm light on the pass. Pure optical glass.",
                    locationTag = "Himachal Pradesh",
                    aspectRatio = 1.25f,
                    likesCount = 765,
                    authenticityLabel = "Authentic Capture ✓",
                    exifCameraModel = "Canon EOS R6 • 35mm f/1.8",
                    reelAudioTitle = "",
                    durationSec = 0,
                    createdAt = now - 28_800_000L
                )
            )
            dao.insertPosts(posts)

            val comments = listOf(
                CommentEntity(
                    postId = 1L,
                    authorHandle = "meera_streets",
                    authorName = "Meera Nair",
                    text = "That natural morning haze looks unreal—so refreshing to know it's 100% real!",
                    createdAt = now - 5_000_000L
                ),
                CommentEntity(
                    postId = 2L,
                    authorHandle = "arjun_lens",
                    authorName = "Arjun Malhotra",
                    text = "Love the warm tungsten light on the spice sacks.",
                    createdAt = now - 10_000_000L
                )
            )
            dao.insertComments(comments)

            val messages = listOf(
                DirectMessageEntity(
                    peerUserId = "creator_arjun",
                    senderId = "creator_arjun",
                    text = "Hey! Welcome to Video Insta. Every account here is human-verified and AI edits are blocked.",
                    isVideoAttachment = false,
                    createdAt = now - 3_600_000L
                ),
                DirectMessageEntity(
                    peerUserId = "creator_arjun",
                    senderId = "creator_arjun",
                    text = "Check out my Himalayan trail Reel when you get a chance!",
                    isVideoAttachment = true,
                    attachmentTitle = "Morning ridge climb Reel (18s)",
                    createdAt = now - 3_500_000L
                ),
                DirectMessageEntity(
                    peerUserId = "creator_meera",
                    senderId = "creator_meera",
                    text = "Hi! Try blocking a creator from their profile or Reel menu—you'll see their videos disappear while our chat stays open.",
                    isVideoAttachment = false,
                    createdAt = now - 1_800_000L
                )
            )
            dao.insertMessages(messages)
        }
    }
}
