package com.walkingforrochester.walkingforrochester.android.ui.composable.contact

import android.content.Context
import android.content.Intent
import android.net.MailTo
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.walkingforrochester.walkingforrochester.android.BuildConfig
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ktx.isPhoneLandscape
import com.walkingforrochester.walkingforrochester.android.ktx.safeStartActivity
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.NavigationIcon
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun ContactUsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val isPhoneLandscape = currentWindowAdaptiveInfoV2().windowSizeClass.isPhoneLandscape()

    @OptIn(ExperimentalMaterial3Api::class)
    val scrollBehavior =
        if (isPhoneLandscape) TopAppBarDefaults.enterAlwaysScrollBehavior()
        else TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.contact_us)) },
                navigationIcon = { NavigationIcon(onClick = onNavigateBack) },
                scrollBehavior = scrollBehavior
            )
        }) { contentPadding ->
        @OptIn(ExperimentalMaterial3Api::class)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nestedScroll(scrollBehavior.nestedScrollConnection)

                .padding(contentPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            val maxWidth = dimensionResource(R.dimen.max_form_width)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.contact_us_text),
                modifier = Modifier
                    .widthIn(max = maxWidth)
                    .fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                modifier
                    .widthIn(max = maxWidth)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(Modifier.padding(end = 16.dp)) {
                    Text(
                        text = stringResource(id = R.string.phone_label),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.email_label),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Column {
                    Text(
                        text = "585-577-4820",
                        modifier = Modifier
                            .clickable {
                                callOffice(context)
                            },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "info@walkingforrochester.org",
                        modifier = Modifier
                            .clickable {
                                emailUs(context)
                            },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(
                    id = R.string.app_version,
                    BuildConfig.VERSION_NAME
                ),
                modifier = Modifier
                    .widthIn(max = maxWidth)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun callOffice(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = "tel:+15855774820".toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    context.safeStartActivity(intent)
}

fun emailUs(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = buildUri(context)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    context.safeStartActivity(intent)
}

private fun buildUri(context: Context): Uri {
    val uriString = MailTo.MAILTO_SCHEME +
        Uri.encode("info@walkingforrochester.org") +
        "?subject=" +
        Uri.encode(context.getString(R.string.email_subject))

    return uriString.toUri()
}

@PreviewScreenSizes
@Composable
fun ContactUsPreview() {
    WalkingForRochesterTheme {
        Surface {
            ContactUsScreen()
        }
    }
}