# prompts · 拉入执行模板

本目录存放「**只改填写区，拉进对话即可执行**」的**编排壳**。细则在 `ai-workbench/`，用 **▶ include** 调用。

▶ include 语义：[`ai-workbench/宪法/include约定.md`](../ai-workbench/宪法/include约定.md)

## 用法

1. 打开对应模板（如 `票_接票开发.md`）
2. 只改文末填写区
3. 把该文件拉进对话并发送（AI 按壳内 ▶ include 打开分册）

## 命名

**扁平**：一律放在 `prompts/` 根下，命名为 `{用途}_{文件名}.md`（原用途子目录名 + `_` + 原文件名）。例：`git/拉取最新代码.md` → `git_拉取最新代码.md`。

## 分类索引

### 票 · 起票 / 接票 / 互审

| 文件 | 细则（include） |
|------|------|
| [票_起票分析.md](票_起票分析.md) | `ai-workbench/细则/prompts/起票分析.md` |
| [票_接票开发.md](票_接票开发.md) | `ai-workbench/细则/接票/` |
| [票_交叉审查.md](票_交叉审查.md) | `ai-workbench/细则/prompts/交叉审查.md` |

### 问答 · 只问不改

| 文件 | 细则（include） |
|------|------|
| [问答_式样问答.md](问答_式样问答.md) | `ai-workbench/细则/prompts/式样问答.md` |

### 文档 · 对齐现状

| 文件 | 细则（include） |
|------|------|
| [文档_文档整理.md](文档_文档整理.md) | `ai-workbench/细则/skills/sync-docs.md`（技能 sync-docs） |

### git · 提交 / 拉取

| 文件 | 细则（include） |
|------|------|
| [git_提交推送.md](git_提交推送.md) | `ai-workbench/细则/prompts/提交推送.md` |
| [git_拉取最新代码.md](git_拉取最新代码.md) | `ai-workbench/细则/prompts/拉取最新代码.md` |

## 新增模板约定

- 上半：编排壳 + ▶ include 分册
- 下半：空白填写区
- 新文件用 `{用途}_{名}.md` 放本目录根下；细则落 `ai-workbench/细则/`
