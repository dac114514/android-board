# 智绘白板 — 设计文档

## 概述

智绘白板（com.faster.aiboard）是一款面向平板用户无纸化学习场景的 AI 白板 App。基于 Jetpack Compose + Material Design 3，自研 Canvas 绘制引擎，集成 DeepSeek 视觉模型实现圈选识图问答。

## 一、导航与页面结构

使用 Navigation Compose，共 3 个路由：

| 路由 | 页面 | 说明 |
|------|------|------|
| `BoardListScreen` | 白板列表主页 | 默认首页，网格显示缩略图/名称/修改时间 |
| `CanvasScreen` | 全屏画布 | 左上角悬浮返回按钮，自动保存 |
| `SettingsScreen` | 设置列表索引页 | 点击每项弹出 BottomSheet/Dialog |

**新建白板流程**：点击主页 + 按钮 → 底部弹出模板选择（空白/横线/方格/五线谱/自定义）→ 自动进入 CanvasScreen。

**设置入口**：主页右上角 + 画布工具栏菜单均可进入。

## 二、画布引擎

自研 Compose Canvas，数据模型：

```kotlin
sealed interface CanvasElement {
    val id: String
    val transform: Transform  // position, rotation, scale
}

data class Stroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val alpha: Float,
    val blendMode: BlendMode
) : CanvasElement

data class ImageElement(
    val bitmapRef: String,  // 图片文件路径索引
    override val transform: Transform
) : CanvasElement

data class TextBlock(
    val content: String,
    val style: TextStyle,
    override val transform: Transform
) : CanvasElement
```

**6 层渲染（从底到顶）**：
0. Canvas Background（纯色背景）
1. Template Grid（横线/方格/五线谱底纹）
2. Canvas Elements（笔迹/图片/文字）
3. Tool Overlay（套索轨迹、框选虚线等）
4. AI Selection（流动渐变选中层）
5. AI Tag / Label（结果标签）

### 手势模型

| 工具 | 单指 | 双指（所有工具统一） |
|------|------|---------------------|
| 选择（指针图标） | 套索圈选元素 | 漫游 + 缩放 |
| 画笔 | 绘制笔迹 | 漫游 + 缩放 |
| 荧光笔 | 半透明绘制 | 漫游 + 缩放 |
| 橡皮 | 擦除笔迹 | 漫游 + 缩放 |
| AI 套索 | 圈选区域 → AI 分析 | 漫游 + 缩放 |
| 插入 | 点击放置图片/文字 | 漫游 + 缩放 |

### 选择工具（指针图标）行为

1. 单指圈画闭合区域 → 选中区域内所有 CanvasElement
2. 选中后显示变换手柄（8 个缩放锚点 + 旋转柄）
3. 拖拽手柄可旋转/缩放/移动
4. 底部弹出属性面板：笔画大小、颜色、旋转角度（图片支持等比缩放）
5. 连续圈选追加到多选
6. 点击空白处取消选中

## 三、工具栏

- **位置**：右侧悬浮，可拖动手柄自由移动
- **不可折叠**，始终显示
- **默认 7 个工具**：选择 / 画笔 / 荧光笔 / 橡皮 / AI 套索 / 插入 / 更多
- **颜色面板**：选中绘制工具时工具栏下方展开（9 色 + 自定义取色器）
- **设置可定制**：排列列数（1~3）、工具显隐、默认位置

## 四、AI 套索功能

### 交互流程

1. 切换到 AI 套索工具（独立工具，区别于选择工具的套索）
2. 单指在画布上画闭合区域
   - 轨迹颜色：黄橙蓝绿范围内的随机静态彩色，低饱和度，不刺眼
3. 松开手指，区域覆盖黄橙蓝绿流动渐变层（动画循环）
   - 每次仅限一个 AI 选中区域
4. 在选区旁弹出气泡式对话框：
   - 文字输入框
   - 3 个预设快捷问答按钮（回答问题 / 解释 / 翻译）
   - 发送按钮
5. 将选区截图（Canvas Bitmap 裁剪）+ 用户问题 → DeepSeek 视觉模型
6. 结果以标签卡片贴在选区旁：
   - 标题：用户问题（截短）
   - 正文：模型回答
   - 右上角展开/折叠按钮
7. 展开标签时显示圈选轮廓（极淡背景色，alpha ≈ 0.1）
8. 同区域重复提问（选区重合率大）→ 标签归到同一组
9. 标签可自由拖动
10. 对话持久化保存，除非用户手动删除
11. 理论不限制标签数量

### AI 数据模型

```kotlin
data class AISession(
    val id: String,
    val lassoPath: List<Offset>,  // 圈选路径
    val tags: List<AITag>
)

data class AITag(
    val id: String,
    val question: String,
    val answer: String,
    val position: Offset,
    val expanded: Boolean
)
```

## 五、持久化

### 目录结构

```
whiteboards/<board-uuid>/
├── board.json            # 主文件（全部可编辑数据）
├── images/               # 插入的图片原文件
├── ai_snapshots/         # AI 分析时区域截图缓存
└── exports/              # 手动导出的 PNG/PDF
whiteboards/index.json    # 白板列表索引
```

board.json 包含：board 元信息、elements 数组、aiSessions 数组。JSON 格式确保 reopen 后可完整编辑。

### 保存策略

- 离开画布时自动保存
- 每 5 分钟后台自动保存
- 工具栏菜单提供手动保存

### 导出

PNG 渲染导出、SVG 路径转换、PDF（后期）。

## 六、设置页

设置页为 Navigation 路由中的列表索引页，点击每项弹出 BottomSheet/Dialog：

| 条目 | 内容 |
|------|------|
| DeepSeek API 配置 | API Key 输入、模型选择、连接测试 |
| 快捷问答管理 | 增删改预设词条（默认：回答问题/解释/翻译） |
| 工具栏定制 | 排列列数、工具显隐开关、重置默认 |
| 默认模板 | 新建白板时默认模板选择 |
| 关于 | 版本号、开源许可 |

## 七、预留 AI 编辑接口

当前不实现，架构中预留：

```kotlin
interface AIEditService {
    suspend fun editElement(element: CanvasElement, instruction: String): CanvasElement
    suspend fun generateContent(instruction: String, context: List<CanvasElement>): List<CanvasElement>
    suspend fun beautifyStroke(stroke: Stroke): Stroke
}
```

接口定义在 `ai/AIEditService.kt`，UI 层预留菜单入口但不激活。

## 八、模块分层

```
UI Layer (Compose)
├── BoardListScreen       # 白板列表
├── CanvasScreen          # 画布主屏
│   ├── CanvasView        # 绘制引擎
│   ├── Toolbar           # 浮动工具条
│   ├── AIPopup           # AI 对话框
│   └── AITag             # AI 结果标签
├── SettingsScreen        # 设置列表页
└── components/           # 通用组件

ViewModel Layer
├── CanvasViewModel       # 画布状态/元素/选中
├── AISessionViewModel    # AI 对话状态
└── SettingsViewModel     # 设置状态

AI Layer
├── AIService             # DeepSeek API 封装
├── AIImageAnalyzer       # 区域截图→Base64→推理
└── AIEditService         # 预留编辑接口

Data Layer
├── WhiteboardSerializer  # JSON 序列化
├── FileRepository        # 本地文件读写
├── ExportManager         # PNG/SVG/PDF 导出
└── SettingsDataStore     # 偏好设置持久化
```

## 九、技术栈

- Kotlin 2.3.10, AGP 9.0.0, Compose BOM 2026.01.01
- compileSdk/targetSdk 35, minSdk 24
- Navigation Compose
- DataStore Preferences（设置持久化）
- DeepSeek Vision API（HTTP）
- Kotlinx Serialization（JSON）
