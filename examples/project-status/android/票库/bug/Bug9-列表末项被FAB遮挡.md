# Bug9 票：V · 列表末项被 FAB 遮挡

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | Bug9 |
| 类型 | Bug 票 |
| 标题 | V · 可滚动列表最底一项被「+」FAB 盖住 |
| 状态 | ✅ 已完成 |
| 起票日期 | 2026-09-14 |
| 所属节点 | V |
| 涉及节点 | V |
| 提出 | 用户 |
| 关联 | [标签管理画面](../../../../project-docs/画面/标签管理画面/)；[重命名模板管理画面](../../../../project-docs/画面/重命名模板管理画面/)（同有 FAB，须一并过一遍） |
| 档位 | 黄（多画面审计 + 修） |

## 关联画面（路径级）

- **预估**：
  - `project-docs/画面/标签管理画面/`（标签列表 / 标签模板；已确认复现）
  - `project-docs/画面/重命名模板管理画面/`（FAB；待确认是否同病）
  - 其它带底部叠层（FAB / 固定底栏）且列表可滚的画面（接票时扫一遍）
- **实际改动**：
  - `project-docs/画面/标签管理画面/`（README §5、标签列表.md、标签模板.md）
  - `project-docs/画面/重命名模板管理画面/`（README §5）
  - 壳层约定：`project-docs/画面/README.md`、`ai-workbench/模板/画面文档/README.md` §5
  - 架构 / 工具类：`FabOverlayListContentPadding`

## 现象（用户可观察）

1. 复现步骤：标签管理 → 标签或模板列表超过一屏 → 滚到最底
2. 实际结果：最底下一条被右下角创建（+）FAB 盖住，难以点选 / 阅读
3. 期望结果：列表末项完整可见可点；FAB 不遮挡内容（如列表 `contentPadding` 底边留空，或等价 inset）

## 影响范围

- 涉及画面 / route：`tag-manage`（必改）；`rename-template-manage` 等有 FAB 的列表（审计后按需改）
- 涉及数据或文件：无

## 根因（调查后填）

- 初步判断：`Scaffold` FAB 叠在内容上，`LazyColumn` 未预留底部 padding
- 定位证据：全应用仅 `TagManageScreen`、`RenameTemplateManageScreen` 使用 `FloatingActionButton`；两处 `LazyColumn` 均无底边 `contentPadding`。`Scaffold.innerPadding` 不含 FAB。主画面 / 筛选底栏走 `bottomBar` 槽，内容已被 `innerPadding` 垫起，无同类叠层。

## 已锁定（接票时确认；绿档可写「见现象/期望」或省略本节）

- 有 FAB 的可滚列表统一底边净空（`FabOverlayListContentPadding` = 88.dp），不改 FAB 位置或交互
- 审计范围：FAB 两处必修；`bottomBar` 画面注明无问题

## 实施步骤（接票后填；绿档可写「一步完成」）

| # | 步骤 | 状态 | 备注 |
|---|---|---|---|
| 1 | 抽出 `FabOverlayListContentPadding`，标签管理两 Tab + 重命名模板管理列表套用 | ✅ | 无纯逻辑，未加测试 |
| 2 | 审计其它可滚列表叠层；回写画面文档 / 模板壳层 | ✅ | 仅上述两处 FAB |

## 方案（接票后填）

- 文档改动（画面文档 / 式样 / 架构，若涉及）：标签管理 / 重命名模板管理壳层；画面索引与新建模板补 FAB 净空约定；架构 §2 / 工具类登记常量
- 代码改动点：`ui/common/FabOverlayListPadding.kt`；`TagManageScreen` 两列表；`RenameTemplateManageScreen` 列表
- 验证方式：两画面列表滚到底，末项操作不被 FAB 挡住；其它画面无 FAB 叠层

## 验收标准

- [x] 标签管理两 Tab：滚到末项不被 FAB 遮挡，可正常操作
- [x] 全应用有 FAB / 同类叠层的可滚列表已过一遍；有问题的一并修或注明无问题
- [x] 票内开发记录（带本票号）已写

### 验收对照自评（Bug9）

| 验收项 | 结果 | 证据（改了哪 / 验证了什么） |
|---|---|---|
| 标签管理两 Tab：滚到末项不被 FAB 遮挡，可正常操作 | ☑ | `TagsList` / `TemplatesList` 的 `LazyColumn` 使用 `FabOverlayListContentPadding`（底 88.dp = FAB 56 + Scaffold 边距 16 + 余量 16） |
| 全应用有 FAB / 同类叠层的可滚列表已过一遍；有问题的一并修或注明无问题 | ☑ | 仅两处 `FloatingActionButton`；`rename-template-manage` 同病已修。主画面 / 筛选 `bottomBar` 由 `innerPadding` 垫起，无叠层。设置 / 默认标签 / 已打包 / 开源许可无 FAB |
| 票内开发记录（带本票号）已写 | ☑ | 本表开发记录 |

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-09-14 | 抽出 FAB 列表底边净空；标签管理两 Tab + 重命名模板管理套用；审计其余可滚列表无 FAB 叠层 | ffd06b0 |

## 完结复盘（✅ / ❌ 后填；票与台账行保留，不删除）

- **此 bug 的产生原因**（根因链，非表面现象）：M3 `Scaffold` 的 FAB 叠在 content 上，`innerPadding` 只含 top/bottomBar 与系统栏，不含 FAB；列表未另留 `contentPadding`
- **为什么此前没被发现 / 防不住**：短列表不露末项遮挡；画面模板只写了 insets / 自定义 bottomBar（Bug1/Bug3），未写 FAB 净空
- **修改是否带来新风险**（若有，转新票或备注）：底边多 88.dp 空白，短列表也会多一段可滚余量，可接受
- **错题本登记**：已登记 [ai-workbench/错题本.md](../../../../../ai-workbench/错题本.md)
