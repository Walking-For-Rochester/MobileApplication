package com.walkingforrochester.walkingforrochester.android.ui.composable.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.model.Leader
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardPeriod
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardType
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LocalSnackbarHostState
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderboardScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderboardScreenState
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.LeaderboardViewModel
import timber.log.Timber

@Composable
fun LeaderboardScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    leaderboardViewModel: LeaderboardViewModel = hiltViewModel()
) {
    val state by leaderboardViewModel.leaderState.collectAsStateWithLifecycle()

    val snackbarHostState = LocalSnackbarHostState.current
    val resources = LocalResources.current

    LaunchedEffect(state.event) {

        when (state.event) {
            LeaderboardScreenEvent.NoEvent -> {
                Timber.d("Show leaderboard")
            }

            LeaderboardScreenEvent.UnexpectedError -> {
                snackbarHostState.showSnackbar(resources.getString(R.string.unexpected_error))
                leaderboardViewModel.clearEvent()
            }
        }
    }

    LeaderboardContent(
        state = state,
        modifier = modifier,
        contentPadding = contentPadding,
        onTypeFilterChange = { type -> leaderboardViewModel.onTypeFilterChange(type) },
        onPeriodFilterChange = { period -> leaderboardViewModel.onPeriodFilterChange(period) }
    )
}

@Composable
private fun LeaderboardContent(
    state: LeaderboardScreenState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onTypeFilterChange: (LeaderboardType) -> Unit = {},
    onPeriodFilterChange: (LeaderboardPeriod) -> Unit = {}
) {
    val filtersState = state.leaderboardFiltersState
    val maxWidth = dimensionResource(R.dimen.max_form_width)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            LeaderFilters(
                modifier = Modifier
                    .padding(bottom = 8.dp),
                currentType = filtersState.type,
                currentPeriod = filtersState.period,
                onTypeFilterChange = onTypeFilterChange,
                onPeriodFilterChange = onPeriodFilterChange
            )
        }
        when {
            state.loading -> {
                item {
                    Box(modifier = Modifier.fillParentMaxHeight(.5f)) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            state.leaders.isNotEmpty() -> {
                itemsIndexed(
                    items = state.leaders,
                    key = { _, item -> item.accountId }
                ) { index, leader ->
                    LeaderCard(
                        leader = leader,
                        index = index,
                        type = filtersState.type,
                        modifier.widthIn(max = maxWidth)
                    )
                }
            }

            else -> {
                item {
                    Box {
                        Text(
                            text = stringResource(R.string.empty_leaderboard),
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun PreviewLeaderboardScreen() {
    WalkingForRochesterTheme {
        Surface {
            LeaderboardContent(
                state = LeaderboardScreenState(
                    leaders = listOf(
                        Leader(
                            collectionPosition = 3,
                            accountId = 2,
                            firstName = "John",
                            nickname = "",
                            imgUrl = "",
                            collection = 13L,
                            distance = 3.3,
                            duration = 7200L
                        ),
                        Leader(
                            collectionPosition = 4,
                            accountId = 3,
                            firstName = "Bob",
                            nickname = "",
                            imgUrl = "",
                            collection = 11L,
                            distance = 3.2,
                            duration = 7100L
                        ),
                        Leader(
                            collectionPosition = 5,
                            accountId = 4,
                            firstName = "Amy",
                            nickname = "",
                            imgUrl = "",
                            collection = 10L,
                            distance = 3.4,
                            duration = 7300L
                        ),
                        Leader(
                            collectionPosition = 6,
                            accountId = 5,
                            firstName = "Julie",
                            nickname = "",
                            imgUrl = "",
                            collection = 10L,
                            distance = 3.41,
                            duration = 7100L
                        ),
                    ),
                    loading = false
                )
            )
        }
    }
}
