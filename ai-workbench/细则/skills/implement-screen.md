# implement-screen · 细则

> 供 `shared/skills/implement-screen/SKILL.md` ▶ include。

# implement-screen（画面实现）

本技能以**已有画面文档**为唯一规格输入，完成 Kotlin / Compose 实现与路由注册。**仅 Android 例子**（`examples/android/`）。文档 SOP 见 `create-screen-doc`（**F17**）。

权威锚点：`examples/project-docs/架构设计.md` §2；`examples/project-docs/路由设计.md` §2 / §5；对应 `examples/project-docs/画面/<画面名>/README.md`（+ 区域文件）。

## 触发条件

命中任一即应用本技能：

- 画面文档已存在，需要写 / 补 `Screen` / `ViewModel` 并注册导航
- 用户点名 `implement-screen` / 「按画面文档实现」
- 接票后文档已齐、进入写码阶段（本技能只覆盖实现部分）

**不要**用于：尚无 `examples/project-docs/画面/<名>/`（先 `create-screen-doc`）；纯文案 / 非画面改动；与本画面无关的 util / data 大改（那些跟票走，不套本 SOP）。

## 输入约定（硬性）

| 规则 | 说明 |
|---|---|
| 唯一输入 | `examples/project-docs/画面/<画面名>/README.md` + 同目录区域文件；功能以文档勾选 / 描述为准 |
| 文档 vs 代码冲突 | **先停**：对照本画面 README、[`画面/README`](../../../examples/project-docs/画面/README.md) 清单、路由表、架构 §2；回票或请用户确认后再改。**禁止**为迁就代码擅自改画面文档 |
| 待定 | 文档 §9 或票上「待定」未清 → **不实现**该块 |
| 包结构 | 严格按 `examples/project-docs/架构设计.md` **§2**（`ui/<screen>/`、VM、Routes 集中定义） |

## SOP（五步，顺序固定）

### 1. 读文档定边界

打开画面 README（必）与区域文件（按 §3 / §5 索引）：

| 节 | 实现时取什么 |
|---|---|
| §1–2 | 画面名、route 字符串、路径/query 参数 |
| §3–4 | 功能清单与主操作路径（验收对照） |
| §5 | UI 壳层：Scaffold / TopBar / 分区 / **insets** |
| §6 | 系统返回 / `BackHandler` 场景 |
| §7 | 进入出口：从哪 `navigate`、回哪 `popBackStack` |
| §8 | 有无 VM；UiState / Event / Effect；Repository |
| §9 | 跳过待定 |

列「本步拟改文件」后再动刀；禁止无目的扫全库。

### 2. 落包与文件

按架构 §2.2：

| 内容 | 位置 |
|---|---|
| Composable | `examples/android/app/src/main/java/com/pictureorganizer/ui/<screen>/` |
| ViewModel + UiState/Event/Effect | 同包；无业务逻辑则可不建 VM（文档 §8 写「无」时遵守） |
| route 常量 | **仅** `navigation/Routes.kt`（全小写、多词连字符，见路由 §2） |
| 导航组装 | `navigation/PictureOrganizerNavHost.kt`（或当前 NavHost 文件） |

新建 `ui/<screen>/` 目录名与现有包风格一致（如 `osslicenses`、`imagedetail`）。若架构 §2.1 树尚未含该包，**同票**补一行目录树（小补丁，勿另起架构大改）。

### 3. 注册路由

1. `Routes.kt`：常量（及带参 `fun`）与文档 §2 **逐字一致**
2. NavHost：`composable(route) { … }`；参数用 `navArgument`；回调导航对齐文档 §7
3. 入口画面：设置项 / 按钮等 `navigate(Routes.…)` 与文档「从哪里进入」一致
4. **不要**只改代码不改文档：若发现路由表（`路由设计.md` §3）与 `Routes.kt` 不一致，先停对齐（通常文档已由 `create-screen-doc` 登记）

### 4. 实现 UI / VM（对照文档）

- UI 只负责展示与把事件交给 VM / 回调；业务规则进 VM 或已有 `util/` / `repository`
- 有可测纯逻辑 → 遵守「先测后写」（本技能不替代接票流程的测试义务）
- strings 进 `res/values`；勿硬编码用户可见文案（项目既有习惯）
- Preview / 假数据仅在文档或现有模式需要时加

### 5. 壳层与交互核对（强制清单）

实现后、声称完成前，对照画面文档 §5 / §6 与下表**逐项勾选**（不适用标 N/A 并写一句理由）：

| # | 项 | 出处 | 核对要点 |
|---|---|---|---|
| A | SystemBars / Scaffold | Bug1 | 整页 M3 `Scaffold`（或等价）消费 system bars；内容用 `innerPadding`，防裁切 |
| B | IME 与顶栏 | Bug2 | **含输入**的画面：`adjustResize` + Scaffold **不把 IME 算进整体顶走顶栏**；输入区 `imePadding` |
| C | 自定义 bottomBar | Bug3 | 非 `BottomAppBar` 的 `bottomBar` 槽位加 `navigationBarsPadding` |
| D | 系统返回 | 路由 §5、Bug4 / F10 | 默认 `popBackStack`；有未保存 / 「禁止退出」时用 `BackHandler`——**禁止**用 `enabled=false` 假装拦截（F10：禁用=把事件交回系统） |
| E | 可见性联动 | F4 / F5 | 文档若要求放大藏编辑区、溢出菜单等：状态与手势/菜单可见性与区域文档一致 |
| F | edge-to-edge | 模板 §5 | 与相邻画面策略一致，或文档已写明差异 |

任一项文档要求未落地 → 先补代码或回查文档，再收尾。

## 完成后自检

- [ ] 功能勾选与文档 §3 一致（本票范围）
- [ ] `Routes.kt` + NavHost + 入口导航已注册，route 字符串与文档一致
- [ ] 壳层清单 A–F 已勾或 N/A
- [ ] 未擅自改画面文档；冲突已升级
- [ ] [`examples/project-docs/画面/README.md`](../../../examples/project-docs/画面/README.md) 清单该行若约定「已实现」，由**本票收尾**更新（与接票流程一致）
- [ ] 架构 §2.1 已含新包（若新建）

## 注意事项

- 与 `create-screen-doc` 分工：文档四步 ↔ 本技能实现五步；新画面通常先文档后本技能。
- 流程文档（`examples/project-docs/画面/README`、路由 §4、dev-convention / 指南）变更时，**同步改本技能 SOP**。
- 统一源：细则在 `ai-workbench/细则/skills/implement-screen.md`；壳在 `shared/skills/implement-screen/`；改完须同步 `.codebuddy/skills/` 与 `.cursor/skills/`。
- 本技能不替代起票 / 验收自评 / 票复盘；收尾仍走 `prompts/票_接票开发.md`。
