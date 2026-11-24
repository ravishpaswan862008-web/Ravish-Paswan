package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.authenticity.AuthenticityReport
import com.example.data.authenticity.IdentityVerificationResult
import com.example.data.authenticity.LocalPlaceholderAuthenticityRepository
import com.example.data.authenticity.MediaAuthenticityRepository
import com.example.data.local.CommentEntity
import com.example.data.local.DirectMessageEntity
import com.example.data.local.PostEntity
import com.example.data.local.StoryEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.VideoInstaDatabase
import com.example.data.repository.VideoInstaRepository
import com.example.ui.components.persistMediaCopy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DmThreadSummary(
    val peer: UserProfileEntity,
    val lastMessage: DirectMessageEntity?
)

sealed interface VerificationUiState {
    data object Idle : VerificationUiState
    data object Checking : VerificationUiState
    data class Failed(val reasons: List<String>) : VerificationUiState
    data object Success : VerificationUiState
}

sealed interface MediaScanState {
    data object Idle : MediaScanState
    data object Scanning : MediaScanState
    data class Completed(val report: AuthenticityReport) : MediaScanState
}

class VideoInstaViewModel(
    private val repository: VideoInstaRepository,
    private val authenticityRepository: MediaAuthenticityRepository
) : ViewModel() {

    val currentUser: StateFlow<UserProfileEntity?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Track whether the initial Room load has completed to avoid onboarding flicker
    private val _hasLoadedSession = MutableStateFlow(false)
    val hasLoadedSession: StateFlow<Boolean> = _hasLoadedSession.asStateFlow()

    init {
        viewModelScope.launch {
            repository.currentUser.collect {
                _hasLoadedSession.value = true
            }
        }
    }

    val otherCreators: StateFlow<List<UserProfileEntity>> = repository.otherCreators
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedUsers: StateFlow<List<UserProfileEntity>> = repository.blockedUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feedPosts: StateFlow<List<PostEntity>> = repository.visibleFeedPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reels: StateFlow<List<PostEntity>> = repository.visibleReels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stories: StateFlow<List<StoryEntity>> = repository.visibleStories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dmThreads: StateFlow<List<DmThreadSummary>> = combine(
        repository.otherCreators,
        repository.allMessages
    ) { creators, messages ->
        creators.map { creator ->
            val lastMsg = messages
                .filter { it.peerUserId == creator.userId }
                .maxByOrNull { it.createdAt }
            DmThreadSummary(peer = creator, lastMessage = lastMsg)
        }.sortedByDescending { it.lastMessage?.createdAt ?: 0L }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _verificationState = MutableStateFlow<VerificationUiState>(VerificationUiState.Idle)
    val verificationState: StateFlow<VerificationUiState> = _verificationState.asStateFlow()

    private val _mediaScanState = MutableStateFlow<MediaScanState>(MediaScanState.Idle)
    val mediaScanState: StateFlow<MediaScanState> = _mediaScanState.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun submitIdentityVerification(
        fullName: String,
        handle: String,
        phoneDigits: String,
        idNumber: String,
        selfieUri: String?
    ) {
        viewModelScope.launch {
            _verificationState.value = VerificationUiState.Checking
            when (
                val result = authenticityRepository.verifyHumanIdentity(
                    fullName = fullName,
                    rawHandle = handle,
                    phoneDigits = phoneDigits,
                    idNumber = idNumber,
                    selfieCaptured = !selfieUri.isNullOrBlank()
                )
            ) {
                is IdentityVerificationResult.Rejected -> {
                    _verificationState.value = VerificationUiState.Failed(result.reasons)
                }
                is IdentityVerificationResult.Verified -> {
                    val user = UserProfileEntity(
                        userId = result.userId,
                        handle = result.normalizedHandle,
                        displayName = result.displayName,
                        phoneLast4 = result.phoneLast4,
                        idDocumentHash = result.idDocumentHash,
                        isVerifiedHuman = true,
                        isCurrentUser = true,
                        isBlocked = false,
                        bio = "Verified Human Creator on Video Insta • Zero AI edits.",
                        avatarUri = selfieUri,
                        followersCount = 1,
                        followingCount = 2
                    )
                    repository.saveVerifiedUser(user)
                    _verificationState.value = VerificationUiState.Success
                }
            }
        }
    }

    fun inspectMedia(
        context: Context,
        uri: Uri?,
        isVideo: Boolean,
        fromLiveCamera: Boolean,
        simulateAiEditedDemo: Boolean = false
    ) {
        viewModelScope.launch {
            _mediaScanState.value = MediaScanState.Scanning
            val report = authenticityRepository.inspectMedia(
                context = context,
                uri = uri,
                isVideo = isVideo,
                fromLiveCamera = fromLiveCamera,
                simulateAiEditedDemo = simulateAiEditedDemo
            )
            _mediaScanState.value = MediaScanState.Completed(report)
        }
    }

    fun resetMediaScan() {
        _mediaScanState.value = MediaScanState.Idle
    }

    fun publishVerifiedMedia(
        context: Context,
        uri: Uri?,
        publishMode: String, // "PHOTO", "REEL_VIDEO", "STORY"
        caption: String,
        locationTag: String,
        report: AuthenticityReport,
        onPublished: () -> Unit
    ) {
        val user = currentUser.value ?: return
        if (!report.isAllowed || uri == null) {
            _statusBanner.value = "Upload blocked: AI-edited or unverified media cannot be published."
            return
        }

        viewModelScope.launch {
            val savedUri = persistMediaCopy(context, uri, prefix = publishMode.lowercase())
            if (publishMode == "STORY") {
                repository.publishStory(
                    StoryEntity(
                        authorId = user.userId,
                        authorHandle = user.handle,
                        authorDisplayName = user.displayName,
                        authorAvatarUri = user.avatarUri,
                        mediaUri = savedUri,
                        caption = caption.ifBlank { "Live authentic story" },
                        isViewed = false
                    )
                )
                _statusBanner.value = "Authentic Story published ✓"
            } else {
                val isReel = publishMode == "REEL_VIDEO"
                repository.publishPost(
                    PostEntity(
                        authorId = user.userId,
                        authorHandle = user.handle,
                        authorDisplayName = user.displayName,
                        authorAvatarUri = user.avatarUri,
                        mediaType = if (isReel) "REEL_VIDEO" else "PHOTO",
                        mediaUri = savedUri,
                        caption = caption.ifBlank { if (isReel) "Verified real Reel" else "Unedited real camera capture" },
                        locationTag = locationTag.trim(),
                        aspectRatio = if (isReel) 0.65f else 1.0f,
                        likesCount = 1,
                        isLiked = false,
                        authenticityLabel = if (isReel) "Verified Real Video ✓" else "Authentic Capture ✓",
                        exifCameraModel = report.cameraMakeModel,
                        reelAudioTitle = if (isReel) "Original Audio • ${user.handle}" else "",
                        durationSec = if (isReel) 15 else 0
                    )
                )
                _statusBanner.value = if (isReel) "Verified Reel published ✓" else "Authentic Photo published ✓"
            }
            _mediaScanState.value = MediaScanState.Idle
            onPublished()
        }
    }

    fun toggleLike(post: PostEntity) {
        viewModelScope.launch { repository.toggleLike(post) }
    }

    fun toggleBookmark(post: PostEntity) {
        viewModelScope.launch { repository.toggleBookmark(post) }
    }

    fun toggleBlockCreator(creator: UserProfileEntity) {
        viewModelScope.launch {
            val willBlock = !creator.isBlocked
            repository.toggleBlockUser(creator.userId, creator.isBlocked)
            _statusBanner.value = if (willBlock) {
                "Blocked @${creator.handle}. Their videos & Reels are now hidden, while messaging remains active."
            } else {
                "Unblocked @${creator.handle}. Their videos & Reels are visible again."
            }
        }
    }

    fun toggleBlockByUserId(userId: String, currentlyBlocked: Boolean, handle: String) {
        viewModelScope.launch {
            repository.toggleBlockUser(userId, currentlyBlocked)
            _statusBanner.value = if (!currentlyBlocked) {
                "Blocked @$handle. Videos & Reels hidden; messaging remains active."
            } else {
                "Unblocked @$handle. Videos & Reels restored."
            }
        }
    }

    fun observeComments(postId: Long): Flow<List<CommentEntity>> =
        repository.observeComments(postId)

    fun addComment(postId: Long, text: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addComment(postId, user.handle, user.displayName, text)
        }
    }

    fun markStoryViewed(storyId: Long) {
        viewModelScope.launch { repository.markStoryViewed(storyId) }
    }

    fun observeUser(userId: String): Flow<UserProfileEntity?> =
        repository.observeUser(userId)

    fun observeVisiblePostsByAuthor(authorId: String): Flow<List<PostEntity>> =
        repository.observeVisiblePostsByAuthor(authorId)

    fun observeOwnPosts(): Flow<List<PostEntity>> {
        val uid = currentUser.value?.userId ?: return MutableStateFlow(emptyList())
        return repository.observeOwnPosts(uid)
    }

    fun observeMessagesForPeer(peerUserId: String): Flow<List<DirectMessageEntity>> =
        repository.observeMessagesForPeer(peerUserId)

    fun sendDirectMessage(peerUserId: String, text: String) {
        val uid = currentUser.value?.userId ?: return
        viewModelScope.launch {
            repository.sendMessage(peerUserId, uid, text)
        }
    }

    fun observeBlockedStatusMap(): Flow<Map<String, Boolean>> =
        repository.otherCreators.map { list -> list.associate { it.userId to it.isBlocked } }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = VideoInstaDatabase.getInstance(context.applicationContext)
                    val dao = db.videoInstaDao()
                    val repo = VideoInstaRepository(dao)
                    val authRepo = LocalPlaceholderAuthenticityRepository(dao)
                    return VideoInstaViewModel(repo, authRepo) as T
                }
            }
        }
    }
}
