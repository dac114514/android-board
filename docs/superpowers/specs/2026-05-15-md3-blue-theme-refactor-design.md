# MD3 蓝色主题全面布局重构 — 设计文档

## 概述

将「智绘白板」App 的 UI 从默认 Material Design 3 紫色主题重构为 MD3 蓝色风格，全面采用 Jetpack Compose Material 3 最佳实践，参考笔记 App（`D:\开发\Android开发\笔记`）的设计语言。

## 1. Theme（主题）

### 颜色体系

参考笔记 App 的蓝色色系，定义适用于白板场景的完整 MD3 调色板。

**Light 模式：**
| Token | 颜色 | 值 |
|-------|------|-----|
| primary | Blue700 | `#1976D2` |
| onPrimary | NearWhite | `#FDFCFF` |
| primaryContainer | Blue100 | `#D1E4FF` |
| onPrimaryContainer | BlueDark | `#003258` |
| secondary | GreySecondary | `#535F70` |
| secondaryContainer | BlueGrey100 | `#D7E3F7` |
| onSecondaryContainer | BlueDark | `#003258` |
| background | NearWhite | `#FDFCFF` |
| onBackground | DarkSurface | `#1A1C1E` |
| surface | NearWhite | `#FDFCFF` |
| onSurface | DarkSurface | `#1A1C1E` |
| error | ErrorRed | `#BA1A1A` |

**Dark 模式：**
| Token | 颜色 | 值 |
|-------|------|-----|
| primary | BlueLight | `#9ECAFF` |
| onPrimary | BlueDark | `#003258` |
| primaryContainer | BlueDarkContainer | `#00497D` |
| onPrimaryContainer | Blue100 | `#D1E4FF` |
| secondary | GreySecondaryDark | `#BAC8DC` |
| secondaryContainer | GreySecondaryContainer | `#3B4858` |
| onSecondaryContainer | BlueGrey100 | `#D7E3F7` |
| background | DarkSurface | `#1A1C1E` |
| onBackground | DarkOnSurface | `#E2E2E6` |
| surface | DarkSurface | `#1A1C1E` |
| onSurface | DarkOnSurface | `#E2E2E6` |
| error | ErrorRedDark | `#FFB4AB` |

### 决策：禁用 dynamic color
- 原因：保持蓝色品牌一致性，而非跟随系统壁纸变色
- 动态取色（Android 12+）关闭，统一使用静态蓝色方案

### Typography

```kotlin
headlineLarge = TextStyle(fontWeight = Bold, fontSize = 28.sp)
titleLarge = TextStyle(fontWeight = Bold, fontSize = 22.sp)
titleMedium = TextStyle(fontWeight = SemiBold, fontSize = 18.sp)
bodyLarge = TextStyle(fontSize = 16.sp)
bodyMedium = TextStyle(fontSize = 14.sp)
labelLarge = TextStyle(fontWeight = Medium, fontSize = 14.sp)
labelSmall = TextStyle(fontSize = 12.sp)
```

### 状态栏
- 通过 `SideEffect` + `WindowCompat.getInsetsController` 设置状态栏图标颜色
- 深色模式：白色状态栏图标；浅色模式：黑色状态栏图标

## 2. 导航结构

### 路由设计
- 使用 `NavHost` + 底部 `NavigationBar`（2 个 tab）
- Tab 1：**白板**（BoardList）
- Tab 2：**设置**（Settings）

### 底部导航栏
- 使用 `NavigationBar` + `NavigationBarItem`
- Icon：白板用 `Icons.Outlined/Filled.Dashboard`，设置用 `Icons.Outlined/Filled.Settings`
- 使用 `AnimatedVisibility`（slideInVertically/slideOutVertically）控制显隐
- Canvas 路由时隐藏底部栏（通过 `currentDestination?.route` 判断）

### 导航流程
```
BoardList (tab 1) ──→ Canvas (全屏, 底部栏隐藏)
    │                     │
    │                     └── popBackStack() → BoardList (底部栏恢复)
    │
    └── tab 2 ──→ Settings
```

## 3. BoardList 白板列表

### 空状态
- 居中显示 Material Icons 插图 + 提示文字
- 图标：`Icons.Default.Dashboard`（大号，onSurfaceVariant 颜色）
- 文字：`"暂无白板"`（titleMedium）、`"点击 + 新建白板"`（bodyMedium, 降低透明度）

### 有白板时
- `LazyVerticalGrid(columns = Fixed(2))` 两列卡片网格
- 每张：`ElevatedCard` 或 `Card(elevation = 2.dp)`
  - 缩略区域（占位符，后续可展示白板预览）
  - 白板名称文本（bodyMedium）
  - 最后修改时间（labelSmall, onSurfaceVariant）
- 卡片间距 12.dp，内容 padding 16.dp

### FAB
- `FloatingActionButton` + `Icons.Default.Add`
- containerColor = primaryContainer
- @string: "新建白板"

## 4. Canvas 画布页

### 返回按钮
- `FilledTonalButton("← 返回")` → `IconButton(Icons.AutoMirrored.Filled.ArrowBack)`
- 位置：左上角，padding 12.dp
- 背景：`FilledIconButton` 默认 MD3 样式

### 右侧工具栏
- 保留现有浮动卡片样式（RoundedCornerShape 12.dp + background surfaceVariant）
- emoji 图标全部替换为 Material Icons 矢量图
- 激活态：`primaryContainer` 背景 + `primary` 图标色
- 非激活态：透明背景 + `onSurfaceVariant` 图标色

图标映射：
| 工具 | 当前 emoji | Material Icon |
|------|-----------|---------------|
| Select | 🖱 | `Icons.Default.TouchApp` |
| Pen | ✏️ | `Icons.Default.Draw` |
| Highlighter | 🖍 | `Icons.Default.Highlight` |
| Eraser | 🧹 | `Icons.Default.AutoFixOff` |
| AI Lasso | 🤖 | `Icons.Default.AutoAwesome` |
| Insert | 📎 | `Icons.Default.AttachFile` |

### 颜色条
- 保留现有圆形颜色选择器布局
- 选中状态：外圈 border 使用 `primary` 色 + 2.dp 宽度
- 可选：选中时添加轻微缩放效果（1.0 → 1.15）

### AI Popup
- 保留现有 Card 样式
- 按钮对齐 MD3 规范：`Button`（填充，蓝色）和 `TextButton`（文字）
- `SuggestionChip` 样式保持不变

## 5. Settings 设置页

### 整体布局
- `Column` + `verticalScroll(rememberScrollState())` + padding horizontal 16.dp
- 每个设置组用 `Card(elevation = CardDefaults.cardElevation(2.dp))` 包裹
- 卡片间距 12.dp

### Card: AI 配置
```
🔑 DeepSeek API 配置    >
   API Key、模型选择
```
- Icon: `Icons.Default.Key`
- 点击弹出 API Key 对话框（保留现有 Dialog 设计，按钮用 MD3 风格）

### Card: 白板设置
```
💬 快捷问答管理    >
🛠 工具栏定制       >
📄 默认模板         >
```
- Icon: `Icons.Default.QuestionAnswer`、`Icons.Default.Tune`、`Icons.Default.Description`
- 暂时 TODO 占位，保留可扩展性

### Card: 关于
```
ℹ️ 关于    >
```
- Icon: `Icons.Default.Info`
- TODO 占位

### 每行样式（参考笔记 App FileEntryItem）
- Row：Icon(24.dp, primary) + 16.dp spacing + Title(bodyLarge) + ">" (onSurfaceVariant 0.5f)
- clickable 处理
- 可展开项使用 `AnimatedVisibility` 实现折叠动画

## 6. 涉及的源代码文件

| 文件 | 改动内容 |
|------|---------|
| `ui/theme/Color.kt` | 替换为蓝色色系定义 |
| `ui/theme/Theme.kt` | 替换 ColorScheme，添加 SideEffect 状态栏，去掉 dynamic color |
| `ui/theme/Type.kt` | 扩展为完整 Typography |
| `MainActivity.kt` | 添加 NavigationBar，底部导航逻辑，AnimatedVisibility |
| `navigation/Screen.kt` | 添加 BoardList/Settings 路由（底部导航） |
| `navigation/NavGraph.kt` | 调整为适配底部导航的结构 |
| `ui/screens/BoardListScreen.kt` | 卡片网格布局 + 空状态 |
| `ui/screens/CanvasScreen.kt` | 返回按钮 IconButton，调整颜色 |
| `ui/screens/SettingsScreen.kt` | Card 分组 + Material Icons，移除内联 SettingsItem |
| `ui/toolbar/FloatingToolbar.kt` | emoji → Material Icons，ToolItem 更新 |
| `ui/toolbar/ToolDefinitions.kt` | icon 类型从 String 改为 ImageVector |
| `ui/settings/SettingsDialogs.kt` | 按钮 MD3 规范化 |
| `ui/ai/AIPopup.kt` | 按钮 MD3 规范化 |
| `res/values/colors.xml` | 替换为蓝色色值 |
| `res/values/themes.xml` | 更新主题名 |

## 7. 新增依赖

在 `gradle/libs.versions.toml` 和 `app/build.gradle.kts` 中添加：

```toml
# libs.versions.toml
[versions]
materialIconsExtended = "1.7.8"  # 与 composeBom 版本匹配

[libraries]
androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
```

```kotlin
// app/build.gradle.kts
implementation(libs.androidx.material.icons.extended)
```

注：`material-icons-extended` 包含所有 Material Icons 矢量图，Release 构建时 R8 会剥离未使用的图标，不会显著增加 APK 体积。

## 8. 不修改的文件

- `data/model/*` — 数据模型不变
- `data/repository/*` — 数据层不变
- `data/serializer/*` — 序列化不变
- `ai/*` — AI 服务层不变
- `settings/SettingsDataStore.kt` — 数据存储不变
- `ui/canvas/CanvasView.kt` — 画布绘制逻辑不变
- `ui/canvas/CanvasState.kt` — 状态定义不变
- `ui/canvas/CanvasViewModel.kt` — ViewModel 逻辑不变
- `ui/ai/AISessionViewModel.kt` — AI ViewModel 不变
- `ui/ai/AITag.kt` — AI 标签不变
