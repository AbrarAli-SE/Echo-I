package com.example.echoi

import okhttp3.*
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import android.util.Log

class ApiClient {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val apiKey = BuildConfig.OPENAI_API_KEY  // Access API key securely

    fun sendTextToApi(text: String, callback: (String?) -> Unit) {
        val url = "https://api.openai.com/v1/completions"

        try {
            Log.d("ApiClient", "Creating JSON payload")
            val json = """
                {
                    "model": "gpt-3.5-turbo",
                    "prompt": "$text",
                    "max_tokens": 100
                }
            """.trimIndent()

            Log.d("ApiClient", "Creating request body")
            val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json)

            Log.d("ApiClient", "Building request")
            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer $apiKey")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("ApiClient", "Request failed: ${e.message}")
                    callback(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            Log.e("ApiClient", "Response code: ${response.code}")
                            Log.e("ApiClient", "Response body: ${response.body?.string()}")
                            callback(null)
                        } else {
                            try {
                                val responseBody = response.body?.string()
                                Log.d("ApiClient", "Response body: $responseBody")
                                val result = gson.fromJson(responseBody, OpenAiResponse::class.java)
                                callback(result.choices.firstOrNull()?.text)
                            } catch (e: Exception) {
                                Log.e("ApiClient", "Error parsing response: ${e.message}")
                                callback(null)
                            }
                        }
                    }
                }
            })

        } catch (e: Exception) {
            Log.e("ApiClient", "Error creating request: ${e.message}")
            callback(null)
        }
    }

    data class OpenAiResponse(val choices: List<Choice>) {
        data class Choice(val text: String)
    }
}
