package kattcrazy.sharemything.data

enum class ItemType {
    TEXT,
    QR_CODE,
    BOTH,
}

val ItemType.usesQr: Boolean
    get() = this == ItemType.QR_CODE || this == ItemType.BOTH

val ItemType.usesTextBody: Boolean
    get() = this == ItemType.TEXT || this == ItemType.BOTH
