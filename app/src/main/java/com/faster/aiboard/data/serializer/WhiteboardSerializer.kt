package com.faster.aiboard.data.serializer

import com.faster.aiboard.data.model.*
import kotlinx.serialization.json.Json

object WhiteboardSerializer {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun serialize(whiteboard: Whiteboard): String {
        return json.encodeToString(Whiteboard.serializer(), whiteboard)
    }

    fun deserialize(data: String): Whiteboard {
        return json.decodeFromString(Whiteboard.serializer(), data)
    }

    fun serializeIndex(index: BoardIndex): String {
        return json.encodeToString(BoardIndex.serializer(), index)
    }

    fun deserializeIndex(data: String): BoardIndex {
        return json.decodeFromString(BoardIndex.serializer(), data)
    }
}
