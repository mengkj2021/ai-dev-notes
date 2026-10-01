# 导入画面

## 1. 画面概述

| 项 | 内容 |
|---|---|
| 画面名称 | 导入画面 |
| route | `import-images` |
| 职责 | 从相册或文件多选图片，压缩后写入私有目录与 Room；过程展示 Loading |

## 2. 路由定义

- `import-images`；无参数

## 3. 功能清单（总览）

- [x] TopAppBar + 返回（无底部导航）
- [x] 来源选择、进度与结果（见区域文件）
- [x] C10：导入前「按规则压缩」开关（DataStore）
- [x] P1：导入失败明细列表（弹窗；单张/全部重试；替代原 Snackbar）
- [x] F10：导入中系统返回全程拦截；成功后停留可续导 + Snackbar；进入时清理 pending 孤儿
- [x] F11：按原图名判重；无冲突先导入，冲突逐张询问；设置开关可关（默认开）

| 区域 | 文档 |
|---|---|
| 来源选择 | [来源选择.md](来源选择.md) |
| 进度与结果 | [进度与结果.md](进度与结果.md) |

## 4. 用户操作（总览）

1. 主画面待归档「导入图片」进入
2. 选相册或文件 → 等待导入 → 成功停留本画面可续导；自行返回主画面

## 5. UI 壳层

- 顶部：`TopAppBar`「导入图片」+ 返回
- 主体：见区域文件
- 无底部 NavigationBar

## 6. 系统返回动作

| 场景 | 行为 |
|---|---|
| Idle | 安全 pop（`popRouteIfOnTop`；顶栏 / 系统返回 / 手势）；**Bug10** 返回防连点（`closing`）<br>**Bug12**：`popRouteIfOnTop` 内 Lifecycle 守卫（STARTED return false）+ import 屏 `onBack` 接受 `onPopFailed` 回调；`closing` 在 pop 被跳过（transition / 守卫拒绝）时复位，避免冷启动 + 直 FAB 路径下返回永久失效 |
| 导入中 | `BackHandler` 始终启用并吞掉事件（含预测性返回）；顶栏返回 `enabled=false` |

## 7. 进入与退出

| 方向 | 说明 |
|---|---|
| 从哪里进入 | [主画面 · 待归档 · 菜单](../主画面/待归档一览/菜单.md) |
| 可前往 | 无 |
| 退出去向 | 回 `main`（仅用户主动返回；成功后不自动 pop） |

## 8. 数据依赖

- `ImportScreen` / `ImportViewModel`
- `AppFileManager`、`ImageManager`；`ImageRepository.insert`
- `UserPreferencesRepository`：`import_compress_enabled`、`import_duplicate_ask_enabled`、`default_tag_names`（合并进导入标签）
- **须在** `prepareForImport` 压缩 / 重编码**之前**从源 Uri 读 Exif `UserComment` 与 `DateTimeOriginal`（JPEG 重编码会剥 UserComment，拍摄日也可能丢失）
- 回读标签与默认标签并集去重后写 `tagsJson`，并进词表
- **Bug16**：落盘后回写 `UserComment`（ASCII 安全 JSON，`\uXXXX`）与 `DateTimeOriginal` 到私有文件，便于再导入；Room 仍为真源。读侧经 `getAttributeBytes` 解码并剥离 charset 前缀
- 导入写入 `originalName`（Uri `DISPLAY_NAME`）；判重依赖库内非 null 原图名 + 本批已导入
- `DateTimeOriginal` → `dateTakenMillis`（解析见 `ExifDateTaken`）；无 / 失败 → null
- 详见 [架构设计.md](../../架构设计.md)
