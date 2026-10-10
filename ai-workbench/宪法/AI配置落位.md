# AI 配置落位（rules / prompts / skills / docs）

> **权威落位说明**（主题：AI 配置四象限）。权威目录为本目录（`ai-workbench/`）；产品真相在 `examples/project-docs/` / `examples/project-status/`，**不**整份复制进常驻 rules。  
> 口诀：`rules = 红绿灯`；`prompts = 这次怎么开车`；`skills = 某一类活的标准作业`；`ai-workbench = 工具箱 + 元文档`；`项目文档 = 地图（按需打开）`。  
> 闭环见 [文档分层约定.md](文档分层约定.md) **§0**。  
> 下一号：Android [`阶段 README`](../../examples/project-status/android/阶段/README.md)（**F55** / Bug20 / S10 / T5）；体系 [`阶段 README`](../体系/阶段/README.md)（**F58**）。

## 1. 四象限

| 通道 | 何时用 | 体积 | 本仓库路径 |
|---|---|---|---|
| **rules** | 每票高频、几句说清、违了易出事 | 短；常驻注入 | `shared/rules/` → 同步 `.cursor` / `.codebuddy` / `.dsh` |
| **prompts** | 端到端流程 + 填写区，拉入才跑 | 中 | `prompts/`（仓库根） |
| **skills** | 可命名触发、可复用 SOP | 中长；按需 | `shared/skills/<name>/`（细则在 `ai-workbench/细则/skills/`） |
| **项目文档** | 式样 / 画面 / 架构 / 票库真相 | 可很长 | `examples/project-docs/` · `examples/project-status/` — **只引用路径与章节，不塞进 rules** |

## 2. 判定标准（往哪放）

| 问自己 | → 落点 |
|---|---|
| 是否几乎每票都要、且能写成 ≤ 数条短句？ | **rules** |
| 是否「打开模板填票号就能跑完一条流水线」？ | **prompts** |
| 是否某一类重复活（新画面、对文档、改依赖）有固定步骤？ | **skills** |
| 是否产品规格 / 架构 / 验收细节？ | **项目文档**（配置里只写「去读哪个路径」） |

**禁止**：把画面 README、架构全文等规格粘进 `alwaysApply` rules（违背**规则瘦身**底线：常驻短、按需长）。

## 3. 读文档策略（与 rules 短纪律一致）

1. 先票（或当前 prompt 填写区）→ 再票内**关联路径**  
2. 先选世界：真相看 `examples/project-status/` / `examples/project-docs/`；AI 看 `ai-workbench/` + `prompts/` + `shared/`（闭环 §0）；**不通读**整库  
3. 接票时列出「本票必读」路径/章节（见 `prompts/票_接票开发.md`），单次只打开清单内材料  
4. 需要时再横跳链接；历史与错题本按需取，不预装进上下文  

## 4. 盘点表（现状）

### 4.1 rules（已有）

| 文件 | 落位 | 备注 |
|---|---|---|
| `sync-convention` | rules | 三工具同步（CodeBuddy / Cursor / DeepSeek Harness） |
| `dev-convention` | rules | 起票制、文档先行、一票一会话、读文档剂量、拆票/步骤短句 |
| `git-convention` | rules | 提交编码、add、push 门禁、合入方式开关、`ticket/` 分支与绿黄红 |

拆票 / 步骤 / 分支：短句在 `dev`/`git`，细则在 `ai-workbench/细则/接票/` 与票模板 / 票 README「母题与子票」。

三工具落位（同一份壳同步三处；DSH：`AGENTS.md` 正文 + `CLAUDE.md` 转发桩，`.dsh/rules/` 须按指针读；`scripts/check-shell-sync.mjs` 校验，需 Node；push 前门禁自动跑）：

| 工具 | rules | skills |
|---|---|---|
| CodeBuddy | `.codebuddy/rules/<name>/RULE.mdc` | `.codebuddy/skills/<name>/SKILL.md` |
| Cursor | `.cursor/rules/<name>.mdc` | `.cursor/skills/<name>/SKILL.md` |
| DeepSeek Harness（DSH） | `.dsh/rules/<name>/RULE.mdc` | `.dsh/skills/<name>/SKILL.md` |

细则：[`ai-workbench/细则/rules/三工具同步.md`](../细则/rules/三工具同步.md)；根 `AGENTS.md` 有工具总表。

### 4.2 prompts（已有）

| 文件 | 落位 |
|---|---|
| `票_起票分析.md` | 杂散需求 → 起票（关联画面预估；对照拆票门槛） |
| `票_接票开发.md` | 接票主流程（必读清单；关联画面回写；拆票 / 步骤 / 档位） |
| `票_交叉审查.md` | 可选互审 |
| `问答_式样问答.md` | 只问不改 |
| `文档_文档整理.md` | 对照代码对齐文档（▶ include sync-docs；过程写票复盘，**不**另建开发日志） |
| `git_提交推送.md` | 按纪律 commit / 可选 push |
| `git_拉取最新代码.md` | 安全 pull / rebase |
| `收集_GitHub.md` | 摘录进 `指南/GitHub收集/`（摘录≠ SOP） |

### 4.3 skills

| 技能 | 落位目标 | 产品票（正文落地） | 状态 |
|---|---|---|---|
| `create-screen-doc` | skills → 同步 IDE | **F17** | ✅ |
| `implement-screen` | skills → 同步 IDE | **F18** | ✅ |
| `sync-docs` | skills → 同步 IDE | 结构改修已落地 | ✅ |

### 4.4 项目文档、按需读

| 内容 | 路径 |
|---|---|
| 式样概述 / 画面清单 | `examples/project-docs/项目概述.md` · `examples/project-docs/画面/` |
| 是否实现 | 产品 `examples/project-status/<端>/`；笔记 `ai-workbench/体系/` |
| 阶段 / 下一号 | [产品车道](../../examples/project-status/README.md) · [体系](../体系/README.md) |
| 票库 | `examples/project-status/android/` · `ai-workbench/体系/票库/` |
| 架构 / 路由 / 画面 | `examples/project-docs/`（[两端差异](../../examples/project-docs/两端差异.md)） |
| 错题本 / 模板 / 指南 | `ai-workbench/` |
| AI 通识笔记（非本仓纪律） | [`指南/通识/`](../指南/通识/00-目录.md) |
| GitHub 前沿摘录 | [`指南/GitHub收集/00-目录.md`](../指南/GitHub收集/00-目录.md)；拉 `prompts/收集_GitHub.md` |
| 过程 / 完结复盘 | **票内**（开发记录 · 完结复盘）；git 历史 |
| 分层宪法 + 闭环 | `ai-workbench/宪法/文档分层约定.md` |
| 仓库结构 / docs·status 用法 | `ai-workbench/指南/` |

## 5. 主题分工（勿与产品 F 号混读）

| 主题 | 管什么 | 权威路径 |
|---|---|---|
| 文档分层 / 核心+例子 / 闭环 | 生成文档落哪 | `宪法/文档分层约定.md` |
| 本说明（四象限） | rules/prompts/skills/docs 落位 | 本文 |
| 拆票门槛 / 票内步骤 / 分支 | 大票怎么切、档位 | `细则/接票/拆票门槛与档位.md` |
| 先测后写 | 可测纯逻辑顺序 | `细则/接票/先测后写.md`（依赖产品 **F16**） |
| 合入方式开关 | `direct-main` / `via-mr` | `细则/rules/git约定.md` |
| 规则瘦身 | 常驻短、按需长 | 本文 §2 + 开发约定 |
| AI 通识笔记 | 概念/工具/模式/协作；不覆盖本仓细则 | [`指南/通识/00-目录.md`](../指南/通识/00-目录.md)；DSH 口径见 [2.4](../指南/通识/工具/04-DeepSeek%20Harness%20使用.md) |
| GitHub 收集 | 外部摘录；摘录≠ SOP；拉 `prompts/收集_GitHub.md` | [`指南/GitHub收集/00-目录.md`](../指南/GitHub收集/00-目录.md) |
| 过程只留票复盘 | 禁独立 `logs/` 开发日志目录 | 分层宪法；sync-docs |
| 画面技能 | 建文档 / 实现画面 | F17 / F18 + `细则/skills/` |
