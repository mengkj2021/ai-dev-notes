# 个人 AI 开发笔记

本仓是一份**个人笔记**：用 AI 写代码时怎么起票、怎么约束、怎么写回。  
图片整理 App 只是练手例子（Android 已有；Windows 空位）。没有对外交付模板的意思。

GitHub 仓库现名 **`ai-dev-notes`**（旧名 `picture-organizer` 会跳转到新地址）。

**读序**：[体系总览](ai-workbench/指南/AI开发体系总览.md) → [`prompts/`](prompts/) → [产品车道](examples/project-status/README.md) / [体系车道](ai-workbench/体系/README.md)。  
[贡献](CONTRIBUTING.md) · [定位与边界](ai-workbench/指南/AI开发实践-定位与现状.md)。

> 「式样」= 规格意图。规格文件只在 [`examples/project-docs/`](examples/project-docs/)。

## 笔记怎么用

人定意图与验收；AI 按票改当前例子或笔记层本身。规格不塞进常驻 rules。

```
prompts/ + shared  ──▶ include ──▶  ai-workbench/
        ↓ 读 examples/project-docs + 该车道 status
        ↓ 改 examples/<android|windows>/ 或 prompts/shared/workbench
        ↓ 写回该车道票 + examples/project-docs
```

| 层 | 路径 |
|---|---|
| 编排壳 | [`prompts/`](prompts/) |
| 纪律 / 技能 | [`shared/`](shared/) |
| 笔记正文 | [`ai-workbench/`](ai-workbench/) |
| 笔记进度 | [`ai-workbench/体系/`](ai-workbench/体系/) |
| 例子规格 | [`examples/project-docs/`](examples/project-docs/) |
| 例子进度 | [`examples/project-status/`](examples/project-status/) |
| 例子代码 | [`examples/android/`](examples/android/) · [`examples/windows/`](examples/windows/) |

克隆后：`git config core.hooksPath .githooks`。push 含 Kotlin 时对 `examples/android` 跑 ktlint。

## 例子：图片整理

| 端 | 状态 |
|---|---|
| Android | 已有演示 APK；Open `examples/android/` |
| Windows | 未立项 |

Android 版号 1.0.0 / 2。APK 发版脚本在例子里：[`examples/android/scripts/publish-v1.0.0-github-release.sh`](examples/android/scripts/publish-v1.0.0-github-release.sh)（根目录不再放打包）。Release 附件名仍是 `picture-organizer-…apk`。

规格：[概述](examples/project-docs/项目概述.md) · [两端差异](examples/project-docs/两端差异.md)。

## 仓库结构

```
├── prompts/  shared/  ai-workbench/     # 笔记；体系票在 ai-workbench/体系/
├── examples/
│   ├── project-docs/                    # 图片整理规格
│   ├── project-status/                  # android / windows
│   ├── android/                         # Gradle；scripts/ 发 APK
│   └── windows/                         # 空位
└── README.md
```

## 克隆与构建

```bash
git clone https://github.com/mengkj2021/ai-dev-notes.git
# 旧地址 picture-organizer 仍会跳转过来
cd ai-dev-notes
git config core.hooksPath .githooks
cd examples/android
./gradlew assembleDebug
```

## 勿提交

见 `.gitignore`：`build/`、`.gradle/`、`local.properties`、`examples/android/gradle/wrapper/*.zip`、密钥等。

## 许可

[MIT License](LICENSE)。
