package com.Kelasor.app.util

import java.util.*

/**
 * Utility class for date conversions and Solar Hijri (Jalali) calendar support.
 */
object DateUtils {

    /**
     * Gregorian to Jalali (Shamsi) date conversion.
     * Returns Triple(jYear, jMonth, jDay) where jMonth is 1-based (1=فروردین, 12=اسفند).
     */
    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var gy = gYear - 1600
        val gm = gMonth - 1
        val gd = gDay - 1
        var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i + 1] - gDaysInMonth[i]
        }
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd
        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053
        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461
        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (jDayNo < 186) {
            jm = 1 + jDayNo / 31
            jd = 1 + jDayNo % 31
        } else {
            jm = 7 + (jDayNo - 186) / 30
            jd = 1 + (jDayNo - 186) % 30
        }
        return Triple(jy, jm, jd)
    }

    /**
     * Jalali (Shamsi) to Gregorian date conversion.
     * jMonth is 1-based (1=فروردین, 12=اسفند).
     * Returns Triple(gYear, gMonth, gDay) where gMonth is 1-based.
     */
    fun jalaliToGregorian(jYear: Int, jMonth: Int, jDay: Int): Triple<Int, Int, Int> {
        var jy = jYear - 979
        val jm = jMonth - 1
        val jd = jDay - 1
        var jDayNo = 365 * jy + (jy / 33) * 8 + (jy % 33 + 3) / 4
        for (i in 0 until jm) {
            jDayNo += if (i < 6) 31 else 30
        }
        jDayNo += jd
        var gDayNo = jDayNo + 79
        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097
        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }
        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461
        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }
        val gDaysInMonth = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 12 && gDayNo >= gDaysInMonth[gm]) {
            gDayNo -= gDaysInMonth[gm]
            gm++
        }
        return Triple(gy, gm + 1, gDayNo + 1)
    }

    /**
     * Check if a Jalali year is a leap year.
     */
    fun isJalaliLeapYear(jYear: Int): Boolean {
        val breaks = intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
        val rem = jYear % 33
        return rem in breaks
    }

    /**
     * Get the number of days in a Jalali month (1-based month).
     */
    fun jalaliMonthDays(jYear: Int, jMonth: Int): Int {
        return when {
            jMonth in 1..6 -> 31
            jMonth in 7..11 -> 30
            jMonth == 12 -> if (isJalaliLeapYear(jYear)) 30 else 29
            else -> 30
        }
    }

    /**
     * Persian number formatter.
     */
    fun toPersianDigits(text: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return text.map { if (it in '0'..'9') persianDigits[it - '0'] else it }.joinToString("")
    }

    fun toPersianDigits(number: Int): String = toPersianDigits(number.toString())

    /**
     * Format Gregorian "yyyy-MM-dd" to Jalali "yyyy/MM/dd".
     */
    fun formatGregorianToJalali(gDate: String): String {
        if (gDate.isBlank()) return ""
        return try {
            val parts = gDate.split("-")
            val gy = parts[0].toInt()
            val gm = parts[1].toInt()
            val gd = parts[2].toInt()
            val (jy, jm, jd) = gregorianToJalali(gy, gm, gd)
            "$jy/${jm.toString().padStart(2, '0')}/${jd.toString().padStart(2, '0')}"
        } catch (e: Exception) {
            gDate
        }
    }
}
