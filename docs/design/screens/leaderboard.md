# Leaderboard

[← Design index](../README.md) · Route `leaderboard` · `ui/composable/leaderboard/` · `LeaderboardViewModel`

A bottom-bar tab ranking volunteers. Two segmented button rows at the top filter the list.

<img src="../images/leaderboard/PreviewLeaderboardScreen_dark.png" width="260" alt="Leaderboard">

## Filters — `LeaderFilters.kt`

- **Type:** Collection (bags) · Distance (mi) · Duration (hh:mm). Default: Collection.
- **Period:** Day · Week · Month · Year. Default: Week.

<img src="../images/leaderboard/PreviewLeaderFilters_light.png" width="360" alt="Filters light"><br>
<img src="../images/leaderboard/PreviewLeaderFilters_dark.png" width="360" alt="Filters dark">

## Leader cards — `LeaderCard.kt`

Each card shows the rank, profile picture, name and the value for the selected type. The top three cards are colored **gold** `#FFD700`, **silver** `#C0C0C0` and **bronze** `#CD7F32` with black text. The rest use `surfaceContainerHighest`.

<img src="../images/leaderboard/PreviewLeaderCard_light.png" width="360" alt="Leader card (3rd place, bronze)">

## States

- Loading: a centered `CircularProgressIndicator`
- Empty: "No activity found for the specified filters"
- Error: "Unexpected error…" snackbar
