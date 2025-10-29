package com.example.skillmind.presentations.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuturisticDialogCard(onDismissRequest: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
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
                content()
            }
        }
    }
}