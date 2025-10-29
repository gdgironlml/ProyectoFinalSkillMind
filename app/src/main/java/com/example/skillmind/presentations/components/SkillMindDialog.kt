package com.example.skillmind.presentations.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillMindDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null
) {
    val isDarkTheme = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isDarkTheme) Color(0xFF1C1B1F) else MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 8.dp,
            border = if (isDarkTheme) BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)) else null
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (dismissButtonText != null && onDismiss != null) {
                        TextButton(onClick = onDismiss) {
                            Text(dismissButtonText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    }
                    AuthButton(
                        text = confirmButtonText,
                        onClick = onConfirm
                    )
                }
            }
        }
    }
}
