package com.faster.aiboard.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.faster.aiboard.settings.SettingsDataStore
import com.faster.aiboard.ui.settings.AboutDialog
import com.faster.aiboard.ui.settings.ApiKeyDialog
import com.faster.aiboard.ui.settings.DefaultTemplateDialog
import com.faster.aiboard.ui.settings.QuickQuestionsDialog
import com.faster.aiboard.ui.settings.ToolbarCustomizeDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var showApiDialog by remember { mutableStateOf(false) }
    var showQuickQuestions by remember { mutableStateOf(false) }
    var showToolbarCustomize by remember { mutableStateOf(false) }
    var showDefaultTemplate by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = remember { SettingsDataStore(context) }
    val apiKey by dataStore.apiKey.collectAsState(initial = "")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("设置") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // AI Config Card
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                SettingsCardItem(
                    icon = Icons.Default.Key,
                    title = "DeepSeek API 配置",
                    subtitle = "API Key、模型选择",
                    onClick = { showApiDialog = true }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Whiteboard Settings Card
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                Column {
                    SettingsCardItem(
                        icon = Icons.Default.QuestionAnswer,
                        title = "快捷问答管理",
                        subtitle = "预设提问词条",
                        onClick = { showQuickQuestions = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsCardItem(
                        icon = Icons.Default.Tune,
                        title = "工具栏定制",
                        subtitle = "排列列数、工具显隐",
                        onClick = { showToolbarCustomize = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsCardItem(
                        icon = Icons.Default.Description,
                        title = "默认模板",
                        subtitle = "新建白板默认模板",
                        onClick = { showDefaultTemplate = true }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // About Card
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                SettingsCardItem(
                    icon = Icons.Default.Info,
                    title = "关于",
                    subtitle = "版本号、许可",
                    onClick = { showAbout = true }
                )
            }

            Spacer(Modifier.height(24.dp))
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

    if (showQuickQuestions) {
        QuickQuestionsDialog(
            currentQuestions = emptyList(),
            onSave = { showQuickQuestions = false },
            onDismiss = { showQuickQuestions = false }
        )
    }

    if (showToolbarCustomize) {
        ToolbarCustomizeDialog(
            currentColumns = 1,
            currentTools = mapOf(
                "选择" to true,
                "画笔" to true,
                "荧光笔" to true,
                "橡皮" to true,
                "AI套索" to true,
                "插入" to true
            ),
            onSave = { _, _ -> showToolbarCustomize = false },
            onDismiss = { showToolbarCustomize = false }
        )
    }

    if (showDefaultTemplate) {
        DefaultTemplateDialog(
            currentTemplate = "blank",
            onSave = { showDefaultTemplate = false },
            onDismiss = { showDefaultTemplate = false }
        )
    }

    if (showAbout) {
        AboutDialog(
            onDismiss = { showAbout = false }
        )
    }
}

@Composable
private fun SettingsCardItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = ">",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
