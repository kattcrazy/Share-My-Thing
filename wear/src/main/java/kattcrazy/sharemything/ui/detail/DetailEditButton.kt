package kattcrazy.sharemything.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun DetailEditButton(
    onEditClick: () -> Unit,
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
            onClick = onEditClick,
            modifier = buttonModifier,
            transformation = transformation,
            colors = colors,
            contentPadding = PaddingValues(0.dp),
        ) {
            CenteredRoundIcon {
                DetailEditIcon()
            }
        }
    } else {
        Button(
            onClick = onEditClick,
            modifier = buttonModifier,
            colors = colors,
            contentPadding = PaddingValues(0.dp),
        ) {
            CenteredRoundIcon {
                DetailEditIcon()
            }
        }
    }
}

@Composable
private fun DetailEditIcon() {
    Icon(
        painter = painterResource(R.drawable.ic_edit),
        contentDescription = stringResource(R.string.edit_item),
        modifier = Modifier.size(26.dp),
    )
}

@Composable
internal fun CenteredRoundIcon(
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
