# Log a Walk (home)

[← Design index](../README.md) · Route `logAWalk` · `ui/composable/logawalk/` · `LogAWalkViewModel`, `ForegroundLocationService`

The signed-in home screen. A full-screen Google Map follows the user's location and draws the current walk route in `MapPathBlue` (`#0E54FF`), with start and end markers (`trip_origin`, `trip_destination_cap`). A large **Start / Stop** button floats at the bottom center. The map area is a grey placeholder in these screenshots.

| Idle | Walking | Dark |
|---|---|---|
| <img src="../images/shell/LogAWalkShell_idle_light.png" width="220"> | <img src="../images/shell/LogAWalkShell_walking_light.png" width="220"> | <img src="../images/shell/LogAWalkShell_idle_dark.png" width="220"> |

The button (`StartStopWalk.kt`) shows a walking icon and **Start** when idle, and a finish-flag icon and **Stop** while walking. It only appears once location permission is granted.

## Starting a walk

```mermaid
flowchart TD
    Open[Screen opens] --> Perm{Location permission?}
    Perm -->|denied| LocR[Location rationale dialog<br/>Close app / Continue or Settings]
    Perm -->|granted| Idle[Map + Start]
    Idle -->|Start| N{Notification permission?<br/>Android 13+}
    N -->|not granted, not yet asked this session| NotR[Notifications rationale<br/>Skip / Continue]
    N -->|granted or already asked| G[Safety Guidelines and Waiver dialog]
    NotR --> G
    G -->|Accept| Walk[Walk in progress<br/>foreground service tracks route]
    G -->|Decline| Idle
    Walk -->|Stop| Stop["Stop your walk?"]
    Stop -->|Keep walking| Walk
    Stop -->|End walk| Submit[[Submit Walk screen]]
    Walk -->|mock location detected| Spoof[Spoof location detected<br/>Understood → walk cleared]
```

## Dialogs on this screen

| Safety Guidelines and Waiver | Stop your walk? | Spoof location detected |
|---|---|---|
| <img src="../images/logawalk/PreviewGuidelinesDialog_light.png" width="240"> | <img src="../images/logawalk/PreviewStopWalkConfirmationDialog_light.png" width="200"> | <img src="../images/logawalk/PreviewWalkEndedDialog_light.png" width="200"> |
| Must be accepted before every walk. The links open the guidelines PDF and the waiver in a Custom Tab. | Confirms ending the walk. | Shown when `WalkState.MOCK_LOCATION_DETECTED`. The walk is discarded. |

The location and notification permission rationale dialogs are shown in [Shared components](../components.md#permission-rationale-dialogs).

## Other behavior

- An in-progress walk is recovered when the screen restarts (`recoverWalkingState`).
- On first display the app checks the saved account still exists (`validateProfile`). If it doesn't, the user is logged out to Login with the back stack cleared.
- `WalkCompleted` → [Submit Walk](submit-walk.md).
