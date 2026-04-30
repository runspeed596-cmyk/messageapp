package com.Kelasor.app.util

/**
 * Converts standard English digits (0-9) to Persian digits (۰-۹).
 */
fun String.toPersianNumbers(): String {
    if (this.isEmpty()) return this
    val persianNumbers = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    val builder = java.lang.StringBuilder()
    for (char in this) {
        if (char in '0'..'9') {
            builder.append(persianNumbers[char - '0'])
        } else {
            builder.append(char)
        }
    }
    return builder.toString()
}

/**
 * Formats a Long or Int as a price (e.g., 1,000,000) and converts it to Persian digits.
 */
fun Number.toPersianPrice(): String {
    val formatted = String.format(java.util.Locale("en", "US"), "%,d", this.toLong())
    return formatted.toPersianNumbers()
}
