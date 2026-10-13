package com.dukilab.launcher.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DockTest {

    @Test
    fun add_appendsAndIgnoresDuplicates() {
        val dock = Dock().add("a").add("b").add("a")
        assertEquals(listOf("a", "b"), dock.keys)
    }

    @Test
    fun add_stopsAtMaxSize() {
        var dock = Dock()
        for (i in 1..7) dock = dock.add("k$i")
        assertEquals(Dock.MAX_SIZE, dock.keys.size)
        assertTrue(dock.isFull)
        assertFalse("k6" in dock)
    }

    @Test
    fun remove_dropsKey() {
        assertEquals(listOf("b"), Dock(listOf("a", "b")).remove("a").keys)
    }

    @Test
    fun move_shiftsAndClampsAtEdges() {
        val dock = Dock(listOf("a", "b", "c"))
        assertEquals(listOf("b", "a", "c"), dock.move("a", 1).keys)
        assertEquals(listOf("c", "a", "b"), dock.move("c", -5).keys)
        assertEquals(dock, dock.move("a", -1))
        assertEquals(dock, dock.move("missing", 1))
    }

    @Test
    fun retainInstalled_dropsUninstalled() {
        assertEquals(listOf("a", "c"), Dock(listOf("a", "b", "c")).retainInstalled(setOf("a", "c")).keys)
    }

    @Test
    fun resolve_followsDockOrderAndSkipsMissing() {
        val apps = listOf(
            AppEntry("x", "X", "p.x"),
            AppEntry("y", "Y", "p.y"),
        )
        val resolved = Dock(listOf("y", "gone", "x")).resolve(apps)
        assertEquals(listOf("y", "x"), resolved.map { it.key })
    }

    @Test
    fun serialize_roundTrips() {
        val dock = Dock(listOf("com.a/.Main", "com.b/com.b.Launch"))
        assertEquals(dock, Dock.deserialize(dock.serialize()))
    }

    @Test
    fun deserialize_handlesNullBlankAndDuplicates() {
        assertEquals(Dock(), Dock.deserialize(null))
        assertEquals(Dock(), Dock.deserialize(""))
        assertEquals(listOf("a", "b"), Dock.deserialize("a\n\na\nb").keys)
    }
}
