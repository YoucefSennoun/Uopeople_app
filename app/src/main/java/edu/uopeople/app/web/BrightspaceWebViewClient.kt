package edu.uopeople.app.web

import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class BrightspaceWebViewClient(
    private val routeUri: (Uri) -> Boolean
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
        routeUri(request.url)

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        view.evaluateJavascript(buildCssInjection(), null)
        view.evaluateJavascript(BRIDGE_HELPERS_JS, null)
        CookieManager.getInstance().flush()
    }

    private fun buildCssInjection(): String {
        val css = """
            #d2l_minibar,.d2l-minibar,.d2l-navbar,.d2l-navigation,.d2l-branding,
            [role=banner],.homepage-header,.d2l-homepage-header{
                display:none !important;
            }
            .d2l-max-width-wrapper,.d2l-page-max-width{
                max-width:100% !important;
            }
            html{
                -webkit-text-size-adjust:100%;
            }
            body{
                margin:0;
            }
        """.trimIndent().replace("\n", " ").replace("  ", " ")
        return """
            (function(){
                var id='uop-mobile-css';
                var existing=document.getElementById(id);
                if(existing){existing.remove();}
                var style=document.createElement('style');
                style.id=id;
                style.textContent='$css';
                (document.head||document.documentElement).appendChild(style);
            })();
        """.trimIndent()
    }

    companion object {
        private val BRIDGE_HELPERS_JS = """
            (function(){
                if(window.UopNative){
                    window.uopNotify=function(title,message){
                        window.UopNative.postNotification(String(title||''),String(message||''));
                    };
                }
            })();
        """.trimIndent()
    }
}
