# 生成 AI 开发仓库结构（可外带 · 单文件自包含）

> 把**本文件整份**交给任意 AI，按文末填写区生成「用 AI 开发产品」的最小脚手架。  
> 不依赖本仓库其它文件。维护路径：`prompts/脚手架_生成AI开发结构.md`。

---

## 任务

1. 在目标根创建下列目录树，并写入**可开读**的初始 Markdown（占位可，职责与闭环必须写清）
2. `{CODE_ROOT}` 按填写区替换（默认 `android`）；**不**生成业务代码，只写 `{CODE_ROOT}/README.md`
3. **`shared/` 必须有**；**默认不生成** `.cursor/` / `.codebuddy/`（属个人 IDE 偏好，非脚手架必备）
4. 输出：已创建路径清单 + 接下来 5 件事

---

## 必须写入生成物的原则

**两大世界**

| 世界 | 目录 | 写什么 |
|---|---|---|
| 真相 | `project-docs/` · `project-status/` · `{CODE_ROOT}/` | 规格、票、源码 |
| AI 开发 | `prompts/` · `shared/` · `ai-workbench/` ·（可选）IDE 副本 | 流程壳、rules/skills、宪法细则模板 |

**闭环**

```
prompts/ + shared/  →▶ include→  ai-workbench/
        ↓ 读
project-status/ + project-docs/
        ↓ 改
{CODE_ROOT}/
        ↓ 写回
project-status/ + project-docs/
（过程写票内复盘）
```

**▶ include**：壳内出现 `▶ include \`相对仓库根路径\`` → 立刻打开全文视同嵌入，禁止只贴链接。

**四象限**：rules=短常驻红绿灯；prompts=拉入才跑的流程；skills=可命名 SOP；项目文档=地图（可很长）。

**票状态**：`☐` → `🚧` → `✅`（另有 `❌` / 搁置）；**不搬家**，只改符号与台账。

**纪律**：票驱动 · 文档先行 · 一票一会话 · 禁止通读全库 · 待定不猜 · rules 勿粘式样全文。

**IDE 副本（硬性）**

- 统一源永远在 `shared/rules/` · `shared/skills/`。
- **默认不创建** `.cursor/`、`.codebuddy/`。
- 生成前若填写区「IDE 工具」为空或写「待问 / 未定」→ **先问用户**用哪些工具（可多选），再继续；**禁止**默认当成 Cursor+CodeBuddy 并双份生成。
- 选项示例：`Cursor` / `CodeBuddy` / `都不要（仅 shared）` / `其它（说明路径约定）`。
- 仅当用户明确选定后，才从 `shared/` **复制**对应目录：
  - Cursor → `.cursor/rules/*.mdc` · `.cursor/skills/*/SKILL.md`
  - CodeBuddy → `.codebuddy/rules/<name>/RULE.mdc` · `.codebuddy/skills/*/SKILL.md`
- 只选一个则只生成那一份；选「都不要」则根 README / sync 约定写明「需要时再从 shared 同步到所用 IDE」。

---

## 目标目录树（必建）

```
{PROJECT}/
├── README.md
├── project-docs/
│   ├── README.md
│   ├── 项目概述.md
│   ├── 架构设计.md          # 至少 §2 包/模块
│   ├── 路由设计.md          # 无路由可注明 N/A，仍保留文件
│   ├── 技术栈.md
│   ├── 核心流程.md
│   └── 画面/README.md
├── project-status/
│   ├── README.md
│   ├── 阶段/README.md · 阶段1-立项与骨架/ · 阶段2-填充与打磨/{待对应,票目录}.md · 阶段3-全面测试/{待对应,票目录}.md · 阶段4-维护与迭代/{待对应,票目录}.md
│   └── 票库/
│       ├── README.md
│       └── bug/ · 式样变更/ · 调查/ · 功能体验/   # 各一份 README（仅票号列表）
├── {CODE_ROOT}/README.md
├── prompts/
│   ├── README.md
│   ├── 票_起票分析.md · 票_接票开发.md
│   ├── 问答_式样问答.md
│   └── 文档_文档整理.md
├── shared/
│   ├── README.md
│   ├── rules/dev-convention.md · git-convention.md · sync-convention.md
│   └── skills/
│       ├── create-screen-doc/SKILL.md
│       ├── implement-screen/SKILL.md
│       └── sync-docs/SKILL.md
├── ai-workbench/
│   ├── README.md
│   ├── 指南/仓库结构总览.md · 如何使用-project-docs.md · 如何使用-project-status.md
│   │         · AI开发体系总览.md · 用AI开发本仓.md   # 给人看的入口（建议写）
│   ├── 宪法/文档分层约定.md · include约定.md · AI配置落位.md
│   ├── 细则/
│   │   ├── 接票/拆票门槛与档位.md · 读文档定边界.md · 先测后写.md · 收尾与验收自评.md · 硬性约束.md
│   │   ├── prompts/起票分析.md · 式样问答.md · 文档整理.md
│   │   ├── rules/开发约定.md · git约定.md · 双工具同步.md
│   │   └── skills/create-screen-doc.md · implement-screen.md · sync-docs.md
│   └── 模板/
│       ├── 画面文档/README.md · 画面区域文档模板.md
│       └── 票/README.md · bug票.md · 式样变更票.md · 调查票.md · 功能体验票.md · 票代号与节点.md
├── LICENSE                          # 建议生成 MIT 占位
└── .githooks/                       # 可选
```

## 按需生成（仅用户选定后）

```
# 若选 Cursor：
├── .cursor/rules/*.mdc · skills/*/SKILL.md     # 自 shared 复制

# 若选 CodeBuddy：
└── .codebuddy/rules/<name>/RULE.mdc · skills/*/SKILL.md
```

**故意不生成（新项目非必须，可后加）**：交叉审查、git 提交/拉取壳、脚手架自复制、错题本、框架效果短结论、CONTRIBUTING、GitHub Release/APK；以及**未确认前的** `.cursor/` / `.codebuddy/`。  
**本仓作样板时另须有**：`{CODE_ROOT}/README.md`（构建门面）、根 README 链到体系总览。

---

## 内容要点（展开成可读文，勿只建空文件）

| 区域 | 最少写清 |
|---|---|
| 根 README | 产品一句话、两大世界、闭环、指向 docs/status/workbench/prompts/shared；`{CODE_ROOT}`=唯一代码工程；IDE 副本按实际选定写（未选则写「规则在 shared，按需同步到所用 IDE」） |
| `{CODE_ROOT}/README` | Open 本目录、环境、assembleDebug、版号出处；勿只建空文件 |
| project-docs | 现状镜像（冲突以代码为准）；概述/架构§2/路由表/技术栈/主流程/画面清单+新增步骤 |
| project-status | 起票制、四类型、状态机；各阶段 `待对应.md`；各分区列表 |
| prompts | 扁平命名 `{用途}_{名}.md`；上半骨架 + ▶ include 细则；下半空白填写区。接票须串接票分册；起票只落盘不改码；式样问答只问不改；文档整理 → sync-docs |
| shared | rules：YAML frontmatter + include 约定 + 细则；skills 同；改完按用户选定的 IDE 同步（未选 IDE 则只维护 shared） |
| 宪法 | 分层（两大世界、目录名冻结）；include 语义；落位四象限+盘点表 |
| 接票细则 | 可执行短文：拆票≥2、读文档清单、先测或豁免、收尾回写、硬性约束 |
| rules 细则 | 开发：票驱动/文档先行/一票一会话/读剂量；git：禁止 `git add .`、合入开关默认 `direct-main`；同步：先改 shared，再复制到**已启用**的 IDE 目录（无 IDE 目录则跳过复制） |
| skills 细则 | 建画面文档→登记路由；按文档实现并注册；对照代码对齐文档（默认不改业务码） |
| 模板 | 画面十节骨架；票：信息表/关联画面/诉求/步骤/验收/开发记录/完结复盘 |

---

## 生成顺序

1. 读填写区；缺省 `CODE_ROOT=android`  
2. **IDE**：填写区为空 /「待问」/「未定」→ **先停下来问**用户要 Cursor、CodeBuddy、都不要、或其它；写清答案后再往下。**禁止**默认 Cursor+CodeBuddy  
3. 目标根已有冲突 → 先列冲突再问（填写区「允许覆盖」则可覆盖脚手架，仍不删业务代码）  
4. 建**必建**目录 → 写文件 → 仅对已选 IDE 从 shared 复制壳 → 自检（闭环在根 README+指南+分层；prompts 有填写区；若有 IDE 则 shared↔该 IDE 一致；无 IDE 则勿创建空 `.cursor`/`.codebuddy`；`{CODE_ROOT}/README` 可开读）  
5. 清单 + 接下来：填概述 → 填架构§2与技术栈 → 放入/初始化 `{CODE_ROOT}` → `prompts/票_起票分析.md` 起票 → 新对话用 `prompts/票_接票开发.md` 接票  

## 禁止

- 只建空文件夹 · 生成大型业务实现 · 把规格写进 AI 目录 · 另造第三世界文档根 · 未允许时删用户文件  
- **未询问 / 未确认就生成** `.cursor/` 或 `.codebuddy/`  

---

## 本次生成（每次只改这里）

```
项目名称（{PROJECT}）：
代码根目录名（{CODE_ROOT}）：android
产品一句话：
主要技术栈：
IDE 工具：（默认空 = 生成前先问；例：Cursor / CodeBuddy / 都不要）
目标根路径（默认工作区根）：
是否允许覆盖已存在脚手架文件：否
其它约束：
```
