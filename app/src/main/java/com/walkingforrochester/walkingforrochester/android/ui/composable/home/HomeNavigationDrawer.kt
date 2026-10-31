package com.walkingforrochester.walkingforrochester.android.ui.composable.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.NO_URI_TARGET
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.RootRoute
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.drawerRoutes
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import kotlinx.coroutines.launch

@Composable
fun HomeNavigationDrawer(
    darkMode: Boolean,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    onNavigateScreen: (RootRoute) -> Unit = {},
    onToggleDarkMode: (Boolean) -> Unit = {},
    content: @Composable (() -> Unit)
) {
    val scope = rememberCoroutineScope()
    val closeDrawer: () -> Unit = {
        scope.launch {
            drawerState.close()
        }
    }

    BackHandler(drawerState.isOpen) {
        closeDrawer()
    }

    // TODO adjust which drawer based on screen size
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerContent(
                darkMode = darkMode,
                onNavigateScreen = { route ->
                    closeDrawer()
                    onNavigateScreen(route)
                },
                onToggleDarkMode = onToggleDarkMode,
                onCloseDrawer = closeDrawer
            )
        },
        modifier = modifier,
        drawerState = drawerState,
        gesturesEnabled = false,
        content = content
    )
}

@Composable
fun ModalDrawerContent(
    darkMode: Boolean,
    modifier: Modifier = Modifier,
    onNavigateScreen: (RootRoute) -> Unit = {},
    onToggleDarkMode: (Boolean) -> Unit = {},
    onCloseDrawer: () -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    val resources = LocalResources.current

    ModalDrawerSheet(modifier = modifier) {
        Column(
            Modifier
                .fillMaxHeight()
                .verticalScroll(state = rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 28.dp, top = 16.dp, end = 32.dp, bottom = 12.dp)

            )

            drawerRoutes.forEach {
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = stringResource(id = it.titleRes),
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    },
                    selected = false,
                    onClick = {
                        if (it.uriTarget != NO_URI_TARGET) {
                            val uri = resources.getString(it.uriTarget)
                            uriHandler.openUri(uri = uri)

                        } else {
                            onNavigateScreen(it)
                        }
                        onCloseDrawer()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    icon = {
                        Icon(
                            painter = painterResource(it.iconRes),
                            contentDescription = null
                        )
                    },
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            DarkModeSwitch(
                darkMode = darkMode,
                onToggleDarkMode = onToggleDarkMode
            )

            Spacer(modifier.height(16.dp))
        }
    }
}

@Composable
private fun DarkModeSwitch(
    darkMode: Boolean,
    modifier: Modifier = Modifier,
    onToggleDarkMode: (Boolean) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .fillMaxWidth()
            .padding(NavigationDrawerItemDefaults.ItemPadding)
            .clip(RoundedCornerShape(percent = 50))
            .toggleable(
                value = darkMode,
                role = Role.Switch,
                interactionSource = interactionSource,
                onValueChange = onToggleDarkMode
            )
            .padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.dark_mode))
        Switch(
            checked = darkMode,
            onCheckedChange = null
        )
    }
}

@Composable
@PreviewLightDark
private fun PreviewHomeNavigationDrawer() {
    WalkingForRochesterTheme {
        Surface {
            HomeNavigationDrawer(
                darkMode = true,
                drawerState = rememberDrawerState(DrawerValue.Open)
            ) {}
        }
    }
}