# Take Picture

[← Design index](../README.md) · Route `takePicture` · `ui/composable/takepicture/` · `TakePictureViewModel` (CameraX)

A full-screen in-app camera used for the walk photo. There is no top or bottom bar, just a round back button at the top left. The viewfinder is a grey placeholder in these screenshots.

| Capture | Confirm |
|---|---|
| <img src="../images/takepicture/PreviewTakePicture_light.png" width="220"> | <img src="../images/takepicture/PreviewConfirmImage_light.png" width="220"> |
| Large white shutter button | Shows the captured image with **✕** (retake) and **✓** (use photo) |

## Behavior

- **✓** saves the photo to the walk and returns to [Submit Walk](submit-walk.md).
- **✕** discards the capture and returns to the viewfinder.
- Back (on-screen or system) removes any image and returns to Submit Walk.
- The controls follow device rotation (`MonitorOrientation`), and the layout changes between portrait and landscape.
- If camera permission has been revoked, the screen returns to Submit Walk immediately.
