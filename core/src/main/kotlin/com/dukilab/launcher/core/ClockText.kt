package com.dukilab.launcher.core

import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

/** ホーム画面の時計と日付の文字列。 */
object ClockText {

    /** `06:32` の形。24時間表記。 */
    fun time(now: LocalDateTime): String =
        String.format(Locale.ROOT, "%02d:%02d", now.hour, now.minute)

    /** `2026.10.13 TUE` の形。 */
    fun date(now: LocalDateTime): String {
        val dow = now.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase(Locale.ROOT)
        return String.format(Locale.ROOT, "%04d.%02d.%02d %s", now.year, now.monthValue, now.dayOfMonth, dow)
    }

    /** 次の分になるまでのミリ秒。時計の更新を分の切り替わりに合わせるのに使う。 */
    fun millisUntilNextMinute(now: LocalDateTime): Long {
        val intoMinute = now.second * 1000L + now.nano / 1_000_000L
        return 60_000L - intoMinute
    }
}
