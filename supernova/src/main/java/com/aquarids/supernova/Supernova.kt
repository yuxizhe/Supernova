package com.aquarids.supernova

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast

class Supernova : AppCompatActivity(), SupernovaInterface {

    companion object {
        private const val PARAM_SHOW_NAV = "showNavigation"
        private const val PARAM_NAV_COLOR = "navigationColor"
        private const val PARAM_NAV_TEXT_COLOR = "navigationTextColor"
        private const val PARAM_TITLE = "title"

        private const val KEY_URL = "key_url"
        private const val KEY_TRUST_HOST = "key_trust_host"
        private const val KEY_USER_AGENT = "key_user_agent"

        @JvmStatic
        fun launchUrl(context: Context, url: String, trustHost: String, userAgent: String? = null) {
            val intent = Intent(context, Supernova::class.java)
            intent.putExtra(KEY_URL, url)
            intent.putExtra(KEY_TRUST_HOST, trustHost)
            intent.putExtra(KEY_USER_AGENT, userAgent ?: "")
            context.startActivity(intent)
        }
    }

    private var mToolbar: Toolbar? = null
    private var mSwipeRefresh: SwipeRefreshLayout? = null
    private var mWebView: WebView? = null
    private var mLoadingProgress: ProgressBar? = null
    private var mJsBridge: JsBridge? = null
    private val mUrl by lazy { intent.getStringExtra(KEY_URL) }
    private val mTrustHost by lazy { intent.getStringExtra(KEY_TRUST_HOST) }
    private val mUserAgent by lazy { intent.getStringExtra(KEY_USER_AGENT) }

    private val mLoadTimes = SparseArray<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_web_page)
        initView()
        initData()
    }

    private fun initView() {
        mToolbar = findViewById(R.id.toolbar)
        mSwipeRefresh = findViewById(R.id.swipe_refresh)
        mWebView = findViewById(R.id.web_view)
        mLoadingProgress = findViewById(R.id.loading_progress)

        mToolbar?.let {
            setSupportActionBar(it)
        }
        mToolbar?.setNavigationOnClickListener { goBack() }
        mToolbar?.title = ""
        mSwipeRefresh?.setOnRefreshListener {
            mWebView?.reload()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initData() {
        if (mUrl.isNullOrBlank()) {
            finish()
            return
        }

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        mWebView?.let { web ->
            web.settings?.let { settings ->
                web.requestFocus()

                settings.javaScriptEnabled = true
                mJsBridge = JsBridge(web, mTrustHost)
                mJsBridge?.let {
                    web.addJavascriptInterface(it, "supernovaJsHandler")
                }

                settings.setSupportZoom(false)
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.allowFileAccess = true
                settings.defaultTextEncodingName = "utf-8"
                settings.userAgentString = mUserAgent

                initJsBridge()
                initWebViewClient(web)
                initChromeClient(web)
                web.loadUrl(mUrl)
            }
        }
    }

    fun registerHandler(method: String, jsHandler: JsHandler) {
        mJsBridge?.registerHandler(method, jsHandler)
    }

    private fun initJsBridge() {
        JsMethod.registerFunc(this)
    }

    private fun initWebViewClient(webView: WebView) {
        val webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let {
                    mLoadTimes.append(it.hashCode(), System.currentTimeMillis())
                    if (RouteManager.openNative(this@Supernova, it, null)) {
                        return
                    }
                    configFromUrl(it)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                mSwipeRefresh?.isRefreshing = false
            }
        }
        webView.webViewClient = webViewClient
    }

    private fun initChromeClient(web: WebView) {
        val chromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                try {
                    super.onReceivedTitle(view, title)
                    mToolbar?.title = title
                } catch (e: Exception) {
                    Log.e("supernova", e.message)
                }

            }

            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                if (!this@Supernova.isDestroyed) {
                    val dialog = AlertDialog.Builder(this@Supernova)
                            .setMessage(message)
                            .setPositiveButton("confirm") { dialog, _ ->
                                dialog.dismiss()
                                result.confirm()
                            }
                            .setCancelable(false)
                            .create()
                    dialog.show()
                }
                return true
            }

            override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
                if (!this@Supernova.isDestroyed) {
                    val dialog = AlertDialog.Builder(this@Supernova)
                            .setMessage(message)
                            .setPositiveButton("confirm") { _, _ -> result.confirm() }
                            .setNegativeButton("cancel") { _, _ -> result.cancel() }
                            .setCancelable(false)
                            .create()
                    dialog.show()
                }
                return true
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                try {
                    super.onProgressChanged(view, newProgress)
                    mLoadingProgress?.let {
                        it.visibility = if (newProgress == 0 || newProgress == 100) View.GONE else View.VISIBLE
                        if (Build.VERSION.SDK_INT >= 24) {
                            it.setProgress(newProgress, true)
                        } else {
                            it.progress = newProgress
                        }
                    }
                } catch (e: Exception) {
                    Log.d("supernova", " onProgressChanged exception:" + e.message)
                }

            }
        }
        web.webChromeClient = chromeClient
    }


    private fun configFromUrl(url: String) {
        val uri: Uri
        try {
            uri = Uri.parse(url)
        } catch (e: Exception) {
            Log.e("supernova", e.message)
            return
        }

        try {
            val showNavigation = uri.getQueryParameter(PARAM_SHOW_NAV)
            if (showNavigation != null) {
                showNavigation("true" == showNavigation)
            }

            val navigationColor = uri.getQueryParameter(PARAM_NAV_COLOR)
            if (navigationColor != null) {
                setNavigationColor(navigationColor)
            }

            val navigationTextColor = uri.getQueryParameter(PARAM_NAV_TEXT_COLOR)
            if (navigationTextColor != null) {
                setNavigationTextColor(navigationTextColor)
            }

            val title = uri.getQueryParameter(PARAM_TITLE)
            if (title != null) {
                setTitle(title)
            }
        } catch (e: Exception) {
            Log.e("supernova", e.message)
        }
    }

    private fun goBack(): Boolean {
        return if (mWebView?.canGoBack().nullAsFalse()) {
            mWebView?.goBack()
            true
        } else {
            finish()
            false
        }
    }

    override fun setTitle(title: String) {
        mToolbar?.title = title
    }

    override fun setNavigationColor(color: String) {
        mToolbar?.setBackgroundColor(Color.parseColor(color))
    }

    override fun showNavigation(show: Boolean) {
        mToolbar?.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showLoadingDialog(show: Boolean) {
        mLoadingProgress?.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun setNavigationTextColor(color: String) {
        mToolbar?.setTitleTextColor(Color.parseColor(color))
    }

    override fun openLink(link: String) {
        if (!RouteManager.openNative(this, link)) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
        }
    }

    override fun close() {
        finish()
    }

    override fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}
