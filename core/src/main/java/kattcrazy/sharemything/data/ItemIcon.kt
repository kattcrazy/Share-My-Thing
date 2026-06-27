package kattcrazy.sharemything.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kattcrazy.sharemything.core.R

enum class ItemIcon {
    TEXT,
    QR,
    PHONE,
    CREDIT_CARD,
    CARD_ACCOUNT_DETAILS,
    PLAYLIST_EDIT,
    SHARE_VARIANT,
    PIN,
    LINK_VARIANT,
    EMAIL,
    MOVIE_OPEN,
    ;

    @DrawableRes
    fun drawableRes(): Int = when (this) {
        TEXT -> R.drawable.ic_icon_text
        QR -> R.drawable.ic_icon_qr
        PHONE -> R.drawable.ic_icon_phone
        CREDIT_CARD -> R.drawable.ic_icon_credit_card
        CARD_ACCOUNT_DETAILS -> R.drawable.ic_icon_card_account_details
        PLAYLIST_EDIT -> R.drawable.ic_icon_playlist_edit
        SHARE_VARIANT -> R.drawable.ic_icon_share_variant
        PIN -> R.drawable.ic_icon_pin
        LINK_VARIANT -> R.drawable.ic_icon_link_variant
        EMAIL -> R.drawable.ic_icon_email
        MOVIE_OPEN -> R.drawable.ic_icon_movie_open
    }

    @StringRes
    fun contentDescriptionRes(): Int = when (this) {
        TEXT -> R.string.icon_text
        QR -> R.string.icon_qr
        PHONE -> R.string.icon_phone
        CREDIT_CARD -> R.string.icon_credit_card
        CARD_ACCOUNT_DETAILS -> R.string.icon_card_account_details
        PLAYLIST_EDIT -> R.string.icon_playlist_edit
        SHARE_VARIANT -> R.string.icon_share_variant
        PIN -> R.string.icon_pin
        LINK_VARIANT -> R.string.icon_link_variant
        EMAIL -> R.string.icon_email
        MOVIE_OPEN -> R.string.icon_movie_open
    }

    companion object {
        val pickerOrder: List<ItemIcon> = listOf(
            TEXT,
            QR,
            PHONE,
            CREDIT_CARD,
            CARD_ACCOUNT_DETAILS,
            PLAYLIST_EDIT,
            SHARE_VARIANT,
            PIN,
            LINK_VARIANT,
            EMAIL,
            MOVIE_OPEN,
        )

        fun defaultFor(type: ItemType): ItemIcon = when (type) {
            ItemType.TEXT -> TEXT
            ItemType.QR_CODE -> QR
            ItemType.BOTH -> QR
        }

        fun fromStoredName(name: String?, fallbackType: ItemType): ItemIcon {
            if (name.isNullOrBlank()) return defaultFor(fallbackType)
            legacyIconMap[name]?.let { return it }
            return runCatching { valueOf(name) }.getOrDefault(defaultFor(fallbackType))
        }

        /** Maps removed/renamed icons so existing items and sync payloads keep working. */
        private val legacyIconMap: Map<String, ItemIcon> = mapOf(
            "CALL" to PHONE,
            "ACCOUNT_BOX" to CARD_ACCOUNT_DETAILS,
            "EDIT" to PLAYLIST_EDIT,
            "PLAY_CIRCLE" to MOVIE_OPEN,
            "SHARE" to SHARE_VARIANT,
            "PIN_DROP" to PIN,
            "LINK" to LINK_VARIANT,
            "BOTH" to QR,
            "ARTICLE" to TEXT,
            "NOTES" to TEXT,
            "HOME" to TEXT,
            "BUSINESS_CENTER" to TEXT,
        )
    }
}
