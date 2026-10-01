# 用 AI 开发本仓（公开导读）

> 给陌生人 / 新会话：本仓不只是 App 源码，还是一套**可复现的 AI 开发过程**。  
> **完整整合（推荐）**：[AI开发体系总览.md](AI开发体系总览.md) · 地图：[仓库结构总览.md](仓库结构总览.md)。

## 1. 闭环（记住这一条）

```text
拉 prompts/ 壳  →  读 project-status + project-docs
        →  只改 android/ 业务代码
        →  写回票复盘 / project-docs（文档服从代码）
```

- **真相源**：`android/app/src/main`（及 Gradle 版本目录）。  
- **规格镜像**：`project-docs/`。  
- **过程与排期**：`project-status/票库/`、`阶段/`。  
- **AI 怎么干活**：`prompts/`（编排壳）+ `shared/`（rules/skills）▶ include → `ai-workbench/`（细则与模板）。

## 2. 常用 prompts（拉进对话即执行）

| 壳 | 用途 |
|---|---|
| [`prompts/票_起票分析.md`](../../prompts/票_起票分析.md) | 杂散需求 → 归类落盘为票（不改代码） |
| [`prompts/票_接票开发.md`](../../prompts/票_接票开发.md) | 按票实现；**一票一会话** |
| [`prompts/文档_文档整理.md`](../../prompts/文档_文档整理.md) | 对照代码对齐文档（sync-docs） |
| [`prompts/git_提交推送.md`](../../prompts/git_提交推送.md) | 授权：按纪律 commit + push |
| [`prompts/问答_式样问答.md`](../../prompts/问答_式样问答.md) | 只问规格、不写码 |

票模板：[`ai-workbench/模板/票/`](../模板/票/)。技能：`create-screen-doc` / `implement-screen` / `sync-docs`（见 `.cursor/skills` 或 `shared/skills`）。

## 3. 你 vs AI

| 角色 | 负责 |
|---|---|
| **人** | 起票意图、确认「待定」、真机点验、Review、是否公开 / tag |
| **AI** | 接票、文档先行、改 `android/`、写回票与 docs、按 git 约定提交（需你拉提交壳授权） |

纪律摘要：中文提交用 `-F` 文件；禁止 `git add .`；禁止擅自 `--force`。详见 [git-convention](../../shared/rules/git-convention.md)。

## 4. 生命周期（简）

① 立项与骨架 → ② 填细节 / 修 bug / 体验 + 公开包装（已闭合）→ ③ 全面测试（已闭合 · 演示收口）→ ④ **维护与迭代**（当前开放：零散 Bug / 式样追加 / AI 体系更新）。  
票代号 **B / V / P**：[票代号与节点](../模板/票/票代号与节点.md)；阶段：[阶段/](../../project-status/阶段/README.md)。

## 5. 框架效果短结论

见同目录 [框架效果短结论.md](框架效果短结论.md)。  
**定位与边界**（单人实践、不够大型项目什么）：[AI开发实践-定位与现状.md](AI开发实践-定位与现状.md)。
