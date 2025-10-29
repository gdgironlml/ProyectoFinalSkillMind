package com.example.skillmind.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("models/gemini-2.0-flash:generateContent")
    suspend fun generate(
        @Header("X-goog-api-key") apiKey: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): ApiGenerateResponse
}