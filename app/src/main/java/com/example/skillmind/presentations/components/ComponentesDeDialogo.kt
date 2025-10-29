package com.example.skillmind.presentations.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.skillmind.R
import com.example.skillmind.theme.SkillMindTextFieldColors
import kotlinx.coroutines.launch

@Composable
fun DialogoCreacionSala(onDismiss: () -> Unit, onSeleccion: (Int) -> Unit) {
    FuturisticDialogCard(onDismissRequest = onDismiss) {
        Text(
            text = "Crear Sala",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Elige el modo de creación de la sala.")
        Spacer(modifier = Modifier.height(24.dp))
        AuthButton(text = "Basado en un Documento", onClick = { onSeleccion(1) })
        Spacer(modifier = Modifier.height(16.dp))
        AuthButton(text = "Basado en un Tema", onClick = { onSeleccion(2) })
    }
}

@Composable
fun DialogoSeleccionFuente(onDismiss: () -> Unit, onSeleccion: (String) -> Unit) {
    FuturisticDialogCard(onDismissRequest = onDismiss) {
        Text(
            text = "Crear desde documento",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("¿Desde dónde quieres generar las preguntas?")
        Spacer(modifier = Modifier.height(24.dp))
        AuthButton(text = "Desde una foto", onClick = { onSeleccion("FOTO") })
        Spacer(modifier = Modifier.height(16.dp))
        AuthButton(text = "Desde un PDF", onClick = { onSeleccion("PDF") })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopicInputDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var topic by remember { mutableStateOf("") }
    var numQuestions by remember { mutableStateOf("5") }
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    FuturisticDialogCard(onDismissRequest = onDismiss) {
        Text(
            text = "Crear sala desde tema",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = topic, 
            onValueChange = { topic = it }, 
            label = { Text("Tema") }, 
            colors = SkillMindTextFieldColors(),
            modifier = Modifier.fillMaxWidth().bringIntoViewRequester(bringIntoViewRequester).onFocusChanged { if(it.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() } }
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = numQuestions,
            onValueChange = { numQuestions = it.filter { char -> char.isDigit() } },
            label = { Text("Número de preguntas") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = SkillMindTextFieldColors(),
            modifier = Modifier.fillMaxWidth().bringIntoViewRequester(bringIntoViewRequester).onFocusChanged { if(it.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() } }
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) }
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            AuthButton(
                text = "Generar y Crear",
                onClick = { onConfirm(topic, numQuestions.toIntOrNull() ?: 0) }
            )
        }
    }
}

@Composable
fun SalaCountDialog(onDismiss: () -> Unit, onConfirm: (count: Int) -> Unit) {
    var countStr by remember { mutableStateOf("5") }

    FuturisticDialogCard(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(id = R.string.camara_screen_dialog_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(16.dp))
        Text(stringResource(id = R.string.camara_screen_dialog_message))
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = countStr,
            onValueChange = { countStr = it.filter { ch -> ch.isDigit() } },
            label = { Text(stringResource(id = R.string.generar_screen_number_of_questions_label)) },
            colors = SkillMindTextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) }
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            AuthButton(
                text = stringResource(id = R.string.generar_screen_generate),
                onClick = {
                    val n = countStr.toIntOrNull() ?: 0
                    if (n > 0) {
                        onConfirm(n)
                    }
                }
            )
        }
    }
}