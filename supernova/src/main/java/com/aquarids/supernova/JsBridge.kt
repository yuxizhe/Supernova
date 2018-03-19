package com.aquarids.supernova

import android.support.annotation.MainThread
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import java.util.HashMap

/**
 * Created by Zhaoqi Wang
 * on 2018/3/19.
 */
class JsBridge(webView: WebView, trustHost: String) {

    private val mWebView = webView
    private val mTrustHost = trustHost
    private val mHandlers = HashMap<String, JsHandler>()

    @JavascriptInterface
    fun execute(method: String, params: String, callback: String) {
        mWebView.post {
            if (!isValidHost()) {
                return@post
            }
            val handler = mHandlers[method] ?: return@post
            try {
                handler.handle(params, object : JsHandler.Response() {
                    override fun resolve(res: String) {
                        mWebView.post { mWebView.loadUrl(String.format("javascript:window.supernovaBridge.callback('%s', %s, '%s')", callback, true, res)) }
                    }

                    override fun reject(error: String) {
                        mWebView.post { mWebView.loadUrl(String.format("javascript:window.supernovaBridge.callback('%s', %s, '%s')", callback, false, error)) }
                    }
                })
            } catch (e: Exception) {
                Log.e("JsBridge Error", e.message)
                mWebView.loadUrl(String.format("javascript:window.stBridge.callback('%s', %s, '%s')", callback, false, e.cause))
            }
        }
    }

    @MainThread
    private fun isValidHost(): Boolean {
        val uri = mWebView.url.toUri() ?: return false
        return mTrustHost == uri.host
    }

    fun registerHandler(method: String, jsHandler: JsHandler) {
        mHandlers[method] = jsHandler
    }
}