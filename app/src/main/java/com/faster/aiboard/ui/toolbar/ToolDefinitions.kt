package com.faster.aiboard.ui.toolbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixOff
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.ui.graphics.vector.ImageVector
import com.faster.aiboard.ui.canvas.Tool

data class ToolItem(
    val tool: Tool,
    val icon: ImageVector,
    val label: String
)

val defaultTools = listOf(
    ToolItem(Tool.Select, Icons.Default.TouchApp, "选择"),
    ToolItem(Tool.Pen, Icons.Default.Draw, "画笔"),
    ToolItem(Tool.Highlighter, Icons.Default.Highlight, "荧光笔"),
    ToolItem(Tool.Eraser, Icons.Default.AutoFixOff, "橡皮"),
    ToolItem(Tool.AILasso, Icons.Default.AutoAwesome, "AI套索"),
    ToolItem(Tool.Insert, Icons.Default.AttachFile, "插入")
)
