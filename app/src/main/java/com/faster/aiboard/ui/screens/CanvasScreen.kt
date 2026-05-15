package com.faster.aiboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.faster.aiboard.ai.AIService
import com.faster.aiboard.data.repository.FileRepository
import com.faster.aiboard.settings.SettingsDataStore
import com.faster.aiboard.ui.ai.AIPopup
import com.faster.aiboard.ui.ai.AISessionViewModel
import com.faster.aiboard.ui.ai.AITagView
import com.faster.aiboard.ui.canvas.CanvasView
import com.faster.aiboard.ui.canvas.CanvasViewModel
import com.faster.aiboard.ui.canvas.Tool
import com.faster.aiboard.ui.toolbar.ColorStrip
import com.faster.aiboard.ui.toolbar.FloatingToolbar
import kotlinx.coroutines.launch

@Composable
fun CanvasScreen(
    boardId: String,
    onBack: () -> Unit
) {
    val viewModel = remember { CanvasViewModel() }
    val context = LocalContext.current
    val settingsDataStore = remember { SettingsDataStore(context) }
    val apiKey by settingsDataStore.apiKey.collectAsState(initial = "")
    val aiViewModel = remember {
        AISessionViewModel(
            onAnalyze = { question ->
                if (apiKey.isBlank()) return@AISessionViewModel "请先在设置中配置 API Key"
                val service = AIService(apiKey)
                // TODO: capture lasso region as Bitmap and pass to service
                service.analyzeImage("", question)
            }
        )
    }
    val repository = remember { FileRepository(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(boardId) {
        viewModel.init(boardId, repository)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CanvasView(
            state = viewModel.state,
            onDrawStart = viewModel::onDrawStart,
            onDrawMove = viewModel::onDrawMove,
            onDrawEnd = viewModel::onDrawEnd,
            onEraserMove = viewModel::onEraserMove,
            onSelectTap = viewModel::onSelectTap,
            onAILassoStart = {
                viewModel.onAILassoStart(it)
                aiViewModel.onLassoStart(it)
            },
            onAILassoMove = { pos ->
                viewModel.onAILassoMove(pos)
                aiViewModel.onLassoMove(pos)
            },
            onAILassoEnd = {
                viewModel.onAILassoEnd()
                aiViewModel.onLassoEnd()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top-left back button
        FilledTonalButton(
            onClick = {
                scope.launch { viewModel.save(); onBack() }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("← 返回")
        }

        // Right-side floating toolbar
        FloatingToolbar(
            activeTool = viewModel.state.activeTool,
            onToolSelected = { viewModel.setActiveTool(it) },
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        // Bottom color strip (only for Pen/Highlighter)
        if (viewModel.state.activeTool == Tool.Pen || viewModel.state.activeTool == Tool.Highlighter) {
            ColorStrip(
                selectedColor = viewModel.state.penColor,
                onColorSelected = { viewModel.setPenColor(it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }

        // AI popup dialog
        if (aiViewModel.aiState.showDialog) {
            Box(
                modifier = Modifier
                    .offset(
                        x = aiViewModel.aiState.dialogPosition.x.dp,
                        y = aiViewModel.aiState.dialogPosition.y.dp
                    )
            ) {
                AIPopup(
                    quickQuestions = aiViewModel.quickQuestions,
                    onQuestionChanged = { aiViewModel.setQuestion(it) },
                    onSend = { aiViewModel.sendQuestion() },
                    onDismiss = { aiViewModel.dismissDialog() }
                )
            }
        }

        // AI result tags
        for (session in aiViewModel.aiState.sessions) {
            for (tag in session.tags) {
                AITagView(
                    tag = tag,
                    modifier = Modifier
                        .offset(x = tag.positionX.dp, y = tag.positionY.dp)
                )
            }
        }
    }
}
