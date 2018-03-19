package com.aquarids.supernova

import org.json.JSONObject

abstract class JsHandler {

    fun handle(request: String, response: Response) {
        var params = request
        if (params.isBlank()) {
            params = "{}"
        }
        val jsonObject = JSONObject(params)
        handle(jsonObject, response)
    }

    @Throws(Exception::class)
    abstract fun handle(params: JSONObject, response: Response)

    abstract class Response {
        abstract fun resolve(res: String)
        abstract fun reject(error: String)
    }
}