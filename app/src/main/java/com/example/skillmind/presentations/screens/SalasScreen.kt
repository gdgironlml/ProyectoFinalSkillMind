package com.example.skillmind.presentations.screens

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.skillmind.R
import com.example.skillmind.models.Jugador
import com.example.skillmind.models.Pregunta
import com.example.skillmind.presentations.components.* 
import com.example.skillmind.presentations.viewmodel.PreguntasViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

suspend fun crearNuevaSala(hostId: String, nombreUsuario: String, preguntas: List<Pregunta>): String? {
    val db = FirebaseFirestore.getInstance()
    val codigoSala = generarCodigoSala()
    val salaRef = db.collection("salas").document(codigoSala)

    val salaData = hashMapOf(
        "hostId" to hostId,
        "hostName" to nombreUsuario,
        "preguntas" to preguntas.map { mapOf("pregunta" to it.pregunta, "opciones" to it.opciones, "correcta" to it.correcta) },
        "estado" to "esperando",
        "timestamp" to System.currentTimeMillis(),
        "playerCount" to 1L
    )

    val hostComoJugador = Jugador(uid = hostId, nombre = nombreUsuario, puntos = 0L)

    return try {
        db.runBatch { batch ->
            batch.set(salaRef, salaData)
            batch.set(salaRef.collection("jugadores").document(hostId), hostComoJugador)
        }.await()
        codigoSala
    } catch (e: Exception) {
        Log.e("SalasScreen", "Error creando la sala en Firestore", e)
        null
    }
}

fun generarCodigoSala(): String {
    return (100000..999999).random().toString()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalasScreen(navController: NavController, vm: PreguntasViewModel) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    var mostrarDialogoCreacion by remember { mutableStateOf(false) }
    var showTopicInputDialog by remember { mutableStateOf(false) }
    var nombreUsuario by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isCreatingRoom by remember { mutableStateOf(false) }

    var mostrarDialogoFuente by remember { mutableStateOf(false) }
    var showPhotoCountDialog by remember { mutableStateOf(false) }
    var showPdfCountDialog by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var pdfUri by remember { mutableStateOf<Uri?>(null) }

    var creationInProgress by remember { mutableStateOf(false) }

    val loginRoute = stringResource(id = R.string.login_screen_route)

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            showPhotoCountDialog = true
        } else {
            Toast.makeText(context, "Error al tomar la foto.", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            file.createNewFile()
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            photoUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pdfUri = uri
            showPdfCountDialog = true
        } else {
            Toast.makeText(context, "No se seleccionó ningún PDF.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(vm.isLoading.value) {
        if (!vm.isLoading.value && creationInProgress) {
            val hostId = currentUser?.uid

            creationInProgress = false
            isCreatingRoom = false

            if (hostId == null) {
                Toast.makeText(context, "Error de autenticación.", Toast.LENGTH_SHORT).show()
                vm.clearState()
                return@LaunchedEffect
            }

            if (!vm.lastError.value.isNullOrBlank()) {
                Toast.makeText(context, "Error: ${vm.lastError.value}", Toast.LENGTH_LONG).show()
                vm.clearState()
                return@LaunchedEffect
            }

            val preguntasConvertidas = vm.mcQuestionsList.map { mcQuestion ->
                Pregunta(
                    pregunta = mcQuestion.question,
                    opciones = mcQuestion.options,
                    correcta = mcQuestion.correctIndex?.let { mcQuestion.options.getOrNull(it) } ?: ""
                )
            }
            val preguntasValidas = preguntasConvertidas.filter { it.pregunta.isNotBlank() && it.opciones.isNotEmpty() && it.correcta.isNotBlank() }

            if (preguntasValidas.isEmpty()) {
                Toast.makeText(context, "No se pudieron generar preguntas válidas.", Toast.LENGTH_LONG).show()
                 vm.clearState()
            } else {
                scope.launch {
                    val codigoSala = crearNuevaSala(hostId, nombreUsuario, preguntasValidas)
                    vm.clearState() 
                    if (codigoSala != null) {
                        navController.navigate("espera_screen/$codigoSala/${true}/$nombreUsuario")
                    } else {
                        Toast.makeText(context, "Error al crear la sala en la base de datos.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(currentUser.uid)
            try {
                val userDoc = userDocRef.get().await()
                nombreUsuario = userDoc.getString("nombre") ?: currentUser.email?.split("@")?.get(0) ?: "Anónimo"
            } catch (e: Exception) {
                Log.e("SalasScreen", "Error al obtener nombre de usuario", e)
                nombreUsuario = "Anónimo"
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    fun iniciarCreacion(sourceType: String, uri: Uri?, text: String?, count: Int) {
        if (currentUser?.uid == null) {
            Toast.makeText(context, "Debes iniciar sesión para crear una sala.", Toast.LENGTH_SHORT).show()
            return
        }
        isCreatingRoom = true
        creationInProgress = true
        vm.clearState()
        when (sourceType) {
            "TEMA" -> vm.generateQuestionsByTopic(subject = text!!, topic = text, count = count)
            "FOTO" -> vm.generateQuestionsFromImage(uri!!, count, context)
            "PDF" -> vm.generateQuestionsFromPdf(context, uri!!, count)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate(loginRoute) { popUpTo(navController.graph.id) { inclusive = true } }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                FullScreenLoading(isLoading = true)
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (nombreUsuario.isNotBlank() && nombreUsuario != "Anónimo") {
                        Text(
                            text = "Hola, $nombreUsuario",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = "¿Qué deseas hacer?",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(72.dp))

                    ActionButton(text = "Crear una sala", onClick = { mostrarDialogoCreacion = true })
                    Spacer(modifier = Modifier.height(24.dp))
                    ActionButton(text = "Unirse a una sala", onClick = { navController.navigate("unirse_screen/$nombreUsuario") })
                    Spacer(modifier = Modifier.height(24.dp))
                    ActionButton(text = "Jugar solo", onClick = { navController.navigate("sincodigo") })
                }
            }

            FullScreenLoading(
                isLoading = isCreatingRoom,
                text = "Generando preguntas y creando sala..."
            )
        }
    }

    if (mostrarDialogoCreacion) {
        DialogoCreacionSala(
            onDismiss = { mostrarDialogoCreacion = false },
            onSeleccion = {
                selection ->
                mostrarDialogoCreacion = false
                if (selection == 2) { showTopicInputDialog = true } else { mostrarDialogoFuente = true }
            }
        )
    }

    if (mostrarDialogoFuente) {
        DialogoSeleccionFuente(
            onDismiss = { mostrarDialogoFuente = false },
            onSeleccion = {
                fuente ->
                mostrarDialogoFuente = false
                when (fuente) {
                    "FOTO" -> permissionLauncher.launch(Manifest.permission.CAMERA)
                    "PDF" -> filePickerLauncher.launch("application/pdf")
                }
            }
        )
    }

    if (showTopicInputDialog) {
        TopicInputDialog(
            onDismiss = { showTopicInputDialog = false },
            onConfirm = {
                tema, num ->
                showTopicInputDialog = false
                if (tema.isNotBlank() && num > 0) {
                    iniciarCreacion(sourceType = "TEMA", text = tema, count = num, uri = null)
                } else {
                    Toast.makeText(context, "El tema no puede estar vacío y el número de preguntas debe ser mayor a 0.", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    if (showPhotoCountDialog) {
        SalaCountDialog(
            onDismiss = { showPhotoCountDialog = false },
            onConfirm = {
                count ->
                showPhotoCountDialog = false
                photoUri?.let { uri ->
                    iniciarCreacion(sourceType = "FOTO", uri = uri, count = count, text = null)
                }
            }
        )
    }

    if (showPdfCountDialog) {
        SalaCountDialog(
            onDismiss = { showPdfCountDialog = false },
            onConfirm = {
                count ->
                showPdfCountDialog = false
                pdfUri?.let { uri ->
                    iniciarCreacion(sourceType = "PDF", uri = uri, count = count, text = null)
                }
            }
        )
    }
}
