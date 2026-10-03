# 2.3 Cursor 使用

Cursor 配置分层、Rules/Skills/Hooks/MCP，以及 Cursor 3 / Projects。账号中心：https://cursor.com/dashboard。更新于 2026-10。

## 产品形态（Cursor 3 起）

- **Agent 工作区**：侧边栏统一本地与云端 Agent（也可从 Slack / GitHub / Linear / 手机发起）
- **云 ↔ 本地交接**：云上跑完可拉回本机改和测
- **多仓库布局**；随时切回传统 IDE
- **Plugins**：一键装 MCP、Skills、子代理；团队可私有市场
- **Composer 2.5**：自研编码模型，计入 Cursor Models 用量池（与 Grok 4.5–4.7 同池）
- **第三方模型**（Claude / GPT / Gemini / GLM / Kimi 等）：Other Models 池，按各家 API 价；团队另加 Cursor Token Rate
- **Auto**：Cost / Balance / Intelligence 三档路由

定价观察：Pro $20 / Pro Plus $60 / Ultra $200；Teams Standard $40、Premium $120 每席。额度外可按量，不降智。

## Projects（2026-09，beta）

跨数月的功能/迁移/「园艺」用协调器，而不是无限开聊天。

| 能力 | 说明 |
|------|------|
| 协调器 | 自己不写代码，调度子代理；始终可对话 |
| 云默认 | 关电脑不停；本机只跑需要真机测的部分 |
| 共享上下文 | 研究笔记、测法、偏好跨 Agent 同步 |
| Subscriptions | 盯 Slack、定时、跟 PR/CI，事件驱动 |

适合：多 PR 功能、上百 PR 的迁移、设计系统扫 PR。单次小改仍用普通 Agent。与 [3.7 GSD](../开发模式/07-GSD%20编排驱动开发.md) 互补：GSD 是文件化纪律，Projects 是产品化调度。

## 配置分层

```
用户级（~/.cursor/ 或 %APPDATA%\Cursor\）
├── User Rules          → Customize → Rules（UI，非文件）
├── settings.json       → 编辑器 + cursor.* 设置
├── mcp.json            → 全局 MCP
├── hooks.json + hooks/ → 全局 Hooks
└── skills/、commands/  → 个人 Skill、斜杠命令

项目级（.cursor/，可 Git 提交）
├── rules/*.mdc         → 项目 Rules
├── skills/             → 项目 Skills
├── commands/           → 斜杠命令
├── mcp.json、hooks.json、hooks/
└── worktrees.json      → Worktree 初始化

项目根
└── AGENTS.md           → 轻量 Agent 指令（无 frontmatter）

团队级（Dashboard）
├── Team Rules / Team MCP
└── Team Marketplace

内置（勿改）
└── ~/.cursor/skills-cursor/
```

## Rules

持久化 AI 指令。优先级：**Team > Project > User**。

**Project Rule 示例（`.mdc`）：**

```yaml
---
description: Android Kotlin 开发规范
globs: **/*.{kt,java,xml}
alwaysApply: false
---
- 使用 Kotlin + MVVM
- 字符串放 strings.xml
- 请用简体中文回复
```

| 生效方式 | 配置 |
|----------|------|
| 始终 | `alwaysApply: true` |
| 智能匹配 | 写 `description` |
| 按文件 | 写 `globs` |
| 手动 | 对话中 `@规则名` |

注意：`.cursor/rules/` 下 plain `.md` 不识别；Rules 不影响 Tab 补全。单条建议 < 500 行。

## Skills

多步骤可重复流程（构建、审查、发版）。目录：`.cursor/skills/<name>/SKILL.md`。

```yaml
---
name: android-build
description: 构建 Android 并验证。Use when building APK.
paths:
  - "**/*.gradle.kts"
disable-model-invocation: true
---
# Android Build
1. `./gradlew assembleDebug`
2. 检查输出并报告错误
```

| 对比 | Rules | Skills |
|------|-------|--------|
| 长度 | 短 | 长（完整流程） |
| 触发 | 自动/globs/@ | `/name` 或 Agent 自动 |
| 用途 | 规范约束 | 部署、审查、迁移 |

## Hooks

Agent 生命周期脚本。常用事件：`beforeShellExecution`、`afterFileEdit`、`beforeSubmitPrompt`、`preToolUse`。

```json
{
  "version": 1,
  "hooks": {
    "beforeShellExecution": [{
      "command": ".cursor/hooks/block-dangerous.sh",
      "matcher": "rm -rf",
      "failClosed": true
    }],
    "afterFileEdit": [{
      "command": ".cursor/hooks/format.sh"
    }]
  }
}
```

创建：聊天 `/create-hook` 或 Customize → Hooks。

## MCP

连接外部 API、数据库、GitHub 等。配置 `.cursor/mcp.json`：

- **本地**：`command` + `args`（如 `npx -y some-mcp-server`）
- **远程**：URL
- API Key 用 `${env:NAME}`，不写死在文件里

## 斜杠命令 vs Skills

Commands（`.cursor/commands/`）= 固定 Prompt 模板；官方趋势是迁移为 Skills（`/migrate-to-skills`）。

## Worktrees

多 Agent 在独立 checkout 并行：`/worktree`、`/apply-worktree`、`/delete-worktree`。

## @ 上下文引用

| 引用 | 作用 |
|------|------|
| @文件名 | 附加文件 |
| @文件夹/ | 附加目录 |
| @Codebase | 语义搜索代码库 |
| @Docs / @Web | 文档 / 网页 |
| @Git | 变更与提交 |
| @规则名 / @skill名 | 手动引用 |

同名文件易混淆，Prompt 中写**完整路径**。

## 推荐项目结构（摘录）

```
my-project/
├── AGENTS.md
├── .cursor/
│   ├── rules/
│   ├── skills/
│   ├── hooks.json + hooks/
│   └── mcp.json
└── src/
```

## 常用内置命令

| 命令 | 作用 |
|------|------|
| /create-rule、/create-skill、/create-hook | 创建配置 |
| /review、/review-bugbot、/review-security | 审查 |
| /split-to-prs | 拆分 PR |
| /migrate-to-skills | Rules/Commands → Skills |
| /worktree 系列 | 并行 checkout |

Windows 用户设置：`%APPDATA%\Cursor\User\settings.json`

公司主体：2026-08-14 起 Anysphere 为 SpaceX 全资子公司。采购/合规需确认数据处理协议是否随主体变更。OpenAI 模型仍可选，并非停供。
