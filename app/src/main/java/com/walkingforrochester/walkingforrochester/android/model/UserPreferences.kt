package com.walkingforrochester.walkingforrochester.android.model

data class UserPreferences(
    val isDarkMode: Boolean = false,
    val accountId: Long = AccountProfile.NO_ACCOUNT
)
