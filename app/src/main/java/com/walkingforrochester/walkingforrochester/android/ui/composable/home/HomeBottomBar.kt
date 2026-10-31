package com.walkingforrochester.walkingforrochester.android.ui.composable.home

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.BottomBarRoute
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.bottomBarRoutes
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun HomeBottomBar(
    selectedRoute: BottomBarRoute,
    modifier: Modifier = Modifier,
    onScreenSelected: (BottomBarRoute) -> Unit = {},
) {
    NavigationBar(modifier = modifier) {
        bottomBarRoutes.forEach { route ->
            val selected = selectedRoute == route
            NavigationBarItem(
                selected = selected,
                onClick = { onScreenSelected(route) },
                icon = {
                    val iconRes = if (selected) route.selectedIconRes else route.iconRes
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null
                    )
                },
                label = { Text(text = stringResource(id = route.titleRes)) },
            )
        }
    }
}

@Composable
@PreviewLightDark
fun PreviewBottomBar() {
    WalkingForRochesterTheme {
        HomeBottomBar(
            selectedRoute = bottomBarRoutes.first(),
        )
    }
}