package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.StoryEntity
import com.example.ui.screens.ChatThreadScreen
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.CreatorProfileScreen
import com.example.ui.screens.HomeFeedScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.screens.StoryViewerOverlay
import com.example.ui.screens.VerificationOnboardingScreen
import com.example.ui.theme.VideoInstaTheme
import com.example.ui.viewmodel.VideoInstaViewModel

object Routes {
    const val FEED = "feed"
    const val REELS = "reels"
    const val CREATE = "create"
    const val CREATE_WITH_MODE = "create?mode={mode}"
    const val MESSAGES = "messages"
    const val PROFILE = "profile"
    const val CHAT = "chat/{peerId}"
    const val CREATOR = "creator/{creatorId}"

    fun createRoute(mode: String = "PHOTO") = "create?mode=$mode"
    fun chatRoute(peerId: String) = "chat/$peerId"
    fun creatorRoute(creatorId: String) = "creator/$creatorId"
}

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val topDestinations = listOf(
    TopLevelDestination(
        route = Routes.FEED,
        labelRes = R.string.nav_feed,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_feed"
    ),
    TopLevelDestination(
        route = Routes.REELS,
        labelRes = R.string.nav_reels,
        selectedIcon = Icons.Filled.Movie,
        unselectedIcon = Icons.Outlined.Movie,
        testTag = "nav_reels"
    ),
    TopLevelDestination(
        route = Routes.CREATE,
        labelRes = R.string.nav_create,
        selectedIcon = Icons.Filled.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircleOutline,
        testTag = "nav_create"
    ),
    TopLevelDestination(
        route = Routes.MESSAGES,
        labelRes = R.string.nav_messages,
        selectedIcon = Icons.AutoMirrored.Filled.Chat,
        unselectedIcon = Icons.AutoMirrored.Outlined.Chat,
        testTag = "nav_messages"
    ),
    TopLevelDestination(
        route = Routes.PROFILE,
        labelRes = R.string.nav_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_profile"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VideoInstaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VideoInstaRoot()
                }
            }
        }
    }
}

@Composable
fun VideoInstaRoot() {
    val context = LocalContext.current
    val viewModel: VideoInstaViewModel = viewModel(
        factory = VideoInstaViewModel.provideFactory(context)
    )

    val hasLoadedSession by viewModel.hasLoadedSession.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val verificationState by viewModel.verificationState.collectAsStateWithLifecycle()

    if (!hasLoadedSession) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val user = currentUser
    if (user == null) {
        VerificationOnboardingScreen(
            verificationState = verificationState,
            onSubmitVerification = viewModel::submitIdentityVerification
        )
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val feedPosts by viewModel.feedPosts.collectAsStateWithLifecycle()
    val reels by viewModel.reels.collectAsStateWithLifecycle()
    val blockedUsers by viewModel.blockedUsers.collectAsStateWithLifecycle()
    val dmThreads by viewModel.dmThreads.collectAsStateWithLifecycle()
    val mediaScanState by viewModel.mediaScanState.collectAsStateWithLifecycle()
    val ownPosts by viewModel.observeOwnPosts().collectAsStateWithLifecycle(initialValue = emptyList())
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    var activeStory by remember { mutableStateOf<StoryEntity?>(null) }

    val showPrimaryChrome = currentRoute in setOf(
        Routes.FEED,
        Routes.REELS,
        Routes.CREATE_WITH_MODE,
        Routes.CREATE,
        Routes.MESSAGES,
        Routes.PROFILE
    )

    fun navigateTopLevel(route: String) {
        val targetRoute = if (route == Routes.CREATE) Routes.createRoute("PHOTO") else route
        navController.navigate(targetRoute) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (showPrimaryChrome && !useRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        topDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == dest.route ||
                                    (dest.route == Routes.CREATE && it.route == Routes.CREATE_WITH_MODE)
                            } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showPrimaryChrome && useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        topDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == dest.route ||
                                    (dest.route == Routes.CREATE && it.route == Routes.CREATE_WITH_MODE)
                            } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Routes.FEED,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(Routes.FEED) {
                        HomeFeedScreen(
                            currentUser = user,
                            stories = stories,
                            posts = feedPosts,
                            blockedUsersCount = blockedUsers.size,
                            onOpenStory = { story -> activeStory = story },
                            onAddStoryClick = {
                                navController.navigate(Routes.createRoute("STORY"))
                            },
                            onToggleLike = viewModel::toggleLike,
                            onToggleBookmark = viewModel::toggleBookmark,
                            onOpenCreatorProfile = { creatorId ->
                                if (creatorId == user.userId) {
                                    navigateTopLevel(Routes.PROFILE)
                                } else {
                                    navController.navigate(Routes.creatorRoute(creatorId))
                                }
                            },
                            onOpenMessages = { navigateTopLevel(Routes.MESSAGES) },
                            onOpenChatWithCreator = { creatorId ->
                                navController.navigate(Routes.chatRoute(creatorId))
                            },
                            onBlockCreator = { uid, handle ->
                                viewModel.toggleBlockByUserId(uid, currentlyBlocked = false, handle = handle)
                            },
                            observeComments = viewModel::observeComments,
                            onAddComment = viewModel::addComment
                        )
                    }

                    composable(Routes.REELS) {
                        ReelsScreen(
                            currentUser = user,
                            reels = reels,
                            onToggleLike = viewModel::toggleLike,
                            onToggleBookmark = viewModel::toggleBookmark,
                            onOpenCreatorProfile = { creatorId ->
                                if (creatorId == user.userId) {
                                    navigateTopLevel(Routes.PROFILE)
                                } else {
                                    navController.navigate(Routes.creatorRoute(creatorId))
                                }
                            },
                            onOpenChatWithCreator = { creatorId ->
                                navController.navigate(Routes.chatRoute(creatorId))
                            },
                            onBlockCreator = { uid, handle ->
                                viewModel.toggleBlockByUserId(uid, currentlyBlocked = false, handle = handle)
                            },
                            onOpenCreateReel = {
                                navController.navigate(Routes.createRoute("REEL_VIDEO"))
                            },
                            onOpenMessages = { navigateTopLevel(Routes.MESSAGES) },
                            observeComments = viewModel::observeComments,
                            onAddComment = viewModel::addComment
                        )
                    }

                    composable(
                        route = Routes.CREATE_WITH_MODE,
                        arguments = listOf(
                            navArgument("mode") {
                                type = NavType.StringType
                                defaultValue = "PHOTO"
                            }
                        )
                    ) { backStackEntry ->
                        val initialMode = backStackEntry.arguments?.getString("mode") ?: "PHOTO"
                        CreatePostScreen(
                            scanState = mediaScanState,
                            initialMode = initialMode,
                            onInspectMedia = { uri, isVideo, fromCam, simulateDemo ->
                                viewModel.inspectMedia(
                                    context = context,
                                    uri = uri,
                                    isVideo = isVideo,
                                    fromLiveCamera = fromCam,
                                    simulateAiEditedDemo = simulateDemo
                                )
                            },
                            onResetScan = viewModel::resetMediaScan,
                            onPublish = { uri, mode, caption, location, report ->
                                viewModel.publishVerifiedMedia(
                                    context = context,
                                    uri = uri,
                                    publishMode = mode,
                                    caption = caption,
                                    locationTag = location,
                                    report = report,
                                    onPublished = {
                                        if (mode == "REEL_VIDEO") {
                                            navigateTopLevel(Routes.REELS)
                                        } else {
                                            navigateTopLevel(Routes.FEED)
                                        }
                                    }
                                )
                            }
                        )
                    }

                    composable(Routes.MESSAGES) {
                        MessagesScreen(
                            threads = dmThreads,
                            onOpenThread = { peerId ->
                                navController.navigate(Routes.chatRoute(peerId))
                            }
                        )
                    }

                    composable(Routes.PROFILE) {
                        ProfileScreen(
                            currentUser = user,
                            ownPosts = ownPosts,
                            blockedUsers = blockedUsers,
                            onUnblockUser = viewModel::toggleBlockCreator,
                            onMessageBlockedUser = { peerId ->
                                navController.navigate(Routes.chatRoute(peerId))
                            }
                        )
                    }

                    composable(
                        route = Routes.CHAT,
                        arguments = listOf(navArgument("peerId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val peerId = backStackEntry.arguments?.getString("peerId").orEmpty()
                        val peer by viewModel.observeUser(peerId)
                            .collectAsStateWithLifecycle(initialValue = null)
                        val messages by viewModel.observeMessagesForPeer(peerId)
                            .collectAsStateWithLifecycle(initialValue = emptyList())

                        ChatThreadScreen(
                            currentUser = user,
                            peer = peer,
                            messages = messages,
                            onBack = { navController.popBackStack() },
                            onToggleBlock = viewModel::toggleBlockCreator,
                            onSendMessage = { text ->
                                viewModel.sendDirectMessage(peerId, text)
                            }
                        )
                    }

                    composable(
                        route = Routes.CREATOR,
                        arguments = listOf(navArgument("creatorId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val creatorId = backStackEntry.arguments?.getString("creatorId").orEmpty()
                        val creator by viewModel.observeUser(creatorId)
                            .collectAsStateWithLifecycle(initialValue = null)
                        val visiblePosts by viewModel.observeVisiblePostsByAuthor(creatorId)
                            .collectAsStateWithLifecycle(initialValue = emptyList())

                        CreatorProfileScreen(
                            creator = creator,
                            visiblePosts = visiblePosts,
                            onBack = { navController.popBackStack() },
                            onToggleBlock = viewModel::toggleBlockCreator,
                            onMessageCreator = { peerId ->
                                navController.navigate(Routes.chatRoute(peerId))
                            }
                        )
                    }
                }
            }
        }

        activeStory?.let { opened ->
            StoryViewerOverlay(
                stories = stories,
                initialStoryId = opened.id,
                onMarkViewed = viewModel::markStoryViewed,
                onSendReply = { peerUserId, text ->
                    viewModel.sendDirectMessage(peerUserId, text)
                    navController.navigate(Routes.chatRoute(peerUserId))
                },
                onClose = { activeStory = null }
            )
        }
    }
}
