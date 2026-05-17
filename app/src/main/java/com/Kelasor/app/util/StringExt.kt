package com.Kelasor.app.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

/**
 * Formats an Instant to a readable Persian Date Time string.
 */
fun Instant.toPersianDateTime(): String {
    return try {
        val zdt = this.atZone(ZoneId.systemDefault())
        val now = java.time.ZonedDateTime.now(ZoneId.systemDefault())
        
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val timeStr = zdt.format(timeFormatter)
        
        if (zdt.toLocalDate() == now.toLocalDate()) {
            "امروز، ساعت $timeStr".toPersianNumbers()
        } else if (zdt.toLocalDate() == now.toLocalDate().minusDays(1)) {
            "دیروز، ساعت $timeStr".toPersianNumbers()
        } else {
            val (jy, jm, jd) = DateUtils.gregorianToJalali(zdt.year, zdt.monthValue, zdt.dayOfMonth)
            val monthNames = arrayOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
            val monthName = monthNames.getOrNull(jm - 1) ?: jm.toString()
            "$jd $monthName $jy، ساعت $timeStr".toPersianNumbers()
        }
    } catch (e: Exception) {
        this.toString()
    }
}

/**
 * Formats an ISO 8601 date string (e.g. 2023-10-12T14:30:00Z) to a readable Persian Date Time string.
 */
fun String.toPersianDateTime(): String {
    if (this.isBlank() || this == "Unknown") return this
    return try {
        val sanitized = this.trim().uppercase()
        val instant = Instant.parse(sanitized)
        instant.toPersianDateTime()
    } catch (e: Exception) {
        try {
            val sanitized = this.trim().uppercase()
            val offsetDateTime = java.time.OffsetDateTime.parse(sanitized)
            offsetDateTime.toInstant().toPersianDateTime()
        } catch (e2: Exception) {
            try {
                val zonedDateTime = java.time.ZonedDateTime.parse(this.trim())
                zonedDateTime.toInstant().toPersianDateTime()
            } catch (e3: Exception) {
                this
            }
        }
    }
}
