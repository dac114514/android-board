package com.faster.aiboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AISession(
    val id: String,
    val lassoPoints: List<PointF>,
    val tags: List<AITag> = emptyList()
)

@Serializable
data class AITag(
    val id: String,
    val question: String,
    val answer: String,
    val positionX: Float,
    val positionY: Float,
    val expanded: Boolean = true
)
