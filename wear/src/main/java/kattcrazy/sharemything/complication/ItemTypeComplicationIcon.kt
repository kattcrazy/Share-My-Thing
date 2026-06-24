package kattcrazy.sharemything.complication

import kattcrazy.sharemything.R
import kattcrazy.sharemything.data.ItemType

internal fun ItemType.complicationIconRes(): Int = when (this) {
    ItemType.TEXT -> R.drawable.ic_item_text
    ItemType.QR_CODE, ItemType.BOTH -> R.drawable.ic_item_qr
}
