package com.aquarids.supernova

import android.net.Uri
import android.util.Log

fun String?.toUri(): Uri? {
    if (this.isNullOrBlank()) {
        return null
    }

    var uri: Uri? = null
    try {
        uri = Uri.parse(this)
    } catch (e: Exception) {
        Log.e("utils: ", e.message)
    }

    return uri
}

fun Boolean?.nullAsFalse(): Boolean {
    return this ?: false
}