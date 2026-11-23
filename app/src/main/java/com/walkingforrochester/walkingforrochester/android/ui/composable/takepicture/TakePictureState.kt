package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import android.net.Uri
import androidx.camera.core.SurfaceRequest

data class TakePictureState(
    val mode: TakePictureMode = TakePictureMode.CaptureImage,
    val captureInProgress: Boolean = false,
    val imageUri: Uri = Uri.EMPTY,
    val surfaceRequest: SurfaceRequest? = null,
    val event: TakePictureEvent = TakePictureEvent.NoEvent
)

enum class TakePictureMode {
    CaptureImage, ConfirmImage
}

enum class TakePictureEvent {
    NoEvent, CaptureError, ConfirmError, ImageConfirmed
}