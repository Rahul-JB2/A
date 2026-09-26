package com.example.util

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SecurityUtils {
    private const val SALT = "SmartTeacherAttendance2026!"

    fun hashPassword(password: String): String {
        val input = password + SALT
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }
}

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US)
    private val shortDisplayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)

    fun getCurrentTimeFormatted(): String = timeFormat.format(Date())

    fun getTodayIso(): String = isoFormat.format(Date())

    fun formatIsoToDisplay(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate)
            if (date != null) displayFormat.format(date) else isoDate
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatIsoToShort(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate)
            if (date != null) shortDisplayFormat.format(date) else isoDate
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        return displayFormat.format(Date(timestamp)) + " at " + timeFormat.format(Date(timestamp))
    }

    fun getDaysAgoIso(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return isoFormat.format(cal.time)
    }

    fun getDaysList(count: Int): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        for (i in 0 until count) {
            val copy = cal.clone() as Calendar
            copy.add(Calendar.DAY_OF_YEAR, -i)
            list.add(isoFormat.format(copy.time))
        }
        return list
    }

    private val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val monthYearDisplayFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    private val monthShortDisplayFormat = SimpleDateFormat("MMM yyyy", Locale.US)
    private val dayMonthFormat = SimpleDateFormat("dd MMM (EEE)", Locale.US)

    fun getCurrentYearMonth(): String = yearMonthFormat.format(Date())

    fun formatYearMonth(ym: String): String {
        return try {
            val date = yearMonthFormat.parse(ym)
            if (date != null) monthYearDisplayFormat.format(date) else ym
        } catch (_: Exception) {
            ym
        }
    }

    fun formatYearMonthShort(ym: String): String {
        return try {
            val date = yearMonthFormat.parse(ym)
            if (date != null) monthShortDisplayFormat.format(date) else ym
        } catch (_: Exception) {
            ym
        }
    }

    fun formatIsoToDayMonth(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate)
            if (date != null) dayMonthFormat.format(date) else isoDate
        } catch (_: Exception) {
            isoDate
        }
    }

    fun getPreviousMonth(ym: String): String {
        return try {
            val date = yearMonthFormat.parse(ym) ?: return ym
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MONTH, -1)
            yearMonthFormat.format(cal.time)
        } catch (_: Exception) {
            ym
        }
    }

    fun getNextMonth(ym: String): String {
        return try {
            val date = yearMonthFormat.parse(ym) ?: return ym
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MONTH, 1)
            yearMonthFormat.format(cal.time)
        } catch (_: Exception) {
            ym
        }
    }

    fun getPastMonthsList(count: Int): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        for (i in 0 until count) {
            val copy = cal.clone() as Calendar
            copy.add(Calendar.MONTH, -i)
            list.add(yearMonthFormat.format(copy.time))
        }
        return list
    }
}
