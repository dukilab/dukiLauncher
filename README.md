# dukiLauncher

dukiLab.のデザインシステムを取り入れたAndroidのホームアプリ（ランチャー）です。

モノクロ、角丸なし、左上の45°三角マーク、方眼の背景で組んでいます。色・書体・寸法はデザインシステム（Artifact「dukiLab.」）のトークンをそのまま移植しています。

## できること（0.1.0）

- ホーム画面：時計と日付、よく使うアプリのドック（最大5個）
- アプリ一覧：上にスワイプか「アプリ一覧」ボタンで開く。検索はひらがな・カタカナ・全角半角を区別しません
- 長押しメニュー：ドックに追加／外す、ドック内の並べ替え、アプリ情報、アンインストール
- 設定：テーマ（端末に合わせる・ライト・ダーク）、アイコンのモノクロ表示、既定のホームアプリの選択

## 構成

| モジュール | 中身 |
| --- | --- |
| `:core` | 検索・並べ替え・ドック・時計表記などのロジック（Android非依存、JVMで単体テスト） |
| `:app` | 画面（Jetpack Compose）とAndroidとのつなぎ |

`app/src/main/java/com/dukilab/launcher/ui/theme/` にデザイントークン、`ui/components/` にカード・ボタン・タグ・ヘッダーなどの部品があります。

## ビルドとテスト

Android Studio（JDK 17以上）で開くか、コマンドで実行します。

```
./gradlew :core:test                # ロジックの単体テスト
./gradlew :app:testDebugUnitTest    # 画面のテスト（Robolectric）
./gradlew :app:assembleDebug        # APKを作る（app/build/outputs/apk/debug/）
```

## フォント

- M PLUS 2 / M PLUS 1 Code：SIL Open Font License 1.1（`licenses/OFL-MPLUS.txt`）
- dukiLab Display：dukiLab.のロゴ用フォント
