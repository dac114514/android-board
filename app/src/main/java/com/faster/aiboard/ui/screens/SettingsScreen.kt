package com.faster.aiboard.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.faster.aiboard.settings.SettingsDataStore
import com.faster.aiboard.ui.settings.ApiKeyDialog
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var showApiDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = remember { SettingsDataStore(context) }
    val apiKey by dataStore.apiKey.collectAsState(initial = "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SettingsItem(
                title = "DeepSeek API 配置",
                subtitle = "API Key、模型选择",
                onClick = { showApiDialog = true }
            )
            SettingsItem(
                title = "快捷问答管理",
                subtitle = "预设提问词条",
                onClick = { /* TODO: implement */ }
            )
            SettingsItem(
                title = "工具栏定制",
                subtitle = "排列列数、工具显隐",
                onClick = { /* TODO: implement */ }
            )
            SettingsItem(
                title = "默认模板",
                subtitle = "新建白板默认模板",
                onClick = { /* TODO: implement */ }
            )
            SettingsItem(
                title = "关于",
                subtitle = "版本号、许可",
                onClick = { /* TODO: implement */ }
            )
        }
    }

    if (showApiDialog) {
        ApiKeyDialog(
            currentKey = apiKey,
            onSave = { key ->
                scope.launch { dataStore.saveApiKey(key) }
                showApiDialog = false
            },
            onDismiss = { showApiDialog = false }
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider()
    }
}
