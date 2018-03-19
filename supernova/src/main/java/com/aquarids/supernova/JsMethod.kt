package com.aquarids.supernova

import org.json.JSONObject

class JsMethod {

    companion object {

        @JvmStatic
        fun registerFunc(page: Supernova) {

            page.registerHandler("toast", object : JsHandler() {
                @Throws(Exception::class)
                override fun handle(params: JSONObject, response: Response) {
                    val message = params.getString("message")
                    page.toast(message)
                }
            })
            page.registerHandler("setTitle", object : JsHandler() {
                @Throws(Exception::class)
                override fun handle(params: JSONObject, response: Response) {
                    val title = params.getString("title")
                    page.setTitle(title)
                }
            })
            page.registerHandler("setNavigationColor", object : JsHandler() {
                @Throws(Exception::class)
                override fun handle(params: JSONObject, response: Response) {
                    val color = params.getString("color")
                    page.setNavigationColor(color)
                }
            })
            page.registerHandler("showNavigation", object : JsHandler() {
                @Throws(Exception::class)
                override fun handle(params: JSONObject, response: Response) {
                    val show = params.getBoolean("show")
                    page.showNavigation(show)
                }
            })
            page.registerHandler("showLoadingDialog", object : JsHandler() {
                @Throws(Exception::class)
                override fun handle(params: JSONObject, response: Response) {
                    val show = params.getBoolean("show")
                    page.showLoadingDialog(show)
                }
            })
            page.registerHandler("setNavigationTextColor", object : JsHandler() {
                @Throws(Exception::class)
                override fun handle(params: JSONObject, response: Response) {
                    val color = params.getString("color")
                    page.setNavigationTextColor(color)
                }
            })
            page.registerHandler("openLink", object : JsHandler() {
                @Throws(Exception::class)
                override fun handle(params: JSONObject, response: Response) {
                    val link = params.getString("link")
                    page.openLink(link)
                }
            })
            page.registerHandler("close", object : JsHandler() {
                @Throws(Exception::class)
                override fun handle(params: JSONObject, response: Response) {
                    page.close()
                }
            })
        }
    }
}