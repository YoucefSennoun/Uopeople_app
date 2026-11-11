package edu.uopeople.app

import android.app.Application
import android.webkit.CookieManager

class UopApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CookieManager.getInstance().setAcceptCookie(true)
    }
}
