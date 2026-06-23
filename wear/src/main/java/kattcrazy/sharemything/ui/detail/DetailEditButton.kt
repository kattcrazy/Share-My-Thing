package kattcrazy.sharemything.ui.detail

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

    if (transformation != null) {
        Button(
            onClick = onEditClick,
            modifier = buttonModifier,
            transformation = transformation,
            colors = colors,
        ) {
            DetailEditIcon()
        }
    } else {
        Button(
            onClick = onEditClick,
            modifier = buttonModifier,
            colors = colors,
        ) {
            DetailEditIcon()
        }
    }
}

@Composable
private fun DetailEditIcon() {
    Icon(
        painter = painterResource(R.drawable.ic_edit),
        contentDescription = stringResource(R.string.edit_item),
        modifier = Modifier.size(20.dp),
    )
}
