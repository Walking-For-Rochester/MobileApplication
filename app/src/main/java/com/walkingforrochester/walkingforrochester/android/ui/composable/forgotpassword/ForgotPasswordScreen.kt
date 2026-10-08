package com.walkingforrochester.walkingforrochester.android.ui.composable.forgotpassword

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ktx.isPhoneLandscape
import com.walkingforrochester.walkingforrochester.android.network.PasswordCredentialUtil
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.NavigationIcon
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRPasswordField
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRTextField
import com.walkingforrochester.walkingforrochester.android.ui.state.ForgotPasswordScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.ForgotPasswordScreenMode
import com.walkingforrochester.walkingforrochester.android.ui.state.ForgotPasswordScreenState
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.ForgotPasswordViewModel
import timber.log.Timber

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onPasswordResetComplete: () -> Unit = {},
    forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by forgotPasswordViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val autofillManager = LocalAutofillManager.current
    val activityContext = LocalActivity.current ?: context
    val resources = LocalResources.current

    LaunchedEffect(
        uiState.event
    ) {
        when (uiState.event) {
            ForgotPasswordScreenEvent.None -> {
                Timber.d("Showing forgot password screen")
            }

            ForgotPasswordScreenEvent.CodeTimeout -> {
                snackbarHostState.showSnackbar(
                    message = resources.getString(R.string.code_timeout_error),
                )
                forgotPasswordViewModel.resetError()
            }

            ForgotPasswordScreenEvent.PasswordReset -> {

                snackbarHostState.showSnackbar(
                    message = resources.getString(R.string.password_reset_done),
                )

                PasswordCredentialUtil.savePasswordCredential(
                    activityContext = activityContext,
                    email = uiState.email,
                    password = uiState.password
                )

                // Cancel autofill as credential manager used to save passwords
                // and don't want user to be prompted twice
                autofillManager?.cancel()
                onPasswordResetComplete()
            }

            ForgotPasswordScreenEvent.UnexpectedError -> {
                snackbarHostState.showSnackbar(
                    message = resources.getString(R.string.unexpected_error),
                )
                forgotPasswordViewModel.resetEvent()
            }
        }
    }

    BackHandler(enabled = uiState.mode != ForgotPasswordScreenMode.RequestEmail) {
        forgotPasswordViewModel.navigateBackInternally()
    }

    ForgotPasswordContent(
        uiState = uiState,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onNavigateBack = {
            if (!forgotPasswordViewModel.navigateBackInternally()) {
                onNavigateBack()
            }
        },
        onRequestCode = { email -> forgotPasswordViewModel.requestCode(email) },
        onValidateEmail = { email -> forgotPasswordViewModel.validateEmail(email) },
        onVerifyCode = { code -> forgotPasswordViewModel.verifyCode(code) },
        onResetPassword = { password -> forgotPasswordViewModel.resetPassword(password) },
        onResetError = { forgotPasswordViewModel.resetError() }
    )

}

@Composable
private fun ForgotPasswordContent(
    uiState: ForgotPasswordScreenState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateBack: () -> Unit = {},
    onRequestCode: (String) -> Unit = {},
    onValidateEmail: (String) -> Unit = {},
    onVerifyCode: (String) -> Unit = {},
    onResetPassword: (String) -> Unit = {},
    onResetError: () -> Unit = {}
) {
    val isPhoneLandscape = currentWindowAdaptiveInfoV2().windowSizeClass.isPhoneLandscape()

    @OptIn(ExperimentalMaterial3Api::class)
    val scrollBehavior =
        if (isPhoneLandscape) TopAppBarDefaults.enterAlwaysScrollBehavior()
        else TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.forgot_password)) },
                navigationIcon = { NavigationIcon(onClick = onNavigateBack) },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->

        @OptIn(ExperimentalMaterial3Api::class)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState.mode) {
                ForgotPasswordScreenMode.RequestEmail -> {
                    RequestCode(
                        email = uiState.email,
                        emailValidationMessageId = uiState.emailValidationMessageId,
                        loading = uiState.loading,
                        onRequestCode = onRequestCode,
                        onValidateEmail = onValidateEmail
                    )
                }

                ForgotPasswordScreenMode.VerifyCode -> {
                    VerifyCode(
                        email = uiState.email,
                        codeValidationMessageId = uiState.codeValidationMessageId,
                        loading = uiState.loading,
                        onVerifyCode = onVerifyCode,
                        onResetError = onResetError
                    )
                }

                ForgotPasswordScreenMode.UpdatePassword -> {
                    ChangePassword(
                        email = uiState.email,
                        passwordValidationMessageId = uiState.passwordValidationMessageId,
                        loading = uiState.loading,
                        onResetPassword = onResetPassword,
                        onResetError = onResetError
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.RequestCode(
    email: String,
    emailValidationMessageId: Int,
    loading: Boolean,
    onRequestCode: (String) -> Unit = {},
    onValidateEmail: (String) -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf(email) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val requestCode: (String) -> Unit = { email ->
        keyboardController?.hide()
        focusManager.clearFocus()
        onRequestCode(email)
    }

    val maxWidthDp = dimensionResource(R.dimen.max_form_width)
    WFRTextField(
        value = email,
        onValueChange = {
            if (email != it) {
                email = it.trim()
                if (emailValidationMessageId != 0) {
                    onValidateEmail(email)
                }
            }
        },
        labelRes = R.string.email_address,
        modifier = Modifier
            .widthIn(max = maxWidthDp)
            .semantics { contentType = ContentType.EmailAddress },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email, imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { requestCode(email) }
        ),
        validationError = getErrorMessage(msgId = emailValidationMessageId),
        clearFieldIconEnabled = true
    )

    Text(
        text = stringResource(id = R.string.forgot_password_info),
        modifier = Modifier
            .widthIn(max = maxWidthDp)
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.weight(1f))

    WFRButton(
        onClick = { requestCode(email) },
        label = R.string.request_code,
        loading = loading
    )
}

@Composable
@Preview
private fun RequestCodePreview() {
    WalkingForRochesterTheme {
        Surface {
            ForgotPasswordContent(
                uiState = ForgotPasswordScreenState()
            )
        }
    }
}

@Composable
private fun getErrorMessage(@StringRes msgId: Int): String {
    return when (msgId) {
        0 -> ""
        else -> stringResource(id = msgId)
    }
}

@Composable
private fun EmailMessage(
    email: String,
    @StringRes msgResId: Int
) {
    val modifier = Modifier
        .widthIn(max = dimensionResource(R.dimen.max_form_width))
        .fillMaxWidth()

    Text(
        text = email,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        text = stringResource(id = msgResId),
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun ColumnScope.VerifyCode(
    email: String,
    codeValidationMessageId: Int,
    loading: Boolean,
    onVerifyCode: (String) -> Unit = {},
    onResetError: () -> Unit = {}
) {

    var code by rememberSaveable { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val verifyCode: () -> Unit = {
        keyboardController?.hide()
        focusManager.clearFocus()
        onVerifyCode(code)
    }

    EmailMessage(
        email = email,
        msgResId = R.string.forgot_password_check_email
    )

    Spacer(modifier = Modifier.height(8.dp))

    WFRTextField(
        value = code,
        onValueChange = {
            code = it
            onResetError()
        },
        labelRes = R.string.enter_code_desc,
        modifier = Modifier.widthIn(max = dimensionResource(R.dimen.max_form_width)),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { verifyCode() }
        ),
        validationError = getErrorMessage(msgId = codeValidationMessageId),
        clearFieldIconEnabled = true
    )

    Spacer(modifier = Modifier.weight(1f))

    WFRButton(
        onClick = { verifyCode() },
        label = R.string.verify_code,
        modifier = Modifier.padding(top = 8.dp),
        loading = loading
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun VerifyCodePreview() {
    WalkingForRochesterTheme {
        Surface {
            ForgotPasswordContent(
                uiState = ForgotPasswordScreenState(
                    email = "test@email.com",
                    mode = ForgotPasswordScreenMode.VerifyCode
                )
            )
        }
    }
}

@Composable
private fun ColumnScope.ChangePassword(
    email: String,
    passwordValidationMessageId: Int,
    loading: Boolean,
    onResetPassword: (String) -> Unit = {},
    onResetError: () -> Unit = {}
) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var confirmErrorMessageId by rememberSaveable { mutableIntStateOf(0) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val resetPassword: () -> Unit = {
        keyboardController?.hide()
        focusManager.clearFocus()

        if (password == confirmPassword) {
            onResetPassword(password)
        } else {
            confirmErrorMessageId = R.string.invalid_password_match
        }
    }

    EmailMessage(
        email = email,
        msgResId = R.string.forgot_password_change
    )
    Spacer(modifier = Modifier.height(8.dp))

    val maxWidthDp = dimensionResource(R.dimen.max_form_width)

    WFRPasswordField(
        value = password,
        onValueChange = { newPassword ->
            password = newPassword.filterNot { it.isWhitespace() }
            onResetError()
        },
        labelRes = R.string.password,
        modifier = Modifier
            .widthIn(max = maxWidthDp)
            .semantics { contentType = ContentType.NewPassword },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password, imeAction = ImeAction.Next
        ),
        validationError = getErrorMessage(passwordValidationMessageId)
    )

    WFRPasswordField(
        value = confirmPassword,
        onValueChange = { newPassword ->
            confirmPassword = newPassword.filterNot { it.isWhitespace() }
            confirmErrorMessageId = 0
        },
        labelRes = R.string.confirm_password,
        modifier = Modifier
            .widthIn(max = maxWidthDp)
            .semantics { contentType = ContentType.NewPassword },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { resetPassword() }
        ),
        validationError = getErrorMessage(confirmErrorMessageId),
    )

    Spacer(modifier = Modifier.weight(1f))

    WFRButton(
        label = R.string.change_password,
        onClick = resetPassword,
        loading = loading
    )
}

@Composable
@Preview
private fun ChangePasswordPreview() {
    WalkingForRochesterTheme {
        Surface {
            ForgotPasswordContent(
                uiState = ForgotPasswordScreenState(
                    email = "test@email.com",
                    mode = ForgotPasswordScreenMode.UpdatePassword
                )
            )
        }
    }
}
