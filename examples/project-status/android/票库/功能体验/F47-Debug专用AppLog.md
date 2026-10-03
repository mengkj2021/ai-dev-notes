# F47 票：V · Debug 专用 AppLog 与关键路径埋点

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | F47 |
| 类型 | 功能 / 体验票 |
| 类型细分 | 体验增强（工程调试基建） |
| 标题 | V · Debug-only AppLog；Nav / 画面生命周期 / 筛选·Tab 关键路径埋点 |
| 状态 | ✅ 已完成 |
| 起票日期 | 2026-09-14 |
| 所属节点 | V |
| 涉及节点 | V |
| 关联 | 由 [Bug6](../bug/Bug6-筛选后回收站白屏.md) 真机点验失败引出；长期保留，供 AI / 开发者调试 |
| 档位 | 黄 |

## 关联画面（路径级）

- **预估**：
  - 全画面生命周期（不改式样）
  - `project-docs/工具类.md` · `project-docs/架构设计.md` §2
- **实际改动**：
  - `project-docs/工具类.md`
  - `project-docs/架构设计.md` §2
  - `project-docs/README.md`（工具类索引）

## 用户诉求 / 场景

- 要解决的问题或场景：Bug6 等路径问题缺统一 log；需长期、干净、仅 debug 输出的日志，便于 AI 与真机对照
- 预期效果：`AppLog` 仅 `BuildConfig.DEBUG` 输出；Nav 进出、各画面生命周期、主画面 Tab/筛选回传/列表空态等关键路径有可过滤 tag
- 是否已有式样依据：否（工程基建，无 UI 式样）

## 需求确认（接票前若为「待定」先在此澄清）

- 取舍 / 范围界定：release 零输出；不落文件、不上报；不刷屏业务细节；Bug6 修码本票不做（Bug6 保持 🚧）

## 已锁定（接票时确认；绿档可写「见诉求」或省略本节）

- Debug-only；统一 `PO/<tag>`；Nav + 全 Screen 生命周期 + Main 筛选/Tab/列表快照
- 先测后写：否（无新增可测纯逻辑）

## 实施步骤（接票后填；绿档可写「一步完成」）

| # | 步骤 | 状态 | 备注 |
|---|---|---|---|
| 1 | `util/log/AppLog` + 开 `buildConfig`；架构/工具类文档 | ✅ | |
| 2 | `LogScreenLifecycle`；NavHost 目的地监听；Main 筛选/Tab/列表 | ✅ | |
| 3 | 各 `*Screen` 挂生命周期一行 | ✅ | |
| 4 | 台账收尾；Bug6 记点验失败 | ✅ | |

## 方案（接票后填）

- 文档先行：`工具类.md` · 架构 §2 `util/log/`
- 代码改动点：见上
- 验证方式：debug 安装后 logcat 过滤 `PO/`；release 无输出（静态门控）

## 验收标准

- [x] `AppLog` 仅 debug 输出；统一 tag 前缀
- [x] Nav 切换与各画面生命周期可见
- [x] 主画面 Tab / 筛选回传 / 列表 count 可对照 Bug6
- [x] 架构与工具类文档对齐；票内开发记录已写

### 验收对照自评（F47）

| 验收项 | 结果 | 证据 |
|---|---|---|
| `AppLog` 仅 debug 输出；统一 tag 前缀 | ☑ | `util/log/AppLog.kt`：`BuildConfig.DEBUG` 早退；tag=`PO/<name>`；`buildFeatures.buildConfig=true` |
| Nav 切换与各画面生命周期可见 | ☑ | NavHost `OnDestinationChangedListener`；14 个 `*Screen` + `LogScreenLifecycle`；Activity/Application |
| 主画面 Tab / 筛选回传 / 列表 count 可对照 Bug6 | ☑ | `SelectTab`/`SetTagFilter`；Nav `SOURCE_TAB`/`filterApplied`；Main `pageItems`/`total` LaunchedEffect |
| 架构与工具类文档对齐；票内开发记录已写 | ☑ | 架构 §2、工具类.md、README 索引；本表开发记录 |

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-09-14 | AppLog + LogScreenLifecycle + Nav/Main 埋点；全 Screen；文档与 Bug6 点验失败回写 | e2f335d |

## 完结复盘（✅ / ❌ 后填；票与台账行保留，不删除）

- **落地效果**：统一 debug 日志，便于后续 Bug6 真机对照 `PO/Nav`、`PO/Main`、`PO/Lifecycle`
- **代价与遗留**：各 Screen 多一行；Lifecycle 事件在前后台切换时略密（可接受）
- **错题本登记**：不适用
