package com.faster.aiboard.ui.canvas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.faster.aiboard.data.model.*
import com.faster.aiboard.data.model.Stroke as ModelStroke
import com.faster.aiboard.data.repository.FileRepository
import java.util.UUID

class CanvasViewModel {
    var state by mutableStateOf(CanvasState())
        private set

    // Persistence
    private var boardId: String = ""
    private var boardMeta: BoardMeta = BoardMeta(name = "未命名白板", template = "blank", createdAt = "", updatedAt = "")
    private lateinit var repository: FileRepository

    fun init(boardId: String, repository: FileRepository) {
        this.boardId = boardId
        this.repository = repository
    }

    suspend fun save() {
        if (!::repository.isInitialized) return
        val now = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
        val wb = Whiteboard(
            board = boardMeta.copy(updatedAt = now),
            elements = state.elements
        )
        repository.saveBoard(boardId, wb)
    }

    fun setActiveTool(tool: Tool) {
        state = state.copy(activeTool = tool)
    }

    fun setPenColor(color: Long) {
        state = state.copy(penColor = color, currentStrokeColor = color)
    }

    fun setPenWidth(width: Float) {
        state = state.copy(penWidth = width, currentStrokeWidth = width)
    }

    fun onDrawStart(offset: Offset) {
        state = state.copy(
            currentStrokePoints = listOf(offset),
            currentStrokeColor = state.penColor,
            currentStrokeWidth = state.penWidth
        )
    }

    fun onDrawMove(offset: Offset) {
        state = state.copy(
            currentStrokePoints = state.currentStrokePoints + offset
        )
    }

    fun onDrawEnd() {
        val pts = state.currentStrokePoints
        if (pts.size < 2) {
            state = state.copy(currentStrokePoints = emptyList())
            return
        }
        val newStroke = Stroke(
            id = UUID.randomUUID().toString(),
            points = pts.map { PointF(it.x, it.y) },
            color = state.currentStrokeColor,
            strokeWidth = state.currentStrokeWidth,
            alpha = if (state.activeTool == Tool.Highlighter) 0.4f else 1f,
            isHighlighter = state.activeTool == Tool.Highlighter
        )
        state = state.copy(
            elements = state.elements + newStroke,
            currentStrokePoints = emptyList()
        )
    }

    fun clearSelection() {
        state = state.copy(selectedElementIds = emptySet())
    }

    fun onEraserMove(offset: Offset) {
        val hitId = state.elements.filterIsInstance<ModelStroke>().findLast { stroke ->
            val pts = stroke.points.map { Offset(it.x, it.y) }
            pts.any { (it - offset).getDistance() < state.eraserWidth }
        }?.id
        if (hitId != null) {
            state = state.copy(
                elements = state.elements.filter { it.id != hitId }
            )
        }
    }

    fun onSelectTap(offset: Offset) {
        val hitId = state.elements.lastOrNull { element ->
            when (element) {
                is ModelStroke -> {
                    val pts = element.points.map { Offset(it.x, it.y) }
                    pts.any { (it - offset).getDistance() < 20f }
                }
                else -> false
            }
        }?.id
        state = if (hitId != null) {
            state.copy(selectedElementIds = setOf(hitId))
        } else {
            state.copy(selectedElementIds = emptySet())
        }
    }

    // AI Lasso handlers
    fun onAILassoStart(offset: Offset) {
        state = state.copy(aiLassoPoints = listOf(offset))
    }

    fun onAILassoMove(offset: Offset) {
        state = state.copy(aiLassoPoints = state.aiLassoPoints + offset)
    }

    fun onAILassoEnd() {
        val pts = state.aiLassoPoints
        if (pts.size >= 3) {
            val center = pts.fold(Offset.Zero) { acc, p -> acc + p } / pts.size
        }
    }
}
