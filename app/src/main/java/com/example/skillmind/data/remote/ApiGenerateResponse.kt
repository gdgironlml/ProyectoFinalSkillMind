package com.example.skillmind.data.remote

data class ApiGenerateResponse(
    val candidates: List<Candidate>? = null
)

data class Candidate(
    val content: Content? = null
)

data class Content(
    val parts: List<Part>? = null
)

data class Part(
    val text: String? = null
)