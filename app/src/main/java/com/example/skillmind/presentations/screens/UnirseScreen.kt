package com.example.skillmind.presentations.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.skillmind.models.Jugador
import com.example.skillmind.presentations.components.ActionButton
import com.example.skillmind.presentations.components.FullScreenLoading
import com.example.skillmind.theme.SkillMindTextFieldColors
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

suspend fun unirseASala(
    navController: NavController,
    codigo: String,
    nombreJugador: String,
    onError: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val uid = Firebase.auth.currentUser?.uid

    if (uid == null) {
        onError("Error: Debes iniciar sesión.")
        return
    }
    if (codigo.isBlank() || codigo.length != 6) {
        onError("El código debe tener 6 caracteres.")
        return
    }

    val salaRef = db.collection("salas").document(codigo.uppercase())

    try {
        db.runTransaction { transaction ->
            val snapshot = transaction.get(salaRef)

            if (!snapshot.exists()) {
                throw Exception("Código de sala no válido.")
            }

            val estado = snapshot.getString("estado")
            if (estado != "esperando") {
                throw Exception("La sala ya ha iniciado el juego o está cerrada.")
            }

            val jugadorRef = salaRef.collection("jugadores").document(uid)
            val jugadorSnapshot = transaction.get(jugadorRef)

            if (!jugadorSnapshot.exists()) {
                val jugador = Jugador(uid = uid, nombre = nombreJugador, puntos = 0L)
                transaction.set(jugadorRef, jugador)
                transaction.update(salaRef, "playerCount", FieldValue.increment(1))
            }

            null
        }.await()

        navController.navigate("espera_screen/${codigo.uppercase()}/${false}/$nombreJugador")

    } catch (e: Exception) {
        onError(e.message ?: "Error al conectar con la sala.")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UnirseScreen(navController: NavController, nombreUsuario: String) {
    var codigoIngresado by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Unirse a una Sala",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Ingresa el código de 6 dígitos para unirte a la partida.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(48.dp))

            OutlinedTextField(
                value = codigoIngresado,
                onValueChange = { newValue ->
                    if (newValue.length <= 6) {
                        codigoIngresado = newValue.filter { !it.isWhitespace() }.uppercase()
                    }
                    mensajeError = null
                },
                label = { Text("Código de Sala") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onFocusChanged {
                        if (it.isFocused) {
                            scope.launch { bringIntoViewRequester.bringIntoView() }
                        }
                    },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                shape = RoundedCornerShape(16.dp),
                colors = SkillMindTextFieldColors()
            )

            if (mensajeError != null) {
                Text(
                    text = mensajeError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            if (isLoading) {
                FullScreenLoading(isLoading = true, text = "Uniéndote a la sala...")
            } else {
                ActionButton(
                    text = "UNIRSE A LA SALA",
                    onClick = {
                        isLoading = true
                        scope.launch {
                            unirseASala(navController, codigoIngresado, nombreUsuario) { error ->
                                mensajeError = error
                                isLoading = false
                            }
                        }
                    },
                    enabled = codigoIngresado.length == 6 && !isLoading
                )
            }
        }
    }
}
