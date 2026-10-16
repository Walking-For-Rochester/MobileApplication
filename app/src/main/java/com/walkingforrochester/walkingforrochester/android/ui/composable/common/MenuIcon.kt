package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.walkingforrochester.walkingforrochester.android.R

@Composable
fun MenuIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TooltipButton(
        onClick = onClick,
        tooltipLabelResId = R.string.menu_tool_tip,
        iconResId = R.drawable.ic_menu_24dp,
        contentDescriptionResId = R.string.menu_content_description,
        modifier = modifier
    )
}