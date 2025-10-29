package com.example.skillmind.presentations.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.skillmind.models.JugadorResultado
import com.example.skillmind.presentations.components.ActionButton
import com.example.skillmind.presentations.components.FullScreenLoading
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.concurrent.TimeUnit


fun formatTiempo(millis: Long?): String {
    if (millis == null) return "--:--.---"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
    val milliseconds = millis - TimeUnit.SECONDS.toMillis(seconds) - TimeUnit.MINUTES.toMillis(minutes)
    return String.format(Locale.getDefault(), "%02d:%02d.%03d", minutes, seconds, milliseconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadosScreen(navController: NavController, codigoSala: String) {
    val db = FirebaseFirestore.getInstance()
    val auth = Firebase.auth
    val uid = auth.currentUser?.uid
    val salaRef = db.collection("salas").document(codigoSala)
    val scope = rememberCoroutineScope()
    
    var jugadores by remember { mutableStateOf<List<JugadorResultado>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var isLeaving by remember { mutableStateOf(false) }

    val leaveRoomAndNavigate = suspend {
        if (!isLeaving && uid != null) {
            isLeaving = true
            try {
                db.runTransaction { transaction ->
                    val salaSnapshot = transaction.get(salaRef)
                    if (!salaSnapshot.exists()) return@runTransaction null

                    val currentCount = salaSnapshot.getLong("playerCount") ?: 1L

                    if (currentCount > 1) {
                        transaction.update(salaRef, "playerCount", currentCount - 1)
                    } else {
                        transaction.delete(salaRef)
                    }
                    null
                }.await()
            } catch (e: Exception) {
                Log.e("ResultadosScreen", "Error cleaning up room.", e)
            }
            
            // Navegamos DESPUÉS de que la transacción haya terminado.
            navController.navigate("salas") { 
                popUpTo("salas") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    BackHandler { 
        scope.launch {
            leaveRoomAndNavigate()
        }
    }

    DisposableEffect(key1 = codigoSala) {
        val jugadoresListener = salaRef.collection("jugadores").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) {
                cargando = false
                return@addSnapshotListener
            }
            
            val listaJugadores = snapshot.documents.mapNotNull { it.toObject(JugadorResultado::class.java) }
            
            jugadores = listaJugadores.sortedWith(
                compareByDescending<JugadorResultado> { it.puntos }
                    .thenBy { it.tiempoTotal ?: Long.MAX_VALUE }
            )
            cargando = false
        }

        onDispose {
            jugadoresListener.remove()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        FullScreenLoading(isLoading = cargando || isLeaving, text = if (isLeaving) "Saliendo de la sala..." else "Cargando resultados...")
        if(!cargando) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Podio", modifier = Modifier.size(64.dp), tint = Color(0xFFFFD700))
                    Spacer(Modifier.height(16.dp))
                    Text("Resultados Finales", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    val jugadoresTerminados = jugadores.count { it.tiempoTotal != null }
                    if (jugadores.isNotEmpty() && jugadoresTerminados < jugadores.size) {
                        Text("Esperando a que terminen los demás... ($jugadoresTerminados/${jugadores.size})", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))
                    }

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(jugadores) { index, jugador ->
                            PlayerResultCard(index = index, jugador = jugador)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    ActionButton(
                        text = "Salir",
                        onClick = {
                            scope.launch {
                                leaveRoomAndNavigate()
                            }
                        },
                        enabled = !isLeaving
                    )
                
            }
        }
    }
}

@Composable
fun PlayerResultCard(index: Int, jugador: JugadorResultado) {
    val isDark = isSystemInDarkTheme()
    val position = index + 1

    val borderColor = when {
        position == 1 -> Color(0xFFFFD700)
        isDark -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        position == 2 -> Color(0xFFC0C0C0)
        position == 3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (position == 1) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$position. ${jugador.nombre}", 
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text("${jugador.puntos} pts", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(formatTiempo(jugador.tiempoTotal), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}
