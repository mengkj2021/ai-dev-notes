# AGENTS.md

个人 AI 开发笔记。图片整理只是 `examples/` 里的练手例子。

1. 人定意图与验收；规格不写进 alwaysApply rules。
2. 先起票。产品车道：`examples/project-status/android|windows/`。笔记车道：`ai-workbench/体系/`。
3. 拉 `prompts/` 里对应壳（接票 / 起票 / 文档整理 / git）。细则用 ▶ include，打开 `ai-workbench/` 分册全文。
4. 产品规格只在 `examples/project-docs/`。Android 代码在 `examples/android/`。不要改 Windows（未立项）。
5. 画面技能 `create-screen-doc` / `implement-screen` 仅 Android。
6. 提交须用户明确要求。中文 commit 用 `-F` 文件，禁止 `git add .`。
7. 改 rules / skills 后同步三份工具目录（CodeBuddy / Cursor / DeepSeek），见下节。

## 本仓使用哪三个 AI 工具

统一源在 `shared/`（细则正文在 `ai-workbench/`）；改完**三份都同步**，禁止只改一处。

| 工具 | rules | skills | 本仓说明 |
|---|---|---|---|
| CodeBuddy | `.codebuddy/rules/<name>/RULE.mdc` | `.codebuddy/skills/<name>/SKILL.md` | 新增会话才注入规则 |
| Cursor | `.cursor/rules/<name>.mdc` | `.cursor/skills/<name>/SKILL.md` | 项目技能自动发现 |
| DeepSeek Harness（DSH） | `.dsh/rules/<name>/RULE.mdc` | `.dsh/skills/<name>/SKILL.md` | 工作区指令只自动注入根 `AGENTS.md` / `CLAUDE.md`；`.dsh/rules/` **不会自动注入**，须按指针读 |

同步细则：[ai-workbench/细则/rules/三工具同步.md](ai-workbench/细则/rules/三工具同步.md)；改完自查 `node scripts/check-shell-sync.mjs`（push 前门禁也会自动跑，漂移即拒绝推送）。

**DSH 会话须知**：`.dsh/rules/` 里的三份规则不会自动进入上下文。动 git 提交、起票接票这类事之前，先按需打开它们（等价于另两个工具的常驻注入）。**不要**在 `.agents/skills/` 放与 `.dsh/skills/` 同名的技能——DSH 优先用 `.dsh/skills/`，改错那份副本不会有任何报错。

整体系：[ai-workbench/指南/AI开发体系总览.md](ai-workbench/指南/AI开发体系总览.md)
