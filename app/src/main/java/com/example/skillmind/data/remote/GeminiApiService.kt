package com.example.skillmind.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class ContentPart(val text: String)
data class ContentItem(val parts: List<ContentPart>)
data class GenerateRequest(val contents: List<ContentItem>)

data class GenerateResponse(val candidates: List<GeminiCandidate>)
data class GeminiCandidate(val content: String)

interface GeminiApiService {
    @POST("models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Header("X-goog-api-key") apiKey: String,
        @Body request: GenerateRequest
    ): GenerateResponse
}