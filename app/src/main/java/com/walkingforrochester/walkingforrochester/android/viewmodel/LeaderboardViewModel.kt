package com.walkingforrochester.walkingforrochester.android.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.di.DefaultDispatcher
import com.walkingforrochester.walkingforrochester.android.model.Leader
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardPeriod
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardType
import com.walkingforrochester.walkingforrochester.android.repository.NetworkRepository
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderboardFiltersState
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderboardScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderboardScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    savedStateHandle: SavedStateHandle,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {
    private var savedFilter: LeaderboardFiltersState by savedStateHandle.saved(FILTER_KEY) { LeaderboardFiltersState() }
    private val _leaderboardFilter = MutableStateFlow(savedFilter)

    private val _event = MutableStateFlow(LeaderboardScreenEvent.NoEvent)
    private val _loading = MutableStateFlow(true)

    private var lastLeaderList: List<Leader> = emptyList()
    private var lastFetchTime = 0L
    private var lastFetchPeriod = savedFilter.period

    private val leaders = _leaderboardFilter.transform { filter ->
        emit(determineLeaders(filter))
    }

    val leaderState = combine(
        leaders,
        _leaderboardFilter,
        _loading,
        _event,
    ) { leaders, filter, loading, event ->

        LeaderboardScreenState(
            leaders = leaders,
            leaderboardFiltersState = filter,
            loading = loading,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LeaderboardScreenState(
            leaderboardFiltersState = savedFilter,
            loading = true
        )
    )

    private suspend fun determineLeaders(
        filterState: LeaderboardFiltersState
    ): List<Leader> = withContext(defaultDispatcher) {

        if (lastFetchPeriod != filterState.period ||
            lastFetchTime + REFRESH_INTERVAL < System.currentTimeMillis()
        ) {
            val time = System.currentTimeMillis()
            _loading.update { true }
            try {
                lastLeaderList = networkRepository.fetchLeaderboard(filterState.period)
                lastFetchPeriod = filterState.period
                // Only set if load successful. Forces a retry on next user action on failure
                lastFetchTime = System.currentTimeMillis()

            } catch (e: CancellationException) {
                // Allow cancellation exception to cancel flow
                throw e
            } catch (t: Throwable) {
                Timber.e(t, "Unexpected error fetching leaders")
                _event.update { LeaderboardScreenEvent.UnexpectedError }
            }

            // Ensure loading state shown a minimum of 300ms just to not cause
            // UI to jump between loading complete state so fast it looks like a flicker, only
            // needed for network calls.
            // Do not use last fetch as may not be updated if load failed
            delay(timeMillis = max(0L, 300L - (System.currentTimeMillis() - time)))
        }

        val comparator = when (filterState.type) {
            LeaderboardType.Collection -> Leader.collectionComparator
            LeaderboardType.Distance -> Leader.distanceComparator
            LeaderboardType.Duration -> Leader.durationComparator
        }

        val leaders = lastLeaderList.asSequence()
            .filter {
                when (filterState.type) {
                    LeaderboardType.Collection -> it.collection > 0
                    LeaderboardType.Distance -> it.distance > 0.0
                    LeaderboardType.Duration -> it.duration > 0
                }
            }
            .sortedWith(comparator)
            .toList()

        _loading.update { false }
        leaders
    }

    fun clearEvent() {
        _event.update { LeaderboardScreenEvent.NoEvent }
    }

    fun onTypeFilterChange(type: LeaderboardType) {
        savedFilter = savedFilter.copy(type = type)
        _leaderboardFilter.update { savedFilter }
    }

    fun onPeriodFilterChange(period: LeaderboardPeriod) {
        savedFilter = savedFilter.copy(period = period)
        _leaderboardFilter.update { savedFilter }
    }

    companion object {
        const val REFRESH_INTERVAL = 10 * 60L * 1000L // ten minutes
        const val FILTER_KEY = "filter"
    }
}