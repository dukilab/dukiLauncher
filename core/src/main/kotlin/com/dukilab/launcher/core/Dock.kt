package com.dukilab.launcher.core

/** ホーム画面下のドック。よく使うアプリを最大 [MAX_SIZE] 個まで並べる。 */
data class Dock(val keys: List<String> = emptyList()) {

    val isFull: Boolean get() = keys.size >= MAX_SIZE

    operator fun contains(key: String): Boolean = key in keys

    /** 末尾に追加する。すでにある・いっぱいのときは何もしない。 */
    fun add(key: String): Dock =
        if (key in keys || isFull) this else copy(keys = keys + key)

    fun remove(key: String): Dock = copy(keys = keys - key)

    /** [key] を [offset] だけ動かす（負なら左へ）。端で止まる。 */
    fun move(key: String, offset: Int): Dock {
        val from = keys.indexOf(key)
        if (from < 0) return this
        val to = (from + offset).coerceIn(0, keys.lastIndex)
        if (to == from) return this
        val list = keys.toMutableList()
        list.removeAt(from)
        list.add(to, key)
        return copy(keys = list)
    }

    /** アンインストールなどで消えたアプリを取り除く。 */
    fun retainInstalled(installed: Set<String>): Dock =
        copy(keys = keys.filter { it in installed })

    /** ドックに並べる順のアプリ。見つからないキーは飛ばす。 */
    fun resolve(apps: List<AppEntry>): List<AppEntry> {
        val byKey = apps.associateBy { it.key }
        return keys.mapNotNull { byKey[it] }
    }

    fun serialize(): String = keys.joinToString(SEPARATOR)

    companion object {
        const val MAX_SIZE = 5
        private const val SEPARATOR = "\n"

        fun deserialize(text: String?): Dock =
            Dock(text.orEmpty().split(SEPARATOR).filter { it.isNotBlank() }.distinct().take(MAX_SIZE))
    }
}
