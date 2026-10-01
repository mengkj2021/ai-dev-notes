# 图片打包画面

## 1. 画面概述

| 项 | 内容 |
|---|---|
| 画面名称 | 图片打包画面 |
| route | `export-zip` |
| 职责 | 对**已归档**图片按筛选后**打一个 zip**，存私有目录并支持分享 |

## 2. 路由定义

- `export-zip`；无路径参（筛选为本地状态；可 navigate `filter` 回传）

## 3. 功能清单

- [x] 展示当前筛选摘要（含拍摄/导入日区间 chip）；可进 `filter` / 清除
- [x] **F41**：待打包预览小图；可叉掉排除（见 [预览区.md](预览区.md)）
- [x] **S7**：「开始打包」只生成 **一个** zip；内容为筛选 ∩ 预览剩余的已归档图
- [x] **F42 / S7**：打包前可改单个 zip 主文件名（见 [输出文件名.md](输出文件名.md)）
- [x] 无筛选时：全部已归档（预览剩余）进同一包
- [x] 进度；结果列表；系统分享（FileProvider）
- [x] 输出目录 `filesDir/exports/`
- [x] **F43**：入口「管理已打包」→ `export-manage`

## 4. 用户操作

1. 已归档菜单「打包导出」进入
2. （可选）筛选；预览小图可叉掉不想进包的图（F41）
3. （可选）改 zip 主文件名（F42 / S7；默认 `export_{日期}.zip`）
4. 开始打包 → 分享或返回；可进「管理已打包」查看历史 zip

## 5. UI 壳层

- TopAppBar「打包导出」+ 返回
- 筛选行 + 待打包预览网格（F41）+ 单文件名（F42 / S7）+ 开始按钮 +「管理已打包」（F43）+ 进度 / 结果列表
- 含输入：`adjustResize`；Scaffold 不含 IME 计入总高；内容 `imePadding`（Bug2）

## 6. 系统返回动作

| 场景 | 行为 |
|---|---|
| Idle | 安全 pop（`popRouteIfOnTop`；非裸 `popBackStack`） |
| 打包中 | `BackHandler` 拦截 |

## 7. 进入与退出

| 方向 | 说明 |
|---|---|
| 从哪里进入 | [已归档一览 · 菜单](../主画面/已归档一览/菜单.md) |
| 可前往 | [筛选画面](../筛选画面/README.md)；[已打包文件管理画面](../已打包文件管理画面/README.md) |
| 退出去向 | 回 `main` |

## 8. 数据依赖

- `ExportZipViewModel`；`ImageRepository.observeItems(Confirmed)`；`ZipExporter`；`TagFilterCriteria`；`buildSingleExportPack` / `excludingIds`（`model/ExportZipPack.kt`，F41 / S7）；`ExportZipNames`（F42 / S7）
- FileProvider：`@xml/file_paths` → `exports/`
