package com.faster.aiboard.ui.canvas

import androidx.compose.ui.geometry.Offset
import com.faster.aiboard.data.model.CanvasElement

data class CanvasState(
    val elements: List<CanvasElement> = emptyList(),
    val selectedElementIds: Set<String> = emptySet(),
    val activeTool: Tool = Tool.Pen,
    val penColor: Long = 0xFF1A1A1A,
    val penWidth: Float = 4f,
    val eraserWidth: Float = 20f,
    val canvasOffset: Offset = Offset.Zero,
    val canvasScale: Float = 1f,
    val currentStrokePoints: List<Offset> = emptyList(),
    val currentStrokeColor: Long = 0xFF1A1A1A,
    val currentStrokeWidth: Float = 4f,
    val aiLassoPoints: List<Offset> = emptyList()
)

enum class Tool {
    Pen, Highlighter, Eraser, Select, AILasso, Insert
}
