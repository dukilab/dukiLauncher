package com.dukilab.launcher.core

/** 保存しておくランチャーの状態（配置・ドック・非表示・把握済みのアプリ）。 */
data class SavedState(
    val layout: HomeLayout = HomeLayout(),
    val dock: Dock = Dock(),
    val hidden: Set<String> = emptySet(),
    /** 前回までに見たアプリ。新しく入ったアプリを見分けるのに使う。 */
    val known: Set<String> = emptySet(),
    /** 一度でもアプリ一覧を読み込んだか。 */
    val initialized: Boolean = false,
) {
    /**
     * いま入っているアプリ [installed] に合わせて状態を更新する。
     *
     * - 初回は、いま入っているアプリをすべて「把握済み」にするだけで何も置かない
     * - 2回目以降は、新しく入ったアプリを [autoAdd] ならホーム画面の空きに置く
     * - アンインストールされたアプリは配置・ドック・非表示から取り除く
     *
     * 仕事用プロフィールが一時停止中などで、あるユーザーのアプリが1つも
     * 見えないときは、そのユーザーのアプリは取り除かない。
     */
    fun reconcile(installed: Set<String>, autoAdd: Boolean): SavedState {
        if (installed.isEmpty()) return this
        if (!initialized) return copy(known = installed, initialized = true)

        val visibleUsers = installed.map(::userOf).toSet()
        val keep: (String) -> Boolean = { it in installed || userOf(it) !in visibleUsers }
        val keepSet = (layout.appKeys + dock.appKeys + hidden + known).filter(keep).toSet()

        var nextLayout = layout.retainInstalled(keepSet + installed)
        if (autoAdd) {
            val added = installed - known
            for (key in added.sorted()) nextLayout = nextLayout.addApp(key)
        }
        return copy(
            layout = nextLayout,
            dock = dock.retainInstalled(keepSet + installed),
            hidden = hidden.filter(keep).toSet(),
            known = installed + known.filter { userOf(it) !in visibleUsers },
        )
    }

    companion object {
        /** キー `パッケージ/アクティビティ#ユーザー` のユーザー部分。 */
        fun userOf(key: String): String = key.substringAfterLast('#', "")
    }
}
