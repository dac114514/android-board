package com.faster.aiboard.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.faster.aiboard.data.model.AITag

@Composable
fun AITagView(
    tag: AITag,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(tag.expanded) }

    Card(
        modifier = modifier
            .width(220.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = tag.question,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(if (expanded) "▲" else "▼")
            }

            AnimatedVisibility(visible = expanded) {
                Text(
                    text = tag.answer.ifEmpty { "等待 AI 回复..." },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
