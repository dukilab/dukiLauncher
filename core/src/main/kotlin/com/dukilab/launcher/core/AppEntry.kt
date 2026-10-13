package com.dukilab.launcher.core

/**
 * ランチャーに並べるアプリ1件分の情報。
 *
 * [key] はアプリを一意に表す文字列（Androidでは ComponentName を平らにしたもの）で、
 * ドックの保存などに使う。
 */
data class AppEntry(
    val key: String,
    val label: String,
    val packageName: String,
)
