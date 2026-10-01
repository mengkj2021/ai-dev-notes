# picture-organizer

本仓库有两块，**前者优先**：

1. **纯 AI 开发搭法**——可复现的起票制、prompts / rules / skills、文档与状态台账，使人定意图、AI 改码并写回。  
2. **Android 图片整理 App**——这套搭法落地的唯一产物（Kotlin + Jetpack Compose；本机、不联网）。

**读序**：AI → [体系总览](ai-workbench/指南/AI开发体系总览.md) → [用 AI 开发本仓](ai-workbench/指南/用AI开发本仓.md) → [prompts/](prompts/)；App → [产品](#产品图片整理-app) → [android/README](android/README.md) → [技术栈](project-docs/技术栈.md)。  
贡献：[CONTRIBUTING.md](CONTRIBUTING.md)。定位边界：[定位与现状](ai-workbench/指南/AI开发实践-定位与现状.md)。

> **词表**：「式样」= 规格意图；规格**文件**只在 `project-docs/`（无独立式样书）。票类型「式样变更（S）」= 确认需求变化，不是另有一本书。

## AI 开发（主线）

人定意图与验收；AI 按票改 `android/`，规格写回 `project-docs/`，进度只认票 + 阶段待对应。规格**不**塞进常驻 rules。

```
prompts/ + shared（rules / skills）──▶ include ──▶ ai-workbench/
        ↓ 读 project-status / project-docs
        ↓ 改 android/
        ↓ 写回票复盘 · 待对应 · project-docs
```

| 层 | 路径 | 职责 |
|---|---|---|
| 编排壳 | [`prompts/`](prompts/) | 拉进对话才跑：起票 / 接票 / 文档整理 / git… |
| 常驻纪律 | [`shared/rules/`](shared/rules/) | 起票制、git、双工具同步 → `.cursor` / `.codebuddy` |
| 可复用 SOP | [`shared/skills/`](shared/skills/) | 画面文档、实现画面、对齐文档 |
| 材料与元文档 | [`ai-workbench/`](ai-workbench/) | 指南 · 宪法 · 细则 · 模板 · 错题本 |
| 进度真相 | [`project-status/`](project-status/) | 票库 · 阶段 · **待对应** · 下一号 |
| 规格真相 | [`project-docs/`](project-docs/) | 架构 / 路由 / 画面 / 数据层（`android/` 镜像） |

日常：在 [票库](project-status/票库/) 起票 → 拉 [`prompts/票_接票开发.md`](prompts/票_接票开发.md)。  
提交纪律见 [`shared/rules/git-convention.md`](shared/rules/git-convention.md)；克隆后执行一次 `git config core.hooksPath .githooks`（push 含 `android/**/*.kt(s)` 时跑 `ktlintCheck`）。

效果自评：[框架效果短结论](ai-workbench/指南/框架效果短结论.md)。

## 产品：图片整理 App

本地 Android 图片管理：导入、压缩、打标签，按筛选把已归档图片打成 zip。

**当前版号**：`versionName` **1.0.0** / `versionCode` **2**（见 [`android/app/build.gradle.kts`](android/app/build.gradle.kts)）。

**获取 App**：不上应用商店。可在 [GitHub Releases](https://github.com/mengkj2021/picture-organizer/releases) 下载 **debug 签名** APK 侧载安装（证书为 Android 调试密钥，非正式商店签名）；也可按下方自行 `assembleDebug`。若 Release 尚无附件，见 [android/README · 回家补发](android/README.md#回家补发-v100-release若页面上还没有-apk)。

> **隐私**：全程本机、**不联网**（不声明网络权限）；导入复制到应用私有目录，不改相册原图；整理 / 重命名 / 删除只作用于副本；分享 zip 由用户主动发起。

- 导入：相册 / 文件；可选压缩；默认标签；重名询问  
- 三状态一览：待归档 / 已归档 / 回收站；筛选、分页、批量移动与重命名  
- 详情：缩放浏览、重命名、标签库 / 模板  
- 导出：筛选 + 预览排除后打 **一个** zip；已打包可再分享 / 改名 / 删除  
- 设置：标签与模板、默认标签、教程、开源许可；界面跟随系统语言（简中 / 日 / 英）

定位细节：[project-docs/项目概述.md](project-docs/项目概述.md) · 工程门面：[android/README.md](android/README.md)。

## 仓库结构

```
picture-organizer/
├── ai-workbench/      # AI：指南 / 宪法 / 细则 / 模板
├── prompts/           # 拉入对话的流程壳
├── shared/            # rules / skills（同步到 .cursor / .codebuddy）
├── project-status/    # 票库 · 阶段 · 待对应
├── project-docs/      # App 现状规格
├── android/           # Gradle 工程（请 Open 此目录构建）
├── CONTRIBUTING.md
└── README.md
```

整仓地图：[仓库结构总览](ai-workbench/指南/仓库结构总览.md) · 文档分层：[文档分层约定](ai-workbench/宪法/文档分层约定.md)。

**真相源**：`android/app/src/main`（及 `android/gradle/libs.versions.toml`）。文档与代码冲突时，以代码为准或先确认再改。

高频入口：[架构 §2](project-docs/架构设计.md) · [画面清单](project-docs/画面/README.md) · [阶段待对应](project-status/阶段/阶段4-维护与迭代/待对应.md) · [阶段](project-status/阶段/README.md)

## 克隆与构建

```bash
git clone https://github.com/mengkj2021/picture-organizer.git
cd picture-organizer
git config core.hooksPath .githooks   # 一次即可；push 含 kotlin 时跑 ktlintCheck
```

用 Android Studio **Open `android/`**（仓库根是文档 / AI 层，不是 Gradle 根）。细节见 [android/README.md](android/README.md)。

### 环境

- Android Studio（支持 AGP 9.2 + Gradle 9.7）
- JDK 17+
- Android SDK 37（compileSdk / targetSdk）

### Gradle Wrapper

[`android/gradle/wrapper/gradle-wrapper.properties`](android/gradle/wrapper/gradle-wrapper.properties) 使用官方发行包 URL（Gradle 9.7.1）。首次 Sync / 构建会自动下载；**不要**把 `*.zip` 提交进仓库（已在 `.gitignore`）。

本地若仍留有旧的 `android/gradle/wrapper/gradle-9.7.1-bin.zip`，可删除，以 Wrapper 下载为准。

### 构建

```bash
cd android
./gradlew assembleDebug   # Windows: gradlew.bat assembleDebug
```

路径含非 ASCII 时，工程已启用 `android.overridePathCheck=true`；迁到纯 ASCII 路径后可去掉。

## 勿提交（已在 .gitignore）

| 路径 | 说明 |
|------|------|
| `build/`、`.gradle/`、`.kotlin/`、`.idea/`、`*.iml` | 构建 / IDE 产物 |
| `local.properties` | 本机 SDK 路径 |
| `android/gradle/wrapper/*.zip` | 本地 Gradle 发行包（体积大） |
| `*.jks` / `*.keystore`、`*.log`、`.DS_Store` | 签名与杂项 |
| `.codebuddy/` 除 `rules/` / `skills/` 外 | 本地计划等 |

## 许可

本仓库采用 [MIT License](LICENSE)。App 内第三方库许可证见设置 → 开源许可画面。
