package com.faster.aiboard.ui.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.faster.aiboard.ui.canvas.Tool

@Composable
fun FloatingToolbar(
    activeTool: Tool,
    onToolSelected: (Tool) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                RoundedCornerShape(12.dp)
            )
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        defaultTools.forEach { toolItem ->
            val isActive = activeTool == toolItem.tool
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
                    .clickable { onToolSelected(toolItem.tool) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = toolItem.icon,
                    contentDescription = toolItem.label,
                    tint = if (isActive) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ColorStrip(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        0xFF1A1A1A, 0xFFE74C3C, 0xFFE67E22, 0xFFF1C40F,
        0xFF2ECC71, 0xFF3498DB, 0xFF9B59B6, 0xFFFFFFFF
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        colors.forEach { color ->
            val isSelected = selectedColor == color
            Box(
                modifier = Modifier
                    .size(if (isSelected) 28.dp else 24.dp)
                    .clip(CircleShape)
                    .background(
                        if (color == 0xFFFFFFFF) Color.White else Color(color)
                    )
                    .then(
                        if (color == 0xFFFFFFFF) Modifier.border(1.dp, Color.LightGray, CircleShape)
                        else Modifier
                    )
                    .then(
                        if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        else Modifier
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}
