# Bug12 票：V · 冷启动直 FAB 后导入画面返回键无效

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | Bug12 |
| 类型 | Bug 票 |
| 标题 | V · 冷启动直 FAB 进入导入画面后顶栏返回 / 系统返回键均无动作 |
| 状态 | ✅ 已完成 |
| 起票日期 | 2026-09-15 |
| 所属节点 | V |
| 涉及节点 | V |
| 提出 | 用户（真机点验发现） |
| 关联 | [导入画面](../../../../project-docs/画面/导入画面/README.md)；近源 [Bug10](Bug10-主画面标题点击白屏.md)（同 `popRouteIfOnTop` + `closing` 防连点链）；[Bug6](Bug6-筛选后回收站白屏.md)（同族二次 pop / 空栈） |
| 档位 | 黄 |

## 关联画面（路径级）

- **预估**：
  - `project-docs/画面/导入画面/README.md`（顶栏返回 / 系统返回 / 手势）
  - `project-docs/路由设计.md` §5（`popRouteIfOnTop` 守卫）
  - 可能波及：`android/.../ui/importimages/ImportScreen.kt`、`android/.../navigation/PictureOrganizerNavHost.kt`
- **实际改动**：
  - `examples/android/app/src/main/java/com/pictureorganizer/navigation/PictureOrganizerNavHost.kt`：`popRouteIfOnTop` 加 Lifecycle 守卫（STARTED return false）；import 屏调用去掉 `dropUnlessResumed`、改 `onBack` 为 `(onPopFailed: () -> Unit) -> Unit`，pop 失败时回调
  - `examples/android/app/src/main/java/com/pictureorganizer/ui/importimages/ImportScreen.kt`：
    - **v1**（commit `4f63a26`）：`onBack` 签名同步变更；`requestBack` 改用 `onBack { closing = false }`，失败回调复位 `closing`
    - **v2**（commit `08e9a0c`，用户报告新症状"系统返回正常 + 顶栏第一次无效、第二次生效"后补丁）：监听 lifecycle RESUMED 状态（`LifecycleEventObserver` + `mutableStateOf`）；`requestBack` transition 中（lifecycle 非 RESUMED）早 return；顶栏 IconButton `enabled = iconBackEnabled` 跟随 lifecycle（transition 中禁用无 ripple）；BackHandler 显式 `enabled = !state.isImporting`（F10 兜底）
    - **v3**（本提交）：ImportScreen 已 v2 自带 lifecycle + iconBackEnabled；本版本未改 ImportScreen（保持 v2 现状）
  - `examples/android/app/src/main/java/com/pictureorganizer/ui/common/BackNavIconButton.kt`：**新建**。项目级顶栏返回 IconButton helper：lifecycle RESUMED 监听（`LifecycleEventObserver` + `mutableStateOf` + `DisposableEffect`）+ IconButton + ArrowBack Icon；`enabled` 参数允许屏再加额外约束（如 `!isExporting`）；helper 内 `enabled && isResumed` 同时生效
  - **v3 顶栏改用 helper（10 个屏）**：
    - `examples/android/app/src/main/java/com/pictureorganizer/ui/exportmanage/ExportManageScreen.kt`
    - `examples/android/app/src/main/java/com/pictureorganizer/ui/exportzip/ExportZipScreen.kt`（`enabled = !state.isExporting` 通过 helper 参数传入）
    - `examples/android/app/src/main/java/com/pictureorganizer/ui/imagedetail/ImageDetailScreen.kt`
    - `examples/android/app/src/main/java/com/pictureorganizer/ui/settings/SettingsScreen.kt`
    - `examples/android/app/src/main/java/com/pictureorganizer/ui/osslicenses/OssLicensesScreen.kt`
    - `examples/android/app/src/main/java/com/pictureorganizer/ui/tagmanage/TagManageScreen.kt`
    - `examples/android/app/src/main/java/com/pictureorganizer/ui/renametemplate/RenameTemplateManageScreen.kt`
    - `examples/android/app/src/main/java/com/pictureorganizer/ui/renametemplate/RenameTemplateEditScreen.kt`
    - `examples/android/app/src/main/java/com/pictureorganizer/ui/defaulttags/DefaultTagsScreen.kt`
  - **v3 Filter 中等修改**：
    - `examples/android/app/src/main/java/com/pictureorganizer/ui/filter/FilterScreen.kt`：监听 lifecycle RESUMED；`requestBack` 顶部加 `|| !isResumed` 早 return；顶栏 IconButton 改用 helper
  - `project-docs/路由设计.md` §5：增注 Bug12 transition 跳过 + onPopFailed 反馈机制 + **v3 `BackNavIconButton` helper 统一约定**
  - `project-docs/画面/导入画面/README.md` §6：增注 Bug12 `closing` 复位

## 现象（用户可观察）

1. 复现步骤：
   1. 启动 app（冷启动 → splash → main，待归档一览）
   2. 在待归档一览点顶栏标题「图片整理」→ **无动作**（与 Bug10 一致，预期）
   3. 点击「导入图片」FAB → 进入导入画面
   4. 点击顶栏返回按钮 / 系统返回键 / 手势预测性返回 → **均无动作**（不返回）
2. 实际结果：导入画面返回无效（顶栏按钮、系统返回键、手势预测性返回均无反应）
3. 期望结果：非导入中，导入画面点返回 → 安全返回主画面（与 Bug10 设计一致）

## 影响范围

- 涉及画面 / route：`import-images`（导入画面）
- 涉及数据或文件：无（导航栈 / Compose 状态，非 Room）

## 根因（调查后填）

- **根因链**：冷启动 + 直 FAB 路径下，用户在 main → import-images 的导航 transition（~300ms）内极快点击返回键。ImportScreen 的 `requestBack()` 设 `closing = true` 后调用 `onBack = dropUnlessResumed { popRouteIfOnTop(...) }`；此时 `currentBackStackEntry.lifecycle.currentState` 是 STARTED 而非 RESUMED，`dropUnlessResumed` 直接跳过 block（**不返回任何 feedback**），`popRouteIfOnTop` 未被调用，`closing` 永不复位。后续任何 back 调用都被 `requestBack()` 的 `closing` 检查早 return → 顶栏按钮 / `BackHandler` / 手势预测性返回 三处共用 `requestBack`，全部失效。
- **为何用户感知"完全无动作"**：第一次 back 调用就卡住 closing，后续不管点哪里都不响应；与「transition 短暂窗口一闪而过」的用户感受匹配。
- **为何此前没被 Bug10 真机点验捕获**：Bug10 真机点验未覆盖"冷启动 + 直 FAB + transition 中首次 back"这种 race window；用户节奏稍慢 → dropUnlessResumed 不被跳过 → closing 走完即 import 屏 dispose，无可见 bug。
- 定位证据：精读 `ImportScreen.kt` line 97-105 + `PictureOrganizerNavHost.kt` line 320-326（B10 合入） + `androidx.lifecycle.compose.dropUnlessResumed` 源码语义。

## 已锁定（接票时确认；绿档可写「见现象/期望」或省略本节）

- 期望：非导入中，导入画面顶栏返回 / 系统返回 / 手势均能正常返回主画面
- 期望：导入中仍吞掉返回（保留 F10 / Bug10 行为）
- 期望：标题仍无导航（保留 Bug10 行为；Bug10 主路径 ✅）
- 先测后写：否（导航回归，与 Bug10 同类）；可抽 `popRouteIfOnTop` 纯函数判定补单测
- 拆票：未达门槛；档位 **黄**；合入 `direct-main`

## 实施步骤（接票后填；绿档可写「一步完成」）

| # | 步骤 | 状态 | 备注 |
|---|---|---|---|
| 1 | 精读代码定位根因：ImportScreen `closing` 卡死 + `dropUnlessResumed` 跳过无反馈 | ✅ | 根因链见「根因（调查后填）」节 |
| 2 | 修代码：`popRouteIfOnTop` 加 Lifecycle 守卫；import 屏 `onBack` 改 `onPopFailed` 回调形式；`requestBack` 失败复位 `closing`；**v2（用户报告新症状"系统返回正常 + 顶栏第一次无效、第二次生效"后补丁）**：监听 lifecycle RESUMED；`requestBack` transition 中早 return；顶栏 IconButton `enabled = iconBackEnabled` 跟随 lifecycle；**v3（用户报告"打包导出有类似问题"，扫描项目统一修）**：新建 `ui/common/BackNavIconButton.kt` helper（lifecycle RESUMED 监听 + IconButton + ArrowBack）；10 个顶栏返回屏改用 helper（ExportManage / ExportZip / ImageDetail / Settings / OssLicenses / TagManage / RenameTemplateManage / RenameTemplateEdit / DefaultTags / Filter）；Filter 加 lifecycle + `requestBack` 内 `!isResumed` 检查；NavHost 端 `dropUnlessResumed` 保留兜底 | ✅ | 与 Bug10 同处（ImportScreen / NavHost）；v1 = 4f63a26；v2 = 08e9a0c；v3 = 本提交 |
| 3 | 文档：路由设计 §5 / 导入画面 §6 增注；票内根因 + 复盘 | ✅ | 复用价值高（同族 bug 防御） |
| 4 | 用户真机验收（冷启动 + 直 FAB 主复现；Bug10 主路径回归；导入中吞返回；11 个子画面顶栏 / 系统 / 手势三处返回一次成功） | ✅ | 用户实测「没问题了」（2026-09-15） |

档位判定与拆票门槛见 [`拆票门槛与档位.md`](../../../../../ai-workbench/细则/接票/拆票门槛与档位.md)；母题–子票见 [`project-status/android/票库/README.md`](../../README.md)。

## 方案（接票后填）

- 文档改动（画面文档 / 式样 / 架构，若涉及）：
  - `project-docs/画面/导入画面/README.md`（§6 系统返回动作 增注）
  - `project-docs/路由设计.md` §5（`popRouteIfOnTop` 守卫）
- 代码改动点：
  - `ImportScreen.kt`（`closing` 复位点 / 顶栏 IconButton / `BackHandler`）
  - `PictureOrganizerNavHost.kt`（`popRouteIfOnTop` 守卫）
- 验证方式：
  - 冷启动 + 直 FAB 路径一次（主复现）
  - Bug10 已通过主路径再跑一次（回归）
  - 导入中返回仍吞（F10 行为）
  - 三 Tab × 导入/设置/筛选往返不复发（Bug10 范围）

## 验收标准

- [x] 导入画面顶栏返回 + 系统返回键 + 手势预测性返回 均能正常返回主画面（非导入中）
- [x] 导入中仍吞掉返回（保留 F10 / Bug10 行为）
- [x] Bug10 主路径回归通过（导入返回后点顶栏标题不白屏；标题本身无导航）
- [x] 关联画面回归正常（导入画面）
- [x] 票内开发记录（带本票号）已写

### 验收对照自评（Bug12）

| 验收项 | 结果 | 证据（改了哪 / 验证了什么） |
|---|---|---|
| 导入画面顶栏返回 + 系统返回键 + 手势预测性返回 均能正常返回主画面（非导入中） | ☑ | 代码（v1 `4f63a26`）：`popRouteIfOnTop` Lifecycle 守卫（STARTED return false）+ import 屏 `onBack` 改 `onPopFailed` 回调复位 `closing`。**用户实测主复现「没问题了」（2026-09-15）** |
| 导入中仍吞掉返回（保留 F10 / Bug10 行为） | ☑ | 代码：`BackHandler(enabled = !state.isImporting)` + `requestBack` 内 `if (state.isImporting) return` 早 return 双保险；`isImporting` 路径未改。**用户实测导入中返回仍吞** |
| Bug10 主路径回归通过（导入返回后点顶栏标题不白屏；标题本身无导航） | ☑ | 代码：`popRouteIfOnTop` 空栈守卫未删；标题本身保持无 `onClick`；Bug10 commit `4bc9aa4` 已通过主路径回归，本票不破坏既有契约。**用户实测主路径** |
| 关联画面回归正常（导入画面） | ☑ | 代码改动仅 import 屏调用形式 + `popRouteIfOnTop` 加一条守卫；其他子画面调用形式不变；**v3（commit `88b9eac`）统一修 11 个子画面（含 ImportImages 已 v2 自带）**，所有顶栏 / 系统 / 手势三处返回一次成功 |

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-09-15 | 起票：冷启动 + 直 FAB 后导入画面返回无效；根因初判 `closing` 卡死与 `popRouteIfOnTop` 守卫链；同 Bug10 族 | （44a1393 起票） |
| 2026-09-15 | 接票 + 实施 v1：精读定位根因（`dropUnlessResumed` 跳过无反馈 + `closing` 卡死）；`popRouteIfOnTop` 加 Lifecycle 守卫；import 屏 `onBack` 改 `onPopFailed` 回调；`requestBack` 失败复位 `closing`；文档（路由 §5 / 导入画面 §6） | 4f63a26 |
| 2026-09-15 | 实施 v2（用户报告新症状后补丁）：症状：进 import 屏后**系统返回正常 + 顶栏第一次无效、第二次生效**（用户在 RESUMED 后按系统返回；在 transition 中按顶栏 IconButton）。根因：transition 中（lifecycle STARTED）点 IconButton，`popRouteIfOnTop` Lifecycle 守卫 return false → `onFailed` 复位 `closing` → 用户感到"第一次无效"，再点一次（RESUMED 后）成功。修复：监听 lifecycle RESUMED 状态；`requestBack` transition 中早 return；顶栏 IconButton `enabled = iconBackEnabled`（`!isImporting && isResumed`）；BackHandler 显式 `enabled = !state.isImporting`（F10 兜底） | 08e9a0c |
| 2026-09-15 | 实施 v3（用户报告"打包导出有类似问题"，扫描项目统一修）：扫描 11 个有顶栏返回的子画面 → 9 个 ExportManage / ExportZip / ImageDetail / Settings / OssLicenses / TagManage / RenameTemplateManage / RenameTemplateEdit / DefaultTags 都用 `dropUnlessResumed { popRouteIfOnTop }`，transition 中 IconButton 可点 → dropUnlessResumed 吞 → 用户感到「第一次无效」（同 Bug12 v2 根因族）。修复：**新建 `ui/common/BackNavIconButton.kt` helper**（lifecycle RESUMED 监听 + IconButton + ArrowBack）；10 个屏顶栏改用 helper（ExportManage / ExportZip / ImageDetail / Settings / OssLicenses / TagManage / RenameTemplateManage / RenameTemplateEdit / DefaultTags / Filter）；Filter 额外加 lifecycle 监听 + `requestBack` 内 `!isResumed` 早 return（保留 closing/requestBack/BackHandler 已有逻辑）；NavHost 端 `dropUnlessResumed` 保留兜底（屏端禁用已挡掉 transition click）。 | （本提交） |
| 2026-09-15 | 验收通过（用户实测）：**用户真机 11 个子画面（ImportImages / ExportZip / ExportManage / ImageDetail / Settings / OssLicenses / TagManage / RenameTemplateManage / RenameTemplateEdit / DefaultTags / Filter）顶栏 / 系统 / 手势三处返回一次成功**，主复现路径（冷启动 + 直 FAB / App 启动 → 标题栏 → FAB）均通过；Bug10 主路径回归通过；导入中仍吞返回（F10 / Bug10 行为保留） | 88b9eac（v3 commit） |

## 完结复盘（✅ / ❌ 后填；票与台账行保留，不删除）

- **此 bug 的产生原因**（根因链，非表面现象）：**单点 race + 同族 race 全集遗漏**。v1 根因：冷启动 + 直 FAB 路径下 transition（~300ms）期间点返回 → `dropUnlessResumed` 直接跳过 block（无 feedback）→ `closing` 永不复位 → 三处返回全失效。v2 根因：v1 加 `popRouteIfOnTop` Lifecycle 守卫后正确拦下 race，但**用户体验差**——transition 中 IconButton 看似可点、按下没反应，用户感到「第一次无效」。v3 根因（教训）：v1/v2 只修 `ImportScreen` 一个点，**项目里 11 个有顶栏返回的子画面有 10 个共用 `dropUnlessResumed { popRouteIfOnTop }` 同根 race**——用户报导出画面才有反应，纯靠**用户报点驱动单点修复**，缺乏全局扫描
- **为什么此前没被发现 / 防不住**：Bug10 真机点验未覆盖"冷启动 + 直 FAB + transition 中首次 back" 这种 race window；v1 收口 ImportScreen 后用户报「打包导出有类似问题」，才暴露**同族 race 在所有子画面**。接票流程缺"扫描项目同类用法"环节：v1/v2 文档（路由 §5 / 导入画面 §6）只针对 ImportScreen，路由 §5 v3 才补项目级 helper 统一约定
- **修改是否带来新风险**：helper `BackNavIconButton` 把 lifecycle 监听收敛到一处，未来 NavHost 端 `dropUnlessResumed` 策略若改，屏端 helper 已挡掉 transition click，兜底链条仍成立；ExportZip 额外 `enabled = !isExporting` 通过 helper 参数传入，**对导出中行为契约无变化**；Filter 改 `requestBack` 早 return 与现有 closing/requestBack/BackHandler 行为相容。**无新风险**
- **错题本登记**：已登记 [ai-workbench/错题本.md](../../../../../ai-workbench/错题本.md)（同族 race 全局扫描 + 项目级 helper）