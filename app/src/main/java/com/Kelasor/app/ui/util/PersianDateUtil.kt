package com.Kelasor.app.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate

/**
 * Persian/Jalali (Shamsi) date utility functions.
 */
object PersianDateUtil {
    private val shamsiMonthNames: List<String> = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )
    /**
     * Converts Gregorian date to Jalali (Shamsi).
     * Returns Triple(jYear, jMonth, jDay) where jMonth is 1-based (1=فروردین, 12=اسفند).
     */
    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1
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
     * Formats an Instant to "HH:mm - dd MonthName" in Jalali (Shamsi) calendar.
     * Example: "14:30 - 26 بهمن"
     */
    fun formatShamsi(instant: Instant): String {
        val zoned = instant.atZone(ZoneId.systemDefault())
        val (_, jMonth, jDay) = gregorianToJalali(
            zoned.year, zoned.monthValue, zoned.dayOfMonth
        )
        val hour = zoned.hour.toString().padStart(2, '0')
        val minute = zoned.minute.toString().padStart(2, '0')
        val monthName = shamsiMonthNames[jMonth - 1]
        return "$hour:$minute - $jDay $monthName"
    }
    /**
     * Formats an Instant to "dd MonthName yyyy" in Jalali (Shamsi) calendar.
     * Example: "26 بهمن 1404"
     */
    fun formatShamsiDate(instant: Instant): String {
        val zoned = instant.atZone(ZoneId.systemDefault())
        val (jYear, jMonth, jDay) = gregorianToJalali(
            zoned.year, zoned.monthValue, zoned.dayOfMonth
        )
        val monthName = shamsiMonthNames[jMonth - 1]
        return "$jDay $monthName $jYear"
    }
}
