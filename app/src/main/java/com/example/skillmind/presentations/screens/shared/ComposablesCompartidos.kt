package com.example.skillmind.presentations.screens.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import com.example.skillmind.R
import com.example.skillmind.presentations.components.ActionButton
import com.example.skillmind.presentations.components.RespuestaButton
import com.example.skillmind.presentations.components.ReviewDialog
import com.example.skillmind.presentations.components.SkillMindDialog
import com.example.skillmind.models.Pregunta
import com.example.skillmind.presentations.viewmodel.MCQuestion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MCQuestionsScreen(
    mcQuestions: List<MCQuestion>,
    onQuizFinished: () -> Unit
) {
    val totalQuestions = mcQuestions.size
    var seleccionUsuario by remember { mutableStateOf<String?>(null) }
    var respuestaEnviada by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current

    var showResultDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var incorrectQuestions by remember { mutableStateOf<List<Pair<MCQuestion, String>>>(emptyList()) }

    var resultTitle by remember { mutableStateOf("") }
    var resultBody by remember { mutableStateOf("") }
    val userSelections = remember { mutableMapOf<Int, String>() }

    val preguntaActual = mcQuestions[currentIndex]
    val preguntaActualDomain = Pregunta(preguntaActual.question, preguntaActual.options, preguntaActual.options.getOrNull(preguntaActual.correctIndex ?: -1) ?: "")

    fun handleNext() {
        if (currentIndex < totalQuestions - 1) {
            currentIndex++
            seleccionUsuario = userSelections[currentIndex]
            respuestaEnviada = false
        } else {
            // Calcular resultados y mostrar diálogo
            var correct = 0
            val incorrect = mutableListOf<Pair<MCQuestion, String>>()
            mcQuestions.forEachIndexed { index, mcQuestion ->
                val correctOption = mcQuestion.options.getOrNull(mcQuestion.correctIndex ?: -1)
                val userSelection = userSelections[index]
                if (userSelection == correctOption) {
                    correct++
                } else if(userSelection != null) {
                    incorrect.add(mcQuestion to userSelection)
                }
            }
            incorrectQuestions = incorrect
            resultTitle = context.getString(R.string.generar_screen_results_title)
            resultBody = context.getString(R.string.generar_screen_results_body, correct, totalQuestions - correct)
            showResultDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.generar_screen_question_progress, currentIndex + 1, totalQuestions)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Columna que contiene el contenido desplazable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(), // Sin peso, se adapta al contenido
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1C1B1F) else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = preguntaActual.question, fontSize = 20.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Start)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Respuestas
                Column {
                    preguntaActual.options.forEach { opcion ->
                        RespuestaButton(opcion, preguntaActualDomain, seleccionUsuario, respuestaEnviada) {
                            if (!respuestaEnviada) {
                                seleccionUsuario = opcion
                                userSelections[currentIndex] = opcion
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Espacio entre el área de desplazamiento y el botón

            // Botón de acción en la parte inferior, fuera del área de desplazamiento
            ActionButton(
                text = if (currentIndex < totalQuestions - 1) stringResource(id = R.string.generar_screen_next) else stringResource(id = R.string.generar_screen_check_answers),
                onClick = { handleNext() },
                enabled = seleccionUsuario != null || userSelections.containsKey(currentIndex)
            )
        }
    }

    if (showResultDialog) {
        SkillMindDialog(
            onDismissRequest = {
                showResultDialog = false
                onQuizFinished()
            },
            title = resultTitle,
            message = resultBody,
            confirmButtonText = stringResource(id = R.string.generar_screen_accept),
            onConfirm = {
                showResultDialog = false
                onQuizFinished()
            },
            dismissButtonText = if (incorrectQuestions.isNotEmpty()) "Revisar Errores" else null,
            onDismiss = {
                showResultDialog = false
                showReviewDialog = true
            }
        )
    }

    if (showReviewDialog) {
        ReviewDialog(
            incorrectQuestions = incorrectQuestions,
            onDismissRequest = {
                showReviewDialog = false
                onQuizFinished()
            }
        )
    }
}