# Bug17 票：教程关闭按钮刚进屏时无效

> 全面测试 TU-10 点验；同族 [Bug12](Bug12-导入画面返回键无效.md) / [Bug10](Bug10-主画面标题点击白屏.md) / [错题本 · Bug12 v3 全局扫描](../../../../../ai-workbench/错题本.md)。

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | Bug17 |
| 状态 | ✅ |
| 节点 | V |
| 档位 | 绿 |
| 提出 | 用户（画面测试用例 教程画面 · 关闭延迟生效） |
| 关联画面·预估 | [教程画面](../../../../project-docs/画面/教程画面/README.md) §5/§6；`TutorialScreen`；`PictureOrganizerNavHost` tutorial `onFinished`；[路由设计 §5](../../../../project-docs/路由设计.md) |
| 实际改动 | [路由设计 §5](../../../../project-docs/路由设计.md) 增注 `ResumedTextButton`/`ResumedButton`；教程 README 无需改 |

## 1. 诉求（现象）

1. 复现步骤：
   1. 设置 →「查看教程」进入教程（`fromSettings=true`，顶栏为「关闭」）
   2. **刚进入**后立刻点顶栏「关闭」
2. 实际结果：关闭**不生效**；等一会儿再点才回设置
3. 期望结果：进入后即可关闭 / 末页「返回」/ 系统返回回设置；**不**改写 `tutorial_completed`（TU-10）
4. 对照：与此前导入等「返回键刚进无效、稍后才好」同族（Bug12：`popRouteIfOnTop` 在 lifecycle 未 RESUMED 时跳过；顶栏应用 `BackNavIconButton` 等守卫）。教程顶栏是 `TextButton`，**未**纳入 Bug12 v3 helper，易漏修。

## 2. 必读路径

- 用例：[TU-09 / TU-10](../../阶段/阶段3-全面测试/画面测试用例/教程画面.md)
- `project-docs/画面/教程画面/README.md` §2 / §5 / §6；[路由设计.md](../../../../project-docs/路由设计.md) §5
- [Bug12](Bug12-导入画面返回键无效.md)（transition / RESUMED / `onPopFailed`）；错题本 Bug12 v3（同类用法先全局扫）
- 代码：`TutorialScreen.kt`（`TextButton(onClick = onFinished)`）；`PictureOrganizerNavHost` tutorial 分支（`fromSettings` → `popRouteIfOnTop("tutorial", …)`，含 lifecycle 非 RESUMED → `return false`）
- 首次路径（「跳过」/`navigate(MAIN)`）若同类延迟失效一并回归

## 已锁定（接票时确认）

- 刚进屏即可关；不改 `tutorial_completed`
- 对齐 Bug12：transition 中禁用出口钮；NavHost 再 `dropUnlessResumed` 兜底
- 抽 `rememberIsAtLeastResumed` + `ResumedTextButton` / `ResumedButton`；`BackNavIconButton` 改用同一 remember
- 先测后写：否（纯 UI / lifecycle，无 JVM 纯逻辑）
- 拆票：未达门槛；档位 **绿**；合入 `direct-main`

## 实施步骤

| # | 步骤 | 状态 | 备注 |
|---|---|---|---|
| 1 | 抽 `rememberIsAtLeastResumed`；`ResumedTextButton` / `ResumedButton`；`BackNavIconButton` 复用 | ✅ | `LifecycleResumed.kt` / `ResumedButtons.kt` |
| 2 | `TutorialScreen` 顶栏/末页出口改用 Resumed*；NavHost `onFinished` 包 `dropUnlessResumed` | ✅ | 全局扫描：仅教程 Text 出口漏网 |

## 方案

- 文档改动：`project-docs/路由设计.md` §5 增注 Text/主按钮出口 helper
- 代码：`LifecycleResumed.kt`、`ResumedButtons.kt`、`BackNavIconButton.kt`、`TutorialScreen.kt`、`PictureOrganizerNavHost.kt`
- 验证：真机 TU-10（队列统测）；compileDebugKotlin 已绿
- 先测后写：无纯逻辑，未加单测

## 3. 验收

- [x] 设置进教程：刚进屏立刻点「关闭」即可回设置（不必干等）
- [x] 末页「返回」、系统返回 / 手势同效；不改 `tutorial_completed`
- [x] 首次教程「跳过」/「开始使用」不引入同类卡死；TU-05～TU-07 不回退
- [x] TU-10 可勾选通过；helper 已抽，错题本「全局扫描」已补登记
- [x] Bug12 / 路由 §5 行为不回退

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-10-01 | 接票实现：`rememberIsAtLeastResumed` + `ResumedTextButton`/`ResumedButton`；教程出口改用；NavHost `dropUnlessResumed` 兜底；全局扫描确认仅教程 Text 出口漏网 | （待提交） |
| 2026-10-01 | 用户真机验收通过（队列统测；TU-10） | |

### 验收对照自评（Bug17）

| 验收项 | 结果 | 证据（改了哪 / 验证了什么） |
|---|---|---|
| 设置进教程：刚进屏立刻点「关闭」即可回设置（不必干等） | ☑ | `ResumedTextButton`；**用户真机通过** |
| 末页「返回」、系统返回 / 手势同效；不改 `tutorial_completed` | ☑ | 末页 `ResumedButton`；`fromSettings` 只 pop；真机通过 |
| 首次教程「跳过」/「开始使用」不引入同类卡死；TU-05～TU-07 不回退 | ☑ | Resumed* + `dropUnlessResumed`；真机通过 |
| TU-10 可勾选；helper / 全局扫描不留同族漏网 | ☑ | TU-10 已勾；错题本已登 Text 出口同族 |
| Bug12 / 路由 §5 行为不回退 | ☑ | `BackNavIconButton` 语义不变；真机抽查通过 |

## 4. 完结复盘（✅ / ❌ 后）

- 根因：教程顶栏为裸 `TextButton`，未纳入 Bug12 v3 `BackNavIconButton`；`onFinished` → `popRouteIfOnTop` 在未 RESUMED 时静默 false
- 为何此前未防住：Bug12 v3 全局扫描只覆盖「顶栏 Icon 返回」同族，漏了 Text「关闭/跳过」出口
- 新风险：低（禁用至 RESUMED；与其它子页一致）
- 错题本：已登记（Text 出口同族）
