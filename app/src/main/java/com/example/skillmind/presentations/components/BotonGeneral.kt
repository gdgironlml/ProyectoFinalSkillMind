package com.example.skillmind.presentations.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillmind.theme.DarkPrimary
import com.example.skillmind.theme.DarkSecondary
import com.example.skillmind.theme.DarkTertiary

@Composable fun FuturisticButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 2.dp
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            DarkPrimary,    
            DarkSecondary,  
            DarkTertiary,   
            DarkPrimary     
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .alpha(if (enabled) 1f else 0.5f) 
            .clip(shape)
            .background(Color.Transparent) 
            .drawWithContent {
                
                drawContent()
                
                drawRoundRect(
                    brush = gradientBrush,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth.toPx()),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                )
            }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground, 
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
