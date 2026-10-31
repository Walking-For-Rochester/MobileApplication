package com.walkingforrochester.walkingforrochester.android.ui.composable.home

data class HomeState(
    val isDarkMode: Boolean = true,
    val profileValid: Boolean = true,
    val event: HomeStateEvent = HomeStateEvent.None
)

enum class HomeStateEvent {
    None, UnexpectedError
}