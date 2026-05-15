# 智绘白板 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the AI-powered whiteboard app for tablet paperless learning.

**Architecture:** Single-Activity + Navigation Compose with 3 screens (BoardList → Canvas → Settings). Canvas rendering is self-built on Compose Canvas with 6-layer pipeline. AI integration via DeepSeek Vision API. Data persisted as JSON files on local storage.

**Tech Stack:** Kotlin 2.3.10, AGP 9.0.0, Compose BOM 2026.01.01, Navigation Compose, Kotlinx Serialization, DataStore Preferences, OkHttp (DeepSeek API)

---

### Task 1: Rename package to com.faster.aiboard

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Rename: `app/src/main/java/com/java/myapplication/` → `app/src/main/java/com/faster/aiboard/`

- [ ] **Step 1: Update build.gradle.kts**

Change namespace and applicationId:

```kotlin
android {
    namespace = "com.faster.aiboard"
    defaultConfig {
        applicationId = "com.faster.aiboard"
    }
}
```

- [ ] **Step 2: Rename directory**

Move `app/src/main/java/com/java/myapplication/` to `app/src/main/java/com/faster/aiboard/`

```bash
git mv app/src/main/java/com/java/myapplication app/src/main/java/com/faster/aiboard
git rm -rf app/src/main/java/com/java 2>/dev/null; rmdir app/src/main/java/com/java 2>/dev/null; true
```

- [ ] **Step 3: Update AndroidManifest.xml**

The `package` attribute is no longer needed in modern AGP, but ensure the activity reference still resolves. Change:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application ...>
        <activity android:name="com.faster.aiboard.MainActivity" ...>
```

- [ ] **Step 4: Self-check code correctness**

Verify by reading the modified files: imports are correct, no dangling references to old package name.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: rename package to com.faster.aiboard"
```

---

### Task 2: Update dependencies (Navigation + Serialization + OkHttp)

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add versions to libs.versions.toml**

```toml
[versions]
# ... existing versions ...
navigationCompose = "2.8.5"
kotlinxSerializationJson = "1.7.3"
okhttp = "4.12.0"
datastorePreferences = "1.1.1"

[libraries]
# ... existing libraries ...
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }

[plugins]
# ... existing plugins ...
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- [ ] **Step 2: Apply plugins and dependencies in app/build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // ... existing dependencies ...
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.androidx.datastore.preferences)
}
```

- [ ] **Step 3: Self-check code correctness**

Verify by reading the modified files: imports are correct, no dangling references to old package name.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "chore: add navigation, serialization, okhttp, datastore dependencies"
```

---

### Task 3: Data model layer

**Files:**
- Create: `app/src/main/java/com/faster/aiboard/data/model/CanvasElement.kt`
- Create: `app/src/main/java/com/faster/aiboard/data/model/Whiteboard.kt`
- Create: `app/src/main/java/com/faster/aiboard/data/model/AISession.kt`

- [ ] **Step 1: Create CanvasElement.kt**

```kotlin
package com.faster.aiboard.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import kotlinx.serialization.Serializable

@Serializable
data class Transform(
    val x: Float = 0f,
    val y: Float = 0f,
    val rotation: Float = 0f,
    val scale: Float = 1f
)

@Serializable
sealed interface CanvasElement {
    val id: String
    val transform: Transform
}

@Serializable
data class Stroke(
    override val id: String,
    val points: List<PointF>,
    val color: Long,          // Color.value.toLong()
    val strokeWidth: Float,
    val alpha: Float = 1f,
    val isHighlighter: Boolean = false,
    override val transform: Transform = Transform()
) : CanvasElement

@Serializable
data class PointF(val x: Float, val y: Float)

@Serializable
data class ImageElement(
    override val id: String,
    val bitmapRef: String,    // relative path "images/xxx.jpg"
    override val transform: Transform = Transform()
) : CanvasElement

@Serializable
data class TextBlock(
    override val id: String,
    val content: String,
    val fontSize: Float = 16f,
    val color: Long = 0xFF1A1A1A,
    override val transform: Transform = Transform()
) : CanvasElement
```

- [ ] **Step 2: Create Whiteboard.kt**

```kotlin
package com.faster.aiboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Whiteboard(
    val version: Int = 1,
    val board: BoardMeta,
    val elements: List<CanvasElement> = emptyList(),
    val aiSessions: List<AISession> = emptyList()
)

@Serializable
data class BoardMeta(
    val name: String,
    val template: String = "blank",   // blank | lined | grid | music
    val width: Int = 1920,
    val height: Int = 1080,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class BoardListItem(
    val id: String,
    val name: String,
    val template: String,
    val thumbnailPath: String = "",
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BoardIndex(
    val boards: List<BoardListItem> = emptyList()
)
```

- [ ] **Step 3: Create AISession.kt**

```kotlin
package com.faster.aiboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AISession(
    val id: String,
    val lassoPoints: List<PointF>,
    val tags: List<AITag> = emptyList()
)

@Serializable
data class AITag(
    val id: String,
    val question: String,
    val answer: String,
    val positionX: Float,
    val positionY: Float,
    val expanded: Boolean = true
)
```

- [ ] **Step 4: Self-check code correctness**

Verify by reading the modified files: imports are correct, no dangling references to old package name.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add data model layer (CanvasElement, Whiteboard, AISession)"
```

---

### Task 4: Navigation + Screen scaffold

**Files:**
- Create: `app/src/main/java/com/faster/aiboard/navigation/Screen.kt`
- Create: `app/src/main/java/com/faster/aiboard/navigation/NavGraph.kt`
- Modify: `app/src/main/java/com/faster/aiboard/MainActivity.kt`

- [ ] **Step 1: Create Screen.kt**

```kotlin
package com.faster.aiboard.navigation

sealed class Screen(val route: String) {
    data object BoardList : Screen("board_list")
    data object Canvas : Screen("canvas/{boardId}") {
        fun createRoute(boardId: String) = "canvas/$boardId"
    }
    data object Settings : Screen("settings")
}
```

- [ ] **Step 2: Create NavGraph.kt**

```kotlin
package com.faster.aiboard.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.BoardList.route) {
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

- [ ] **Step 3: Create placeholder screens**

Create `app/src/main/java/com/faster/aiboard/ui/screens/BoardListScreen.kt`:

```kotlin
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
```

Create `app/src/main/java/com/faster/aiboard/ui/screens/CanvasScreen.kt`:

```kotlin
package com.faster.aiboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CanvasScreen(
    boardId: String,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // TODO: Canvas will be placed here
        Text("Canvas: $boardId")
    }
}
```

Create `app/src/main/java/com/faster/aiboard/ui/screens/SettingsScreen.kt`:

```kotlin
package com.faster.aiboard.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onBack: () -> Unit) {
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
            SettingsItem("DeepSeek API 配置", "API Key、模型选择")
            SettingsItem("快捷问答管理", "预设提问词条")
            SettingsItem("工具栏定制", "排列列数、工具显隐")
            SettingsItem("默认模板", "新建白板默认模板")
            SettingsItem("关于", "版本号、许可")
        }
    }
}

@Composable
private fun SettingsItem(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: show dialog */ }
            .padding(16.dp)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    HorizontalDivider()
}
```

- [ ] **Step 4: Update MainActivity.kt**

```kotlin
package com.faster.aiboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.faster.aiboard.navigation.AppNavGraph
import com.faster.aiboard.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}
```

- [ ] **Step 5: Self-check code correctness**

Verify by reading the modified files: imports are correct, no dangling references to old package name.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add navigation scaffold with placeholder screens"
```

---

### Task 5: Canvas engine — rendering pipeline

**Files:**
- Create: `app/src/main/java/com/faster/aiboard/ui/canvas/CanvasState.kt`
- Create: `app/src/main/java/com/faster/aiboard/ui/canvas/CanvasView.kt`
- Create: `app/src/main/java/com/faster/aiboard/ui/canvas/CanvasViewModel.kt`

- [ ] **Step 1: Create CanvasState.kt**

```kotlin
package com.faster.aiboard.ui.canvas

import androidx.compose.ui.geometry.Offset
import com.faster.aiboard.data.model.CanvasElement

data class CanvasState(
    val elements: List<CanvasElement> = emptyList(),
    val selectedElementIds: Set<String> = emptySet(),
    val activeTool: Tool = Tool.Pen,
    val penColor: Long = 0xFF1A1A1A,
    val penWidth: Float = 4f,
    val eraserWidth: Float = 20f,
    val canvasOffset: Offset = Offset.Zero,
    val canvasScale: Float = 1f,
    val currentStrokePoints: List<Offset> = emptyList(),
    val currentStrokeColor: Long = 0xFF1A1A1A,
    val currentStrokeWidth: Float = 4f
)

enum class Tool {
    Pen, Highlighter, Eraser, Select, AILasso, Insert
}
```

- [ ] **Step 2: Create CanvasView.kt**

```kotlin
package com.faster.aiboard.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import com.faster.aiboard.data.model.*
import com.faster.aiboard.ui.canvas.CanvasState
import com.faster.aiboard.ui.canvas.Tool

fun Path.applyPoints(points: List<Offset>) {
    if (points.isEmpty()) return
    moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size) {
        lineTo(points[i].x, points[i].y)
    }
}

@Composable
fun CanvasView(
    state: CanvasState,
    onDrawStart: (Offset) -> Unit,
    onDrawMove: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
            .pointerInput(state.activeTool) {
                if (state.activeTool == Tool.Pen || state.activeTool == Tool.Highlighter) {
                    detectDragGestures(
                        onDragStart = { offset -> onDrawStart(offset) },
                        onDrag = { change, _ ->
                            change.consume()
                            onDrawMove(change.position)
                        },
                        onDragEnd = { onDrawEnd() }
                    )
                }
            }
    ) {
        // Layer 1: Draw template grid
        drawTemplateGrid(state)

        // Layer 2: Draw existing elements
        for (element in state.elements) {
            drawElement(element, state)
        }

        // Layer 3: Draw current stroke in progress
        if (state.currentStrokePoints.size >= 2) {
            val path = Path().apply {
                applyPoints(state.currentStrokePoints)
            }
            drawPath(
                path = path,
                color = Color(state.currentStrokeColor),
                style = Stroke(
                    width = state.currentStrokeWidth * (1f / state.canvasScale),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                alpha = if (state.activeTool == Tool.Highlighter) 0.4f else 1f
            )
        }
    }
}

private fun DrawScope.drawTemplateGrid(state: CanvasState) {
    // Draw grid pattern based on template
    val gridColor = Color(0xFFE0E0E0)
    val gridSpacing = 40f * state.canvasScale
    val offset = state.canvasOffset

    if (state.activeTool == Tool.Pen || state.activeTool == Tool.Highlighter) {
        // Simple grid lines for visual reference
        val startX = offset.x % gridSpacing
        val startY = offset.y % gridSpacing
        // Draw limited grid lines within visible area
        for (x in startX until size.width step gridSpacing) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 0.5f)
        }
        for (y in startY until size.height step gridSpacing) {
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5f)
        }
    }
}

private fun DrawScope.drawElement(element: CanvasElement, state: CanvasState) {
    when (element) {
        is Stroke -> {
            if (element.points.size < 2) return
            val path = Path().apply {
                val pts = element.points.map { Offset(it.x, it.y) }
                applyPoints(pts)
            }
            val transform = element.transform
            drawPath(
                path = path,
                color = Color(element.color),
                style = Stroke(
                    width = element.strokeWidth * (1f / state.canvasScale),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                alpha = element.alpha
            )
            // Draw selection handles if selected
            if (element.id in state.selectedElementIds) {
                val bounds = path.getBounds()
                drawSelectionHandles(bounds)
            }
        }
        is ImageElement -> {
            // TODO: load and draw bitmap from bitmapRef
        }
        is TextBlock -> {
            // TODO: draw text
        }
    }
}

private fun DrawScope.drawSelectionHandles(bounds: androidx.compose.ui.geometry.Rect) {
    val handleColor = Color(0xFF2196F3)
    val handleSize = 8f
    val points = listOf(
        bounds.topLeft, bounds.topRight,
        bounds.bottomLeft, bounds.bottomRight,
        Offset(bounds.center.x, bounds.top),    // top center (rotation handle)
        Offset(bounds.center.x, bounds.bottom), // bottom center
        Offset(bounds.left, bounds.center.y),
        Offset(bounds.right, bounds.center.y)
    )
    for (pt in points) {
        drawCircle(handleColor, handleSize, pt)
    }
}
```

- [ ] **Step 3: Create CanvasViewModel.kt** (basic skeleton)

```kotlin
package com.faster.aiboard.ui.canvas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.faster.aiboard.data.model.*
import java.util.UUID

class CanvasViewModel {
    var state by mutableStateOf(CanvasState())
        private set

    fun setActiveTool(tool: Tool) {
        state = state.copy(activeTool = tool)
    }

    fun setPenColor(color: Long) {
        state = state.copy(penColor = color, currentStrokeColor = color)
    }

    fun setPenWidth(width: Float) {
        state = state.copy(penWidth = width, currentStrokeWidth = width)
    }

    // Drawing handlers
    fun onDrawStart(offset: Offset) {
        state = state.copy(
            currentStrokePoints = listOf(offset),
            currentStrokeColor = state.penColor,
            currentStrokeWidth = state.penWidth
        )
    }

    fun onDrawMove(offset: Offset) {
        state = state.copy(
            currentStrokePoints = state.currentStrokePoints + offset
        )
    }

    fun onDrawEnd() {
        val pts = state.currentStrokePoints
        if (pts.size < 2) {
            state = state.copy(currentStrokePoints = emptyList())
            return
        }
        val newStroke = Stroke(
            id = UUID.randomUUID().toString(),
            points = pts.map { PointF(it.x, it.y) },
            color = state.currentStrokeColor,
            strokeWidth = state.currentStrokeWidth,
            alpha = if (state.activeTool == Tool.Highlighter) 0.4f else 1f,
            isHighlighter = state.activeTool == Tool.Highlighter
        )
        state = state.copy(
            elements = state.elements + newStroke,
            currentStrokePoints = emptyList()
        )
    }

    fun clearSelection() {
        state = state.copy(selectedElementIds = emptySet())
    }
}
```

- [ ] **Step 4: Integrate CanvasView into CanvasScreen**

Update `CanvasScreen.kt`:

```kotlin
package com.faster.aiboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.faster.aiboard.ui.canvas.CanvasView
import com.faster.aiboard.ui.canvas.CanvasViewModel

@Composable
fun CanvasScreen(
    boardId: String,
    onBack: () -> Unit
) {
    val viewModel = remember { CanvasViewModel() }

    Box(modifier = Modifier.fillMaxSize()) {
        CanvasView(
            state = viewModel.state,
            onDrawStart = viewModel::onDrawStart,
            onDrawMove = viewModel::onDrawMove,
            onDrawEnd = viewModel::onDrawEnd,
            modifier = Modifier.fillMaxSize()
        )

        // Back button (top-left floating)
        FilledTonalButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("← 返回")
        }
    }
}
```

- [ ] **Step 5: Self-check code correctness**

Verify by reading the modified files: imports are correct, no dangling references to old package name.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add canvas engine with pen drawing and selection handles"
```

---

### Task 6: Floating toolbar

**Files:**
- Create: `app/src/main/java/com/faster/aiboard/ui/toolbar/ToolDefinitions.kt`
- Create: `app/src/main/java/com/faster/aiboard/ui/toolbar/FloatingToolbar.kt`

- [ ] **Step 1: Create ToolDefinitions.kt**

```kotlin
package com.faster.aiboard.ui.toolbar

import com.faster.aiboard.ui.canvas.Tool

data class ToolItem(
    val tool: Tool,
    val icon: String,     // emoji or icon text
    val label: String
)

val defaultTools = listOf(
    ToolItem(Tool.Select, "🖱", "选择"),
    ToolItem(Tool.Pen, "✏️", "画笔"),
    ToolItem(Tool.Highlighter, "🖍", "荧光笔"),
    ToolItem(Tool.Eraser, "🧹", "橡皮"),
    ToolItem(Tool.AILasso, "🤖", "AI套索"),
    ToolItem(Tool.Insert, "📎", "插入")
)
```

- [ ] **Step 2: Create FloatingToolbar.kt**

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faster.aiboard.ui.canvas.Tool

@Composable
fun FloatingToolbar(
    activeTool: Tool,
    onToolSelected: (Tool) -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Column(
        modifier = modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
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
                Text(
                    text = toolItem.icon,
                    fontSize = 18.sp
                )
            }
        }
    }
}
```

- [ ] **Step 3: Add color picker strip**

Add to `FloatingToolbar.kt` — only visible when Pen or Highlighter is active:

```kotlin
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        colors.forEach { color ->
            val isSelected = selectedColor == color
            Box(
                modifier = Modifier
                    .size(24.dp)
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

- [ ] **Step 4: Integrate toolbar + color strip into CanvasScreen**

```kotlin
// In CanvasScreen.kt, inside the Box:
FloatingToolbar(
    activeTool = viewModel.state.activeTool,
    onToolSelected = { viewModel.setActiveTool(it) },
    modifier = Modifier.align(Alignment.CenterEnd)
)

if (viewModel.state.activeTool == Tool.Pen || viewModel.state.activeTool == Tool.Highlighter) {
    ColorStrip(
        selectedColor = viewModel.state.penColor,
        onColorSelected = { viewModel.setPenColor(it) },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 24.dp)
    )
}
```

- [ ] **Step 5: Self-check code correctness**

Verify by reading the modified files: imports are correct, no dangling references to old package name.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add floating toolbar with color strip"
```

---

### Task 7: Eraser and selection tool support

**Files:**
- Modify: `app/src/main/java/com/faster/aiboard/ui/canvas/CanvasView.kt`
- Modify: `app/src/main/java/com/faster/aiboard/ui/canvas/CanvasViewModel.kt`

- [ ] **Step 1: Add eraser gesture handling in CanvasViewModel**

```kotlin
fun onEraserMove(offset: Offset) {
    // Hit test: check if offset intersects any stroke
    val hitId = state.elements.filterIsInstance<Stroke>().findLast { stroke ->
        val pts = stroke.points.map { Offset(it.x, it.y) }
        pts.any { (it - offset).getDistance() < state.eraserWidth }
    }?.id

    if (hitId != null) {
        state = state.copy(
            elements = state.elements.filter { it.id != hitId }
        )
    }
}
```

- [ ] **Step 2: Add eraser pointer input in CanvasView**

```kotlin
// Inside the pointerInput block, add an eraser branch:
if (state.activeTool == Tool.Eraser) {
    detectDragGestures(
        onDrag = { change, _ ->
            change.consume()
            onDrawMove(change.position)  // reuse as eraser move
        }
    )
}
```

- [ ] **Step 3: Add selection gesture skeleton**

In CanvasViewModel:

```kotlin
fun onSelectTap(offset: Offset) {
    // Find topmost element near tap point
    val hitId = state.elements.lastOrNull { element ->
        when (element) {
            is Stroke -> {
                val pts = element.points.map { Offset(it.x, it.y) }
                pts.any { (it - offset).getDistance() < 20f }
            }
            else -> false
        }
    }?.id

    state = if (hitId != null) {
        state.copy(selectedElementIds = setOf(hitId))
    } else {
        state.copy(selectedElementIds = emptySet())
    }
}
```

- [ ] **Step 4: Self-check code correctness**

Verify by reading the modified files: imports are correct, no dangling references to old package name.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add eraser and selection tool support"
```

---

### Task 8: AI lasso — selection UI + popup

**Files:**
- Create: `app/src/main/java/com/faster/aiboard/ui/ai/AIPopup.kt`
- Create: `app/src/main/java/com/faster/aiboard/ui/ai/AITag.kt`
- Create: `app/src/main/java/com/faster/aiboard/ui/ai/AISessionViewModel.kt`
- Modify: `app/src/main/java/com/faster/aiboard/ui/canvas/CanvasView.kt`
- Modify: `app/src/main/java/com/faster/aiboard/ui/canvas/CanvasViewModel.kt`

- [ ] **Step 1: Create AISessionViewModel.kt**

```kotlin
package com.faster.aiboard.ui.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.faster.aiboard.data.model.*
import java.util.UUID

data class AISessionState(
    val isSelecting: Boolean = false,
    val lassoPoints: List<Offset> = emptyList(),
    val showDialog: Boolean = false,
    val dialogPosition: Offset = Offset.Zero,
    val sessions: List<AISession> = emptyList(),
    val currentQuestion: String = ""
)

class AISessionViewModel {
    var aiState by mutableStateOf(AISessionState())
        private set

    val quickQuestions = listOf("回答问题", "解释", "翻译")

    fun onLassoStart(offset: Offset) {
        aiState = aiState.copy(
            isSelecting = true,
            lassoPoints = listOf(offset)
        )
    }

    fun onLassoMove(offset: Offset) {
        if (aiState.isSelecting) {
            aiState = aiState.copy(
                lassoPoints = aiState.lassoPoints + offset
            )
        }
    }

    fun onLassoEnd() {
        if (aiState.lassoPoints.size >= 3) {
            val center = aiState.lassoPoints.fold(Offset.Zero) { acc, p -> acc + p } /
                    aiState.lassoPoints.size.coerceAtLeast(1)
            aiState = aiState.copy(
                isSelecting = false,
                showDialog = true,
                dialogPosition = center + Offset(100f, 0f)
            )
        } else {
            aiState = aiState.copy(isSelecting = false, lassoPoints = emptyList())
        }
    }

    fun dismissDialog() {
        aiState = aiState.copy(showDialog = false, lassoPoints = emptyList())
    }

    fun setQuestion(q: String) {
        aiState = aiState.copy(currentQuestion = q)
    }

    fun sendQuestion() {
        val q = aiState.currentQuestion
        if (q.isBlank()) return

        val newTag = AITag(
            id = UUID.randomUUID().toString(),
            question = q,
            answer = "",     // Will be filled after API call
            positionX = aiState.dialogPosition.x,
            positionY = aiState.dialogPosition.y
        )

        val sessionId = UUID.randomUUID().toString()
        val newSession = AISession(
            id = sessionId,
            lassoPoints = aiState.lassoPoints.map { PointF(it.x, it.y) },
            tags = listOf(newTag)
        )

        aiState = aiState.copy(
            sessions = aiState.sessions + newSession,
            showDialog = false,
            lassoPoints = emptyList(),
            currentQuestion = ""
        )
    }

    fun addToExistingSession(tag: AITag, sessionId: String) {
        aiState = aiState.copy(
            sessions = aiState.sessions.map {
                if (it.id == sessionId) it.copy(tags = it.tags + tag) else it
            }
        )
    }
}
```

- [ ] **Step 2: Create AIPopup.kt**

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
    positionX: Float,
    positionY: Float,
    quickQuestions: List<String>,
    onQuestionChanged: (String) -> Unit,
    onSend: () -> Unit,
    onQuickQuestion: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .width(260.dp)
            .padding(8.dp),
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
                            onQuickQuestion(q)
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

- [ ] **Step 3: Create AITag.kt**

```kotlin
package com.faster.aiboard.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                Text(if (expanded) "▲" else "▼", fontSize = MaterialTheme.typography.labelSmall.fontSize)
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
```

- [ ] **Step 4: Add AI lasso parameters to CanvasView**

Add new parameters and pointerInput to `CanvasView.kt`:

```kotlin
@Composable
fun CanvasView(
    state: CanvasState,
    onDrawStart: (Offset) -> Unit,
    onDrawMove: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    onAILassoStart: (Offset) -> Unit = {},
    onAILassoMove: (Offset) -> Unit = {},
    onAILassoEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
            .pointerInput(state.activeTool) {
                when (state.activeTool) {
                    Tool.Pen, Tool.Highlighter -> {
                        detectDragGestures(
                            onDragStart = { onDrawStart(it) },
                            onDrag = { change, _ -> change.consume(); onDrawMove(change.position) },
                            onDragEnd = onDrawEnd
                        )
                    }
                    Tool.AILasso -> {
                        detectDragGestures(
                            onDragStart = { onAILassoStart(it) },
                            onDrag = { change, _ -> change.consume(); onAILassoMove(change.position) },
                            onDragEnd = onAILassoEnd
                        )
                    }
                    else -> { /* handled by other gesture detectors */ }
                }
            }
    ) {
        // ... existing drawing code ...

        // AI lasso overlay rendering
        val lassoPts = state.aiLassoPoints
        if (lassoPts.size >= 3) {
            val lassoPath = Path().apply {
                applyPoints(lassoPts)
                close()
            }
            drawPath(
                path = lassoPath,
                color = Color(0x40FFA500),
                style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // AI selection gradient overlay
        // TODO: implement flow gradient animation (Task 8 refinement)
    }
}
```

- [ ] **Step 5: Update CanvasViewModel with AI lasso state**

Add to CanvasState:

```kotlin
val aiLassoPoints: List<Offset> = emptyList()
```

Add handlers:

```kotlin
fun onAILassoStart(offset: Offset) {
    state = state.copy(aiLassoPoints = listOf(offset))
}

fun onAILassoMove(offset: Offset) {
    state = state.copy(aiLassoPoints = state.aiLassoPoints + offset)
}

fun onAILassoEnd() {
    // Calculate center for popup position
    val pts = state.aiLassoPoints
    if (pts.size >= 3) {
        val center = pts.fold(Offset.Zero) { acc, p -> acc + p } / pts.size
        // Show dialog
    }
}
```

- [ ] **Step 6: Self-check code correctness**

Verify by reading the modified files: imports are correct, no dangling references to old package name.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: add AI lasso UI with popup dialog"
```

---

### Task 9: DeepSeek API integration

**Files:**
- Create: `app/src/main/java/com/faster/aiboard/ai/AIService.kt`
- Create: `app/src/main/java/com/faster/aiboard/ai/AIImageAnalyzer.kt`

- [ ] **Step 1: Create AIService.kt**

```kotlin
package com.faster.aiboard.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

private val json = Json { ignoreUnknownKeys = true }
private val client = OkHttpClient()
private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

@Serializable
data class DeepSeekMessage(
    val role: String,
    val content: List<ContentPart>
)

@Serializable
data class ContentPart(
    val type: String,  // "text" or "image_url"
    val text: String? = null,
    val image_url: ImageUrl? = null
)

@Serializable
data class ImageUrl(val url: String)  // "data:image/png;base64,..."

@Serializable
data class DeepSeekRequest(
    val model: String = "deepseek-vl2"
    val messages: List<DeepSeekMessage>,
    val max_tokens: Int = 1024
)

@Serializable
data class DeepSeekResponse(
    val choices: List<Choice>? = null
)

@Serializable
data class Choice(
    val message: MessageContent? = null
)

@Serializable
data class MessageContent(
    val content: String? = null
)

class AIService(private val apiKey: String) {

    suspend fun analyzeImage(base64Image: String, question: String): String = withContext(Dispatchers.IO) {
        val requestBody = DeepSeekRequest(
            messages = listOf(
                DeepSeekMessage(
                    role = "user",
                    content = listOf(
                        ContentPart(type = "text", text = question),
                        ContentPart(type = "image_url", image_url = ImageUrl(url = "data:image/png;base64,$base64Image"))
                    )
                )
            )
        )

        val jsonBody = json.encodeToString(requestBody)
        val request = Request.Builder()
            .url("https://api.deepseek.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(jsonBody.toRequestBody(JSON_MEDIA))
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext "API 返回为空"
            val apiResponse = json.decodeFromString<DeepSeekResponse>(body)
            apiResponse.choices?.firstOrNull()?.message?.content ?: "无法获取回复"
        } catch (e: Exception) {
            "请求失败: ${e.message}"
        }
    }
}
```

- [ ] **Step 2: Create AIImageAnalyzer.kt**

```kotlin
package com.faster.aiboard.ai

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

class AIImageAnalyzer(private val aiService: AIService) {

    suspend fun analyzeArea(
        areaBitmap: Bitmap,
        question: String
    ): String {
        val base64 = bitmapToBase64(areaBitmap)
        return aiService.analyzeImage(base64, question)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
```

- [ ] **Step 3: Self-check code correctness**

Verify by reading the modified files: imports are correct, no dangling references to old package name.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: add DeepSeek vision API service"
```

---

### Task 10: Settings screen with dialogs

**Files:**
- Create: `app/src/main/java/com/faster/aiboard/ui/settings/SettingsDialogs.kt`
- Create: `app/src/main/java/com/faster/aiboard/settings/SettingsDataStore.kt`
- Modify: `app/src/main/java/com/faster/aiboard/ui/screens/SettingsScreen.kt`

- [ ] **Step 1: Create SettingsDataStore.kt**

```kotlin
package com.faster.aiboard.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val API_KEY = stringPreferencesKey("deepseek_api_key")
        val QUICK_QUESTIONS = stringPreferencesKey("quick_questions")  // JSON array
        val TOOL_COLUMNS = stringPreferencesKey("tool_columns")
        val DEFAULT_TEMPLATE = stringPreferencesKey("default_template")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { it[API_KEY] ?: "" }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { it[API_KEY] = key }
    }
}
```

- [ ] **Step 2: Create SettingsDialogs.kt**

```kotlin
package com.faster.aiboard.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row {
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

- [ ] **Step 3: Update SettingsScreen to show dialogs**

```kotlin
// In SettingsScreen.kt, add state for which dialog is open
var showApiDialog by remember { mutableStateOf(false) }

// Replace clickable items with dialog triggers
SettingsItem("DeepSeek API 配置", "...") { showApiDialog = true }

if (showApiDialog) {
    ApiKeyDialog(
        currentKey = "",
        onSave = { showApiDialog = false },
        onDismiss = { showApiDialog = false }
    )
}
```

- [ ] **Step 4: Self-check code correctness**

Verify by reading the modified files: imports are correct, no dangling references to old package name.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add settings page with API key dialog"
```

---

### Task 11: Whiteboard persistence (JSON save/load)

**Files:**
- Create: `app/src/main/java/com/faster/aiboard/data/serializer/WhiteboardSerializer.kt`
- Create: `app/src/main/java/com/faster/aiboard/data/repository/FileRepository.kt`
- Modify: `app/src/main/java/com/faster/aiboard/data/model/Whiteboard.kt` (add serde annotations)

- [ ] **Step 1: Create WhiteboardSerializer.kt**

```kotlin
package com.faster.aiboard.data.serializer

import com.faster.aiboard.data.model.*
import kotlinx.serialization.json.Json

object WhiteboardSerializer {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun serialize(whiteboard: Whiteboard): String {
        return json.encodeToString(Whiteboard.serializer(), whiteboard)
    }

    fun deserialize(data: String): Whiteboard {
        return json.decodeFromString(Whiteboard.serializer(), data)
    }

    fun serializeIndex(index: BoardIndex): String {
        return json.encodeToString(BoardIndex.serializer(), index)
    }

    fun deserializeIndex(data: String): BoardIndex {
        return json.decodeFromString(BoardIndex.serializer(), data)
    }
}
```

- [ ] **Step 2: Create FileRepository.kt**

```kotlin
package com.faster.aiboard.data.repository

import android.content.Context
import com.faster.aiboard.data.model.*
import com.faster.aiboard.data.serializer.WhiteboardSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class FileRepository(private val context: Context) {

    private val rootDir: File get() = File(context.filesDir, "whiteboards")

    private fun boardDir(boardId: String) = File(rootDir, boardId)

    fun loadIndex(): BoardIndex {
        val file = File(rootDir, "index.json")
        if (!file.exists()) return BoardIndex()
        val data = file.readText()
        return WhiteboardSerializer.deserializeIndex(data)
    }

    suspend fun saveIndex(index: BoardIndex) = withContext(Dispatchers.IO) {
        rootDir.mkdirs()
        val file = File(rootDir, "index.json")
        file.writeText(WhiteboardSerializer.serializeIndex(index))
    }

    suspend fun loadBoard(boardId: String): Whiteboard? = withContext(Dispatchers.IO) {
        val file = File(boardDir(boardId), "board.json")
        if (!file.exists()) return@withContext null
        val data = file.readText()
        WhiteboardSerializer.deserialize(data)
    }

    suspend fun saveBoard(boardId: String, whiteboard: Whiteboard) = withContext(Dispatchers.IO) {
        boardDir(boardId).mkdirs()
        val file = File(boardDir(boardId), "board.json")
        file.writeText(WhiteboardSerializer.serialize(whiteboard))
    }

    suspend fun createBoard(name: String, template: String): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val now = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
        val whiteboard = Whiteboard(
            board = BoardMeta(name = name, template = template, createdAt = now, updatedAt = now)
        )
        saveBoard(id, whiteboard)

        val index = loadIndex()
        val newItem = BoardListItem(id = id, name = name, template = template, createdAt = now, updatedAt = now)
        saveIndex(index.copy(boards = index.boards + newItem))
        id
    }
}
```

- [ ] **Step 3: Wire persistence into CanvasViewModel**

```kotlin
// Add to CanvasViewModel:
private var _boardId: String = ""
private lateinit var repository: FileRepository
private lateinit var currentWhiteboard: Whiteboard

fun init(boardId: String, repository: FileRepository) {
    this._boardId = boardId
    this.repository = repository
    // Load board data in background
}

suspend fun save() {
    val wb = currentWhiteboard.copy(
        elements = state.elements,
        board = currentWhiteboard.board.copy(
            updatedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
        )
    )
    repository.saveBoard(_boardId, wb)
}
```

- [ ] **Step 4: Self-check code correctness**

Verify by reading the modified files: imports are correct, no dangling references to old package name.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add whiteboard JSON persistence layer"
```

---

### Task 12: AI edit reserved interface + final wiring

- [ ] **Step 1: Create AIEditService.kt**

```kotlin
package com.faster.aiboard.ai

import com.faster.aiboard.data.model.CanvasElement
import com.faster.aiboard.data.model.Stroke

interface AIEditService {
    suspend fun editElement(
        element: CanvasElement,
        instruction: String
    ): CanvasElement

    suspend fun generateContent(
        instruction: String,
        context: List<CanvasElement>
    ): List<CanvasElement>

    suspend fun beautifyStroke(
        stroke: Stroke
    ): Stroke
}

sealed interface AIEditAction {
    data class Modify(val elementId: String, val instruction: String) : AIEditAction
    data class Generate(val instruction: String) : AIEditAction
    data class Beautify(val elementId: String) : AIEditAction
}
```

- [ ] **Step 2: Wire auto-save on CanvasScreen back navigation**

```kotlin
// In CanvasScreen.kt
val scope = rememberCoroutineScope()

// On back button click:
scope.launch {
    viewModel.save()
    onBack()
}
```

- [ ] **Step 3: Self-check code correctness**

Verify by reading the modified files: imports are correct, no dangling references to old package name.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: add AI edit reserved interface and final wiring"
```
