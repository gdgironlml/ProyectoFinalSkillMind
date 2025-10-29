package com.example.skillmind.presentations.screens

import android.util.Patterns
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.skillmind.R
import com.example.skillmind.presentations.components.AuthButton
import com.example.skillmind.presentations.components.FullScreenLoading
import com.example.skillmind.presentations.components.SkillMindDialog
import com.example.skillmind.theme.SkillMindTextFieldColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun isEmailValid(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

suspend fun isUsernameAvailable(db: FirebaseFirestore, nombre: String): Boolean {
    val result = db.collection("users").whereEqualTo("nombre", nombre).get().await()
    return result.isEmpty
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun RegisterScreen(navController: NavController) {

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var dialogTitle by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val isDarkTheme = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FullScreenLoading(isLoading = isLoading, text = "Verificando datos y creando cuenta...")
            
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Icon",
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            if (isDarkTheme) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                        .padding(16.dp),
                    tint = if (isDarkTheme) Color.Black else MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.register_screen_text),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it.filter { char -> !char.isWhitespace() } },
                    label = { Text("Nombre de usuario") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusChanged { if (it.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() } },
                    colors = SkillMindTextFieldColors()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.filter { char -> !char.isWhitespace() } },
                    label = { Text(stringResource(R.string.fields_email)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusChanged { if (it.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() } }
                        .semantics { contentType = ContentType.EmailAddress },
                    colors = SkillMindTextFieldColors()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.filter { char -> !char.isWhitespace() } },
                    label = { Text(stringResource(R.string.fields_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusChanged { if (it.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() } }
                        .semantics { contentType = ContentType.NewPassword },
                    colors = SkillMindTextFieldColors()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it.filter { char -> !char.isWhitespace() } },
                    label = { Text(stringResource(R.string.fields_confirm_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusChanged { if (it.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() } },
                    colors = SkillMindTextFieldColors()
                )
                Spacer(modifier = Modifier.height(32.dp))
                AuthButton(
                    text = stringResource(R.string.register_screen_register_button),
                    onClick = {
                        scope.launch {
                            if (nombre.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                                if (password == confirmPassword) {
                                    if (isEmailValid(email)) {
                                        isLoading = true
                                        if (isUsernameAvailable(db, nombre)) {
                                            auth.createUserWithEmailAndPassword(email, password)
                                                .addOnCompleteListener { task ->
                                                    isLoading = false
                                                    if (task.isSuccessful) {
                                                        val user = auth.currentUser
                                                        user?.let {
                                                            val userData = hashMapOf("nombre" to nombre)
                                                            db.collection("users").document(it.uid).set(userData)
                                                        }
                                                        dialogTitle = navController.context.getString(R.string.register_success_title)
                                                        dialogMessage = navController.context.getString(R.string.registration_successful)
                                                        showDialog = true
                                                    } else {
                                                        dialogTitle = navController.context.getString(R.string.alert_title)
                                                        when (task.exception) {
                                                            is FirebaseAuthUserCollisionException -> {
                                                                dialogMessage = "Este correo electrónico ya está registrado. Por favor, inicia sesión."
                                                            }
                                                            is FirebaseAuthWeakPasswordException -> {
                                                                dialogMessage = "La contraseña es demasiado débil. Debe tener al menos 6 caracteres."
                                                            }
                                                            is FirebaseAuthInvalidCredentialsException -> {
                                                                dialogMessage = "El formato del correo electrónico no es válido."
                                                            }
                                                            else -> {
                                                                dialogMessage = "${navController.context.getString(R.string.register_error)} ${task.exception?.message}"
                                                            }
                                                        }
                                                        showDialog = true
                                                    }
                                                }
                                        } else {
                                            isLoading = false
                                            dialogTitle = navController.context.getString(R.string.alert_title)
                                            dialogMessage = "El nombre de usuario '$nombre' ya está en uso."
                                            showDialog = true
                                        }
                                    } else {
                                        dialogTitle = navController.context.getString(R.string.alert_title)
                                        dialogMessage = "Por favor, ingresa un formato de correo electrónico válido."
                                        showDialog = true
                                    }
                                } else {
                                    dialogTitle = navController.context.getString(R.string.alert_title)
                                    dialogMessage = navController.context.getString(R.string.passwords_do_not_match)
                                    showDialog = true
                                }
                            } else {
                                dialogTitle = navController.context.getString(R.string.alert_title)
                                dialogMessage = navController.context.getString(R.string.fill_all_fields)
                                showDialog = true
                            }
                        }
                    }
                )
            }
        }

        if (showDialog) {
            SkillMindDialog(
                onDismissRequest = { showDialog = false },
                title = dialogTitle,
                message = dialogMessage,
                confirmButtonText = "OK",
                onConfirm = {
                    showDialog = false
                    if (dialogTitle == navController.context.getString(R.string.register_success_title)) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewRegisterScreen() {
    RegisterScreen(rememberNavController())
}
