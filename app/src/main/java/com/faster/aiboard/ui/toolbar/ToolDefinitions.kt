package com.faster.aiboard.ui.toolbar

import com.faster.aiboard.ui.canvas.Tool

data class ToolItem(
    val tool: Tool,
    val icon: String,
    val label: String
)

val defaultTools = listOf(
    ToolItem(Tool.Select, "🖱", "选择"),
    ToolItem(Tool.Pen, "✏️", "画笔"),
    ToolItem(Tool.Highlighter, "🖍", "荧光笔"),
    ToolItem(Tool.Eraser, "🧹", "橡皮"),
    ToolItem(Tool.AILasso, "🤖", "AI套索"),
    ToolItem(Tool.Insert, "📎", "插入")
)
