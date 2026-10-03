# sync-docs · 细则

> 供 `shared/skills/sync-docs/SKILL.md` ▶ include。  
> 亦可由 `prompts/文档_文档整理.md` ▶ include（拉入填写区时跑同一 SOP）。

# sync-docs（对照代码对齐文档）

对照 **当前例子源码**（默认 `examples/android/`），对齐 `examples/project-docs/` / 对应车道 `examples/project-status/`。  
**默认只改文档，不改业务代码**（填写区写明「极小标注」仍不改逻辑）。

服从 [`文档分层约定.md`](../../宪法/文档分层约定.md)。读法参考 [如何使用-project-docs](../../指南/如何使用-project-docs.md) · [如何使用-project-status](../../指南/如何使用-project-status.md)。

## 触发条件

命中任一即应用：

- 用户点名 `sync-docs` / 「整理文档」/ 「对齐文档」/ 「检查交叉引用」
- 拉入 `prompts/文档_文档整理.md` 并填写范围
- 收尾发现规格与代码漂移，且本会话以对齐文档为唯一目标

**不要**用于：实现新功能、改 Kotlin 逻辑、新建画面目录（→ `create-screen-doc` / `implement-screen` / `接票开发`）。

## 执行前确认

| 项 | 来源 |
|---|---|
| 范围 | 填写区或用户口述：功能名 / 画面 / route / 票号 |
| 模式 | 默认先列拟改要点等确认；写 `直接整理` 则跳过确认 |
| 开发日志 | 不维护；过程在票复盘 |

未写范围：据最近实现痕迹推断，方案里写明推断范围，**等确认再改**。

## SOP

### 1. 锁定范围

以功能名 / 画面 / route / 票号为准；禁止借机通读全库。

### 2. 定点读码（真相源）

只读相关：

- `examples/android/.../navigation/Routes.kt`（或等价路由注册）
- 对应 `ui/<screen>/`
- 涉及的 `data/` · `util/`
- 依赖变更时：`examples/android/gradle/libs.versions.toml`

### 3. 文档对照清单

按相关性勾选；无变动注明「无需改」。

| 文档 | 检查点 |
|---|---|
| `examples/project-docs/画面/<画面>/README.md` | 总览、进出、数据依赖、§10；区域索引 |
| `examples/project-docs/画面/<画面>/` 区域文件 | 与代码一致 |
| `examples/project-docs/路由设计.md` | §3 登记；过程见票 |
| `examples/project-docs/项目概述.md` · `画面/README.md` | 定位与清单 |
| `examples/project-docs/架构设计.md` | §2；按需数据流 / Room / 工具类 |
| `examples/project-docs/技术栈.md` | 仅当依赖/版本变了 |
| `examples/project-status/<车道>/阶段/*/待对应.md` | 未决是否过时 |
| `examples/project-status/<车道>/阶段/README.md` | 下一号是否仍准 |
| 该车道 `票库/` 分区 README | 票号列表是否缺行 |
| 相关其它画面 | 交叉入口文案 |
| 相关票复盘 | 仅填写区要求补过程说明时 |
| 根 `README.md` · `prompts/` · `shared/` 入口 | 仅路径/说明过时时 |

### 4. 交叉引用抽查（本技能增量）

在范围内至少核对：

1. 路由表行 ↔ `examples/project-docs/画面/<名>/` 目录存在且名称一致  
2. 画面清单 ↔ 路由表 / 目录  
3. 进行中票 ↔ 阶段 `待对应.md`；已完成票状态为 ✅ 且不出现在待对应
4. 票「关联画面」路径是否仍有效（旧 `docs/` 路径标出待修）

### 5. 方案与修改

1. 列出拟改文件与要点（除非 `直接整理`）  
2. 用户确认后改文档  
3. **原则**：文档服从代码；不扩无关章；不把「待定」改成已确认；结束后清空 prompt 填写区（若有）

### 6. 停手条件

- 发现必须改业务代码才能对齐 → **只列问题**，请用户改用 `prompts/票_接票开发.md`
- 式样「待定」→ 不得写成已落地

## 完成检查

- [ ] 范围内对照表逐项有结论（已改 / 无需改）  
- [ ] 交叉引用抽查无残留断链（或已列出待修项）  
- [ ] 未改业务 Kotlin（除非用户另行开票）  
- [ ] 若本票驱动：回写票「实际改动」与完结复盘  
