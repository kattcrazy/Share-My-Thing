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

/** QR payloads must be one line; collapse whitespace for encoding and display. */
fun String.asSingleLineQrContent(): String = replace(Regex("\\s+"), " ").trim()
