package com.dukilab.launcher.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class CodecAndStateTest {
    @Test
    fun layoutRoundTrip() {
        val l = HomeLayout(4, 5)
            .addApp("com.a/Main#0").addApp("com.b/Main#0").addApp("com.c/Main#0")
            .move(Cell(0, 1), Cell(0, 0)) { "f1" }
            .renameFolder("f1", "タブ\tと%と\n改行")
        assertEquals(l, LayoutCodec.decode(LayoutCodec.encode(l)))
    }

    @Test
    fun brokenTextGivesEmptyLayout() {
        assertEquals(HomeLayout(), LayoutCodec.decode(null))
        assertEquals(HomeLayout(), LayoutCodec.decode("v9\t4\t5"))
        val l = LayoutCodec.decode("v1\t2\t2\nA\t0\t9\tx\nA\tzz\t0\ty\nA\t0\t1\tz")
        assertEquals(mapOf(Cell(0, 1) to HomeItem.App("z")), l.items)
    }

    @Test
    fun listRoundTrip() {
        val list = listOf("a\tb", "c%d", "")
        assertEquals(list, LayoutCodec.decodeList(LayoutCodec.encodeList(list)))
        assertEquals(emptyList<String>(), LayoutCodec.decodeList(""))
    }

    @Test
    fun dockLimitsAndMoves() {
        val d = (1..7).fold(Dock()) { acc, i -> acc.add("k$i") }
        assertEquals(Dock.MAX, d.appKeys.size)
        assertTrue(d.isFull)
        assertEquals(listOf("k3", "k1", "k2", "k4", "k5"), d.move(2, 0).appKeys)
        assertFalse(d.remove("k1").contains("k1"))
    }

    @Test
    fun clockText() {
        val t = LocalDateTime.of(2026, 10, 13, 7, 5)
        assertEquals("07:05", ClockText.time(t, true))
        assertEquals("7:05", ClockText.time(t, false))
        assertEquals("12:00", ClockText.time(t.withHour(0).withMinute(0), false))
        assertEquals("2026.10.13 TUE", ClockText.dateLine(t))
        assertEquals("APPS · 042", Tags.of("apps", 42))
    }

    @Test
    fun firstReconcileOnlyRemembersApps() {
        val s = SavedState().reconcile(setOf("a#0", "b#0"), autoAdd = true)
        assertTrue(s.initialized)
        assertEquals(setOf("a#0", "b#0"), s.known)
        assertTrue(s.layout.items.isEmpty())
    }

    @Test
    fun newAppsAreAddedAndRemovedOnesPruned() {
        val s0 = SavedState().reconcile(setOf("a#0", "b#0"), autoAdd = true)
        val s1 = s0.copy(layout = s0.layout.addApp("a#0"), dock = s0.dock.add("b#0"), hidden = setOf("b#0"))
            .reconcile(setOf("a#0", "c#0"), autoAdd = true)
        assertTrue(s1.layout.contains("c#0"))
        assertFalse(s1.dock.contains("b#0"))
        assertTrue(s1.hidden.isEmpty())
        assertEquals(setOf("a#0", "c#0"), s1.known)
    }

    @Test
    fun appsOfInvisibleUserAreKept() {
        val s0 = SavedState().reconcile(setOf("a#0", "w#10"), autoAdd = false)
        val s1 = s0.copy(layout = s0.layout.addApp("w#10")).reconcile(setOf("a#0"), autoAdd = false)
        assertTrue(s1.layout.contains("w#10"))
        assertTrue("w#10" in s1.known)
    }

    @Test
    fun emptyInstalledListChangesNothing() {
        val s0 = SavedState().reconcile(setOf("a#0"), autoAdd = false)
        val s = s0.copy(layout = s0.layout.addApp("a#0"))
        assertEquals(s, s.reconcile(emptySet(), autoAdd = true))
    }
}
