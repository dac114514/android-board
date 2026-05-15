package com.faster.aiboard.ui.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.faster.aiboard.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

data class AISessionState(
    val isSelecting: Boolean = false,
    val lassoPoints: List<Offset> = emptyList(),
    val showDialog: Boolean = false,
    val dialogPosition: Offset = Offset.Zero,
    val sessions: List<AISession> = emptyList(),
    val currentQuestion: String = ""
)

class AISessionViewModel(
    private val onAnalyze: suspend (question: String) -> String = { "" }
) {
    var aiState by mutableStateOf(AISessionState())
        private set

    val quickQuestions = listOf("回答问题", "解释", "翻译")

    fun onLassoStart(offset: Offset) {
        aiState = aiState.copy(
            isSelecting = true,
            lassoPoints = listOf(offset)
        )
    }

    fun onLassoMove(offset: Offset) {
        if (aiState.isSelecting) {
            aiState = aiState.copy(
                lassoPoints = aiState.lassoPoints + offset
            )
        }
    }

    fun onLassoEnd() {
        if (aiState.lassoPoints.size >= 3) {
            val center = aiState.lassoPoints.fold(Offset.Zero) { acc, p -> acc + p } /
                    aiState.lassoPoints.size.coerceAtLeast(1).toFloat()
            aiState = aiState.copy(
                isSelecting = false,
                showDialog = true,
                dialogPosition = center + Offset(100f, 0f)
            )
        } else {
            aiState = aiState.copy(isSelecting = false, lassoPoints = emptyList())
        }
    }

    fun dismissDialog() {
        aiState = aiState.copy(showDialog = false, lassoPoints = emptyList())
    }

    fun setQuestion(q: String) {
        aiState = aiState.copy(currentQuestion = q)
    }

    fun sendQuestion() {
        val q = aiState.currentQuestion
        if (q.isBlank()) return

        val tagId = UUID.randomUUID().toString()
        val newTag = AITag(
            id = tagId,
            question = q,
            answer = "",
            positionX = aiState.dialogPosition.x,
            positionY = aiState.dialogPosition.y
        )

        val sessionId = UUID.randomUUID().toString()
        val newSession = AISession(
            id = sessionId,
            lassoPoints = aiState.lassoPoints.map { PointF(it.x, it.y) },
            tags = listOf(newTag)
        )

        aiState = aiState.copy(
            sessions = aiState.sessions + newSession,
            showDialog = false,
            lassoPoints = emptyList(),
            currentQuestion = ""
        )

        // Call AI analysis in background
        CoroutineScope(Dispatchers.IO).launch {
            val answer = onAnalyze(q)
            aiState = aiState.copy(
                sessions = aiState.sessions.map { session ->
                    if (session.id == sessionId) {
                        session.copy(tags = session.tags.map { tag ->
                            if (tag.id == tagId) tag.copy(answer = answer) else tag
                        })
                    } else session
                }
            )
        }
    }

    fun addToExistingSession(tag: AITag, sessionId: String) {
        aiState = aiState.copy(
            sessions = aiState.sessions.map {
                if (it.id == sessionId) it.copy(tags = it.tags + tag) else it
            }
        )
    }
}
