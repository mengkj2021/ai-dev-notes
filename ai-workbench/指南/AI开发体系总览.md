# AI 开发体系总览（给人看）

> 整仓怎么拼在一起。干活跟 [产品车道](../../examples/project-status/README.md)、[体系车道](../体系/README.md) 与 `prompts/`。

## 1. 一句话

本仓库 = **个人 AI 开发笔记** + **练手例子**（图片整理：Android 已有，Windows 空位）。  
人定意图与验收；AI 按票改当前目标并写回；规格不进常驻 rules。

## 2. 目录

```
├── prompts/  shared/  ai-workbench/     # 笔记：怎么干
│                 └── 体系/              # 笔记层票
├── examples/
│   ├── project-docs/                    # 图片整理规格
│   ├── project-status/                  # android / windows 票
│   ├── android/                         # Gradle；scripts/ 发 APK
│   └── windows/                         # 空位
└── README.md
```

| 层 | 目录 | 写什么 | 不写什么 |
|---|---|---|---|
| **核心 · 笔记** | `prompts/` · `shared/` · `ai-workbench/` | 怎么干、模板、宪法细则、体系票 | 画面规格 |
| **例子** | `examples/` | 共享规格、分端进度、例子源码 | AI 纪律 |

冲突：以**当前接票例子**源码为准 → 再改 `examples/project-docs/`。

## 3. 闭环

```
拉 prompts/  +  shared（▶ include → 细则）
        ↓ 读
examples/project-docs + 该车道 status
        ↓ 改
examples/<端>/ 或笔记层（prompts/ · shared/ · ai-workbench/）
        ↓ 写回
该车道票复盘 · examples/project-docs
```

产品车道：`examples/project-status/android|windows/`。笔记车道：`ai-workbench/体系/`。

## 4. 四象限

| 通道 | 何时 | 本仓 |
|---|---|---|
| **rules** | 每票都要、几句说清 | `shared/rules/` → `.codebuddy/` · `.cursor/` · `.dsh/` |
| **prompts** | 填票号跑完一条流水线 | `prompts/` |
| **skills** | 可命名重复活 | `create-screen-doc` · `implement-screen` · `sync-docs` |
| **项目文档** | 规格 / 是否实现 | `examples/project-docs/` · 该车道 status |

见 [AI配置落位](../宪法/AI配置落位.md)。

## 5. 工作怎么转

| 步 | 做什么 | 落点 |
|---|---|---|
| 起票 | 先选车道；Bug / S / T / F | `模板/票/` → 该车道票库 + 待对应 ☐ |
| 接票 | 一票一会话；待定不猜 | `prompts/票_接票开发.md` |
| 实现 | 只改必读路径 | `examples/<端>/` 或笔记层 + docs |
| 收尾 | ✅/❌；待对应删行 | 该车道票目录 |
| 提交 | 人授权 | `prompts/git_提交推送.md` |

无票改动：该车道票库附记，或体系极简 FN（P）。

## 6. 进度

| 问 | 看 |
|---|---|
| 产品先去哪？ | [选车道](../../examples/project-status/README.md) |
| 笔记层还剩什么？ | [体系待对应](../体系/阶段/维护/待对应.md) |
| Android 还剩什么？ | [android 阶段4 待对应](../../examples/project-status/android/阶段/阶段4-维护与迭代/待对应.md) |
| Windows？ | [windows README](../../examples/project-status/windows/README.md)（未立项） |
| App 长什么样？ | [project-docs](../../examples/project-docs/README.md) |

## 7. 人 vs AI

| | 人 | AI |
|---|---|---|
| 意图 / 待定 / 真机点验 / Review | ✓ | — |
| 起票落盘、接票实现、写回 | 授权拉壳 | ✓ |

## 8. 常用入口

| 要做 | 拉 / 开 |
|---|---|
| 杂散需求变票 | `prompts/票_起票分析.md`（写明车道） |
| 按票开发 | `prompts/票_接票开发.md` |
| 只问规格 | `prompts/问答_式样问答.md` |
| 文档对齐代码 | `prompts/文档_文档整理.md` / `sync-docs` |
| GitHub 摘录 | `prompts/收集_GitHub.md` |
| 提交推送 | `prompts/git_提交推送.md` |
| 新画面（Android） | `create-screen-doc` · `implement-screen` |

## 9. 三个工具

同一份 `shared/` 壳同步三处，禁止只改一处：

| 工具 | rules | skills |
|---|---|---|
| CodeBuddy | `.codebuddy/rules/<name>/RULE.mdc` | `.codebuddy/skills/<name>/SKILL.md` |
| Cursor | `.cursor/rules/<name>.mdc` | `.cursor/skills/<name>/SKILL.md` |
| DeepSeek Harness（DSH） | `.dsh/rules/<name>/RULE.mdc` | `.dsh/skills/<name>/SKILL.md` |

DSH 工作区指令：**`AGENTS.md` 正文 + `CLAUDE.md` 转发桩**（逐级就近）；`.dsh/rules/` **不会被注入**，须按指针打开。同步：`node scripts/check-shell-sync.mjs`（需 Node），push 前门禁也会跑。细则：[三工具同步](../细则/rules/三工具同步.md)。

## 10. 刻意不做

- 进度不写进 `project-docs`
- 不建独立 `logs/`
- 不把规格塞进 rules
- 本轮不实现 Windows

---

下钻：[指南索引](README.md) · [定位与现状](AI开发实践-定位与现状.md) · [通识](通识/00-目录.md) · [分层](../宪法/文档分层约定.md)
