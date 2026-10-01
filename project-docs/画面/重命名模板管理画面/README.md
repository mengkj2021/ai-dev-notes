# 重命名模板管理画面

## 1. 画面概述

| 项 | 内容 |
|---|---|
| 画面名称 | 重命名模板管理 |
| route | `rename-template-manage` |
| 职责 | CRUD 重命名模板；设默认；供详情套用 |

## 2. 路由定义

- `rename-template-manage`；无参数

## 3. 功能清单

- [x] 列表：名称、pattern、默认徽标
- [x] 新增 / 编辑：跳转 [重命名模板编辑画面](../重命名模板编辑画面/README.md)（**F36**：不再用 Dialog）
- [x] 删除确认；设为默认（互斥）
- [x] pattern 占位符说明：`{name}` `{date}` `{yyyy}` `{mm}` `{dd}` `{time}` `{tag}` `{tags}` `{n}`（**F34/F45**：`{date}` 与年月日=拍摄日优先，无则导入日；`{time}` 拍摄时分秒优先否则导入时刻；规则按钮插入在编辑画面，见 F44）
- [x] 从编辑画面保存返回后 Snackbar 提示成功（`showSnackbarReplacing`）

## 4. 用户操作

1. 设置 → 本画面
2. FAB → 编辑画面（添加）；行/编辑 → 编辑画面；删除 / 设默认在本画面
3. 返回设置

## 5. UI 壳层

- TopAppBar + FAB 添加
- 列表；删除确认 Dialog（添加/编辑已迁出）
- **Bug9**：列表 `contentPadding` 底边预留 FAB 净空（`FabOverlayListContentPadding`），避免末项被挡

## 6. 系统返回动作

| 场景 | 行为 |
|---|---|
| 有上级 | 安全 pop（`popRouteIfOnTop`；非裸 `popBackStack`） |

## 7. 进入与退出

| 方向 | 说明 |
|---|---|
| 从哪里进入 | [设置画面](../设置画面/README.md) |
| 可前往 | [重命名模板编辑画面](../重命名模板编辑画面/README.md) |
| 退出去向 | 回 `settings` |

## 8. 数据依赖

- `RenameTemplateRepository` ← Room `rename_templates`（DB v3）
