package com.example.skillmind.presentations.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.skillmind.R
import com.example.skillmind.presentations.components.ActionButton
import com.example.skillmind.presentations.components.FullScreenLoading
import com.example.skillmind.presentations.screens.shared.MCQuestionsScreen
import com.example.skillmind.presentations.viewmodel.PreguntasViewModel
import com.example.skillmind.theme.SkillMindTextFieldColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PDFScreen(navController: NavHostController, vm: PreguntasViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var numQuestions by remember { mutableStateOf("5") }
    var pdfFileName by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        pdfUri = uri
        pdfFileName = uri?.lastPathSegment?.split("/")?.last()
    }

    DisposableEffect(Unit) {
        vm.clearState()
        onDispose {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.pdf_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.pdf_screen_back_button_desc),
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

        if (vm.isLoading.value) {
            FullScreenLoading(isLoading = true, text = stringResource(id = R.string.pdf_screen_generating_questions))
        } else if (vm.mcQuestionsList.isNotEmpty()) {
            Column(modifier = Modifier.padding(padding)) {
                MCQuestionsScreen(
                    mcQuestions = vm.mcQuestionsList.toList(),
                    onQuizFinished = { navController.popBackStack("salas", inclusive = false) }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.pdf_screen_description),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                ActionButton(
                    onClick = { filePickerLauncher.launch("application/pdf") },
                    text = stringResource(id = R.string.pdf_screen_select_pdf)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (pdfFileName != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1C1B1F) else MaterialTheme.colorScheme.surfaceVariant),
                        border = if (isSystemInDarkTheme()) BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Icon", tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = pdfFileName!!,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = numQuestions,
                    onValueChange = { numQuestions = it.filter { ch -> ch.isDigit() } },
                    label = { Text(stringResource(id = R.string.pdf_screen_number_of_questions)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusChanged {
                            if(it.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() }
                        },
                    enabled = pdfUri != null,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = SkillMindTextFieldColors()
                )

                Spacer(modifier = Modifier.height(32.dp))

                ActionButton(
                    onClick = {
                        val n = numQuestions.toIntOrNull()
                        if (pdfUri != null && n != null && n > 0) {
                            vm.generateQuestionsFromPdf(context, pdfUri!!, n)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.pdf_screen_no_pdf_selected))
                            }
                        }
                    },
                    enabled = pdfUri != null && numQuestions.isNotBlank(),
                    text = stringResource(id = R.string.pdf_screen_generate_questions)
                )
                if (!vm.lastError.value.isNullOrBlank()) {
                    Text(
                        stringResource(id = R.string.generar_screen_error, vm.lastError.value ?: ""),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top=16.dp)
                    )
                }
            }
        }
    }
}
