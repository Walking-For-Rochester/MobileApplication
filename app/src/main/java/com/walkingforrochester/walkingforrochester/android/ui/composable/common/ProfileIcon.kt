package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.walkingforrochester.walkingforrochester.android.R

@Composable
fun ProfileIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TooltipButton(
        onClick = onClick,
        tooltipLabelResId = R.string.profile,
        iconResId = R.drawable.ic_account_circle_24dp,
        contentDescriptionResId = R.string.open_profile_screen,
        modifier = modifier
    )
}