package kattcrazy.sharemything.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kattcrazy.sharemything.R
import kattcrazy.sharemything.data.ItemType

@Composable
fun ItemTypeIcons(
    type: ItemType,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 22.dp,
) {
    when (type) {
        ItemType.TEXT -> {
            Icon(
                painter = painterResource(R.drawable.ic_item_text),
                contentDescription = stringResource(R.string.type_text),
                modifier = modifier.size(iconSize),
                tint = LocalContentColor.current,
            )
        }

        ItemType.QR_CODE -> {
            Icon(
                painter = painterResource(R.drawable.ic_item_qr),
                contentDescription = stringResource(R.string.type_qr),
                modifier = modifier.size(iconSize),
                tint = LocalContentColor.current,
            )
        }

        ItemType.BOTH -> {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_item_text),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize - 4.dp),
                    tint = LocalContentColor.current,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_item_qr),
                    contentDescription = stringResource(R.string.type_both),
                    modifier = Modifier.size(iconSize - 4.dp),
                    tint = LocalContentColor.current,
                )
            }
        }
    }
}

@Composable
fun itemTypeContentDescription(type: ItemType): String = when (type) {
    ItemType.TEXT -> stringResource(R.string.type_text)
    ItemType.QR_CODE -> stringResource(R.string.type_qr)
    ItemType.BOTH -> stringResource(R.string.type_both)
}
