package com.walkingforrochester.walkingforrochester.android.ui.composable.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.repository.NetworkRepository
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val preferenceRepository: PreferenceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var checkProfileTime: Long by savedStateHandle.saved(DELAY_KEY) { 0 }
    private var _homeState = MutableStateFlow(HomeState())

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->

        Timber.e(t = throwable, "Unexpected error fetching profile")

        _homeState.update { it.copy(event = HomeStateEvent.UnexpectedError) }
    }

    val homeState: StateFlow<HomeState> =
        _homeState.combine(preferenceRepository.userPreferences) { homeState, userPreferences ->
            homeState.copy(isDarkMode = userPreferences.isDarkMode)
        }.onStart {
            validateProfile()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeState()
        )

    fun onToggleDarkMode(darkMode: Boolean) = viewModelScope.launch {
        preferenceRepository.updateDarkMode(darkMode)
    }

    fun clearEvent() {
        _homeState.update { it.copy(event = HomeStateEvent.None) }
    }

    private fun validateProfile() = viewModelScope.launch(exceptionHandler) {
        // Validate the profile still exists in rare situation the user deleted the
        // account on another device
        if (checkProfileTime > System.currentTimeMillis()) {
            Timber.d("Profile validation cooldown in effect")
            return@launch
        }

        checkProfileTime = System.currentTimeMillis() + PROFILE_CHECK_DELAY

        val accountId = preferenceRepository.fetchAccountId()
        val profile = if (accountId != AccountProfile.NO_ACCOUNT) {
            networkRepository.fetchProfile(accountId)
        } else {
            AccountProfile.DEFAULT_PROFILE
        }

        when {
            profile.accountId == AccountProfile.NO_ACCOUNT -> {
                Timber.d("Account does not exist, triggering logout")
                preferenceRepository.removeAccountInfo()
                checkProfileTime = 0L
                _homeState.update { it.copy(profileValid = false) }
            }

            profile.phoneNumber.isNotBlank() -> {
                // Phone number is no longer collected. Fix old accounts by
                // removing the phone number via update
                Timber.d("Removing legacy phone number from profile")
                networkRepository.updateProfile(profile)
            }

            else -> {
                Timber.d("Profile is valid")
            }
        }
    }

    companion object {
        private var DELAY_KEY = "delay"
        private val PROFILE_CHECK_DELAY = 8.hours.inWholeMilliseconds
    }
}