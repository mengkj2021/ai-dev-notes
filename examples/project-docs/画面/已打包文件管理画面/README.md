# 已打包文件管理画面

## 1. 画面概述

| 项 | 内容 |
|---|---|
| 画面名称 | 已打包文件管理画面 |
| route | `export-manage` |
| 职责 | 列出 App 私有 `filesDir/exports/` 内已生成的 zip，支持再分享、删除与事后重命名 |

## 2. 路由定义

- `export-manage`；无参数

## 3. 功能清单

- [x] TopAppBar + 返回
- [x] 扫描并列出 `exports/` 下 `.zip`（按修改时间新→旧）
- [x] 空目录提示
- [x] 单项「分享」：FileProvider → 系统分享（同打包画面）
- [x] 删除 / 重命名（**F46**；见 [删改名.md](删改名.md)）

## 4. 用户操作

1. 设置「已打包文件」或打包画面「管理已打包」进入
2. 浏览列表；点「分享」调起系统分享
3. 点「删除」确认后移除；点「重命名」改主文件名（规则见删改名）
4. 返回上一画面

## 5. UI 壳层

- `TopAppBar`「已打包文件」+ 返回
- `LazyColumn`：每行文件名 + 大小/时间摘要 + 「分享 / 重命名 / 删除」
- 整页 M3 `Scaffold` 消费 SystemBars；无自定义 bottomBar

## 6. 系统返回动作

| 场景 | 行为 |
|---|---|
| 任意 | 安全 pop（`popRouteIfOnTop`；非裸 `popBackStack`） |

## 7. 进入与退出

| 方向 | 说明 |
|---|---|
| 从哪里进入 | [设置画面](../设置画面/README.md)；[图片打包画面](../图片打包画面/README.md) 结果区入口 |
| 可前往 | 无（系统分享为外部 Intent） |
| 退出去向 | 回设置或打包画面 |

## 8. 数据依赖

- `ExportManageViewModel` + `AppFileManager.exportsDir()`
- `ExportManageUiState`（列表 + 删除确认 + 改名草稿）/ `ExportManageUiEvent` / `ExportManageUiEffect`
- 改名规则：`ExportZipNames.planZipRename`（冲突拒绝，不自动 `_2`）
- FileProvider：`@xml/file_paths` → `exports/`（既有）
- **不含** Room；仅文件系统扫描
