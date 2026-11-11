package edu.uopeople.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import edu.uopeople.app.databinding.ActivityMainBinding
import edu.uopeople.app.web.BrightspaceChromeClient
import edu.uopeople.app.web.BrightspaceWebViewClient
import edu.uopeople.app.web.DomainRules
import edu.uopeople.app.web.NativeJsBridge

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var webChromeClient: BrightspaceChromeClient

    private val webView: WebView
        get() = binding.webView

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            webChromeClient.deliverFileChooserResult(result.resultCode, result.data)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureWebView()

        if (savedInstanceState == null || webView.restoreState(savedInstanceState) == null) {
            webView.loadUrl(HOME_URL)
        }

        onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) webView.goBack() else finish()
        }
    }

    private fun configureWebView() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            setSupportMultipleWindows(true)
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        webView.addJavascriptInterface(NativeJsBridge(this), JS_BRIDGE_NAME)

        webView.webViewClient = BrightspaceWebViewClient(::routeUri)
        webChromeClient = BrightspaceChromeClient(
            onProgressChanged = ::updateProgress,
            launchFileChooser = fileChooserLauncher::launch,
            routeUri = ::routeUri,
            loadInMainView = ::loadInWebView
        )
        webView.webChromeClient = webChromeClient
    }

    private fun routeUri(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase()
        return when {
            scheme == null -> false
            scheme == "http" || scheme == "https" ->
                if (DomainRules.isInternalHost(uri.host)) false else openExternally(uri)
            scheme in DomainRules.EXTERNAL_SCHEMES -> openExternally(uri)
            else -> false
        }
    }

    private fun openExternally(uri: Uri): Boolean = try {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }

    private fun loadInWebView(url: String) {
        webView.loadUrl(url)
    }

    private fun updateProgress(progress: Int) {
        binding.progressBar.progress = progress
        binding.progressBar.isVisible = progress in 1 until 100
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
        CookieManager.getInstance().flush()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this::binding.isInitialized) webView.saveState(outState)
    }

    override fun onDestroy() {
        webView.apply {
            (parent as? ViewGroup)?.removeView(this)
            destroy()
        }
        super.onDestroy()
    }

    companion object {
        private const val HOME_URL = "https://portal.uopeople.edu/login"
        private const val JS_BRIDGE_NAME = "UopNative"
    }
}
