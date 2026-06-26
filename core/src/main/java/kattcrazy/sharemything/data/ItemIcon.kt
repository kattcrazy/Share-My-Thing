package kattcrazy.sharemything.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kattcrazy.sharemything.core.R

enum class ItemIcon {
    TEXT,
    QR,
    BOTH,
    ARTICLE,
    CALL,
    CREDIT_CARD,
    ACCOUNT_BOX,
    BUSINESS_CENTER,
    EDIT,
    PLAY_CIRCLE,
    SHARE,
    PIN_DROP,
    LINK,
    NOTES,
    EMAIL,
    HOME,
    ;

    @DrawableRes
    fun drawableRes(): Int = when (this) {
        TEXT -> R.drawable.ic_icon_text
        QR -> R.drawable.ic_icon_qr
        BOTH -> R.drawable.ic_icon_both
        ARTICLE -> R.drawable.ic_icon_article
        CALL -> R.drawable.ic_icon_call
        CREDIT_CARD -> R.drawable.ic_icon_credit_card
        ACCOUNT_BOX -> R.drawable.ic_icon_account_box
        BUSINESS_CENTER -> R.drawable.ic_icon_business_center
        EDIT -> R.drawable.ic_icon_edit
        PLAY_CIRCLE -> R.drawable.ic_icon_play_circle
        SHARE -> R.drawable.ic_icon_share
        PIN_DROP -> R.drawable.ic_icon_pin_drop
        LINK -> R.drawable.ic_icon_link
        NOTES -> R.drawable.ic_icon_notes
        EMAIL -> R.drawable.ic_icon_email
        HOME -> R.drawable.ic_icon_home
    }

    @StringRes
    fun contentDescriptionRes(): Int = when (this) {
        TEXT -> R.string.icon_text
        QR -> R.string.icon_qr
        BOTH -> R.string.icon_both
        ARTICLE -> R.string.icon_article
        CALL -> R.string.icon_call
        CREDIT_CARD -> R.string.icon_credit_card
        ACCOUNT_BOX -> R.string.icon_account_box
        BUSINESS_CENTER -> R.string.icon_business_center
        EDIT -> R.string.icon_edit
        PLAY_CIRCLE -> R.string.icon_play_circle
        SHARE -> R.string.icon_share
        PIN_DROP -> R.string.icon_pin_drop
        LINK -> R.string.icon_link
        NOTES -> R.string.icon_notes
        EMAIL -> R.string.icon_email
        HOME -> R.string.icon_home
    }

    companion object {
        val pickerOrder: List<ItemIcon> = listOf(
            TEXT,
            QR,
            ARTICLE,
            CALL,
            CREDIT_CARD,
            ACCOUNT_BOX,
            BUSINESS_CENTER,
            EDIT,
            PLAY_CIRCLE,
            SHARE,
            PIN_DROP,
            LINK,
            NOTES,
            EMAIL,
            HOME,
        )

        fun defaultFor(type: ItemType): ItemIcon = when (type) {
            ItemType.TEXT -> TEXT
            ItemType.QR_CODE -> QR
            ItemType.BOTH -> QR
        }

        fun fromStoredName(name: String?, fallbackType: ItemType): ItemIcon {
            if (name.isNullOrBlank()) return defaultFor(fallbackType)
            return runCatching { valueOf(name) }.getOrDefault(defaultFor(fallbackType))
        }
    }
}
