package com.Kelasor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.Kelasor.app.ui.theme.DanaFontFamily
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Converts a Shamsi (Jalali) date to Gregorian date.
 * Algorithm based on the well-known Jalali-Gregorian conversion formulas.
 */
object ShamsiConverter {
    fun shamsiToGregorian(jYear: Int, jMonth: Int, jDay: Int): Triple<Int, Int, Int> {
        val jy = jYear - 979
        val jm = jMonth - 1
        val jd = jDay - 1
        val jDayNo = 365 * jy + (jy / 33) * 8 + (jy % 33 + 3) / 4 + jm * 30 + minOf(jm, 6) + jd
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

    fun gregorianToShamsi(gYear: Int, gMonth: Int, gDay: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
        val gy = gYear - 1600
        val gm = gMonth - 1
        val gd = gDay - 1
        var gDayNo = 365 * gy + ((gy + 3) / 4) - ((gy + 99) / 100) + ((gy + 399) / 400)
        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i]
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
        var jm = 0
        while (jm < 11 && jDayNo >= jDaysInMonth[jm]) {
            jDayNo -= jDaysInMonth[jm]
            jm++
        }
        return Triple(jy, jm + 1, jDayNo + 1)
    }

    fun shamsiToInstant(jYear: Int, jMonth: Int, jDay: Int, hour: Int = 0, minute: Int = 0): Instant {
        val (gy, gm, gd) = shamsiToGregorian(jYear, jMonth, jDay)
        val localDateTime = LocalDateTime.of(gy, gm, gd, hour, minute, 0)
        return localDateTime.atZone(ZoneId.of("Asia/Tehran")).toInstant()
    }

    fun getDaysInShamsiMonth(month: Int, year: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            month == 12 -> if (isShamsiLeapYear(year)) 30 else 29
            else -> 30
        }
    }

    fun isShamsiLeapYear(year: Int): Boolean {
        val remainder = year % 33
        return remainder in listOf(1, 5, 9, 13, 17, 22, 26, 30)
    }

    fun getCurrentShamsiYear(): Int {
        val now = LocalDate.now()
        val (jy, _, _) = gregorianToShamsi(now.year, now.monthValue, now.dayOfMonth)
        return jy
    }

    fun getCurrentShamsiMonth(): Int {
        val now = LocalDate.now()
        val (_, jm, _) = gregorianToShamsi(now.year, now.monthValue, now.dayOfMonth)
        return jm
    }

    fun getCurrentShamsiDay(): Int {
        val now = LocalDate.now()
        val (_, _, jd) = gregorianToShamsi(now.year, now.monthValue, now.dayOfMonth)
        return jd
    }

    val shamsiMonthNames: List<String> = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
}

data class ShamsiDateTimeResult(
    val displayString: String,       // e.g., "1404/02/15"
    val isoInstantString: String,    // e.g., "2025-05-05T10:30:00Z"
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int
)

@Composable
fun ShamsiDatePickerDialog(
    initialDate: String? = null,
    onDateSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val currentShamsiYear = ShamsiConverter.getCurrentShamsiYear()
    val currentShamsiMonth = ShamsiConverter.getCurrentShamsiMonth()
    val currentShamsiDay = ShamsiConverter.getCurrentShamsiDay()
    var selectedYear by remember { mutableIntStateOf(currentShamsiYear) }
    var selectedMonth by remember { mutableIntStateOf(currentShamsiMonth) }
    var selectedDay by remember { mutableIntStateOf(currentShamsiDay) }
    LaunchedEffect(initialDate) {
        if (!initialDate.isNullOrEmpty() && initialDate.contains("/")) {
            val parts = initialDate.split("/")
            if (parts.size == 3) {
                selectedYear = parts[0].toIntOrNull() ?: currentShamsiYear
                selectedMonth = parts[1].toIntOrNull() ?: 1
                selectedDay = parts[2].toIntOrNull() ?: 1
            }
        }
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "انتخاب تاریخ (شمسی)",
                    fontFamily = DanaFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Show selected date preview with month name
                val monthName = ShamsiConverter.shamsiMonthNames.getOrNull(selectedMonth - 1) ?: ""
                Text(
                    text = "$selectedDay $monthName $selectedYear",
                    fontFamily = DanaFontFamily,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Number Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Day
                    val maxDay = ShamsiConverter.getDaysInShamsiMonth(selectedMonth, selectedYear)
                    if (selectedDay > maxDay) selectedDay = maxDay
                    NumberPickerColumn(
                        range = 1..maxDay,
                        selectedValue = selectedDay,
                        onValueChange = { selectedDay = it },
                        label = "روز"
                    )
                    Text("/", fontSize = 24.sp, color = MaterialTheme.colorScheme.outline)
                    // Month
                    NumberPickerColumn(
                        range = 1..12,
                        selectedValue = selectedMonth,
                        onValueChange = { selectedMonth = it },
                        label = "ماه"
                    )
                    Text("/", fontSize = 24.sp, color = MaterialTheme.colorScheme.outline)
                    // Year
                    NumberPickerColumn(
                        range = currentShamsiYear..(currentShamsiYear + 10),
                        selectedValue = selectedYear,
                        onValueChange = { selectedYear = it },
                        label = "سال",
                        isHighlight = selectedYear == currentShamsiYear
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("انصراف", fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.outline)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val formattedMonth = selectedMonth.toString().padStart(2, '0')
                            val formattedDay = selectedDay.toString().padStart(2, '0')
                            onDateSelected("$selectedYear/$formattedMonth/$formattedDay")
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("تایید", fontFamily = DanaFontFamily)
                    }
                }
            }
        }
    }
}

@Composable
fun ShamsiDateTimePickerDialog(
    initialDate: String? = null,
    initialHour: Int = 10,
    initialMinute: Int = 0,
    onDateTimeSelected: (ShamsiDateTimeResult) -> Unit,
    onDismissRequest: () -> Unit
) {
    val currentShamsiYear = ShamsiConverter.getCurrentShamsiYear()
    val currentShamsiMonth = ShamsiConverter.getCurrentShamsiMonth()
    val currentShamsiDay = ShamsiConverter.getCurrentShamsiDay()
    var selectedYear by remember { mutableIntStateOf(currentShamsiYear) }
    var selectedMonth by remember { mutableIntStateOf(currentShamsiMonth) }
    var selectedDay by remember { mutableIntStateOf(currentShamsiDay) }
    
    // Calculate current hour/minute for restriction
    val nowDateTime = LocalDateTime.now(ZoneId.of("Asia/Tehran"))
    val currentHour = nowDateTime.hour
    val currentMinute = nowDateTime.minute

    var selectedHour by remember { mutableIntStateOf(maxOf(initialHour, if (selectedYear == currentShamsiYear && selectedMonth == currentShamsiMonth && selectedDay == currentShamsiDay) currentHour else 0)) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(initialDate) {
        if (!initialDate.isNullOrEmpty() && initialDate.contains("/")) {
            val parts = initialDate.split("/")
            if (parts.size == 3) {
                selectedYear = parts[0].toIntOrNull() ?: currentShamsiYear
                selectedMonth = parts[1].toIntOrNull() ?: 1
                selectedDay = parts[2].toIntOrNull() ?: 1
            }
        }
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "انتخاب زمان برگزاری",
                    fontFamily = DanaFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Show preview
                val monthName = ShamsiConverter.shamsiMonthNames.getOrNull(selectedMonth - 1) ?: ""
                val hourStr = selectedHour.toString().padStart(2, '0')
                val minuteStr = selectedMinute.toString().padStart(2, '0')
                Text(
                    text = "$selectedDay $monthName $selectedYear - ساعت $hourStr:$minuteStr",
                    fontFamily = DanaFontFamily,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Date Pickers
                Text("تاریخ", fontFamily = DanaFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val minDay = if (selectedYear == currentShamsiYear && selectedMonth == currentShamsiMonth) currentShamsiDay else 1
                    val maxDay = ShamsiConverter.getDaysInShamsiMonth(selectedMonth, selectedYear)
                    if (selectedDay < minDay) selectedDay = minDay
                    if (selectedDay > maxDay) selectedDay = maxDay
                    
                    NumberPickerColumn(
                        range = minDay..maxDay,
                        selectedValue = selectedDay,
                        onValueChange = { selectedDay = it },
                        label = "روز",
                        isHighlight = selectedYear == currentShamsiYear && selectedMonth == currentShamsiMonth && selectedDay == currentShamsiDay
                    )
                    Text("/", fontSize = 24.sp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    
                    val minMonth = if (selectedYear == currentShamsiYear) currentShamsiMonth else 1
                    NumberPickerColumn(
                        range = minMonth..12,
                        selectedValue = selectedMonth,
                        onValueChange = { 
                            selectedMonth = it
                        },
                        label = "ماه",
                        isHighlight = selectedYear == currentShamsiYear && selectedMonth == currentShamsiMonth
                    )
                    Text("/", fontSize = 24.sp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    NumberPickerColumn(
                        range = currentShamsiYear..(currentShamsiYear + 5),
                        selectedValue = selectedYear,
                        onValueChange = { selectedYear = it },
                        label = "سال",
                        isHighlight = selectedYear == currentShamsiYear
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))
                // Time Pickers
                Text("ساعت", fontFamily = DanaFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val minHour = if (selectedYear == currentShamsiYear && selectedMonth == currentShamsiMonth && selectedDay == currentShamsiDay) currentHour else 0
                    if (selectedHour < minHour) selectedHour = minHour
                    
                    NumberPickerColumn(
                        range = minHour..23,
                        selectedValue = selectedHour,
                        onValueChange = { selectedHour = it },
                        label = "ساعت",
                        isHighlight = selectedYear == currentShamsiYear && selectedMonth == currentShamsiMonth && selectedDay == currentShamsiDay && selectedHour == currentHour
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(":", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    val minMinute = if (selectedYear == currentShamsiYear && selectedMonth == currentShamsiMonth && selectedDay == currentShamsiDay && selectedHour == currentHour) currentMinute else 0
                    if (selectedMinute < minMinute) selectedMinute = minMinute
                    
                    NumberPickerColumn(
                        range = minMinute..59,
                        selectedValue = selectedMinute,
                        onValueChange = { selectedMinute = it },
                        label = "دقیقه",
                        isHighlight = selectedYear == currentShamsiYear && selectedMonth == currentShamsiMonth && selectedDay == currentShamsiDay && selectedHour == currentHour && selectedMinute == currentMinute
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("انصراف", fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.outline)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val instant = ShamsiConverter.shamsiToInstant(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)
                            
                            // Prevent past dates
                            if (instant.isBefore(java.time.Instant.now().minusSeconds(10))) {
                                android.widget.Toast.makeText(context, "امکان انتخاب زمان در گذشته وجود ندارد", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val formattedMonth = selectedMonth.toString().padStart(2, '0')
                            val formattedDay = selectedDay.toString().padStart(2, '0')
                            val displayString = "$selectedYear/$formattedMonth/$formattedDay"
                            
                            val result = ShamsiDateTimeResult(
                                displayString = displayString,
                                isoInstantString = instant.toString(),
                                year = selectedYear,
                                month = selectedMonth,
                                day = selectedDay,
                                hour = selectedHour,
                                minute = selectedMinute
                            )
                            onDateTimeSelected(result)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تایید و ثبت", fontFamily = DanaFontFamily)
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberPickerColumn(
    range: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    isHighlight: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label, 
            fontFamily = DanaFontFamily, 
            fontSize = 11.sp, 
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        // AndroidView with native NumberPicker
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { context ->
                android.widget.NumberPicker(context).apply {
                    minValue = range.first
                    maxValue = range.last
                    value = selectedValue
                    wrapSelectorWheel = range.last - range.first > 10
                    setOnValueChangedListener { _, _, newVal ->
                        onValueChange(newVal)
                    }
                }
            },
            update = { view ->
                if (view.minValue != range.first || view.maxValue != range.last) {
                    view.minValue = range.first
                    view.maxValue = range.last
                }
                if (view.value != selectedValue) {
                    view.value = selectedValue
                }
            },
            modifier = Modifier.wrapContentSize()
        )
    }
}
