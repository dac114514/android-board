package com.faster.aiboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Transform(
    val x: Float = 0f,
    val y: Float = 0f,
    val rotation: Float = 0f,
    val scale: Float = 1f
)

@Serializable
sealed interface CanvasElement {
    val id: String
    val transform: Transform
}

@Serializable
data class Stroke(
    override val id: String,
    val points: List<PointF>,
    val color: Long,
    val strokeWidth: Float,
    val alpha: Float = 1f,
    val isHighlighter: Boolean = false,
    override val transform: Transform = Transform()
) : CanvasElement

@Serializable
data class PointF(val x: Float, val y: Float)

@Serializable
data class ImageElement(
    override val id: String,
    val bitmapRef: String,
    override val transform: Transform = Transform()
) : CanvasElement

@Serializable
data class TextBlock(
    override val id: String,
    val content: String,
    val fontSize: Float = 16f,
    val color: Long = 0xFF1A1A1A,
    override val transform: Transform = Transform()
) : CanvasElement
