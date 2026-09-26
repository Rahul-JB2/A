package com.example

import com.example.util.DateUtils
import com.example.util.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPasswordHashing() {
        val hash1 = SecurityUtils.hashPassword("password123")
        val hash2 = SecurityUtils.hashPassword("password123")
        val hashDiff = SecurityUtils.hashPassword("differentPassword")

        assertEquals(hash1, hash2)
        assertNotEquals(hash1, hashDiff)
        assertTrue(hash1.length >= 64)
    }

    @Test
    fun testDateFormatting() {
        val today = DateUtils.getTodayIso()
        assertTrue(today.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))

        val formatted = DateUtils.formatIsoToDisplay("2026-09-23")
        assertTrue(formatted.contains("2026"))
    }

    @Test
    fun testAttendancePercentageFormula() {
        // (Present + Late) / Total * 100
        val present = 16
        val late = 2
        val total = 20
        val attended = present + late
        val percentage = (attended.toFloat() / total) * 100f
        assertEquals(90.0f, percentage, 0.01f)
    }
}
