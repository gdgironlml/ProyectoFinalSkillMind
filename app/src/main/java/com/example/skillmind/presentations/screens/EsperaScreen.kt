package com.example.skillmind.presentations.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.skillmind.models.Jugador
import com.example.skillmind.presentations.components.ActionButton
import com.example.skillmind.presentations.components.FullScreenLoading
import com.example.skillmind.presentations.components.SkillMindDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EsperaScreen(
    navController: NavController,
    codigoSala: String,
    esHost: Boolean,
    nombreUsuario: String
) {
    val jugadores = remember { mutableStateListOf<Jugador>() }
    var hostId by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var isLeaving by remember { mutableStateOf(false) } // Estado para mostrar loading al salir
    var showHostLeftDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val salaRef = db.collection("salas").document(codigoSala)
    val jugadoresRef = salaRef.collection("jugadores")
    val currentUser = Firebase.auth.currentUser

    DisposableEffect(key1 = codigoSala) {
        var juegoIniciado = false

        val salaListener = salaRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("EsperaScreen", "Error al escuchar la sala: $e")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                if (!snapshot.exists() && !esHost) {
                    showHostLeftDialog = true
                    return@addSnapshotListener
                }
                hostId = snapshot.getString("hostId")
                val estado = snapshot.getString("estado")
                if (estado == "en_juego") {
                    juegoIniciado = true
                    navController.navigate("juego_screen/$codigoSala") { popUpTo("salas") }
                }
            }
        }

        val jugadoresListener = jugadoresRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("EsperaScreen", "Error al escuchar jugadores: $e")
                cargando = false
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val nuevosJugadores = snapshot.documents.mapNotNull { it.toObject(Jugador::class.java) }
                val jugadoresOrdenados = nuevosJugadores.sortedWith(
                    compareBy<Jugador> { it.uid != hostId }.thenBy { it.nombre }
                )
                jugadores.clear()
                jugadores.addAll(jugadoresOrdenados)
                cargando = false
            }
        }

        onDispose {
            salaListener.remove()
            jugadoresListener.remove()

            // This will run if the user leaves via system back gesture
            GlobalScope.launch {
                if (!juegoIniciado) {
                    val uid = currentUser?.uid ?: return@launch
                    try {
                        db.runTransaction { transaction ->
                            val salaSnapshot = transaction.get(salaRef)
                            if (!salaSnapshot.exists()) {
                                return@runTransaction null
                            }

                            if (esHost) {
                                transaction.delete(salaRef)
                            } else {
                                val playerCount = salaSnapshot.getLong("playerCount") ?: 1L
                                val jugadorRef = jugadoresRef.document(uid)

                                if (transaction.get(jugadorRef).exists()) { // Check if player exists before acting
                                    transaction.delete(jugadorRef)
                                    if (playerCount <= 1) {
                                        transaction.delete(salaRef)
                                    } else {
                                        transaction.update(salaRef, "playerCount", playerCount - 1)
                                    }
                                }
                            }
                            null
                        }.await()
                    } catch (e: Exception) {
                        Log.e("EsperaScreen", "Error en la transacción de limpieza para $uid", e)
                    }
                }
            }
        }
    }

    if (showHostLeftDialog) {
        SkillMindDialog(
            onDismissRequest = { },
            title = "La sala ha sido cerrada",
            message = "El anfitrión ha cerrado la sala.",
            confirmButtonText = "OK",
            onConfirm = { navController.popBackStack() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sala de Espera") },
                navigationIcon = {
                    IconButton(onClick = { if (!isLeaving) navController.popBackStack() }) { // Prevent leaving while processing
                        Icon(Icons.Default.ArrowBack,
                            contentDescription = "Salir de la sala",
                            tint = MaterialTheme.colorScheme.tertiary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        FullScreenLoading(isLoading = cargando || isLeaving, text = if (isLeaving) "Saliendo de la sala..." else "Cargando sala...")

        if (!cargando) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¡Comparte este código con tus amigos!", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))

                val isDark = isSystemInDarkTheme()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if(isDark) Color(0xFF1C1B1F) else MaterialTheme.colorScheme.surfaceVariant),
                    border = if(isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary) else null
                ) {
                    Text(
                        text = codigoSala,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        letterSpacing = 8.sp
                    )
                }

                Spacer(Modifier.height(32.dp))
                Text(text = "Jugadores en la sala: ${jugadores.size}", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier.heightIn(max = 400.dp)) { 
                    LazyColumn() {
                        items(jugadores) { jugador ->
                            PlayerCard(jugador, esHost = jugador.uid == hostId)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (esHost) {
                    ActionButton(
                        text = "¡INICIAR JUEGO!",
                        onClick = {
                            scope.launch {
                                salaRef.update("estado", "en_juego")
                            }
                        },
                    )
                } else if (!showHostLeftDialog) {
                    Text("Esperando que el anfitrión inicie el juego...", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (!esHost) { 
                    ActionButton(
                        text = "Salir de la sala", 
                        onClick = {
                            if (isLeaving) return@ActionButton

                            scope.launch {
                                isLeaving = true
                                val uid = currentUser?.uid
                                if (uid != null) {
                                    try {
                                        db.runTransaction { transaction ->
                                            val salaSnapshot = transaction.get(salaRef)
                                            if (salaSnapshot.exists()) {
                                                val playerCount = salaSnapshot.getLong("playerCount") ?: 1L
                                                val jugadorRef = jugadoresRef.document(uid)
                                                if (transaction.get(jugadorRef).exists()) {
                                                    transaction.delete(jugadorRef)
                                                    if (playerCount <= 1) {
                                                        transaction.delete(salaRef)
                                                    } else {
                                                        transaction.update(salaRef, "playerCount", playerCount - 1)
                                                    }
                                                }
                                            }
                                            null
                                        }.await()
                                    } catch (e: Exception) {
                                        Log.e("EsperaScreen", "Error al salir manualmente de la sala", e)
                                    }
                                }
                                navController.popBackStack()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerCard(jugador: Jugador, esHost: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (esHost) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Anfitrión",
                    tint = if(isSystemInDarkTheme()) MaterialTheme.colorScheme.tertiary else Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = jugador.nombre, style = MaterialTheme.typography.titleMedium)
        }
    }
}
