package edu.uopeople.app.web

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.widget.Toast

class NativeJsBridge(context: Context) {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun postNotification(title: String, message: String) {
        val text = listOf(title, message)
            .filter { it.isNotBlank() }
            .joinToString(": ")
            .ifBlank { "UoPeople notification" }
        mainHandler.post {
            Toast.makeText(appContext, text, Toast.LENGTH_LONG).show()
        }
    }
}
