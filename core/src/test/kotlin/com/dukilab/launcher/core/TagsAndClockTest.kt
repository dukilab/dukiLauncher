package com.dukilab.launcher.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class TagsAndClockTest {

    @Test
    fun tag_formatsCategoryAndNumber() {
        assertEquals("APPS · 042", Tags.of("apps", 42))
        assertEquals("HOME", Tags.of("home"))
        assertEquals("APPS · 1234", Tags.of("APPS", 1234))
        assertEquals("APPS · 000", Tags.of("APPS", -3))
    }

    @Test
    fun clock_formatsTimeAndDate() {
        val now = LocalDateTime.of(2026, 10, 13, 6, 5, 9)
        assertEquals("06:05", ClockText.time(now))
        assertEquals("2026.10.13 TUE", ClockText.date(now))
    }

    @Test
    fun clock_millisUntilNextMinute() {
        val now = LocalDateTime.of(2026, 10, 13, 6, 5, 59, 500_000_000)
        assertEquals(500L, ClockText.millisUntilNextMinute(now))
        assertEquals(60_000L, ClockText.millisUntilNextMinute(LocalDateTime.of(2026, 1, 1, 0, 0, 0)))
    }
}
