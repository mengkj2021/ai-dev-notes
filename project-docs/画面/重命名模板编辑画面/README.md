# 重命名模板编辑画面

## 1. 画面概述

| 项 | 内容 |
|---|---|
| 画面名称 | 重命名模板编辑 |
| route | `rename-template-edit` / `rename-template-edit/{templateId}` |
| 职责 | 添加或编辑单条重命名模板（名称、pattern、是否默认） |

## 2. 路由定义

| 项 | 内容 |
|---|---|
| route 字符串 | `rename-template-edit`（添加）；`rename-template-edit/{templateId}`（编辑） |
| 参数 | 可选路径参 `templateId`：缺省=添加；有值=编辑该模板 |

## 3. 功能清单

- [x] 表单：名称、命名规则（pattern）、设为默认
- [x] pattern 占位符说明：`{name}` `{date}` `{yyyy}` `{mm}` `{dd}` `{time}` `{tag}` `{tags}` `{n}`（与管理画面一致；**F34/F45**：`{date}` 与年月日同一来源=拍摄日优先，无则导入日）
- [x] **规则按钮插入（F44）**：命名规则旁按钮条，点击将对应 `{…}` 插入到输入框当前光标 / 替换选区；仍允许手改全文
- [x] 保存：成功返回管理列表；失败表单内联错误（对齐 F35）
- [x] 标题随模式：添加 / 编辑

## 4. 用户操作

1. 管理画面 FAB → 本画面（添加）→ 填写 → 保存 → 回管理列表
2. 管理画面点编辑 / 行点击 → 本画面（编辑）→ 修改 → 保存 → 回管理列表
3. TopAppBar 返回 / 系统返回 → 直接回管理列表（不提示未保存）
4. 编辑命名规则时：点规则按钮插入占位符，或直接手改 pattern

## 5. UI 壳层

- TopAppBar（返回 + 标题 + 保存）+ 可滚动表单；Scaffold 消费 SystemBars（Bug1）
- 含输入：`adjustResize` + 内容 `imePadding`（Bug2）；无自定义 bottomBar
- edge-to-edge 与 [重命名模板管理画面](../重命名模板管理画面/README.md) 一致

## 6. 系统返回

| 场景 | 行为 |
|---|---|
| 任意（含未保存） | 安全 pop（`popRouteIfOnTop`；非裸 `popBackStack`），不二次确认 |

## 7. 进入与退出

| 方向 | 说明 |
|---|---|
| 从哪里进入 | [重命名模板管理画面](../重命名模板管理画面/README.md)（FAB / 编辑） |
| 可前往 | 无 |
| 退出去向 | 回 `rename-template-manage` |

## 8. 数据依赖

- `RenameTemplateEditViewModel`：`RenameTemplateEditUiState` / `RenameTemplateEditUiEvent` / `RenameTemplateEditUiEffect`
- `RenameTemplateRepository`（加载、insert / update）
- 实现要点：F36 全屏表单；F44 `PatternTokenInsert` + `TextFieldValue`；F45 按钮条随 `RenamePatternInsertTokens`
