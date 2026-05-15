package com.faster.aiboard.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ToolbarCustomizeDialog(
    currentColumns: Int,
    currentTools: Map<String, Boolean>,
    onSave: (Int, Map<String, Boolean>) -> Unit,
    onDismiss: () -> Unit
) {
    var columns by remember { mutableStateOf(currentColumns) }
    var toolVisibility by remember { mutableStateOf(currentTools) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("工具栏定制") },
        text = {
            Column {
                Text("排列列数", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3).forEach { n ->
                        FilterChip(
                            selected = columns == n,
                            onClick = { columns = n },
                            label = { Text("${n}列") }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("工具显隐", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                toolVisibility.entries.forEach { (tool, visible) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(tool, style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = visible,
                            onCheckedChange = { toolVisibility = toolVisibility + (tool to it) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(columns, toolVisibility) }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
