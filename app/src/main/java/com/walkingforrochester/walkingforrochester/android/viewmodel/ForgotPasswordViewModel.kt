package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.repository.NetworkRepository
import com.walkingforrochester.walkingforrochester.android.ui.state.ForgotPasswordScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.ForgotPasswordScreenMode
import com.walkingforrochester.walkingforrochester.android.ui.state.ForgotPasswordScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ForgotPasswordScreenState(
            email = savedState[EMAIL_KEY] ?: "",
            mode = savedState[MODE_KEY] ?: ForgotPasswordScreenMode.RequestEmail
        )
    )
    val uiState = _uiState.asStateFlow()

    fun resetEvent() {
        _uiState.update { it.copy(event = ForgotPasswordScreenEvent.None) }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Unexpected error processing profile")

        _uiState.update {
            it.copy(
                loading = false,
                event = ForgotPasswordScreenEvent.UnexpectedError
            )
        }
    }


    fun navigateBackInternally(): Boolean {
        return when (_uiState.value.mode) {
            ForgotPasswordScreenMode.RequestEmail -> false
            else -> {
                savedState[MODE_KEY] = ForgotPasswordScreenMode.RequestEmail
                savedState[CODE_KEY] = ""
                _uiState.update {
                    it.copy(
                        mode = ForgotPasswordScreenMode.RequestEmail
                    )
                }
                true
            }
        }
    }

    fun requestCode(email: String) = viewModelScope.launch(context = exceptionHandler) {
        if (validateEmail(email)) {
            _uiState.update {
                it.copy(
                    loading = true,
                    email = email,
                    emailValidationMessageId = 0
                )
            }

            savedState[EMAIL_KEY] = email

            savedState[CODE_KEY] = networkRepository.forgotPassword(email = email)

            // Update mode after network call as exception will prevent screen switch
            savedState[MODE_KEY] = ForgotPasswordScreenMode.VerifyCode

            _uiState.update { state ->
                state.copy(
                    mode = ForgotPasswordScreenMode.VerifyCode,
                    loading = false
                )
            }
        }
    }

    fun verifyCode(code: String) = viewModelScope.launch(context = exceptionHandler) {

        _uiState.update { it.copy(loading = true) }
        delay(timeMillis = 1000)

        val internalCode = savedState[CODE_KEY] ?: ""

        when {
            internalCode.isBlank() -> {
                savedState[MODE_KEY] = ForgotPasswordScreenMode.RequestEmail
                _uiState.update {
                    it.copy(
                        mode = ForgotPasswordScreenMode.RequestEmail,
                        loading = false,
                        event = ForgotPasswordScreenEvent.CodeTimeout
                    )
                }
            }

            code.isNotEmpty() && code == internalCode -> {
                savedState[MODE_KEY] = ForgotPasswordScreenMode.UpdatePassword
                _uiState.update {
                    it.copy(
                        mode = ForgotPasswordScreenMode.UpdatePassword,
                        codeValidationMessageId = 0,
                        loading = false
                    )
                }
            }

            else -> {
                _uiState.update {
                    it.copy(
                        codeValidationMessageId = R.string.invalid_code,
                        loading = false
                    )
                }
            }
        }
    }

    fun resetPassword(
        password: String
    ) = viewModelScope.launch(context = exceptionHandler) {

        if (validatePassword(password)) {
            _uiState.update { it.copy(loading = true) }

            networkRepository.resetPassword(email = _uiState.value.email, password = password)

            _uiState.update {
                it.copy(
                    loading = false,
                    password = password,
                    event = ForgotPasswordScreenEvent.PasswordReset
                )
            }
        }
    }

    fun validateEmail(email: String): Boolean {
        var isValid = true
        var emailValidationMessageId = 0

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailValidationMessageId = R.string.invalid_email
            isValid = false
        }

        _uiState.update {
            it.copy(
                emailValidationMessageId = emailValidationMessageId
            )
        }

        return isValid
    }

    fun resetError() {
        _uiState.update {
            it.copy(
                emailValidationMessageId = 0,
                codeValidationMessageId = 0,
                passwordValidationMessageId = 0
            )
        }
    }

    private fun validatePassword(
        password: String
    ): Boolean {
        var passwordValidationMessageId = 0
        var isValid = true

        val adjustedPassword = password.filterNot { it.isWhitespace() }
        if (password != adjustedPassword || password.length < 8) {
            passwordValidationMessageId = R.string.invalid_password
            isValid = false
        }

        _uiState.update {
            it.copy(
                passwordValidationMessageId = passwordValidationMessageId
            )
        }
        return isValid
    }

    companion object {
        const val EMAIL_KEY = "email"
        const val MODE_KEY = "mode"
        const val CODE_KEY = "code"
    }
}