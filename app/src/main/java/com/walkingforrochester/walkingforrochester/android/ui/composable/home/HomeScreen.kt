package com.walkingforrochester.walkingforrochester.android.ui.composable.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LocalSnackbarHostState
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.MenuIcon
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.ProfileIcon
import com.walkingforrochester.walkingforrochester.android.ui.composable.leaderboard.LeaderboardScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk.LogAWalkScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.BottomBarRoute
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.LeaderboardRoute
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.LogAWalkRoute
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.NewsFeedRoute
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.ProfileRoute
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.RootRoute
import com.walkingforrochester.walkingforrochester.android.ui.composable.newsfeed.NewsFeedScreen
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateScreen: (RootRoute) -> Unit = {},
    onLoginRequired: () -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val resources = LocalResources.current
    val homeState by homeViewModel.homeState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(homeState.event) {
        if (homeState.event == HomeStateEvent.UnexpectedError) {
            snackbarHostState.showSnackbar(resources.getString(R.string.unexpected_error))
            homeViewModel.clearEvent()
        }
    }

    if (!homeState.profileValid) {
        // User profile is not found, force user to login again
        onLoginRequired()
    }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        HomeScreenContent(
            darkMode = homeState.isDarkMode,
            modifier = modifier,
            onNavigateScreen = onNavigateScreen,
            onToggleDarkMode = { homeViewModel.onToggleDarkMode(it) }
        )
    }
}

@Composable
fun HomeScreenContent(
    darkMode: Boolean,
    modifier: Modifier = Modifier,
    onNavigateScreen: (RootRoute) -> Unit = {},
    onToggleDarkMode: (Boolean) -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    HomeNavigationDrawer(
        darkMode = darkMode,
        modifier = modifier,
        drawerState = drawerState,
        onNavigateScreen = onNavigateScreen,
        onToggleDarkMode = onToggleDarkMode,
    ) {
        DrawerContent(
            onNavigateScreen = onNavigateScreen,
            onToggleDrawer = {
                coroutineScope.launch {
                    when (drawerState.isOpen) {
                        true -> drawerState.close()
                        false -> drawerState.open()
                    }
                }
            }
        )
    }
}

@Composable
fun DrawerContent(
    modifier: Modifier = Modifier,
    onNavigateScreen: (RootRoute) -> Unit = {},
    onToggleDrawer: () -> Unit = {},
) {

    val snackbarHostState = LocalSnackbarHostState.current

    var selectedRoute by rememberSerializable { mutableStateOf<BottomBarRoute>(LogAWalkRoute) }

    BackHandler(enabled = selectedRoute != LogAWalkRoute) {
        selectedRoute = LogAWalkRoute
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            CenterAlignedTopAppBar(
                title = { Text(stringResource(selectedRoute.titleRes)) },
                navigationIcon = { MenuIcon(onClick = onToggleDrawer) },
                actions = { ProfileIcon(onClick = { onNavigateScreen(ProfileRoute) }) }
            )
        },
        bottomBar = {
            HomeBottomBar(
                selectedRoute = selectedRoute,
                onScreenSelected = { selectedRoute = it }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        // Do not include content in compose preview due to view models
        if (!LocalInspectionMode.current) {
            DisplayRoute(
                selectedRoute = selectedRoute,
                contentPadding = contentPadding
            )
        }
    }
}

@Composable
private fun DisplayRoute(
    selectedRoute: BottomBarRoute,
    onNavigateToSubmitWalk: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues()
) {
    when (selectedRoute) {
        LogAWalkRoute -> LogAWalkScreen(
            onNavigateToSubmitWalk = onNavigateToSubmitWalk,
            contentPadding = contentPadding
        )

        LeaderboardRoute -> LeaderboardScreen(
            contentPadding = contentPadding
        )

        NewsFeedRoute -> NewsFeedScreen(
            contentPadding = contentPadding
        )
    }
}

@Composable
@PreviewLightDark
private fun HomeScreenContentPreview() {
    WalkingForRochesterTheme {
        Surface {
            val snackbarHostState = remember { SnackbarHostState() }
            CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                HomeScreenContent(darkMode = false)
            }
        }
    }
}