package kattcrazy.sharemything.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kattcrazy.sharemything.data.ItemIcon

@Composable
fun ItemIconDisplay(
    icon: ItemIcon,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
) {
    Icon(
        imageVector = ImageVector.vectorResource(icon.drawableRes()),
        contentDescription = stringResource(icon.contentDescriptionRes()),
        modifier = modifier.size(size),
        tint = LocalContentColor.current,
    )
}
