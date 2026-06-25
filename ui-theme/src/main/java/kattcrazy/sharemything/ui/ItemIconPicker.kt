package kattcrazy.sharemything.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kattcrazy.sharemything.data.ItemIcon

@Composable
fun ItemIconPicker(
    selectedIcon: ItemIcon,
    onIconSelected: (ItemIcon) -> Unit,
    modifier: Modifier = Modifier,
    cellSize: Dp = 40.dp,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIcon) {
        val index = ItemIcon.pickerOrder.indexOf(selectedIcon)
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }
    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
    ) {
        items(ItemIcon.pickerOrder, key = { it.name }) { icon ->
            val selected = icon == selectedIcon
            Box(
                modifier = Modifier
                    .size(cellSize)
                    .clip(CircleShape)
                    .border(
                        width = if (selected) 2.dp else 0.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0f)
                        },
                        shape = CircleShape,
                    )
                    .clickable { onIconSelected(icon) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon.drawableRes()),
                    contentDescription = stringResource(icon.contentDescriptionRes()),
                    modifier = Modifier.size(cellSize - 12.dp),
                    tint = LocalContentColor.current,
                )
            }
        }
    }
}
