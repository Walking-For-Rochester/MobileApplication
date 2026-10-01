package com.walkingforrochester.walkingforrochester.android.ui.state

import androidx.annotation.StringRes

data class ForgotPasswordScreenState(
    val email: String = "",
    val password: String = "",
    @param:StringRes val emailValidationMessageId: Int = 0,
    @param:StringRes val codeValidationMessageId: Int = 0,
    @param:StringRes val passwordValidationMessageId: Int = 0,
    val loading: Boolean = false,
    val mode: ForgotPasswordScreenMode = ForgotPasswordScreenMode.RequestEmail,
    val event: ForgotPasswordScreenEvent = ForgotPasswordScreenEvent.None
)

enum class ForgotPasswordScreenMode {
    RequestEmail, VerifyCode, UpdatePassword
}

enum class ForgotPasswordScreenEvent {
    None, CodeTimeout, PasswordReset, UnexpectedError
}