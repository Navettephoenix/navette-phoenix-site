package com.phoenix.navette.network

import com.phoenix.navette.BuildConfig
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

object NavetteApi {
    private const val WEBHOOK_URL = "https://example.com/your-apps-script-webhook"
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    fun isConfigured(): Boolean = !WEBHOOK_URL.contains("example.com")

    fun post(
        type: String,
        fields: Map<String, Any?>,
        callback: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        if (!isConfigured()) {
            callback(false, null)
            return
        }

        val payload = JSONObject()
        payload.put("type", type)
        fields.forEach { (key, value) ->
            when (value) {
                null -> payload.put(key, JSONObject.NULL)
                is Number, is Boolean -> payload.put(key, value)
                else -> payload.put(key, value.toString())
            }
        }
        if (BuildConfig.NP_TOKEN.isNotBlank()) {
            payload.put("token", BuildConfig.NP_TOKEN)
        }

        val body = payload.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(WEBHOOK_URL)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.localizedMessage)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        callback(true, null)
                    } else {
                        callback(false, it.message)
                    }
                }
            }
        })
    }
}
