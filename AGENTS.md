# AGENTS.md

个人 AI 开发笔记。图片整理只是 `examples/` 练手例子。

1. 人定意图与验收；规格不写进 alwaysApply rules。
2. 先起票。产品：`examples/project-status/android|windows/`。笔记：`ai-workbench/体系/`。
3. 拉 `prompts/` 壳；细则 ▶ include → 打开 `ai-workbench/` 分册全文。
4. 规格只在 `examples/project-docs/`。码在 `examples/android/`。Windows 未立项勿改。
5. `create-screen-doc` / `implement-screen` 仅 Android。
6. 提交须用户明确要求；中文 commit 用 `-F`；禁止 `git add .`。
7. 改 rules / skills：先 `shared/`，再同步三工具目录（下表）。

## 三个 AI 工具

源：`shared/`；细则：`ai-workbench/`。改完三份都同步。

| 工具 | rules | skills | 说明 |
|---|---|---|---|
| CodeBuddy | `.codebuddy/rules/<name>/RULE.mdc` | `.codebuddy/skills/<name>/SKILL.md` | 新会话才注入 rules |
| Cursor | `.cursor/rules/<name>.mdc` | `.cursor/skills/<name>/SKILL.md` | 项目技能自动发现 |
| DeepSeek Harness | `.dsh/rules/<name>/RULE.mdc` | `.dsh/skills/<name>/SKILL.md` | 正文只在本文件；`CLAUDE.md` 转发桩。`.dsh/rules/` 不自动注入 |

同步：[三工具同步](ai-workbench/细则/rules/三工具同步.md)；`node scripts/check-shell-sync.mjs`（需 Node；push 门禁也会跑）。

**DSH**：动 git / 起票接票前按需打开 `.dsh/rules/`。勿在 `.agents/skills/` 放与 `.dsh/skills/` 同名技能。

总览：[AI开发体系总览](ai-workbench/指南/AI开发体系总览.md)
