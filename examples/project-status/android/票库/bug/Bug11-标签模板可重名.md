# Bug11 票：V · 标签模板可重名

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | Bug11 |
| 类型 | Bug 票 |
| 标题 | V · 标签模板 Tab 添加/编辑允许重名 |
| 状态 | ✅ 已完成 |
| 起票日期 | 2026-09-15 |
| 所属节点 | V |
| 涉及节点 | V |
| 提出 | 用户（画面验证点验） |
| 关联 | [标签管理 · 标签模板](../../../../project-docs/画面/标签管理画面/标签模板.md)；近源 [F34](../功能体验/F34-标签添加失败原因.md)（标签列表已有重名内联） |
| 档位 | 绿 |

## 关联画面（路径级）

- **预估**：
  - `project-docs/画面/标签管理画面/标签模板.md`（保存规则补重名）
  - `project-docs/画面/标签管理画面/README.md`（若总览提到失败原因）
- **实际改动**：
  - `project-docs/画面/标签管理画面/标签模板.md`
  - `project-docs/画面/标签管理画面/README.md`
  - （原 `测试/画面/…` 细测册已删）
  - 代码：`TagNameSaveFailure.kt`、`TagManageViewModel`、`RoomTagRepository`、`TagTemplateDao`、三语文案

## 现象（用户可观察）

1. 复现步骤：标签管理 →「标签模板」Tab → 添加（或编辑改名）为与已有模板**相同名称** → 保存
2. 实际结果：可保存成功，出现两条同名模板（或编辑成重名）
3. 期望结果：与标签列表一致——Dialog **内联**拒绝（如「模板名已存在」或复用等价文案），不关闭对话框、不落库；编辑时改回**自身原名**应允许

## 影响范围

- 涉及画面 / route：`tag-manage`（标签模板 Tab）
- 涉及数据或文件：模板名唯一性（UI + 库层 `require`；表未加 UNIQUE 迁移）

## 根因（调查后填）

- `saveTemplate` 仅校验空名；标签侧 F34 已有 `classifyTagNameSaveFailure` + 库层重名 `require`，模板未对齐。
- 定位证据：代码对照；`TagNameSaveFailureTest`（含模板用例）`testDebugUnitTest` 绿灯

## 已锁定（接票时确认；绿档可写「见现象/期望」或省略本节）

- trim 后比较；编辑排除自身 `id`；自身原名可保存
- Dialog 内联「模板名已存在」三语；先测后写：是
- 库层补重名 `require`；不另加 UNIQUE 迁移；档位绿；`direct-main`

## 实施步骤（接票后填；绿档可写「一步完成」）

| # | 步骤 | 状态 | 备注 |
|---|---|---|---|
| 1 | `classifyTemplateNameSaveFailure` + 单测 → VM/Repo → 文案/文档/测项 | ☑ | 绿档一步 |

## 方案（接票后填）

- 见实施：纯函数 + VM 内联 + Repo `findByName` require + 三语文案 + 文档

## 验收标准

- [x] 添加/编辑与已有模板重名 → Dialog 内联错误，不保存成功
- [x] 编辑不改名（或仅改标签勾选）仍可保存
- [x] 画面文档 / 测项与实现一致；票内开发记录（带本票号）已写

### 验收对照自评（Bug11）

| 验收项 | 结果 | 证据（改了哪 / 验证了什么） |
|---|---|---|
| 添加/编辑与已有模板重名 → Dialog 内联错误，不保存成功 | ☑ | `saveTemplate` + `classifyTemplateNameSaveFailure`；文案 `tag_manage_template_name_duplicate`；Repo `require`；单测 duplicate 绿灯 |
| 编辑不改名（或仅改标签勾选）仍可保存 | ☑ | 查重 `template.id != dialog.editingId`；单测 `classifyTemplate_ok` |
| 画面文档 / 测项与实现一致；票内开发记录已写 | ☑ | `标签模板.md` / README / 测项勾选；本票开发记录 |

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-09-15 | 模板名空名/重名分类 + 单测；VM 内联；Repo findByName require；三语文案；文档/测项 | f09dbdb |

## 完结复盘（✅ / ❌ 后填；票与台账行保留，不删除）

- **此 bug 的产生原因**：标签模板保存未复用 F34 重名路径，只拦空名
- **为什么此前没被发现 / 防不住**：F34 只覆盖标签列表；模板 Dialog 未写重名规则；测项后补
- **修改是否带来新风险**：历史已存在的同名模板不会自动合并/清理（仅拦新建/改名）；若需清洗另起票
- **错题本登记**：不适用（教训已含于 F34「Dialog 内联分原因」；模板侧属漏对齐）
