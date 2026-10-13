package com.dukilab.launcher.core

import java.util.Locale

/** dukiLab.のタグ表記（`HOMELAB · 014` の形）を作る。 */
object Tags {
    private const val DOT = " · "

    /** 分類と3桁の通し番号。番号が1000以上ならそのまま出す。 */
    fun of(category: String, number: Int? = null): String {
        val head = category.uppercase(Locale.ROOT)
        return if (number == null) head else head + DOT + number.coerceAtLeast(0).toString().padStart(3, '0')
    }
}
