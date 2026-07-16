package kattcrazy.sharemything.ui.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import kattcrazy.sharemything.R
import kattcrazy.sharemything.ui.pressBounce

@Composable
fun DetailHelpButton(
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation? = null,
) {
    val colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
    val buttonModifier = modifier
        .size(44.dp)
        .clip(CircleShape)
        .pressBounce()

    if (transformation != null) {
        Button(
            onClick = onHelpClick,
            modifier = buttonModifier,
            transformation = transformation,
            colors = colors,
            contentPadding = PaddingValues(0.dp),
        ) {
            DetailHelpIcon()
        }
    } else {
        Button(
            onClick = onHelpClick,
            modifier = buttonModifier,
            colors = colors,
            contentPadding = PaddingValues(0.dp),
        ) {
            DetailHelpIcon()
        }
    }
}

@Composable
private fun DetailHelpIcon() {
    Icon(
        painter = painterResource(R.drawable.ic_help),
        contentDescription = stringResource(R.string.qr_tips_title),
        modifier = Modifier.size(28.dp),
    )
}
