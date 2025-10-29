package com.example.skillmind.presentations.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.skillmind.R
import com.example.skillmind.presentations.components.FullScreenLoading
import com.example.skillmind.presentations.components.InputDialog
import com.example.skillmind.presentations.screens.shared.MCQuestionsScreen
import com.example.skillmind.presentations.viewmodel.PreguntasViewModel
import com.example.skillmind.theme.DarkPrimary
import com.example.skillmind.theme.DarkSecondary
import com.example.skillmind.theme.DarkTertiary
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CamaraScreen(navController: NavController, vm: PreguntasViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var showCountDialog by remember { mutableStateOf(false) }
    var generationInProgress by remember { mutableStateOf(false) }

    var countStr by remember { mutableStateOf("5") }

    DisposableEffect(Unit) {
        vm.clearState()
        onDispose { }
    }

    LaunchedEffect(vm.isLoading.value) {
        if (!vm.isLoading.value && generationInProgress) {
            generationInProgress = false
            val error = vm.lastError.value
            if (!error.isNullOrBlank()) {
                scope.launch { snackbarHostState.showSnackbar("Error: $error") }
            } else if (vm.mcQuestionsList.isEmpty()) {
                scope.launch { snackbarHostState.showSnackbar("No se pudieron generar preguntas de la imagen.") }
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            showCountDialog = true
        } else {
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.camara_screen_photo_failed)) }
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
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.camara_screen_permission_denied)) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.camara_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.sincodigo_back_button_desc),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (vm.isLoading.value) {
                FullScreenLoading(isLoading = true, text = stringResource(id = R.string.camara_screen_generating_questions))
            } else if (vm.mcQuestionsList.isNotEmpty()) {
                MCQuestionsScreen(
                    mcQuestions = vm.mcQuestionsList.toList(),
                    onQuizFinished = { navController.popBackStack("salas", inclusive = false) }
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = "Genera preguntas a partir de una foto",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Toca en el recuadro para abrir la cámara y capturar texto, fórmulas o lo que necesites.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(32.dp))

                    val gradientBrush = Brush.horizontalGradient(
                        colors = listOf(DarkPrimary, DarkSecondary, DarkTertiary, DarkPrimary)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.3f)
                            .drawWithContent {
                                drawContent()
                                drawRoundRect(
                                    brush = gradientBrush,
                                    style = Stroke(width = 2.dp.toPx()),
                                    size = size,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                                )
                            }
                            .clickable { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(80.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Tocar para abrir cámara", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    val error = vm.lastError.value
                    if (!error.isNullOrBlank()) {
                        Text(
                            text = stringResource(id = R.string.generar_screen_error, error),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                }
            }
        }
    }

    if (showCountDialog) {
        val bringIntoViewRequester = remember { BringIntoViewRequester() }
        InputDialog(
            onDismissRequest = { showCountDialog = false },
            title = stringResource(id = R.string.camara_screen_dialog_title),
            confirmButtonText = stringResource(id = R.string.generar_screen_generate),
            onConfirm = {
                val n = countStr.toIntOrNull() ?: 0
                if (n > 0) {
                    showCountDialog = false
                    generationInProgress = true
                    vm.generateQuestionsFromImage(photoUri!!, n, context)
                }
            },
            dismissButtonText = stringResource(id = R.string.generar_screen_cancel),
            onDismiss = { showCountDialog = false },
            confirmButtonEnabled = (countStr.toIntOrNull() ?: 0) > 0
        ) {
            Column {
                Text(stringResource(id = R.string.camara_screen_dialog_message))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = countStr,
                    onValueChange = { countStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text(stringResource(id = R.string.generar_screen_number_of_questions_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusChanged { if (it.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() } },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }
    }
}
