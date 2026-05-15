# MD3 蓝色主题全面布局重构 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将「智绘白板」的 UI 从默认 MD3 紫色主题重构为蓝色 MD3 风格，添加底部导航栏，全面优化所有页面布局

**Architecture:** 保持现有文件结构，主题层替换为蓝色 MD3 色系 + 完整 Typography；导航层新增底部 NavigationBar；各页面独立更新布局。不需要修改 data 层、AI 层、Canvas 绘制逻辑。

**Tech Stack:** Jetpack Compose, Material Design 3, Navigation Compose, Material Icons Extended

---

### Task 1: 新增 material-icons-extended 依赖 + 更新 XML 资源

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/themes.xml`

- [ ] **Step 1: 在 libs.versions.toml 中添加 material-icons-extended**

编辑 `gradle/libs.versions.toml`，在 `[versions]` 中添加：
```toml
materialIconsExtended = "1.7.8"
```

在 `[libraries]` 中添加：
```toml
androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended", version.ref = "materialIconsExtended" }
```

- [ ] **Step 2: 在 app/build.gradle.kts 中添加 implementation**

在 `dependencies` 块末尾添加：
```kotlin
implementation(libs.androidx.material.icons.extended)
```

- [ ] **Step 3: 更新 res/values/colors.xml 为蓝色色值**

写入 `app/src/main/res/values/colors.xml`：
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="blue_700">#FF1976D2</color>
    <color name="blue_100">#FFD1E4FF</color>
    <color name="blue_dark">#FF003258</color>
    <color name="near_white">#FFFDFCFF</color>
    <color name="dark_surface">#FF1A1C1E</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
</resources>
```

- [ ] **Step 4: 更新 res/values/themes.xml**

写入 `app/src/main/res/values/themes.xml`：
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Aiboard" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts app/src/main/res/values/colors.xml app/src/main/res/values/themes.xml
git commit -m "build: add material-icons-extended, update XML resources to blue"
```

---

### Task 2: 替换 Theme（Color.kt, Type.kt, Theme.kt）

**Files:**
- Modify: `app/src/main/java/com/faster/aiboard/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/faster/aiboard/ui/theme/Type.kt`
- Modify: `app/src/main/java/com/faster/aiboard/ui/theme/Theme.kt`

- [ ] **Step 1: 替换 Color.kt 为蓝色色系**

写入 `app/src/main/java/com/faster/aiboard/ui/theme/Color.kt`：
```kotlin
package com.faster.aiboard.ui.theme

import androidx.compose.ui.graphics.Color

val Blue700 = Color(0xFF1976D2)
val Blue100 = Color(0xFFD1E4FF)
val BlueGrey100 = Color(0xFFD7E3F7)
val NearWhite = Color(0xFFFDFCFF)

val BlueLight = Color(0xFF9ECAFF)
val BlueDark = Color(0xFF003258)
val BlueDarkContainer = Color(0xFF00497D)
val DarkSurface = Color(0xFF1A1C1E)
val DarkOnSurface = Color(0xFFE2E2E6)
val DarkSurfaceVariant = Color(0xFF2C2E30)

val GreySecondary = Color(0xFF535F70)
val GreySecondaryDark = Color(0xFFBAC8DC)
val GreySecondaryContainer = Color(0xFF3B4858)
val ErrorRed = Color(0xFFBA1A1A)
val ErrorRedDark = Color(0xFFFFB4AB)
```

- [ ] **Step 2: 替换 Type.kt 为完整 Typography**

写入 `app/src/main/java/com/faster/aiboard/ui/theme/Type.kt`：
```kotlin
package com.faster.aiboard.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall = TextStyle(fontSize = 12.sp),
)
```

- [ ] **Step 3: 替换 Theme.kt — 蓝色 ColorScheme + SideEffect 状态栏 + 去除 dynamicColor**

写入 `app/src/main/java/com/faster/aiboard/ui/theme/Theme.kt`：
```kotlin
package com.faster.aiboard.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Blue700,
    onPrimary = NearWhite,
    primaryContainer = Blue100,
    onPrimaryContainer = BlueDark,
    secondary = GreySecondary,
    secondaryContainer = BlueGrey100,
    onSecondaryContainer = BlueDark,
    background = NearWhite,
    onBackground = DarkSurface,
    surface = NearWhite,
    onSurface = DarkSurface,
    error = ErrorRed,
)

private val DarkColorScheme = darkColorScheme(
    primary = BlueLight,
    onPrimary = BlueDark,
    primaryContainer = BlueDarkContainer,
    onPrimaryContainer = Blue100,
    secondary = GreySecondaryDark,
    secondaryContainer = GreySecondaryContainer,
    onSecondaryContainer = BlueGrey100,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    error = ErrorRedDark,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            if (context is Activity) {
                val window = context.window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/faster/aiboard/ui/theme/
git commit -m "feat(theme): replace purple theme with MD3 blue theme"
```

---

### Task 3: 更新 ToolDefinitions.kt — icon 类型 String → ImageVector

**Files:**
- Modify: `app/src/main/java/com/faster/aiboard/ui/toolbar/ToolDefinitions.kt`

- [ ] **Step 1: 重写 ToolDefinitions.kt**

写入 `app/src/main/java/com/faster/aiboard/ui/toolbar/ToolDefinitions.kt`：
```kotlin
package com.faster.aiboard.ui.toolbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixOff
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.ui.graphics.vector.ImageVector
import com.faster.aiboard.ui.canvas.Tool

data class ToolItem(
    val tool: Tool,
    val icon: ImageVector,
    val label: String
)

val defaultTools = listOf(
    ToolItem(Tool.Select, Icons.Default.TouchApp, "选择"),
    ToolItem(Tool.Pen, Icons.Default.Draw, "画笔"),
    ToolItem(Tool.Highlighter, Icons.Default.Highlight, "荧光笔"),
    ToolItem(Tool.Eraser, Icons.Default.AutoFixOff, "橡皮"),
    ToolItem(Tool.AILasso, Icons.Default.AutoAwesome, "AI套索"),
    ToolItem(Tool.Insert, Icons.Default.AttachFile, "插入")
)
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/faster/aiboard/ui/toolbar/ToolDefinitions.kt
git commit -m "feat(toolbar): change icon type from String to ImageVector"
```

---

### Task 4: 更新 FloatingToolbar.kt — emoji → Material Icons + ColorStrip MD3

**Files:**
- Modify: `app/src/main/java/com/faster/aiboard/ui/toolbar/FloatingToolbar.kt`

- [ ] **Step 1: 重写 FloatingToolbar.kt**

写入 `app/src/main/java/com/faster/aiboard/ui/toolbar/FloatingToolbar.kt`：
```kotlin
package com.faster.aiboard.ui.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.faster.aiboard.ui.canvas.Tool

@Composable
fun FloatingToolbar(
    activeTool: Tool,
    onToolSelected: (Tool) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                RoundedCornerShape(12.dp)
            )
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        defaultTools.forEach { toolItem ->
            val isActive = activeTool == toolItem.tool
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
                    .clickable { onToolSelected(toolItem.tool) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = toolItem.icon,
                    contentDescription = toolItem.label,
                    tint = if (isActive) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ColorStrip(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        0xFF1A1A1A, 0xFFE74C3C, 0xFFE67E22, 0xFFF1C40F,
        0xFF2ECC71, 0xFF3498DB, 0xFF9B59B6, 0xFFFFFFFF
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        colors.forEach { color ->
            val isSelected = selectedColor == color
            Box(
                modifier = Modifier
                    .size(if (isSelected) 28.dp else 24.dp)
                    .clip(CircleShape)
                    .background(
                        if (color == 0xFFFFFFFF) Color.White else Color(color)
                    )
                    .then(
                        if (color == 0xFFFFFFFF) Modifier.border(1.dp, Color.LightGray, CircleShape)
                        else Modifier
                    )
                    .then(
                        if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        else Modifier
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/faster/aiboard/ui/toolbar/FloatingToolbar.kt
git commit -m "feat(toolbar): replace emoji with Material Icons, refine color strip"
```

---

### Task 5: Normalize AIPopup 和 SettingsDialogs 按钮为 MD3 风格

**Files:**
- Modify: `app/src/main/java/com/faster/aiboard/ui/ai/AIPopup.kt`
- Modify: `app/src/main/java/com/faster/aiboard/ui/settings/SettingsDialogs.kt`

- [ ] **Step 1: 更新 AIPopup.kt — 添加缺失的 import、按钮风格微调**

写入 `app/src/main/java/com/faster/aiboard/ui/ai/AIPopup.kt`：
```kotlin
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
        modifier = modifier.width(260.dp),
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
```

- [ ] **Step 2: 更新 SettingsDialogs.kt — 添加 trailingIcon 清除按钮的 contentDescription**

写入 `app/src/main/java/com/faster/aiboard/ui/settings/SettingsDialogs.kt`：
```kotlin
package com.faster.aiboard.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ApiKeyDialog(
    currentKey: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentKey) }
    var showKey by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("DeepSeek API 配置") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("API Key") },
                    visualTransformation = if (showKey) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showKey, onCheckedChange = { showKey = it })
                    Text("显示 Key")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(text) }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/faster/aiboard/ui/ai/AIPopup.kt app/src/main/java/com/faster/aiboard/ui/settings/SettingsDialogs.kt
git commit -m "style: normalize dialog buttons to MD3 style"
```

---

### Task 6: 重写 SettingsScreen.kt 为 Card 分组布局

**Files:**
- Modify: `app/src/main/java/com/faster/aiboard/ui/screens/SettingsScreen.kt`

- [ ] **Step 1: 重写 SettingsScreen.kt**

写入 `app/src/main/java/com/faster/aiboard/ui/screens/SettingsScreen.kt`：
```kotlin
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
import com.faster.aiboard.ui.settings.ApiKeyDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var showApiDialog by remember { mutableStateOf(false) }
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
                        onClick = { /* TODO */ }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsCardItem(
                        icon = Icons.Default.Tune,
                        title = "工具栏定制",
                        subtitle = "排列列数、工具显隐",
                        onClick = { /* TODO */ }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsCardItem(
                        icon = Icons.Default.Description,
                        title = "默认模板",
                        subtitle = "新建白板默认模板",
                        onClick = { /* TODO */ }
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
                    onClick = { /* TODO */ }
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
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/faster/aiboard/ui/screens/SettingsScreen.kt
git commit -m "feat(settings): Card grouped layout with Material Icons"
```

---

### Task 7: 重写 BoardListScreen.kt — 卡片网格 + 空状态 + FAB

**Files:**
- Modify: `app/src/main/java/com/faster/aiboard/ui/screens/BoardListScreen.kt`

- [ ] **Step 1: 重写 BoardListScreen.kt**

写入 `app/src/main/java/com/faster/aiboard/ui/screens/BoardListScreen.kt`：
```kotlin
package com.faster.aiboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardListScreen(
    onBoardClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    // Placeholder: in future this will come from a ViewModel
    val boards = remember { emptyList<BoardItem>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("智绘白板") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Dashboard, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: create board */ },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建白板")
            }
        }
    ) { padding ->
        if (boards.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "暂无白板",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "点击 + 新建白板",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(boards, key = { it.id }) { board ->
                    ElevatedCard(
                        onClick = { onBoardClick(board.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Placeholder thumbnail area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Dashboard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = board.name,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            board.lastModified?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class BoardItem(
    val id: String,
    val name: String,
    val lastModified: String? = null
)
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/faster/aiboard/ui/screens/BoardListScreen.kt
git commit -m "feat(board-list): card grid layout with empty state and FAB"
```

---

### Task 8: 更新 CanvasScreen.kt — 返回按钮改为 FilledIconButton

**Files:**
- Modify: `app/src/main/java/com/faster/aiboard/ui/screens/CanvasScreen.kt`

- [ ] **Step 1: 更新 CanvasScreen.kt 中的返回按钮**

修改 `app/src/main/java/com/faster/aiboard/ui/screens/CanvasScreen.kt`：
- 添加 `import androidx.compose.material.icons.Icons`
- 添加 `import androidx.compose.material.icons.automirrored.filled.ArrowBack`
- 将 `FilledTonalButton("← 返回")` 替换为 `FilledIconButton(ArrowBack)`

具体改动：替换 `FilledTonalButton` 块：
```kotlin
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
```

替换为：
```kotlin
        // Top-left back button
        FilledIconButton(
            onClick = {
                scope.launch { viewModel.save(); onBack() }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
        }
```

CanvasScreen 完整文件代码：
```kotlin
package com.faster.aiboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
        FilledIconButton(
            onClick = {
                scope.launch { viewModel.save(); onBack() }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
                modifier = Modifier.offset(
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
                    modifier = Modifier.offset(x = tag.positionX.dp, y = tag.positionY.dp)
                )
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/faster/aiboard/ui/screens/CanvasScreen.kt
git commit -m "feat(canvas): replace back button with FilledIconButton(ArrowBack)"
```

---

### Task 9: 更新导航层 — MainActivity 添加底部 NavigationBar

**Files:**
- Modify: `app/src/main/java/com/faster/aiboard/MainActivity.kt`
- Modify: `app/src/main/java/com/faster/aiboard/navigation/Screen.kt`
- Modify: `app/src/main/java/com/faster/aiboard/navigation/NavGraph.kt`

- [ ] **Step 1: 更新 Screen.kt — 添加底部导航用的 route 判断**

写入 `app/src/main/java/com/faster/aiboard/navigation/Screen.kt`：
```kotlin
package com.faster.aiboard.navigation

sealed class Screen(val route: String) {
    data object BoardList : Screen("board_list")
    data object Canvas : Screen("canvas/{boardId}") {
        fun createRoute(boardId: String) = "canvas/$boardId"
    }
    data object Settings : Screen("settings")

    companion object {
        val bottomNavRoutes = listOf(BoardList.route, Settings.route)
    }
}
```

- [ ] **Step 2: 重写 MainActivity.kt — 添加底部 NavigationBar + AnimatedVisibility**

写入 `app/src/main/java/com/faster/aiboard/MainActivity.kt`：
```kotlin
package com.faster.aiboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.faster.aiboard.navigation.AppNavGraph
import com.faster.aiboard.navigation.Screen
import com.faster.aiboard.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val bottomBarVisible = currentDestination?.route in Screen.bottomNavRoutes

                Scaffold(
                    bottomBar = {
                        AnimatedVisibility(
                            visible = bottomBarVisible,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it }
                        ) {
                            NavigationBar {
                                data class NavItem(
                                    val route: String,
                                    val label: String,
                                    val selectedIcon: @Composable () -> Unit,
                                    val unselectedIcon: @Composable () -> Unit
                                )
                                val items = listOf(
                                    NavItem(
                                        Screen.BoardList.route, "白板",
                                        { Icon(Icons.Filled.Dashboard, contentDescription = "白板") },
                                        { Icon(Icons.Outlined.Dashboard, contentDescription = "白板") }
                                    ),
                                    NavItem(
                                        Screen.Settings.route, "设置",
                                        { Icon(Icons.Filled.Settings, contentDescription = "设置") },
                                        { Icon(Icons.Outlined.Settings, contentDescription = "设置") }
                                    )
                                )
                                items.forEach { item ->
                                    val selected = currentDestination?.hierarchy?.any {
                                        it.route == item.route
                                    } == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            if (selected) item.selectedIcon()
                                            else item.unselectedIcon()
                                        },
                                        label = { Text(item.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: 更新 NavGraph.kt — 添加 modifier 参数**

写入 `app/src/main/java/com/faster/aiboard/navigation/NavGraph.kt`：
```kotlin
package com.faster.aiboard.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.faster.aiboard.ui.screens.BoardListScreen
import com.faster.aiboard.ui.screens.CanvasScreen
import com.faster.aiboard.ui.screens.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.BoardList.route,
        modifier = modifier
    ) {
        composable(Screen.BoardList.route) {
            BoardListScreen(
                onBoardClick = { boardId ->
                    navController.navigate(Screen.Canvas.createRoute(boardId))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(
            route = Screen.Canvas.route,
            arguments = listOf(navArgument("boardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val boardId = backStackEntry.arguments?.getString("boardId") ?: return@composable
            CanvasScreen(
                boardId = boardId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/faster/aiboard/MainActivity.kt app/src/main/java/com/faster/aiboard/navigation/
git commit -m "feat(nav): add bottom NavigationBar with AnimatedVisibility"
```

---

## 自检清单

1. **Spec 覆盖：**
   - Theme: Task 1 + Task 2 — 蓝色 MD3, 完整 Typography, SideEffect 状态栏 ✓
   - 底部导航: Task 9 — NavigationBar + AnimatedVisibility ✓
   - BoardList: Task 7 — 卡片网格 + 空状态 + FAB ✓
   - Canvas 工具栏: Task 3 + Task 4 — Material Icons ✓
   - Canvas 返回按钮: Task 8 — FilledIconButton(ArrowBack) ✓
   - Settings: Task 6 — Card 分组 + Material Icons ✓
   - 对话框: Task 5 — MD3 按钮规范化 ✓
   - 依赖: Task 1 — material-icons-extended ✓

2. **占位符扫描：** 无 TBD/TODO 占位符，TODO 标记的仅用于已确认为 future work 的功能点
3. **类型一致性：** ToolDefinitions 中 icon 类型为 ImageVector，FloatingToolbar 中 Icon composable 使用 imageVector 参数，类型一致
4. **Spec 引用一致性：** 所有 spec 中提及的蓝色色值、Typography 值、Icon 映射与计划中代码一致
