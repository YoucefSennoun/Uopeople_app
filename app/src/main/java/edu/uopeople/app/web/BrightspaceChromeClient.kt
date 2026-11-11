package edu.uopeople.app.web

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class BrightspaceChromeClient(
    private val onProgressChanged: (Int) -> Unit,
    private val launchFileChooser: (Intent) -> Unit,
    private val routeUri: (Uri) -> Boolean,
    private val loadInMainView: (String) -> Unit
) : WebChromeClient() {

    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        onProgressChanged(newProgress)
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        this.fileChooserCallback?.onReceiveValue(null)
        this.fileChooserCallback = filePathCallback
        return try {
            launchFileChooser(fileChooserParams.createIntent())
            true
        } catch (e: ActivityNotFoundException) {
            this.fileChooserCallback = null
            false
        }
    }

    fun deliverFileChooserResult(resultCode: Int, data: Intent?) {
        val callback = fileChooserCallback ?: return
        fileChooserCallback = null
        callback.onReceiveValue(FileChooserParams.parseResult(resultCode, data))
    }

    override fun onCreateWindow(
        view: WebView,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message
    ): Boolean {
        val popup = WebView(view.context)
        popup.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                popupView: WebView,
                request: WebResourceRequest
            ): Boolean {
                val uri = request.url
                if (!routeUri(uri)) {
                    loadInMainView(uri.toString())
                }
                popupView.stopLoading()
                return true
            }
        }
        val transport = resultMsg.obj as WebView.WebViewTransport
        transport.webView = popup
        resultMsg.sendToTarget()
        return true
    }
}
