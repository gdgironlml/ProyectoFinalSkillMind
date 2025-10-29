package com.example.skillmind.presentations.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
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
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginPage(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var dialogTitle by remember { mutableStateOf("") }
    var isLoginSuccessful by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val loginRoute = stringResource(id = R.string.login_screen_route)
    val registerRoute = stringResource(id = R.string.register_screen_route)
    val salasRoute = stringResource(id = R.string.salas_screen_route)

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    Scaffold {
        paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FullScreenLoading(isLoading = isLoading, text = "Iniciando sesión...")

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.titulo_de_app),
                    contentDescription = stringResource(id = R.string.app_title),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.login_screen_text),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.filter { char -> !char.isWhitespace() } },
                    label = { Text(stringResource(R.string.fields_email)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusChanged {
                            if (it.isFocused) {
                                scope.launch { bringIntoViewRequester.bringIntoView() }
                            }
                        }
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
                        .onFocusChanged {
                            if (it.isFocused) {
                                scope.launch { bringIntoViewRequester.bringIntoView() }
                            }
                        }
                        .semantics { contentType = ContentType.Password },
                    colors = SkillMindTextFieldColors()
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthButton(
                    text = stringResource(R.string.login_screen_login_button),
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        dialogTitle = navController.context.getString(R.string.welcome_title)
                                        dialogMessage = navController.context.getString(R.string.login_successful)
                                        isLoginSuccessful = true
                                        showDialog = true
                                    } else {
                                        dialogTitle = navController.context.getString(R.string.alert_title)
                                        dialogMessage = if (task.exception is FirebaseAuthInvalidUserException) {
                                            navController.context.getString(R.string.unregistered_user_message)
                                        } else {
                                            navController.context.getString(R.string.authentication_error_message, task.exception?.message)
                                        }
                                        showDialog = true
                                    }
                                }
                        } else {
                            dialogTitle = navController.context.getString(R.string.alert_title)
                            dialogMessage = navController.context.getString(R.string.empty_fields_message)
                            showDialog = true
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { navController.navigate(registerRoute) }) {
                    Text(
                        stringResource(R.string.login_screen_register_button),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }

    if (showDialog) {
        val isUnregisteredUser = dialogMessage == navController.context.getString(R.string.unregistered_user_message)

        SkillMindDialog(
            onDismissRequest = { showDialog = false },
            title = dialogTitle,
            message = dialogMessage,
            confirmButtonText = if (isUnregisteredUser) stringResource(R.string.register_button) else stringResource(R.string.ok_button),
            onConfirm = {
                showDialog = false
                if (isLoginSuccessful) {
                    navController.navigate(salasRoute) {
                        popUpTo(loginRoute) { inclusive = true }
                    }
                } else if (isUnregisteredUser) {
                    navController.navigate(registerRoute)
                }
            },
            dismissButtonText = if (isUnregisteredUser) stringResource(R.string.cancel_button) else null,
            onDismiss = if (isUnregisteredUser) { { showDialog = false } } else null
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewLoginScreen() {
    val navController = rememberNavController()
    LoginPage(navController = navController)
}
