package com.faster.aiboard.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.faster.aiboard.data.model.BoardListItem
import com.faster.aiboard.data.repository.FileRepository

data class BoardListState(
    val boards: List<BoardListItem> = emptyList(),
    val isLoading: Boolean = true,
    val showCreateDialog: Boolean = false,
    val newBoardName: String = "",
    val selectedTemplate: String = "blank"
)

class BoardViewModel(private val repository: FileRepository) {
    var state by mutableStateOf(BoardListState())
        private set

    fun loadBoards() {
        val index = repository.loadIndex()
        state = state.copy(
            boards = index.boards.sortedByDescending { it.updatedAt },
            isLoading = false
        )
    }

    fun showCreateDialog() {
        state = state.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        state = state.copy(showCreateDialog = false, newBoardName = "")
    }

    fun setBoardName(name: String) {
        state = state.copy(newBoardName = name)
    }

    fun setTemplate(template: String) {
        state = state.copy(selectedTemplate = template)
    }

    suspend fun createBoard(): String? {
        val name = state.newBoardName.ifBlank { "未命名白板" }
        val id = repository.createBoard(name, state.selectedTemplate)
        loadBoards()
        state = state.copy(showCreateDialog = false, newBoardName = "")
        return id
    }
}
