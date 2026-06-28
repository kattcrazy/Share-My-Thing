package kattcrazy.sharemything.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kattcrazy.sharemything.data.ItemIcon
import kattcrazy.sharemything.theme.ShareMyThingColorSchemes

enum class IconPickerVariant {
    /** Matches mobile FilterChip type styling. */
    Phone,
    /** Matches wear type toggle button styling. */
    Wear,
}

private data class IconPickerColors(
    val selectedContainer: Color,
    val selectedIcon: Color,
    val unselectedContainer: Color,
    val unselectedIcon: Color,
)

@Composable
private fun iconPickerColors(variant: IconPickerVariant): IconPickerColors {
    return when (variant) {
        IconPickerVariant.Wear -> {
            val scheme = ShareMyThingColorSchemes.dark
            IconPickerColors(
                selectedContainer = scheme.primary,
                selectedIcon = scheme.onPrimary,
                unselectedContainer = scheme.secondaryContainer,
                unselectedIcon = scheme.onSecondaryContainer,
            )
        }
        IconPickerVariant.Phone -> {
            val dark = isSystemInDarkTheme()
            val scheme = if (dark) ShareMyThingColorSchemes.dark else ShareMyThingColorSchemes.light
            IconPickerColors(
                selectedContainer = scheme.secondaryContainer,
                selectedIcon = scheme.onSecondaryContainer,
                unselectedContainer = if (dark) scheme.surfaceContainerLow else Color.Transparent,
                unselectedIcon = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ItemIconPicker(
    selectedIcon: ItemIcon,
    onIconSelected: (ItemIcon) -> Unit,
    modifier: Modifier = Modifier,
    cellSize: Dp = 40.dp,
    variant: IconPickerVariant = IconPickerVariant.Phone,
) {
    val colors = iconPickerColors(variant)
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
            val containerColor = if (selected) colors.selectedContainer else colors.unselectedContainer
            val iconTint = if (selected) colors.selectedIcon else colors.unselectedIcon
            Box(
                modifier = Modifier
                    .size(cellSize)
                    .clip(EditControlShape)
                    .background(containerColor)
                    .clickable { onIconSelected(icon) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(icon.drawableRes()),
                    contentDescription = stringResource(icon.contentDescriptionRes()),
                    modifier = Modifier.size(cellSize - 12.dp),
                    tint = iconTint,
                )
            }
        }
    }
}
