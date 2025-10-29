package com.example.skillmind.models

import androidx.annotation.Keep

@Keep
data class Pregunta(
    val pregunta: String = "",
    val opciones: List<String> = emptyList(),
    val correcta: String = ""
)
