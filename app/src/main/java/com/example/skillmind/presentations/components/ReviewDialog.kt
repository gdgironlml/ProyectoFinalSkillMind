package com.example.skillmind.presentations.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.skillmind.presentations.viewmodel.MCQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDialog(
    onDismissRequest: () -> Unit,
    incorrectQuestions: List<Pair<MCQuestion, String>>
) {
    val isDarkTheme = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = 32.dp),
        shape = RoundedCornerShape(16.dp),
        containerColor = if (isDarkTheme) Color(0xFF1C1B1F) else MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        title = {
            Text(
                text = "Preguntas Incorrectas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn {
                items(incorrectQuestions) { (question, userAnswer) ->
                    val correctAnswer = question.options.getOrNull(question.correctIndex ?: -1) ?: "N/A"
                    Column(Modifier.padding(vertical = 12.dp).fillMaxWidth()) {
                        Text(
                            text = question.question,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Tu respuesta: $userAnswer",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Respuesta correcta: $correctAnswer",
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text("Cerrar", color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    )
}
