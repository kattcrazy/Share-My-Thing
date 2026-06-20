package com.sharemyththing.ui.about

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.sharemyththing.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName.orEmpty()
        }.getOrDefault("")
    }
    val supportUrl = stringResource(R.string.support_banner_url)
    val githubUrl = stringResource(R.string.about_github_url)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name_short),
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = stringResource(R.string.about_version, versionName),
                style = MaterialTheme.typography.bodyLarge,
            )

            AboutSection(
                heading = stringResource(R.string.about_developer_heading),
                body = {
                    Text(
                        text = stringResource(R.string.about_developer_body),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
            )

            AboutSection(
                heading = stringResource(R.string.about_support_heading),
                body = {
                    InlineLinkParagraph(
                        prefix = stringResource(R.string.about_support_prefix),
                        linkText = stringResource(R.string.about_support_link),
                        suffix = stringResource(R.string.about_support_suffix),
                        url = supportUrl,
                    )
                },
            )

            AboutSection(
                heading = stringResource(R.string.about_bug_heading),
                body = {
                    InlineLinkParagraph(
                        prefix = stringResource(R.string.about_bug_prefix),
                        linkText = stringResource(R.string.about_bug_link),
                        suffix = stringResource(R.string.about_bug_suffix),
                        url = githubUrl,
                    )
                },
            )
        }
    }
}

@Composable
private fun AboutSection(
    heading: String,
    body: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = heading,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        body()
    }
}

@Composable
private fun InlineLinkParagraph(
    prefix: String,
    linkText: String,
    suffix: String,
    url: String,
) {
    val context = LocalContext.current
    val linkColor = MaterialTheme.colorScheme.primary
    val openUrl = { targetUrl: String ->
        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(targetUrl))
    }

    Text(
        text = buildAnnotatedString {
            append(prefix.trimEnd())
            append(' ')
            withLink(
                LinkAnnotation.Url(
                    url = url,
                    styles = TextLinkStyles(style = SpanStyle(color = linkColor)),
                    linkInteractionListener = { openUrl(url) },
                ),
            ) {
                append(linkText)
            }
            append(suffix)
        },
        style = MaterialTheme.typography.bodyMedium,
    )
}
