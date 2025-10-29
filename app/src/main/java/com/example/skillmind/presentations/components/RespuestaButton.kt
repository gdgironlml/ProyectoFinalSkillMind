package com.example.skillmind.presentations.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.skillmind.models.Pregunta

@Composable
fun RespuestaButton(
    opcion: String,
    preguntaActual: Pregunta,
    seleccionUsuario: String?,
    respuestaEnviada: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val esSeleccionada = seleccionUsuario?.equals(opcion, ignoreCase = true) == true
    val esCorrecta = preguntaActual.correcta.equals(opcion, ignoreCase = true)

    val targetColor = when {
        respuestaEnviada && esCorrecta -> if(isDark) Color(0xFF00FFAB).copy(alpha = 0.4f) else Color(0xFFC8E6C9)
        respuestaEnviada && esSeleccionada && !esCorrecta -> if(isDark) Color(0xFFFF5252).copy(alpha = 0.4f) else Color(0xFFFFCDD2)
        esSeleccionada && !isDark -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> if (isDark) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface
    }
    val animatedColor by animateColorAsState(targetColor, tween(300), label = "colorAnimation")
    
    val border = when {
        esSeleccionada && !respuestaEnviada -> {
            BorderStroke(2.dp, if (isDark) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
        }
        !isDark -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        else -> BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
    }

    Surface(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = animatedColor,
        border = border
    ) {
        Text(
            opcion, 
            Modifier.padding(16.dp), 
            textAlign = TextAlign.Center, 
            fontWeight = FontWeight.SemiBold
        )
    }
}
