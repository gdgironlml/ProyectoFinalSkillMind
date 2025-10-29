package com.example.skillmind.presentations.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.skillmind.R
import com.example.skillmind.presentations.components.ActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JugarSoloScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.sincodigo_back_button_desc),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.sincodigo_question),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(72.dp))

            ActionButton(
                text = stringResource(R.string.sincodigo_generar_preguntas),
                onClick = { navController.navigate("generate") }
            )
            Spacer(modifier = Modifier.height(24.dp))
            ActionButton(
                text = stringResource(R.string.sincodigo_ingresar_pdf),
                onClick = { navController.navigate("pdf") }
            )
            Spacer(modifier = Modifier.height(24.dp))
            ActionButton(
                text = stringResource(R.string.sincodigo_tomar_fotografia),
                onClick = { navController.navigate("camera") }
            )
        }
    }
}