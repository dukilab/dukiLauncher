package com.dukilab.launcher.core

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSearchTest {

    private fun app(label: String, pkg: String = "pkg." + label.hashCode()) =
        AppEntry(key = "$pkg/.Main", label = label, packageName = pkg)

    private val camera = app("カメラ", "com.android.camera")
    private val chrome = app("Chrome", "com.android.chrome")
    private val clock = app("時計", "com.google.android.deskclock")
    private val calc = app("電卓", "com.android.calculator")
    private val settings = app("設定", "com.android.settings")
    private val youtube = app("YouTube", "com.google.android.youtube")

    @Test
    fun normalize_foldsWidthCaseAndKatakana() {
        assertEquals("youtube", AppSearch.normalize("ＹｏｕＴｕｂｅ"))
        assertEquals("かめら", AppSearch.normalize("カメラ"))
        assertEquals("ｶﾒﾗ".let { AppSearch.normalize(it) }, "かめら")
        assertEquals("a b", AppSearch.normalize("  a　 b  "))
    }

    @Test
    fun filter_emptyQueryKeepsAll() {
        val apps = listOf(camera, chrome)
        assertEquals(apps, AppSearch.filter(apps, "   "))
    }

    @Test
    fun filter_matchesHiraganaAgainstKatakanaLabel() {
        assertEquals(listOf(camera), AppSearch.filter(listOf(camera, chrome, clock), "かめ"))
    }

    @Test
    fun filter_matchesPackageName() {
        assertEquals(listOf(clock), AppSearch.filter(listOf(camera, clock), "deskclock"))
    }

    @Test
    fun filter_allTermsMustMatch() {
        val apps = listOf(chrome, youtube, settings)
        assertEquals(listOf(youtube), AppSearch.filter(apps, "google you"))
    }

    @Test
    fun filter_prefixMatchesComeFirst() {
        val calendar = app("Googleカレンダー", "com.google.android.calendar")
        val cal = app("カレンダー", "com.example.cal")
        assertEquals(listOf(cal, calendar), AppSearch.filter(listOf(calendar, cal), "カレ"))
    }

    @Test
    fun filter_noMatchReturnsEmpty() {
        assertEquals(emptyList<AppEntry>(), AppSearch.filter(listOf(camera, chrome), "zzz"))
    }

    @Test
    fun sort_ordersByLabelThenPackage() {
        val a1 = app("Alpha", "b.pkg")
        val a2 = app("alpha", "a.pkg")
        val beta = app("Beta", "c.pkg")
        val sorted = AppSearch.sort(listOf(beta, a1, a2))
        assertEquals(listOf(a2, a1, beta), sorted)
    }

    @Test
    fun sort_putsLatinBeforeKana() {
        val sorted = AppSearch.sort(listOf(calc, camera, chrome))
        assertEquals(chrome, sorted.first())
    }
}
