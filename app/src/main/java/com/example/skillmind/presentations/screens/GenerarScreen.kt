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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.skillmind.R
import com.example.skillmind.presentations.components.ActionButton
import com.example.skillmind.presentations.components.FullScreenLoading
import com.example.skillmind.presentations.components.InputDialog
import com.example.skillmind.presentations.screens.shared.MCQuestionsScreen
import com.example.skillmind.presentations.viewmodel.PreguntasViewModel
import com.example.skillmind.theme.SkillMindTextFieldColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GenerarScreen(navController: NavController, vm: PreguntasViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var currentSubject by remember { mutableStateOf("") }
    val context = LocalContext.current

    var topic by remember { mutableStateOf("") }
    var countStr by remember { mutableStateOf("5") }

    DisposableEffect(Unit) {
        vm.clearState()
        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.generar_screen_title)) },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (vm.mcQuestionsList.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(id = R.string.generar_screen_select_subject),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    val subjects = listOf(
                        R.string.generar_screen_math,
                        R.string.generar_screen_language,
                        R.string.generar_screen_english,
                        R.string.generar_screen_other_course
                    )

                    subjects.forEach { subjectResId ->
                        ActionButton(
                            text = stringResource(id = subjectResId),
                            onClick = {
                                currentSubject = context.getString(subjectResId)
                                topic = ""
                                countStr = "5"
                                showDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    if (vm.isLoading.value) {
                        FullScreenLoading(isLoading = true, text = "Generando preguntas...")
                    } else if (!vm.lastError.value.isNullOrBlank()) {
                        Text(
                            stringResource(id = R.string.generar_screen_error, vm.lastError.value ?: ""),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            stringResource(id = R.string.generar_screen_no_questions_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                MCQuestionsScreen(
                    mcQuestions = vm.mcQuestionsList.toList(),
                    onQuizFinished = { navController.popBackStack("salas", inclusive = false) }
                )
            }
        }

        if (showDialog) {
            val bringIntoViewRequester = remember { BringIntoViewRequester() }
            val topicLabel = stringResource(id = R.string.generar_screen_topic_label)
            val exampleText = when (currentSubject) {
                context.getString(R.string.generar_screen_math) -> "(ej: Álgebra)"
                context.getString(R.string.generar_screen_language) -> "(ej: Tiempos verbales)"
                context.getString(R.string.generar_screen_english) -> "(ej: Present simple)"
                context.getString(R.string.generar_screen_other_course) -> "(ej: Capitales de Europa)"
                else -> ""
            }

            InputDialog(
                onDismissRequest = { showDialog = false },
                title = context.getString(R.string.generar_screen_generate_for, currentSubject),
                confirmButtonText = stringResource(id = R.string.generar_screen_generate),
                onConfirm = {
                    val n = countStr.toIntOrNull() ?: 0
                    if (topic.isNotBlank() && n > 0) {
                        vm.generateQuestionsByTopic(currentSubject, topic.trim(), n)
                        showDialog = false
                    }
                },
                dismissButtonText = stringResource(id = R.string.generar_screen_cancel),
                onDismiss = { showDialog = false },
                confirmButtonEnabled = topic.isNotBlank() && (countStr.toIntOrNull() ?: 0) > 0
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("$topicLabel $exampleText") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusChanged {
                                if (it.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = SkillMindTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = countStr,
                        onValueChange = { countStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text(stringResource(id = R.string.generar_screen_number_of_questions_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusChanged {
                                if (it.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = SkillMindTextFieldColors()
                    )
                }
            }
        }
    }
}
