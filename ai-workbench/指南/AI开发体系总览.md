# AI 开发体系总览（给人看）

> **整仓 AI 开发怎么拼在一起**——全面、精确、不展开细则。  
> 给维护者 / 开源读者；AI 干活请跟 [开场读序](../../project-status/README.md) 与具体 `prompts/`。  
> 目录地图：[仓库结构总览](仓库结构总览.md) · 效果自评：[框架效果短结论](框架效果短结论.md)

## 1. 一句话

本仓库 = **可复现的 AI 开发操作系统** + **唯一产物 `android/`**。  
人定意图与验收；AI 按票改码并写回文档；过程可追溯，规格不进常驻 rules。

## 2. 两大世界（冻结）

| 世界 | 目录 | 写什么 | 不写什么 |
|---|---|---|---|
| **项目真相** | `android/` · `project-docs/` · `project-status/` | 代码、已落地规格、票与进度 | AI 纪律、prompt 流程 |
| **AI 开发** | `prompts/` · `shared/` · `ai-workbench/` ·（同步到 `.cursor` / `.codebuddy`） | 怎么干、模板、宪法细则 | 画面规格正文、票正文 |

冲突时：`android/` 为准 → 再改 `project-docs/`；进度只认票 + [待对应](../../project-status/阶段/阶段4-维护与迭代/待对应.md)。

### 词表（易混）

| 说法 | 含义 |
|---|---|
| **式样**（口语） | 规格意图 / 需求口径 |
| **规格文件** | 只在 `project-docs/`（无独立「式样书」文件） |
| **式样变更票（S）** | 确认规格变化后的票类型，不表示另有式样书 |

## 3. 闭环

```
拉 prompts/ 壳  +  shared rules/skills（▶ include → ai-workbench 细则）
        ↓ 读
project-status（做到哪） + project-docs（做成什么样）
        ↓ 改
android/
        ↓ 写回
票复盘 / 待对应 · project-docs（文档服从代码）
```

| 层 | 路径 | 职责 |
|---|---|---|
| 编排壳 | `prompts/` | 拉进对话才跑的端到端流程（起票 / 接票 / 文档整理 / git…） |
| 常驻纪律 | `shared/rules/` | 短：起票制、git、双工具同步 → IDE |
| 可复用 SOP | `shared/skills/` | 新画面文档、实现画面、对齐文档 |
| 材料与元文档 | `ai-workbench/` | 指南 · 宪法 · 细则 · 模板 · 错题本 |
| 进度真相 | `project-status/` | 票库 · 阶段 · **待对应**（仪表盘）· 下一号 |
| 规格真相 | `project-docs/` | 架构 §2 · 路由 · 画面 · 数据层（android 镜像） |
| 代码 | `android/` | 唯一业务工程 |

## 4. 配置四象限（往哪放）

| 通道 | 何时 | 本仓 |
|---|---|---|
| **rules** | 每票都要、几句说清 | `shared/rules/`（瘦） |
| **prompts** | 填票号就能跑完一条流水线 | `prompts/*_….md` |
| **skills** | 可命名重复活 | `create-screen-doc` · `implement-screen` · `sync-docs` |
| **项目文档** | 规格 / 是否实现 | `project-docs/` · `project-status/`（只引用路径） |

细则权威在 `ai-workbench/`；壳用 ▶ include，禁止把 `project-docs` 规格全文粘进 alwaysApply。见 [AI配置落位](../宪法/AI配置落位.md)。

## 5. 工作怎么转（起票制）

| 步 | 做什么 | 落点 |
|---|---|---|
| 起票 | 需求 → Bug / S / T / F；绿档四块 | `模板/票/` → `票库/<分区>/` + **阶段4 待对应** ☐ |
| 接票 | **一票一会话**；文档先行；待定不猜 | `prompts/票_接票开发.md`；票 → 🚧 同步待对应 |
| 实现 | 只改必读路径；新画面先 docs 再码 | `android/` + 对应 `project-docs/` |
| 收尾 | 验收自评 → ✅/❌；待对应删行；✅ 进票目录 | 票复盘；可选 [错题本](../错题本.md) |
| 提交 | 人拉壳授权 | `prompts/git_提交推送.md`（`-F`、禁 `git add .`） |

- **节点**：B 骨架 / V 体验打磨 / P 公开包装 — [票代号与节点](../模板/票/票代号与节点.md)  
- **无票改动**：附记进票库 README，或极简 FN（P）；禁止无痕迹  
- **拆票**：门槛见细则；子票用新号，不用 `F28.1`

## 6. 进度怎么控（对人精确）

| 问 | 看 |
|---|---|
| 还剩什么 / 进行中？ | [阶段4 · 待对应](../../project-status/阶段/阶段4-维护与迭代/待对应.md)（☐/🚧 **必须**在此；搁置不进） |
| 下一号？ | [阶段/README](../../project-status/阶段/README.md) |
| 某票细节？ | `project-status/票库/<分区>/<票号>-….md` |
| 阶段 ③ 已做完什么？ | [阶段3 · 票目录](../../project-status/阶段/阶段3-全面测试/票目录.md)（历史；已闭合） |
| 阶段 ② 已做完什么？ | [阶段2 · 票目录](../../project-status/阶段/阶段2-填充与打磨/票目录.md)（历史；已闭合） |
| App 长什么样？ | [project-docs](../../project-docs/README.md)（**不是**进度） |

阶段 ④ **维护与迭代**（当前开放）：零散 Bug / 式样追加 / 产品补丁 + AI 开发体系更新。阶段 ③ 全面测试已收口。

**人新会话**：先 [project-status 开场读序](../../project-status/README.md)（阶段 README → 待对应 → 票 → docs 必读）。

## 7. 人 vs AI

| | 人 | AI |
|---|---|---|
| 意图 / 待定 / 真机点验 / Review / 是否公开 | ✓ | — |
| 起票落盘、接票实现、写回 docs/status、按壳 commit | 授权拉壳 | ✓ |
| 常驻 rules | 改后**新开对话**才生效 | 服从 |

## 8. 常用入口（只列名）

| 要做 | 拉 / 开 |
|---|---|
| 杂散需求变票 | `prompts/票_起票分析.md` |
| 按票开发 | `prompts/票_接票开发.md` |
| 只问规格 | `prompts/问答_式样问答.md` |
| 文档对齐代码 | `prompts/文档_文档整理.md` / skill `sync-docs` |
| 提交推送 | `prompts/git_提交推送.md` |
| 新画面文档 / 实现 | skills `create-screen-doc` · `implement-screen` |
| 分层 / include / 落位 | `ai-workbench/宪法/` |
| 公开短导读 | [用AI开发本仓](用AI开发本仓.md) |

## 9. 刻意不做

- 不把进度写进 `project-docs`  
- 不恢复总「改动清单」；仪表盘 = 待对应  
- 不建独立 `logs/`；过程在票复盘 + git  
- 不把产品规格塞进 rules / workbench 正文  

---

下钻：[指南索引](README.md) · [定位与现状](AI开发实践-定位与现状.md) · [文档分层](../宪法/文档分层约定.md) · [workbench 分区](../README.md) · [shared](../../shared/README.md) · [prompts](../../prompts/README.md)
