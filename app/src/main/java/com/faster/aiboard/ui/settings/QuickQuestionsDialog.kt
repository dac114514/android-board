package com.faster.aiboard.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuickQuestionsDialog(
    currentQuestions: List<String>,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var items by remember {
        mutableStateOf(currentQuestions.toMutableList().ifEmpty { mutableListOf("回答问题", "解释", "翻译") })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("快捷问答管理") },
        text = {
            Column {
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    itemsIndexed(items, key = { i, _ -> i }) { index, q ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            OutlinedTextField(
                                value = q,
                                onValueChange = { items = items.toMutableList().apply { set(index, it) } },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.width(4.dp))
                            IconButton(onClick = { items = items.toMutableList().apply { removeAt(index) } }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                            }
                        }
                    }
                }
                TextButton(onClick = { items = items + "" }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("添加")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(items.filter { it.isNotBlank() }) }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
