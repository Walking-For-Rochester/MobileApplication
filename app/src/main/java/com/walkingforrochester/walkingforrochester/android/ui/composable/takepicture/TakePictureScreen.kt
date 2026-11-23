package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import android.Manifest
import android.view.OrientationEventListener
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ktx.isPortraitMode
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import timber.log.Timber

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TakePictureScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    takePictureViewModel: TakePictureViewModel = hiltViewModel()
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    // Ensure camera permission is still granted. If user used
    // one time permission and app was closed too long, we will loose
    // permission and no longer be able to use camera. In that
    // case we will invoke onCompletion() to trigger navigation back
    // to our caller.
    if (cameraPermission.status == PermissionStatus.Granted) {

        val takePictureState by takePictureViewModel.takePictureState.collectAsStateWithLifecycle()

        BackHandler(
            onBack = {
                when (takePictureState.mode) {
                    TakePictureMode.CaptureImage -> {
                        takePictureViewModel.removeImage()
                        onNavigateBack()
                    }

                    TakePictureMode.ConfirmImage -> takePictureViewModel.discardImage()
                }
            }
        )

        MonitorOrientation(
            onOrientationChanged = { takePictureViewModel.updateOrientation(it) }
        )

        TakePictureContent(
            takePictureState = takePictureState,
            modifier = modifier,
            onNavigateBack = onNavigateBack,
            onBindUseCases = { lifecycleOwner -> takePictureViewModel.bindUseCases(lifecycleOwner) },
            onUnbindUseCases = { takePictureViewModel.unbindUseCases() },
            onCaptureImage = { takePictureViewModel.captureImage() },
            onDiscardImage = { takePictureViewModel.discardImage() },
            onConfirmImage = { takePictureViewModel.confirmImage() },
            onClearEvent = { takePictureViewModel.clearEvent() }
        )
    } else {
        takePictureViewModel.removeImage()
        onNavigateBack()
    }
}

@Composable
fun MonitorOrientation(
    onOrientationChanged: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    // Sensors not available in preview, so only setup this listener
    // on device/emulator
    val orientationEventListener = remember(context) {
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }

                onOrientationChanged(orientation)
            }
        }
    }

    // Monitor rotation for image capture on start/stop
    LifecycleStartEffect(context) {
        orientationEventListener.enable()

        onStopOrDispose { orientationEventListener.disable() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakePictureContent(
    takePictureState: TakePictureState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onBindUseCases: (LifecycleOwner) -> Unit = {},
    onUnbindUseCases: () -> Unit = {},
    onCaptureImage: () -> Unit = {},
    onDiscardImage: () -> Unit = {},
    onConfirmImage: () -> Unit = {},
    onClearEvent: () -> Unit = {},
) {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val portraitMode = windowSizeClass.isPortraitMode()

    val snackBarHostState = remember { SnackbarHostState() }

    val resources = LocalResources.current

    LaunchedEffect(takePictureState.event) {
        when (takePictureState.event) {
            TakePictureEvent.NoEvent -> {
                Timber.d("No active event")
            }

            TakePictureEvent.CaptureError -> {
                snackBarHostState.showSnackbar(resources.getString(R.string.capture_error))
                onClearEvent()
            }

            TakePictureEvent.ConfirmError -> {
                snackBarHostState.showSnackbar(resources.getString(R.string.confirm_error))
                onClearEvent()
            }

            TakePictureEvent.ImageConfirmed -> {
                Timber.d("Image confirmed")
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    FilledIconButton(
                        onClick = {
                            if (takePictureState.mode == TakePictureMode.ConfirmImage) {
                                onDiscardImage()
                            } else {
                                onNavigateBack()
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.4f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_24dp),
                            contentDescription = stringResource(R.string.back_button),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { contentPadding ->

        AnimatedContent(
            targetState = takePictureState.mode,
            transitionSpec = {
                fadeIn(animationSpec = tween(220))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            }
        ) {
            when (it) {

                TakePictureMode.CaptureImage -> {
                    CaptureImage(
                        portraitMode = portraitMode,
                        modifier = modifier.fillMaxSize(),
                        captureInProgress = takePictureState.captureInProgress,
                        surfaceRequest = takePictureState.surfaceRequest,
                        onBindUseCases = onBindUseCases,
                        onUnbindUseCases = onUnbindUseCases,
                        onCaptureImage = onCaptureImage,
                        contentPadding = contentPadding
                    )
                }

                TakePictureMode.ConfirmImage -> {
                    ConfirmImage(
                        imageUri = takePictureState.imageUri,
                        portraitMode = portraitMode,
                        modifier = modifier.fillMaxSize(),
                        onConfirmImage = onConfirmImage,
                        onDiscardImage = onDiscardImage,
                        contentPadding = contentPadding
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewTakePictureCapture() {
    WalkingForRochesterTheme {
        Surface {
            TakePictureContent(
                takePictureState = TakePictureState()
            )
        }
    }
}

@Preview
@Composable
fun PreviewTakePictureConfirm() {
    WalkingForRochesterTheme {
        Surface {
            TakePictureContent(
                takePictureState = TakePictureState(mode = TakePictureMode.ConfirmImage)
            )
        }
    }
}
