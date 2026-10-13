package com.dukilab.launcher.core

import java.text.Collator
import java.text.Normalizer
import java.util.Locale

/** アプリ一覧の並べ替えと検索。 */
object AppSearch {

    private val collator: Collator = Collator.getInstance(Locale.JAPANESE).apply {
        strength = Collator.SECONDARY
    }

    /** 名前順（日本語の読み順に近い並び）。同じ名前ならパッケージ名で並べる。 */
    fun sort(apps: List<AppEntry>): List<AppEntry> =
        apps.sortedWith { a, b ->
            val byLabel = collator.compare(normalize(a.label), normalize(b.label))
            if (byLabel != 0) byLabel else a.packageName.compareTo(b.packageName)
        }

    /**
     * [query] で絞り込む。空白区切りの語をすべて含むアプリを返す（AND検索）。
     * 名前の先頭に一致するものを先に、それ以外は元の順のまま並べる。
     */
    fun filter(apps: List<AppEntry>, query: String): List<AppEntry> {
        val terms = normalize(query).split(' ').filter { it.isNotEmpty() }
        if (terms.isEmpty()) return apps
        val matched = apps.filter { app ->
            val label = normalize(app.label)
            val pkg = app.packageName.lowercase(Locale.ROOT)
            terms.all { label.contains(it) || pkg.contains(it) }
        }
        val first = terms.first()
        return matched.sortedBy { if (normalize(it.label).startsWith(first)) 0 else 1 }
    }

    /**
     * 検索用に文字をそろえる。
     * 全角英数→半角（NFKC）、大文字→小文字、カタカナ→ひらがな、連続する空白→1つ。
     */
    fun normalize(text: String): String {
        val nfkc = Normalizer.normalize(text, Normalizer.Form.NFKC).lowercase(Locale.ROOT)
        val sb = StringBuilder(nfkc.length)
        for (ch in nfkc) {
            sb.append(if (ch in 'ァ'..'ヶ') ch - 0x60 else ch)
        }
        return sb.toString().trim().replace(Regex("\\s+"), " ")
    }
}
