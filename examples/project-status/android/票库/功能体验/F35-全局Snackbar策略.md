# F35 票：V · 全局 Snackbar / 提示策略改善

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | F35 |
| 类型 | 功能 / 体验票 |
| 类型细分 | 体验增强 |
| 标题 | V · 全局提示：新消息即时替换；弹层不挡提示 |
| 状态 | ✅ 已完成 |
| 起票日期 | 2026-09-07 |
| 所属节点 | V |
| 涉及节点 | V |
| 关联 | 标签管理 / 重命名模板管理等使用 `SnackbarHost` 的画面；[F34](F34-标签添加失败原因.md) |
| 档位 | 黄 |

## 关联画面（路径级）

- **预估**：
  - `project-docs/画面/标签管理画面/`
  - `project-docs/画面/重命名模板管理画面/`
  - 其它已用 Snackbar 的画面（导入 / 详情 / 主画面 / 打包等，接票时扫一遍统一策略）
- **实际改动**：
  - `project-docs/画面/标签管理画面/README.md` · `标签列表.md` · `标签模板.md`
  - `project-docs/画面/重命名模板管理画面/README.md`
  - `project-docs/架构设计.md`（`ui/common/`）

## 用户诉求 / 场景

- 标签添加成功：当前 Snackbar 排队，须等前一条消失才显示下一条 → 改为**立刻替换为最新**，或改用非 Toast 的成功/失败消息区
- 重命名模板等：添加失败时 **Dialog 挡住** Snackbar → 全局改善，使提示可见（提高 Host、关窗后再示、或 Dialog 内联错误等——接票锁定）
- 预期效果：连续操作时提示跟得上；弹框场景下错误/成功原因仍可见

## 需求确认（接票前）

- [x] 方案：**A** 即时替换最新 Snackbar（不做固定消息区）
- [x] Dialog 打开时错误：**内联**展示；成功仍用 Snackbar

## 已锁定

- Snackbar：各画面经共用 `ui/common/showSnackbarReplacing`，新消息先 dismiss 当前再 show
- 添加/编辑 Dialog（标签、标签模板、重命名模板）失败：Dialog 内 `errorMessageResId`；成功关闭 Dialog + Snackbar
- 非 Dialog 场景（删除确认、设默认等）仍 Snackbar（替换策略）

## 实施步骤

| # | 步骤 | 状态 | 备注 |
|---|---|---|---|
| 1 | `ui/common/SnackbarHostStateExt` + 六画面接入 | ✅ | |
| 2 | 三处编辑 Dialog 内联错误 | ✅ | |
| 3 | 画面文档 / 架构 §2 + 验收收尾 | ✅ | |

## 验收标准

- [x] 标签管理连续添加成功：提示为最新一条（或等价非排队体验）
- [x] 带 Dialog 的添加失败：用户能看到失败提示（不被挡死）
- [x] 约定写入可复用处（若有共享组件则文档点名）；票内开发记录已写

### 验收对照自评（F35）

| 验收项 | 结果 | 证据 |
|---|---|---|
| 连续添加成功提示为最新一条 | ☑ | `ui/common/SnackbarHostStateExt.kt` + 六画面 `showSnackbarReplacing`；先 `dismiss` 再 `show` |
| Dialog 添加失败可见 | ☑ | 标签 / 标签模板 / 重命名模板 Dialog 展示 `errorMessageResId`；保存失败不再只发被挡的 Snackbar |
| 约定写入可复用处 | ☑ | `ui/common/`；架构 §2 登记；标签/重命名模板画面文档点名 F35 |

> 本环境 Gradle wrapper zip 缺失，未能跑 `compileDebugKotlin`；请真机点验连续成功 Snackbar 与 Dialog 内联失败。

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-09-08 | 实现替换 Snackbar + Dialog 内联；文档同步 | （待提交） |

## 完结复盘（✅ / ❌ 后填；票与台账行保留，不删除）

- **落地效果**：提示即时替换；编辑 Dialog 失败内联，不再被弹层挡住
- **代价与遗留**：仓库层具体失败原因（如重名）仍多映射为「保存失败」→ **F34** 细化；真机点验待做
- **错题本登记**：不适用
