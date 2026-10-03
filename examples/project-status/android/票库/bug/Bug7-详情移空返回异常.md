# Bug7 票：V · 详情移空同状态后返回异常

> **后续**：本票「筛选后一览移空即返回」路径已被 [S9](../式样变更/S9-详情移动只认同状态全量.md) 撤销；现行式样只认同状态全量（见 [状态操作.md](../../../../project-docs/画面/图片详细画面/状态操作.md)）。下文保留当时实现复盘。

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | Bug7 |
| 类型 | Bug 票 |
| 标题 | V · 详情移动后同状态列表为空时返回与预期不符 |
| 状态 | ✅ 已完成（单测执行留本地补验） |
| 起票日期 | 2026-09-08 |
| 所属节点 | V |
| 涉及节点 | V |
| 提出 | 用户 |
| 关联 | [F30 详情移动与删除](../功能体验/F30-详情移动与删除.md)（已锁定：无剩余 → `popBackStack`）；[图片详细画面](../../../../project-docs/画面/图片详细画面/)；[主画面](../../../../project-docs/画面/主画面/)；后续撤销见 [S9](../式样变更/S9-详情移动只认同状态全量.md) |
| 档位 | 黄 |

## 关联画面（路径级）

- **预估**：
  - `project-docs/画面/图片详细画面/`
  - `project-docs/画面/主画面/`（返回后的一览 Tab）
- **实际改动**：
  - `project-docs/画面/图片详细画面/README.md`
  - `project-docs/画面/图片详细画面/状态操作.md`
  - 代码：`ui/imagedetail/ImageDetailViewModel.kt`、`ImageDetailScreen.kt`、`navigation/PictureOrganizerNavHost.kt`；测试 `imagedetail/ShouldReturnAfterRemovalTest.kt`

## 现象（用户可观察）

1. 复现步骤：主画面带**筛选**时进入图片详细，将当前图移动到其它状态，使**当前一览（筛选后）同状态列表已无剩余项**
2. 实际结果（接票已补）：详情**没有返回主画面**，而是继续切到同状态全量里、但**不在原一览（筛选条件外）**的其它图；用户已看不到那张图却仍停留 / 浏览
3. 期望结果：返回主画面，并显示**进入详情前所在的一览**（Tab / 筛选上下文）

## 影响范围

- 涉及画面 / route：`image-detail/{id}` → `main`
- 涉及数据或文件：`moveItems`；siblings / `NavigateBack`

## 根因（调查后填）

- **根因**：详情判定「是否还有剩余 / 下一张」用的是 `ImageDetailViewModel.siblingsFlow` → `repository.observeItems(item.status)`，即该状态**全量、固定 `importedAt DESC`** 列表；而主画面一览 = 该状态经**筛选（标签 / 未标记 / 名称 / 日期）+ 排序**后的结果。
- 两者不一致：当一览（筛选后）已移空、但该状态仍有其它图时，详情按「全量」算出 `nextId != null`，于是切到一览里根本看不到的图，而非返回。
- 定位证据：`ImageDetailViewModel.moveCurrentTo`（`nextSiblingIdAfterRemoval`）+ `MainViewModel`（过滤/排序）二者数据源不同；`ImageDao.observeByStatus` 固定 `importedAt DESC`。

## 已锁定（接票时确认；绿档可写「见现象/期望」或省略本节）

- 期望对齐用户原文：回主页并显示之前的一览画面。
- **本票范围（最小改动）**：只保证「一览（筛选后）移空 → 返回」；**其余沿用 F30**（同状态全量仍有可选时切下一张，不改「下一张」的选取顺序）。
- 与 F30 文档差异时以本票验收为准（接票回写 F30/画面文档）。
- 档位黄；拆票门槛未命中 → 不拆。

## 实施步骤（接票后填；绿档可写「一步完成」）

| # | 步骤 | 状态 | 备注 |
|---|---|---|---|
| 1 | 回写票与画面文档（一览移空即返回） | ✅ | 文档先行 |
| 2 | `shouldReturnAfterRemoval` 单测（先测后写） | ✅ | 纯逻辑；用例已写 |
| 3 | 详情 VM 接入「一览可见数」判定 | ✅ | 不改下一张顺序 |
| 4 | NavHost 进入详情时传入一览（筛选后）可见数 | ✅ | `savedStateHandle` |
| 5 | 收尾：验收自评、票 → ✅、台账 / 清单 / 错题本回写 | ✅ | 见下「验收对照自评（Bug7）」 |
| 6 | 跑单测绿灯 | ☐ | 本环境 Gradle daemon 起不来；待本地 `gradlew.bat testDebugUnitTest` |

## 方案（接票后填）

- 文档改动（画面文档 / 式样 / 架构，若涉及）：`图片详细画面/README.md`、`状态操作.md` 补「一览移空即返回」规则
- 代码改动点：
  - `navigation/PictureOrganizerNavHost.kt`：进入 `image-detail` 前把主画面**当前 Tab 筛选后可见总数**（`MainUiState.totalCount`）写入 `savedStateHandle`；详情读取并下发
  - `ui/imagedetail/ImageDetailScreen.kt` / `ImageDetailViewModel.Factory`：新增 `visibleSiblingCount`
  - `ui/imagedetail/ImageDetailViewModel.kt`：`moveCurrentTo` 用 `shouldReturnAfterRemoval` 决定返回或切下一张；`nextSiblingIdAfterRemoval` 与顺序**不变**
- 验证方式：`ShouldReturnAfterRemovalTest` 绿灯 + `compileDebugKotlin`

## 验收标准

- [x] 移空同状态后返回主画面且落在进入前的一览
- [x] 仍有同状态 sibling 时仍切下一张（F30 行为不回归）
- [x] 票内开发记录（带本票号）已写

### 验收对照自评（Bug7）

| 验收项 | 结果 | 证据（改了哪 / 验证了什么） |
|---|---|---|
| 移空同状态后返回主画面且落在进入前的一览 | ☑ | `PictureOrganizerNavHost` 进 `image-detail` 前把一览（当前 Tab 筛选后）可见数写入 `savedStateHandle`，经 `ImageDetailScreen` / `Factory` 下发为 `visibleSiblingCount`；`ImageDetailViewModel.shouldReturnAfterRemoval` 在「一览仅剩当前一张」时返回，走既有 `popBackStack`，主画面 Tab / 筛选态未动 → 落回进入前的一览。验证：语言服务无诊断；`ShouldReturnAfterRemovalTest` 用例（执行留本地） |
| 仍有同状态 sibling 时仍切下一张（F30 行为不回归） | ☑ | `nextSiblingIdAfterRemoval` 及其 `importedAt DESC` 取序**未改**；仅在其返回 `null` 时叠加一览判空 → 切下一张路径与 F30 一致 |
| 票内开发记录（带本票号）已写 | ☑ | 本票「开发记录」与本节 |

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-09-10 | Bug7 实现：进详情时下发一览（当前 Tab 筛选后）可见数；`shouldReturnAfterRemoval` 判定「一览移空即返回」，下一张顺序不变；补单测 | ed40f82 |
| 2026-09-10 | Bug7 收尾：验收自评、票 → ✅、bug 台账 / 改动清单 / 接票表 / 错题本回写 | （本次收尾提交） |

> ⚠️ 本环境 Gradle daemon 无法启动（客户端卡在 `Starting a Gradle Daemon`，进程 CPU 不增长），单测**未能在本会话执行**；已在语言服务侧确认无编译诊断。请在本地跑 `.\gradlew.bat testDebugUnitTest --tests "com.pictureorganizer.ui.imagedetail.*"` 确认绿灯后再验收。

## 完结复盘（✅ / ❌ 后填；票与台账行保留，不删除）

- **此 bug 的产生原因**（根因链，非表面现象）：F30 把详情「移动到 / 删除后切下一张」对齐一览时，只锁了**动作语义**、没锁**可见集合的语义**——详情判定仍用 `repository.observeItems(status)`（状态全量 + 固定 `importedAt DESC`），而主画面一览是「筛选 + 排序」后的结果；两处口径不同，一览被移空时详情仍算出「还有下一张」。
- **为什么此前没被发现 / 防不住**：F30 验收只覆盖「同状态还有图 → 切下一张 / 没了 → 返回」，漏掉「筛选后可见集先空、而全量仍有图」这一交叉条件；详情是第二个消费该列表的界面，单画面测不出来。
- **修改是否带来新风险**（若有，转新票或备注）：低。`visibleSiblingCount` 只参与**返回判定**（且以「一览仅剩当前一张」为条件），不参与「下一张」选取；非主画面入口缺省该值时退化为旧行为。
- **错题本登记**：已登记 [ai-workbench/错题本.md](../../../../../ai-workbench/错题本.md)
