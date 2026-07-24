package kattcrazy.sharemything.ui

import android.util.Patterns
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink

/**
 * Item body text with auto-detected web links (accent-colored, tappable).
 * Set [selectable] for long-press select/copy (phone only — Wear has no clipboard UX).
 */
@Composable
fun ItemContentText(
    text: String,
    style: TextStyle,
    color: Color,
    linkColor: Color,
    onUrlClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    selectable: Boolean = false,
) {
    val latestOnUrlClick = rememberUpdatedState(onUrlClick)
    val annotated = remember(text, linkColor) {
        buildLinkifiedAnnotatedString(text, linkColor) { latestOnUrlClick.value(it) }
    }
    val mergedStyle = style.merge(
        TextStyle(
            color = color,
            textAlign = textAlign ?: TextAlign.Unspecified,
        ),
    )

    if (selectable) {
        SelectionContainer(modifier = modifier) {
            BasicText(text = annotated, style = mergedStyle)
        }
    } else {
        BasicText(text = annotated, modifier = modifier, style = mergedStyle)
    }
}

fun buildLinkifiedAnnotatedString(
    text: String,
    linkColor: Color,
    onUrlClick: (String) -> Unit,
): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    return buildAnnotatedString {
        val matcher = Patterns.WEB_URL.matcher(text)
        var lastIndex = 0
        while (matcher.find()) {
            val rawStart = matcher.start()
            val rawMatched = text.substring(rawStart, matcher.end())
            val matched = rawMatched.trimEnd { it in TRAILING_URL_PUNCT }
            if (matched.isEmpty()) continue
            val start = rawStart
            val end = rawStart + matched.length
            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }
            val href = normalizeUrl(matched)
            withLink(
                LinkAnnotation.Url(
                    url = href,
                    styles = TextLinkStyles(style = SpanStyle(color = linkColor)),
                    linkInteractionListener = { onUrlClick(href) },
                ),
            ) {
                append(matched)
            }
            lastIndex = end
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

private val TRAILING_URL_PUNCT = charArrayOf('.', ',', ';', ':', '!', '?', ')', ']', '\'', '"')

private fun normalizeUrl(raw: String): String =
    when {
        raw.startsWith("http://", ignoreCase = true) ||
            raw.startsWith("https://", ignoreCase = true) -> raw
        else -> "https://$raw"
    }
