package com.faster.aiboard.ui.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AIPopup(
    quickQuestions: List<String>,
    onQuestionChanged: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .width(260.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onQuestionChanged(it)
                },
                placeholder = { Text("提问...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                singleLine = false
            )
            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickQuestions.take(3).forEach { q ->
                    SuggestionChip(
                        onClick = {
                            text = q
                            onQuestionChanged(q)
                        },
                        label = { Text(q, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("取消") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    onQuestionChanged(text)
                    onSend()
                }) { Text("发送") }
            }
        }
    }
}
