# SenSketch

一筆書きで線を描くAndroidアプリです。画面下部の左右コントローラーを両手で操作し、カーソルを動かして描きます。

## 現在の状態

- [x] Androidプロジェクトにお絵かき画面を組み込み
- [x] 暗い背景に一筆の白線と水色の始点を描いたAndroidランチャーアイコンを追加
- [x] 描画内容・カーソル位置・線の設定を保存し、次回起動時に復元
- [x] 左側に左右操作、右側に上下操作の大きなタッチ領域を配置
- [x] 白い線を初期色に設定し、色を切り替え
- [x] 細線／太線、遅い／速い速度を切り替え
- [x] ネオン効果のON／OFF（初期状態OFF）
- [x] クリア、カーソル位置マーカー、描画画像のPNG保存
- [ ] Android端末で操作と画像保存を確認

## Android Studioで起動

1. このフォルダーをAndroid Studioで開き、Gradle同期が終わるまで待ちます。
2. Android端末またはエミュレーターを選択します。
3. `app` のRun構成で起動します。

PNG保存ボタンを押すと、Androidの保存先選択画面が開きます。

## ファイル構成

- `app/src/main/java/com/matsuge/gm/sensketch/MainActivity.kt` — WebView表示とAndroidへのPNG保存連携
- `app/src/main/res/layout/activity_main.xml` — アプリ画面
- `docs/index.html` — 描画画面・操作・描画処理の共通ソース
- `docs/favicon.svg` — Web版のタブアイコン

AndroidアプリはGradle設定で `docs` をアセットとして読み込みます。描画機能を変更するときは `docs/index.html` を編集すると、Android版とGitHub Pages版の両方に反映されます。

## GitHub Pages

リポジトリの **Settings → Pages** で、公開元に `master` ブランチの `/docs` フォルダーを指定します。公開されたアプリはURLを知っている人がアクセスできます。
