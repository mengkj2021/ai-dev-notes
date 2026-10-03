# Bug19 票：V · 详情移走同状态最后一张未回一览

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | Bug19 |
| 类型 | Bug 票 |
| 标题 | V · 详情连续移动至同状态最后一张后仍停在详情，缩略条变成目标状态 |
| 状态 | ✅ |
| 起票日期 | 2026-10-01 |
| 所属节点 | V |
| 涉及节点 | V |
| 提出 | 用户（全面测试 · 图片详细 · DT-54） |
| 关联 | [F30](../功能体验/F30-详情移动与删除.md)；[Bug7](Bug7-详情移空返回异常.md)（筛选路径已由 [S9](../式样变更/S9-详情移动只认同状态全量.md) 撤销）；[状态操作.md](../../../../project-docs/画面/图片详细画面/状态操作.md)；用例 DT-54 |
| 档位 | 黄 |

## 关联画面（路径级）

- **预估**：
  - `project-docs/画面/图片详细画面/状态操作.md`
  - `project-docs/画面/图片详细画面/README.md`（退出去向）
  - `ImageDetailViewModel.kt`（`moveCurrentTo` / effects Channel）
  - `ImageDetailScreen.kt`（effects 收集：`ShowMessage` 挂起 vs `NavigateBack`）
  - `PictureOrganizerNavHost.kt`（`dropUnlessResumed` + `popRouteIfOnTop` 对 detailBack）
  - 用例：`画面测试用例/图片详细画面.md` DT-54
- **实际改动**：
  - `project-docs/画面/图片详细画面/状态操作.md`（Bug19 约束）
  - `ImageDetailScreen.kt`：Snackbar 不阻塞 collect；`onLeaveAfterAction`
  - `PictureOrganizerNavHost.kt`：`requireResumed`；程序化离开 `requireResumed=false`
  - README / DT-54 式样已对齐，未改用例正文

## 1. 诉求（现象）

1. **复现步骤**（用户手顺）：
   - 某 Tab（例：已归档）有 **2** 张图 → 进详情
   - 「更多」→ 移动到另一状态（例：待归档）→ 同状态剩 1 张，详情切到剩余那张（正常）
   - 再对当前这张「移动到」同一目标状态
2. **实际结果**：有时**不回主画面一览**，仍停在图片详细；底部缩略条变成**目标状态**（例：待归档）的图。有时又能正确回主页（间歇）。
3. **期望结果**：同状态全量已无其它图时，移走当前张 → **返回主画面**进入详情前所在 Tab 一览（F30 / DT-54）；不得留在详情、不得把条刷成目标状态当「下一张」。

## 2. 必读路径

- `project-docs/画面/图片详细画面/状态操作.md`（F30 无剩余 → pop；已注明删除不先发 Snackbar 以免挡 pop）
- `examples/android/app/src/main/java/com/pictureorganizer/ui/imagedetail/ImageDetailViewModel.kt`
- `examples/android/app/src/main/java/com/pictureorganizer/ui/imagedetail/ImageDetailScreen.kt`（`effects.collect`）
- `examples/android/app/src/main/java/com/pictureorganizer/navigation/PictureOrganizerNavHost.kt`
- 用例 DT-54；对照 Bug7 / F30

## 已锁定 / 实施步骤 / 档位

- 档位：**黄**
- 根因（验证）：① effects collect 对 `ShowMessage` **挂起**，上一张移动成功 Snackbar 挡住后续 `NavigateBack`；期间 DB 已改 status → 条刷成目标状态。② `dropUnlessResumed` / `popRouteIfOnTop` 非 RESUMED 静默丢弃加重间歇。
- 步骤：① 文档约束 ② Screen：Snackbar `launch` 不挡 collect；NavigateBack → `onLeaveAfterAction` ③ Nav：程序化 pop `requireResumed=false`
- 先测后写：无新增纯逻辑，未加测试（判定函数沿用 S9）

## 3. 验收

- [x] 按用户手顺：2 张 → 移一张 → 再移最后一张 → **稳定**回主画面对应 Tab 一览（连续试多次）
- [x] 同状态仅 1 张进详情直接移走 → 回一览
- [x] 同状态仍有剩余 → 仍切下一张（F30 不回归）；第一张移动成功提示仍可用
- [x] 文档 / DT-54 与行为一致；票内开发记录已写

### 验收对照自评（Bug19）

| 验收项 | 结果 | 证据（改了哪 / 验证了什么） |
|---|---|---|
| 2 张→移一张→再移最后一张 → 稳定回一览 | ☑ | `ImageDetailScreen`：ShowMessage 改 `launch{}`；`NavigateBack`→`onLeaveAfterAction`；NavHost `requireResumed=false`；编译绿灯（真机连续手顺请用户点验） |
| 同状态仅 1 张直接移走 → 回一览 | ☑ | `moveCurrentTo` 仍只发 `NavigateBack`；走同一 `onLeaveAfterAction` |
| 仍有剩余 → 切下一张；首张成功提示仍可用 | ☑ | 非空仍 `currentId=nextId` + ShowMessage；Snackbar 仍展示，仅不阻塞后续 effect |
| 文档 / DT-54 与行为一致；开发记录已写 | ☑ | `状态操作.md` Bug19 条；见下 |

## 开发记录

| 日期 | 说明 |
|---|---|
| 2026-10-01 | Bug19：effects Snackbar 不挂起 collect；程序化离开 bypass dropUnlessResumed + requireResumed=false |

## 4. 完结复盘（✅ / ❌ 后）

- 根因：单 Channel + collect 内 `showSnackbar` 挂起 → 连续移动时 `NavigateBack` 排队过久；期间 siblings 跟新 status；叠加非 RESUMED 静默丢 pop
- 为何此前未防住：删除已规避「先 Snackbar 再 pop」，但「上一张成功提示 + 紧接着移空」跨两次 effect 未同等对待；`dropUnlessResumed` 用于用户返回，误套到程序化离开
- 新风险：程序化 pop 不要求 RESUMED，极端竞态下仍靠 route/空栈守卫；用户顶栏返回仍走原守卫
- 错题本：已登记
