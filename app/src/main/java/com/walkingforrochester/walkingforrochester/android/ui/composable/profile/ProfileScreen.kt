package com.walkingforrochester.walkingforrochester.android.ui.composable.profile

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ktx.isPhoneLandscape
import com.walkingforrochester.walkingforrochester.android.ktx.safeStartActivity
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.network.GoogleCredentialUtil
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.NavigationIcon
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.TooltipButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFROutlinedButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.ProfileDestination
import com.walkingforrochester.walkingforrochester.android.ui.state.ProfileScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.ProfileScreenState
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.ProfileViewModel
import timber.log.Timber

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onLogoutComplete: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleStartEffect(profileViewModel) {
        profileViewModel.loadProfile()
        onStopOrDispose {}
    }

    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val accountProfile by profileViewModel.accountProfile.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.event) {
        when (uiState.event) {
            ProfileScreenEvent.None -> {
                Timber.d("Viewing Profile")
            }

            ProfileScreenEvent.Logout,
            ProfileScreenEvent.AccountDeleted -> {
                GoogleCredentialUtil.performSignOut(context = context)
                onLogoutComplete()
            }

            ProfileScreenEvent.UnexpectedError -> {
                snackbarHostState.showSnackbar(resources.getString(R.string.unexpected_error))
                profileViewModel.clearEvent()
            }
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        accountProfile = accountProfile,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onEdit = { profileViewModel.onEdit() },
        onShare = { context.safeStartActivity(profileViewModel.onShare(context)) },
        onProfileChange = { profileViewModel.onProfileChange(it) },
        onChoosePhoto = { profileViewModel.onChoosePhoto(it) },
        onSaveProfile = { profileViewModel.onSave() },
        onCancelEdits = { profileViewModel.onCancel() },
        onDeleteAccount = { profileViewModel.onDeleteAccount() },
        onLogout = { profileViewModel.onLogout() }
    )
}

@Composable
fun ProfileScreenContent(
    uiState: ProfileScreenState,
    accountProfile: AccountProfile,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onShare: () -> Unit = {},
    onProfileChange: (AccountProfile) -> Unit = {},
    onChoosePhoto: (Uri?) -> Unit = {},
    onSaveProfile: () -> Unit = {},
    onCancelEdits: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            hasCommunityService = accountProfile.communityService,
            onDismissRequest = { showDeleteAccountDialog = false },
            onConfirmed = onDeleteAccount
        )
    }

    val isPhoneLandscape = currentWindowAdaptiveInfoV2().windowSizeClass.isPhoneLandscape()

    @OptIn(ExperimentalMaterial3Api::class)
    val scrollBehavior =
        if (isPhoneLandscape) TopAppBarDefaults.enterAlwaysScrollBehavior()
        else TopAppBarDefaults.pinnedScrollBehavior()

    BackHandler(enabled = uiState.editProfile) {
        onCancelEdits()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(ProfileDestination.title)) },
                navigationIcon = {
                    when (uiState.editProfile) {

                        true -> TooltipButton(
                            onClick = onCancelEdits,
                            tooltipLabelResId = R.string.cancel,
                            iconResId = R.drawable.ic_close_24dp,
                            contentDescriptionResId = R.string.cancel
                        )

                        false -> NavigationIcon(onClick = onNavigateBack)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        @OptIn(ExperimentalMaterial3Api::class)
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            ProfileCard(
                uiState = uiState,
                accountProfile = accountProfile,
                modifier = Modifier
                    .widthIn(max = dimensionResource(R.dimen.max_form_width))
                    .padding(horizontal = 8.dp),
                onEdit = onEdit,
                onShare = onShare,
                onProfileChange = onProfileChange,
                onChoosePhoto = onChoosePhoto,
                onSaveProfile = onSaveProfile,
                onCancelEdits = onCancelEdits
            )
            when {
                uiState.editProfile -> {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                uiState.profileDataLoading ||
                    uiState.profileDataSaving -> {
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )
                }

                else -> {
                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // If account failed, let user still logout
                    if (accountProfile.accountId != AccountProfile.NO_ACCOUNT) {
                        WFROutlinedButton(
                            onClick = { showDeleteAccountDialog = true },
                            label = R.string.delete_account,
                            modifier = Modifier.widthIn(min = 200.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    WFRButton(
                        onClick = onLogout,
                        label = R.string.logout,
                        modifier = Modifier.widthIn(min = 200.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewProfileScreen() {
    WalkingForRochesterTheme {
        ProfileScreenContent(
            uiState = ProfileScreenState(),
            accountProfile = AccountProfile.DEFAULT_PROFILE.copy(
                accountId = 1234L,
                email = "test@email.com",
                nickname = "Bob",
                communityService = false,
            )
        )
    }
}

@PreviewLightDark
@Composable
fun PreviewEditProfileScreen() {
    WalkingForRochesterTheme {
        ProfileScreenContent(
            uiState = ProfileScreenState(editProfile = true),
            accountProfile = AccountProfile.DEFAULT_PROFILE.copy(
                accountId = 1234L,
                email = "test@email.com",
                nickname = "Bob",
                communityService = false,
            )
        )
    }
}

@PreviewLightDark
@Composable
fun PreviewNoProfileScreen() {
    WalkingForRochesterTheme {
        ProfileScreenContent(
            uiState = ProfileScreenState(),
            accountProfile = AccountProfile.DEFAULT_PROFILE
        )
    }
}
