# create-screen-doc · 细则

> 供 `shared/skills/create-screen-doc/SKILL.md` ▶ include。

# create-screen-doc（新建画面文档）

本技能只做**文档先行**。实现见 `implement-screen`（**F18**）。**仅 Android 例子**；不要用于 Windows 车道。

权威摘要：`examples/project-docs/画面/README.md`「新增画面」、`examples/project-docs/路由设计.md` §4。服从 `ai-workbench/宪法/文档分层约定.md`（新建画面用 `ai-workbench/模板/画面文档/`）。

## 触发条件

命中任一即应用本技能：

- 要**新增**一个 route 对应的画面（尚无 `examples/project-docs/画面/<名>/`）
- 用户点名 `create-screen-doc` / 「新建画面文档」
- 接票后需先建画面目录再写码（本技能只覆盖文档部分）

**不要**用于：只改已有画面文案、纯代码修 bug、实现 Composable（改用 `implement-screen`）。

## 执行前确认

向用户确认（或从票内读取）：

| 项 | 说明 |
|---|---|
| 画面中文名 | 目录名，如 `开源许可画面` |
| route | 全小写、多词连字符；路径参用 `{param}`（见路由设计 §2） |
| 职责一句话 | 写入 README §1 |
| 是否区域拆分 | 简单画面可只 README；复杂再加区域文件 |

票上「待定」未清则**停**，先回票澄清。

## SOP（四步，顺序固定）

### 1. 复制模板 → 画面目录

1. 复制整目录 `ai-workbench/模板/画面文档/` → `examples/project-docs/画面/<画面名>/`
2. 保留 `README.md`；按需复制/改名 `画面区域文档模板.md` 为区域文件（如 `列表.md`）
3. **不要**把 `画面文档模板` 本身改成业务画面

### 2. 登记路由表

在 `examples/project-docs/路由设计.md` **§3 当前导航图**表追加一行：

| 列 | 填法 |
|---|---|
| route | 与约定一致的字符串 |
| 画面 | 中文名 |
| 参数 | 无 / 路径参 / query 说明 |
| 文档 | 链到 `画面/<画面名>/README.md`（相对路由设计） |

可选：在票开发记录写一句「路由已登记」（日期即可）。

### 3. 更新画面清单

1. `examples/project-docs/画面/README.md`「当前画面清单」表追加一行：目录 | route | `ui/` 包 | 说明 | 状态（新建通常 `☐ 待实现`）
2. （可选）所属阶段 `待对应.md` 若该画面相关未决已关闭则删行 / 更新

两处必改：路由表 + 画面清单（同票一起改）。

### 4. 填写 README 十节（+ 区域按需）

按 `ai-workbench/模板/画面文档/README.md` 结构填满，勿删节号：

| 节 | 最低要求 |
|---|---|
| 1 概述 | 名称、route、职责 |
| 2 路由 | 字符串、必选/可选参数 |
| 3 功能清单 | 可勾选条目；细节可指区域文件 |
| 4 用户操作 | 主路径步骤 |
| 5 UI 壳层 | **必含 insets / edge-to-edge 检查**（见下） |
| 6 系统返回 | 对照路由设计 §5；有未保存态写清拦截 |
| 7 进入与退出 | 从哪进、去哪 |
| 8 数据依赖 | 有 VM 则写 UiState/Event/Effect、Repository；无则写「无」 |
| 9 待定 | 无则写「无」或删空勾 |
| 10 开发记录 | README 可留「见相关票」；过程写票复盘，不另建日志文件 |

区域文件按 `ai-workbench/模板/画面文档/画面区域文档模板.md` 填写。

#### §5 UI 壳层 · insets 强制检查（Bug1 / 错题本）

新建画面 README §5 **必须**写明下列检查结果（可勾选）：

- [ ] 整页使用 M3 `Scaffold`（或等价），内容消费 **SystemBars insets**（防系统栏裁切，Bug1）
- [ ] 若自定义 `bottomBar`（非 `BottomAppBar`）：已规划 `navigationBarsPadding`（Bug3）
- [ ] edge-to-edge / 沉浸策略与相邻画面一致，或显式写差异

未写清不得声称「文档完成」。模板原文见 `ai-workbench/模板/画面文档/README.md` §5。

## 完成后自检

- [ ] `examples/project-docs/画面/<名>/README.md` 存在且十节已填
- [ ] 路由设计 §3 有该 route
- [ ] `examples/project-docs/画面/README.md` 清单有该行（含状态）
- [ ] §5 insets 三条已回答
- [ ] **尚未**改 `Routes.kt` / NavHost / 写 Screen（留给 `implement-screen`）

## 注意事项

- 流程文档（路由 §4、画面 README「新增画面」）变更时，**同步改本技能 SOP**，保持一致。
- 统一源文件在 `shared/skills/create-screen-doc/`；改完须同步 `.codebuddy/skills/` 与 `.cursor/skills/`（见 `shared/skills/README.md`）。
- 不擅自发明 route 命名；冲突先查路由表。
- 演示/演练目录勿长期留在 `examples/project-docs/画面/` 正式索引；一次性演练后应撤回登记。
