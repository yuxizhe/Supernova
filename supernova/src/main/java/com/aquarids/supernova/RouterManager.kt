package com.aquarids.supernova

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.aquarids.transmitter.Portal

/**
 * Created by Zhaoqi Wang
 * on 2018/3/16.
 */
object RouteManager {

    var mDeepLinkPrefix = "default"

    var mScheme = "default"

    fun open(context: Context, url: String?) {
        open(context, url, null)
    }

    fun open(context: Context, url: String?, requestCode: Int? = null) {
        if (url.isNullOrEmpty()) {
            return
        }
        if (!openNative(context, url!!, requestCode)) {
            if (url.startsWith("http") || url.startsWith("https")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        }
    }


    fun openNative(context: Context, url: String?, requestCode: Int? = null): Boolean {
        if (url.isNullOrEmpty()) {
            return false
        }
        return Portal.open(context, getPath(url!!), requestCode)
    }

    fun isValidUrl(url: String): Boolean {
        var uri: Uri? = null
        try {
            uri = Uri.parse(url)
        } catch (e: Exception) {
            return false
        }

        return isValidUri(uri)
    }

    fun isValidUri(uri: Uri?): Boolean {
        return uri?.scheme in arrayListOf("http", "https", mScheme)
    }

    fun getPath(url: String): String {
        val uri = url.toUri() ?: return ""
        val host = uri.host
        if (!host.isNullOrEmpty() && host.contains(mDeepLinkPrefix)) {
            return url.replace("$mDeepLinkPrefix/", "")
        }

        return url
    }

}
