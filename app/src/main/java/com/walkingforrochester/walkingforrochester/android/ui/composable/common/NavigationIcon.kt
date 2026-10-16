package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.walkingforrochester.walkingforrochester.android.R

@Composable
fun NavigationIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TooltipButton(
        onClick = onClick,
        tooltipLabelResId = R.string.back_tooltip,
        iconResId = R.drawable.ic_arrow_back_24dp,
        contentDescriptionResId = R.string.back_button,
        modifier = modifier
    )
}