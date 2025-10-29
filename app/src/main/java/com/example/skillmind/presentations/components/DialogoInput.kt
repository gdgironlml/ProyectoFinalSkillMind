package com.example.skillmind.presentations.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null,
    confirmButtonEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isDarkTheme) Color(0xFF1C1B1F) else MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 8.dp,
            border = if (isDarkTheme) BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)) else null,
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(16.dp))

                content()

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
                    ActionButton(
                        text = confirmButtonText,
                        onClick = onConfirm,
                        enabled = confirmButtonEnabled
                    )
                }
            }
        }
    }
}
