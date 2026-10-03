# 技能模板框架（shared/skills）

技能（skills）让 AI 具备领域专用能力。本目录是技能的统一维护入口，先搭建框架，之后逐步补充。

## 如何编写一个技能

每个技能一个子目录，统一 Markdown 格式：

```
shared/skills/<skill-name>/
├── SKILL.md      # 技能说明：用途、触发条件、执行流程（SOP）
└── assets/       # 可选：示例、模板等辅助资源
```

`SKILL.md` 为**编排壳**（frontmatter + ▶ include）；SOP 正文在 [`ai-workbench/细则/skills/`](../../ai-workbench/细则/skills/)。见 [`include约定.md`](../../ai-workbench/宪法/include约定.md)。

建议壳结构：frontmatter → ▶ include include约定 → ▶ include 对应 `细则/skills/`。

## 同步到各工具

统一源在 `shared/skills/<name>/`；改完**必须**同步两份，禁止只改一处：

| 工具 | 技能实际位置 |
|------|--------------|
| CodeBuddy | `.codebuddy/skills/<skill-name>/SKILL.md` |
| Cursor | `.cursor/skills/<skill-name>/SKILL.md`（项目技能） |

可选：`assets/` 一并复制。流程类技能变更时，对照 `examples/project-docs/画面/README.md`「新增画面」等权威摘要是否仍一致。

## 当前技能

| 技能名 | 用途 | 状态 | 正文票 |
|--------|------|------|--------|
| [`create-screen-doc`](create-screen-doc/SKILL.md) | 新增画面文档 SOP（模板 → 路由 §3 → 画面清单 → 填写十节，含 insets） | ✅ 已落地 | **F17** |
| [`implement-screen`](implement-screen/SKILL.md) | 读画面文档 → 实现 Kotlin + 路由注册（含壳层/交互核对） | ✅ 已落地 | **F18** |
| [`sync-docs`](sync-docs/SKILL.md) | 对照代码对齐 project-docs / project-status（含交叉引用） | ✅ 已落地 | 结构改修已落地 |

> 本项目全部代码由 AI 开发；上述技能覆盖文档、代码实现与文档对齐。  
> **勿**在未起票时批量建空目录。新文档服从 [`文档分层约定.md`](../../ai-workbench/宪法/文档分层约定.md)。
