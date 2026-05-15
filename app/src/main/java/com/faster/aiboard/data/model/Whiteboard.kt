package com.faster.aiboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Whiteboard(
    val version: Int = 1,
    val board: BoardMeta,
    val elements: List<CanvasElement> = emptyList(),
    val aiSessions: List<AISession> = emptyList()
)

@Serializable
data class BoardMeta(
    val name: String,
    val template: String = "blank",
    val width: Int = 1920,
    val height: Int = 1080,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class BoardListItem(
    val id: String,
    val name: String,
    val template: String,
    val thumbnailPath: String = "",
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BoardIndex(
    val boards: List<BoardListItem> = emptyList()
)
