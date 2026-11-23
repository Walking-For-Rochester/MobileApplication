package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import androidx.camera.core.SurfaceRequest
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ktx.isPortraitMode
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun CaptureImage(
    portraitMode: Boolean,
    modifier: Modifier = Modifier,
    captureInProgress: Boolean = false,
    surfaceRequest: SurfaceRequest? = null,
    onBindUseCases: (LifecycleOwner) -> Unit = {},
    onUnbindUseCases: () -> Unit = {},
    onCaptureImage: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues()
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Bind use cases only while this screen is active.
    LifecycleStartEffect(key1 = lifecycleOwner) {
        onBindUseCases(lifecycleOwner)

        onStopOrDispose {
            onUnbindUseCases()
        }
    }

    Box(modifier = modifier) {

        PreviewImage(
            surfaceRequest = surfaceRequest,
            modifier = Modifier.fillMaxSize()
        )

        val alignment = if (portraitMode) Alignment.BottomCenter else Alignment.CenterEnd
        val layoutDirection = LocalLayoutDirection.current

        val padding =
            if (portraitMode) {
                PaddingValues(bottom = 24.dp + contentPadding.calculateBottomPadding())
            } else {
                PaddingValues(end = 24.dp + contentPadding.calculateRightPadding(layoutDirection))
            }

        val colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.3f),
            contentColor = Color.White,
        )

        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        val innerScale by animateFloatAsState(
            targetValue = if (isPressed) 0.88f else 1f,
            label = "shutterInnerScale"
        )

        val darkRippleConfig = RippleConfiguration(
            color = Color.Black,
        )

        CompositionLocalProvider(LocalRippleConfiguration provides darkRippleConfig) {
            OutlinedIconButton(
                onClick = { onCaptureImage() },
                modifier = Modifier
                    .padding(padding)
                    .align(alignment)
                    .size(64.dp), // Set total button target size here
                enabled = !captureInProgress,
                colors = colors,
                border = BorderStroke(
                    width = 2.dp,
                    color = if (captureInProgress) colors.disabledContentColor else colors.contentColor
                ),
                interactionSource = interactionSource
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_circle_24dp),
                    modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer {
                            scaleX = innerScale
                            scaleY = innerScale
                        },
                    contentDescription = stringResource(R.string.take_picture)
                )
            }
        }
    }
}

@PreviewScreenSizes
@Composable
private fun PreviewCaptureImage() {
    WalkingForRochesterTheme {
        Surface {
            val portraitMode = currentWindowAdaptiveInfoV2().windowSizeClass.isPortraitMode()

            CaptureImage(
                portraitMode = portraitMode
            )
        }
    }
}