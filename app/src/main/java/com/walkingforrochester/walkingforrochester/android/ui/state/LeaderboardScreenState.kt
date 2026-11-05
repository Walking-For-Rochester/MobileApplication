package com.walkingforrochester.walkingforrochester.android.ui.state

import com.walkingforrochester.walkingforrochester.android.model.Leader
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardPeriod
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardType
import kotlinx.serialization.Serializable

data class LeaderboardScreenState(
    val leaders: List<Leader> = emptyList(),
    val leaderboardFiltersState: LeaderboardFiltersState = LeaderboardFiltersState(),
    val loading: Boolean = false,
    val event: LeaderboardScreenEvent = LeaderboardScreenEvent.NoEvent
)

@Serializable
data class LeaderboardFiltersState(
    val type: LeaderboardType = LeaderboardType.Collection,
    val period: LeaderboardPeriod = LeaderboardPeriod.Week
)

enum class LeaderboardScreenEvent {
    NoEvent,
    UnexpectedError
}