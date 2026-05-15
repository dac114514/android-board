package com.faster.aiboard.data.repository

import android.content.Context
import com.faster.aiboard.data.model.*
import com.faster.aiboard.data.serializer.WhiteboardSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class FileRepository(private val context: Context) {

    private val rootDir: File get() = File(context.filesDir, "whiteboards")

    private fun boardDir(boardId: String) = File(rootDir, boardId)

    fun loadIndex(): BoardIndex {
        val file = File(rootDir, "index.json")
        if (!file.exists()) return BoardIndex()
        val data = file.readText()
        return WhiteboardSerializer.deserializeIndex(data)
    }

    suspend fun saveIndex(index: BoardIndex) = withContext(Dispatchers.IO) {
        rootDir.mkdirs()
        val file = File(rootDir, "index.json")
        file.writeText(WhiteboardSerializer.serializeIndex(index))
    }

    suspend fun loadBoard(boardId: String): Whiteboard? = withContext(Dispatchers.IO) {
        val file = File(boardDir(boardId), "board.json")
        if (!file.exists()) return@withContext null
        val data = file.readText()
        WhiteboardSerializer.deserialize(data)
    }

    suspend fun saveBoard(boardId: String, whiteboard: Whiteboard) = withContext(Dispatchers.IO) {
        boardDir(boardId).mkdirs()
        val file = File(boardDir(boardId), "board.json")
        file.writeText(WhiteboardSerializer.serialize(whiteboard))
    }

    suspend fun createBoard(name: String, template: String): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val now = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
        val whiteboard = Whiteboard(
            board = BoardMeta(name = name, template = template, createdAt = now, updatedAt = now)
        )
        saveBoard(id, whiteboard)

        val index = loadIndex()
        val newItem = BoardListItem(id = id, name = name, template = template, createdAt = now, updatedAt = now)
        saveIndex(index.copy(boards = index.boards + newItem))
        id
    }
}
