package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.ui.composable.contact.ContactUsScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.forgotpassword.ForgotPasswordScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.home.HomeScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.login.LoginScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.profile.ProfileScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.registration.RegistrationScreen
import timber.log.Timber

@Composable
fun RootNavDisplay(
    backStack: NavBackStack<RootRoute>,
    modifier: Modifier = Modifier
) {
    val navigateBack: () -> Unit = {

        Timber.d(
            "navigate back size: %d  last: %s",
            backStack.size,
            backStack.lastOrNull()
        )
        backStack.removeLastOrNull()
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { navigateBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<LoginRoute> {
                LoginScreen(
                    onForgotPassword = { backStack.add(ForgotPasswordRoute) },
                    onRegister = { email, firstName, lastName, facebookId ->
                        backStack.add(
                            RegistrationRoute(
                                email,
                                firstName,
                                lastName,
                                facebookId
                            )
                        )
                    },
                    onLoginComplete = {
                        backStack.clear()
                        backStack.add(HomeRoute)
                    }
                )
            }

            entry<ForgotPasswordRoute> {
                ForgotPasswordScreen(
                    onNavigateBack = navigateBack,
                    onPasswordResetComplete = {
                        backStack.clear()
                        backStack.add(LoginRoute)
                    },
                )
            }

            entry<RegistrationRoute> { registrationRoute ->
                RegistrationScreen(
                    profile = AccountProfile.DEFAULT_PROFILE.copy(
                        email = registrationRoute.email,
                        firstName = registrationRoute.firstName,
                        lastName = registrationRoute.lastName,
                        facebookId = registrationRoute.facebookId
                    ),
                    onNavigateBack = navigateBack,
                    onRegistrationComplete = {
                        backStack.clear()
                        backStack.add(HomeRoute)
                    }
                )
            }

            entry<HomeRoute> {
                HomeScreen(
                    onNavigateScreen = { route ->
                        backStack.add(route)
                    },
                    onLoginRequired = {
                        backStack.clear()
                        backStack.add(LoginRoute)
                    },
                )
            }

            entry<ProfileRoute> {
                ProfileScreen(
                    onNavigateBack = navigateBack,
                    onLogoutComplete = {
                        backStack.clear()
                        backStack.add(LoginRoute)
                    }
                )
            }

            entry<ContactUsRoute> {
                ContactUsScreen(onNavigateBack = navigateBack)
            }
        }
    )
}