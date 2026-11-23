package com.matsuge.gm.sensketch

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Bundle
import android.util.AtomicFile
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    @Volatile
    private var pendingPngUri: Uri? = null
    private var backPressArmed = false
    private var exitToast: Toast? = null
    private val backPressHandler = Handler(Looper.getMainLooper())
    private val resetBackPress = Runnable {
        backPressArmed = false
        exitToast?.cancel()
        exitToast = null
    }
    private val stateLock = Any()
    private val drawingStateFile by lazy { AtomicFile(File(filesDir, "sensketch-state.json")) }

    private val createPngFile = registerForActivityResult(
        ActivityResultContracts.CreateDocument("image/png")
    ) { uri: Uri? ->
        if (uri == null) {
            pendingPngUri = null
            return@registerForActivityResult
        }
        pendingPngUri = uri
        webView.evaluateJavascript("window.sensketchExportPng && window.sensketchExportPng()", null)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
            val safeAreas = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(safeAreas.left, safeAreas.top, safeAreas.right, safeAreas.bottom)
            insets
        }

        webView = findViewById(R.id.webview)
        ViewCompat.requestApplyInsets(mainView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.isFocusable = true
        webView.isFocusableInTouchMode = true
        webView.overScrollMode = WebView.OVER_SCROLL_NEVER
        webView.webChromeClient = object : WebChromeClient() {
            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message ?: "絵をクリアします。よろしいですか？\n※この操作は戻せません。")
                    .setPositiveButton("クリア") { _, _ -> result.confirm() }
                    .setNegativeButton("キャンセル") { _, _ -> result.cancel() }
                    .setOnCancelListener { result.cancel() }
                    .show()
                return true
            }
        }
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(AndroidBridge(), "SenSketchAndroid")
        webView.loadUrl("file:///android_asset/index.html")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressArmed) {
                    backPressHandler.removeCallbacks(resetBackPress)
                    resetBackPress.run()
                    finish()
                } else {
                    backPressArmed = true
                    exitToast = Toast.makeText(this@MainActivity, "もう一度押すと終了します", Toast.LENGTH_SHORT)
                    exitToast?.show()
                    backPressHandler.removeCallbacks(resetBackPress)
                    backPressHandler.postDelayed(resetBackPress, 2_000L)
                }
            }
        })
    }

    override fun onDestroy() {
        backPressHandler.removeCallbacks(resetBackPress)
        exitToast?.cancel()
        exitToast = null
        if (::webView.isInitialized) {
            webView.removeJavascriptInterface("SenSketchAndroid")
            webView.destroy()
        }
        super.onDestroy()
    }

    private inner class AndroidBridge {
        @JavascriptInterface
        fun loadState(): String = synchronized(stateLock) {
            runCatching {
                drawingStateFile.openRead().bufferedReader(Charsets.UTF_8).use { it.readText() }
            }.getOrDefault("")
        }

        @JavascriptInterface
        fun saveState(serializedState: String) {
            if (serializedState.length > 32_000_000) return
            synchronized(stateLock) {
                var output: FileOutputStream? = null
                try {
                    val stream = drawingStateFile.startWrite()
                    output = stream
                    stream.write(serializedState.toByteArray(Charsets.UTF_8))
                    drawingStateFile.finishWrite(stream)
                } catch (_: Exception) {
                    output?.let { drawingStateFile.failWrite(it) }
                }
            }
        }

        @JavascriptInterface
        fun logDiagnostic(entry: String) {
            if (entry.length <= 12_000) Log.i("SenSketchDiag", entry)
        }

        @JavascriptInterface
        fun requestPngSave(requestedName: String) {
            val safeName = requestedName
                .substringAfterLast('/')
                .substringAfterLast('\\')
                .replace(Regex("[^A-Za-z0-9._-]"), "-")
                .ifBlank { "SenSketch.png" }
            runOnUiThread {
                pendingPngUri = null
                createPngFile.launch(safeName)
            }
        }

        @JavascriptInterface
        fun savePng(base64Image: String) {
            val image = runCatching { Base64.decode(base64Image, Base64.DEFAULT) }.getOrNull()
            if (image == null || image.isEmpty()) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "画像を作成できませんでした", Toast.LENGTH_LONG).show()
                }
                return
            }

            val uri = pendingPngUri
            pendingPngUri = null
            if (uri == null) return
            runCatching {
                contentResolver.openOutputStream(uri)?.use { it.write(image) }
                    ?: error("保存先を開けませんでした")
            }.onSuccess {
                runOnUiThread {
                    val toast = Toast.makeText(this@MainActivity, "画像を保存しました", Toast.LENGTH_SHORT)
                    toast.show()
                    Handler(Looper.getMainLooper()).postDelayed({ toast.cancel() }, 2_000L)
                }
            }.onFailure {
                runOnUiThread { Toast.makeText(this@MainActivity, "画像を保存できませんでした", Toast.LENGTH_LONG).show() }
            }
        }
    }
}
