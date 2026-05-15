# CLAUDE.md

## 环境
- Proot/Termux 环境开发，aapt2 daemon 已关闭（`android.aapt2.process.daemon=false`）
- ARM64 架构，首次构建前必须运行 `./setup_android_env.sh` 替换 AAPT2 二进制
- 国内网络，阿里云/华为云 Maven 镜像已配置在 `settings.gradle.kts`

## 依赖管理
- Gradle Version Catalog：版本定义在 `gradle/libs.versions.toml`，模块中通过 `libs.xxx.yyy` 引用
- AGP 9.0.0 + Kotlin 2.3.10，注意版本兼容性

## 推送流程
1. 写代码 → 自我检查 → `git done "修改描述"` 推送
2. 保持监听 CI 构建结果，直至构建完成
3. 构建失败则读日志修复，成功则结束
4. 不在本地构建 APK，全部走 GitHub Actions CI

## 工具使用
- 查 API / 库文档 → 使用 context7 MCP 工具
- 互联网搜索 → 使用 WebSearch 工具
- 浏览器截图 / 调试 → 可选，使用 chrome-devtools MCP 工具
- 任何 `superpowers:*` 技能必须经我明确同意后方可启用，不得擅自调用
