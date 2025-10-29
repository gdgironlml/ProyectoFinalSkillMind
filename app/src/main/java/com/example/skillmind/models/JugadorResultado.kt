package com.example.skillmind.models

import androidx.annotation.Keep

@Keep
data class JugadorResultado(
    val nombre: String = "",
    val puntos: Long = 0L,
    val tiempoTotal: Long? = null
)
