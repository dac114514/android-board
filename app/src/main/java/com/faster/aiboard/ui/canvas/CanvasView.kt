package com.faster.aiboard.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.faster.aiboard.data.model.*
import com.faster.aiboard.data.model.Stroke as ModelStroke

fun Path.applyPoints(points: List<Offset>) {
    if (points.isEmpty()) return
    moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size) {
        lineTo(points[i].x, points[i].y)
    }
}

@Composable
fun CanvasView(
    state: CanvasState,
    onDrawStart: (Offset) -> Unit,
    onDrawMove: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    onAILassoStart: (Offset) -> Unit = {},
    onAILassoMove: (Offset) -> Unit = {},
    onAILassoEnd: () -> Unit = {},
    onEraserMove: (Offset) -> Unit = {},
    onSelectTap: (Offset) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
            .pointerInput(state.activeTool) {
                when (state.activeTool) {
                    Tool.Pen, Tool.Highlighter -> {
                        detectDragGestures(
                            onDragStart = { onDrawStart(it) },
                            onDrag = { change, _ -> change.consume(); onDrawMove(change.position) },
                            onDragEnd = onDrawEnd
                        )
                    }
                    Tool.AILasso -> {
                        detectDragGestures(
                            onDragStart = { onAILassoStart(it) },
                            onDrag = { change, _ -> change.consume(); onAILassoMove(change.position) },
                            onDragEnd = onAILassoEnd
                        )
                    }
                    Tool.Eraser -> {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                change.consume()
                                onEraserMove(change.position)
                            }
                        )
                    }
                    Tool.Select -> {
                        detectTapGestures(
                            onTap = { onSelectTap(it) }
                        )
                    }
                    else -> {}
                }
            }
    ) {
        // Layer 1: Template grid
        drawTemplateGrid(state)

        // Layer 2: Existing elements
        for (element in state.elements) {
            drawElement(element, state)
        }

        // Layer 3: Current stroke in progress
        if (state.currentStrokePoints.size >= 2) {
            val path = Path().apply { applyPoints(state.currentStrokePoints) }
            drawPath(
                path = path,
                color = Color(state.currentStrokeColor),
                style = Stroke(
                    width = state.currentStrokeWidth * (1f / state.canvasScale),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                alpha = if (state.activeTool == Tool.Highlighter) 0.4f else 1f
            )
        }

        // AI lasso overlay
        val lassoPts = state.aiLassoPoints
        if (lassoPts.size >= 3) {
            val lassoPath = Path().apply {
                applyPoints(lassoPts)
                close()
            }
            drawPath(
                path = lassoPath,
                color = Color(0x40FFA500),
                style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

private fun DrawScope.drawTemplateGrid(state: CanvasState) {
    val gridColor = Color(0xFFE0E0E0)
    val gridSpacing = 40f * state.canvasScale
    val offset = state.canvasOffset
    val startX = offset.x % gridSpacing
    val startY = offset.y % gridSpacing
    var x = startX
    while (x < size.width) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 0.5f)
        x += gridSpacing
    }
    var y = startY
    while (y < size.height) {
        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5f)
        y += gridSpacing
    }
}

private fun DrawScope.drawElement(element: CanvasElement, state: CanvasState) {
    when (element) {
        is ModelStroke -> {
            if (element.points.size < 2) return
            val path = Path().apply {
                val pts = element.points.map { Offset(it.x, it.y) }
                applyPoints(pts)
            }
            drawPath(
                path = path,
                color = Color(element.color),
                style = Stroke(
                    width = element.strokeWidth * (1f / state.canvasScale),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                alpha = element.alpha
            )
            if (element.id in state.selectedElementIds) {
                val bounds = path.getBounds()
                drawSelectionHandles(bounds)
            }
        }
        is ImageElement -> { /* TODO: load and draw bitmap */ }
        is TextBlock -> { /* TODO: draw text */ }
    }
}

private fun DrawScope.drawSelectionHandles(bounds: androidx.compose.ui.geometry.Rect) {
    val handleColor = Color(0xFF2196F3)
    val handleSize = 8f
    val points = listOf(
        bounds.topLeft, bounds.topRight,
        bounds.bottomLeft, bounds.bottomRight,
        Offset(bounds.center.x, bounds.top),
        Offset(bounds.center.x, bounds.bottom),
        Offset(bounds.left, bounds.center.y),
        Offset(bounds.right, bounds.center.y)
    )
    for (pt in points) {
        drawCircle(handleColor, handleSize, pt)
    }
}
