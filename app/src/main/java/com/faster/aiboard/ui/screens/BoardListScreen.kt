package com.faster.aiboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BoardListScreen(
    onBoardClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("智绘白板") },
                actions = {
                    TextButton(onClick = onSettingsClick) { Text("设置") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: create board */ }) {
                Text("+")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("暂无白板，点击 + 新建", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
