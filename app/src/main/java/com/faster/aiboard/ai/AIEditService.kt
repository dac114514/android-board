package com.faster.aiboard.ai

import com.faster.aiboard.data.model.CanvasElement
import com.faster.aiboard.data.model.Stroke

/**
 * Reserved interface for future AI direct editing features.
 * Not yet implemented — UI entry points are disabled.
 */
interface AIEditService {
    suspend fun editElement(
        element: CanvasElement,
        instruction: String
    ): CanvasElement

    suspend fun generateContent(
        instruction: String,
        context: List<CanvasElement>
    ): List<CanvasElement>

    suspend fun beautifyStroke(
        stroke: Stroke
    ): Stroke
}

sealed interface AIEditAction {
    data class Modify(val elementId: String, val instruction: String) : AIEditAction
    data class Generate(val instruction: String) : AIEditAction
    data class Beautify(val elementId: String) : AIEditAction
}
