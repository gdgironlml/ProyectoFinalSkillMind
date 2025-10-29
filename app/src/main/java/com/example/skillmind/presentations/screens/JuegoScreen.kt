package com.example.skillmind.presentations.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.skillmind.models.Pregunta
import com.example.skillmind.presentations.components.ActionButton
import com.example.skillmind.presentations.components.FullScreenLoading
import com.example.skillmind.presentations.components.RespuestaButton
import com.example.skillmind.presentations.components.SkillMindDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuegoScreen(navController: NavController, codigoSala: String) {
    val db = FirebaseFirestore.getInstance()
    val salaRef = db.collection("salas").document(codigoSala)
    val uid = Firebase.auth.currentUser?.uid
    val scope = rememberCoroutineScope()

    var preguntas by remember { mutableStateOf<List<Pregunta>>(emptyList()) }
    var preguntaActualIndex by remember { mutableStateOf(0) }
    var seleccionUsuario by remember { mutableStateOf<String?>(null) }
    var respuestaEnviada by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }
    var startTime by remember { mutableStateOf(0L) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showDeleteRoomDialog by remember { mutableStateOf(false) }
    var showHostLeftDialog by remember { mutableStateOf(false) }
    var esHost by remember { mutableStateOf(false) }
    val juegoFinalizado = remember { mutableStateOf(false) } 

    BackHandler(enabled = true) {
        showExitDialog = true
    }

    if (showExitDialog) {
        SkillMindDialog(
            onDismissRequest = { showExitDialog = false },
            title = "¿Seguro que quieres salir?",
            message = if (esHost) "Tu progreso se perderá. Como anfitrión, puedes cerrar la sala para todos." else "Si sales, tu progreso en la partida se perderá.",
            confirmButtonText = if (esHost) "SIGUIENTE" else "SALIR",
            onConfirm = {
                showExitDialog = false
                if (esHost) {
                    showDeleteRoomDialog = true
                } else {
                    navController.popBackStack()
                }
            },
            dismissButtonText = "CANCELAR",
            onDismiss = { showExitDialog = false }
        )
    }

    if (showDeleteRoomDialog) {
        SkillMindDialog(
            onDismissRequest = { showDeleteRoomDialog = false },
            title = "¿Cerrar la sala para todos?",
            message = "Como anfitrión, si sales, la sala se cerrará permanentemente para todos los jugadores.",
            confirmButtonText = "CERRAR SALA",
            onConfirm = {
                scope.launch {
                    try {
                        val jugadoresSnapshot = salaRef.collection("jugadores").get().await()
                        db.runBatch { batch ->
                            for (jugadorDoc in jugadoresSnapshot.documents) {
                                batch.delete(jugadorDoc.reference)
                            }
                            batch.delete(salaRef)
                        }.await()
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Log.e("JuegoScreen", "Error al eliminar la sala por completo", e)
                    }
                }
            },
            dismissButtonText = "CANCELAR",
            onDismiss = { showDeleteRoomDialog = false }
        )
    }

    if (showHostLeftDialog) {
        SkillMindDialog(
            onDismissRequest = { navController.popBackStack() },
            title = "La sala ha sido cerrada",
            message = "El anfitrión ha cerrado la sala. Serás devuelto a la pantalla principal.",
            confirmButtonText = "OK",
            onConfirm = { navController.popBackStack() }
        )
    }

    DisposableEffect(key1 = codigoSala) {
        val salaListener = salaRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("JuegoScreen", "Listener de sala falló.", e)
                cargando = false
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                if (cargando) { 
                    esHost = snapshot.getString("hostId") == uid
                    val preguntasData = snapshot.get("preguntas") as? List<HashMap<String, Any>>
                    if (preguntasData != null) {
                        preguntas = preguntasData.map { preguntaMap ->
                            Pregunta(
                                pregunta = (preguntaMap["pregunta"] as? String)?.trim() ?: "",
                                opciones = (preguntaMap["opciones"] as? List<String>)?.map { it.trim() } ?: emptyList(),
                                correcta = (preguntaMap["correcta"] as? String)?.trim() ?: ""
                            )
                        }
                        startTime = System.currentTimeMillis()
                    }
                    cargando = false
                }
            } else {
                if (!esHost) {
                    showHostLeftDialog = true
                }
            }
        }

        onDispose {
            salaListener.remove()
        }
    }
    
    DisposableEffect(key1 = uid) {
        onDispose {
            if (!esHost && uid != null && !juegoFinalizado.value) {
                salaRef.collection("jugadores").document(uid).delete()
            }
        }
    }

    fun finalizarJuego() {
        juegoFinalizado.value = true 
        val totalTime = System.currentTimeMillis() - startTime
        if (uid != null) {
            salaRef.collection("jugadores").document(uid)
                .update("tiempoTotal", totalTime)
                .addOnCompleteListener {
                    navController.navigate("resultados_screen/$codigoSala") { popUpTo("salas_screen") }
                }
        } else {
            navController.navigate("resultados_screen/$codigoSala") { popUpTo("salas_screen") }
        }
    }

    fun handleSiguientePregunta() {
        if (preguntaActualIndex < preguntas.size - 1) {
            preguntaActualIndex++
            seleccionUsuario = null
            respuestaEnviada = false
        } else {
            finalizarJuego()
        }
    }

    fun enviarRespuesta() {
        if (respuestaEnviada || uid == null) return
        respuestaEnviada = true

        val preguntaActual = preguntas.getOrNull(preguntaActualIndex)
        if (preguntaActual != null && seleccionUsuario?.equals(preguntaActual.correcta, ignoreCase = true) == true) {
            salaRef.collection("jugadores").document(uid).update("puntos", FieldValue.increment(10L))
        }

        scope.launch {
            delay(2000) 
            handleSiguientePregunta()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pregunta ${preguntaActualIndex + 1} de ${preguntas.size}") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        paddingValues ->
        if (cargando) {
            FullScreenLoading(isLoading = true, text = "Cargando partida...")
        } else if (preguntas.isEmpty() || preguntas.any { it.opciones.isEmpty() || it.correcta.isBlank() }) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Hubo un error al cargar las preguntas. Por favor, intenta crear otra sala.", textAlign = TextAlign.Center)
            }
        } else {
            val preguntaActual = preguntas.getOrNull(preguntaActualIndex)
            if (preguntaActual != null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if(isSystemInDarkTheme()) Color(0xFF1C1B1F) else MaterialTheme.colorScheme.surfaceVariant)
                    ){
                        Box(modifier=Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp), contentAlignment = Alignment.CenterStart){
                             Text(text = preguntaActual.pregunta, fontSize = 20.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Start)
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))

                    Column(modifier = Modifier.weight(1.5f)) {
                        preguntaActual.opciones.forEach { opcion ->
                           RespuestaButton(opcion, preguntaActual, seleccionUsuario, respuestaEnviada) {
                               if(!respuestaEnviada) seleccionUsuario = opcion 
                           }
                           Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                     ActionButton(
                        text = "ENVIAR",
                        onClick = { enviarRespuesta() },
                        enabled = seleccionUsuario != null && !respuestaEnviada
                    )
                }
            }
        }
    }
}
