# 标签管理画面

## 1. 画面概述

| 项 | 内容 |
|---|---|
| 画面名称 | 标签管理画面 |
| route | `tag-manage` |
| 职责 | 维护标签库（`tags`）与标签模板（`tag_templates`） |

## 2. 路由定义

- `tag-manage`；无参数

## 3. 功能清单（总览）

- [x] 无底部 NavigationBar；TopAppBar + 返回
- [x] 双 Tab：标签列表 / 标签模板

| 区域 | 文档 |
|---|---|
| 标签列表 | [标签列表.md](标签列表.md) |
| 标签模板 | [标签模板.md](标签模板.md) |

## 4. 用户操作（总览）

1. 从设置进入
2. 在两 Tab 内增删改
3. 返回设置

## 5. UI 壳层

- 顶部：`TopAppBar`「标签管理」
- 主体：TabRow + 对应列表
- 无底栏；FAB 添加。**Bug9**：两 Tab 可滚列表 `contentPadding` 底边预留 FAB 净空（`FabOverlayListContentPadding`），避免末项被挡
- **F35**：Snackbar 用 `ui/common/showSnackbarReplacing`；添加/编辑 Dialog 失败内联错误
- **F34**：标签添加/编辑失败分原因（空名、重名、状态保留字）
- **Bug11**：标签模板添加/编辑重名 Dialog 内联拒绝（对齐 F34 体验；不做状态保留字）

## 6. 系统返回动作

| 场景 | 行为 |
|---|---|
| 有上级 | 安全 pop（`popRouteIfOnTop`；非裸 `popBackStack`）→ settings |

## 7. 进入与退出

| 方向 | 说明 |
|---|---|
| 从哪里进入 | [设置画面](../设置画面/README.md) |
| 可前往 | 无 |
| 退出去向 | 回 `settings` |

## 8. 数据依赖

- `TagManageViewModel` + `TagRepository` + `ImageRepository`（C4 / **F8**）
- **F8**：删库标签时判断图片引用；有引用二次确认后批量去掉 `tagsJson` 与 Exif；模板内同名文本不联动
- **F49**：删库标签成功路径同步从默认标签集合去掉该名
- **F48**：改库标签名时判断图片引用；有引用二次确认（标题/按钮为「重命名」）后批量改名 `tagsJson` 与 Exif，并同步默认标签集合；模板内同名文本不联动
