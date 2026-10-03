# 主画面

## 1. 画面概述

| 项 | 内容 |
|---|---|
| 画面名称 | 主画面 |
| route | `main` |
| 职责 | App 主界面：标题栏 + 底部三 Tab，按整理状态展示分类一览 |

## 2. 路由定义

- `main`；无参数。Tab 为本地状态，不单独注册 route

## 3. 功能清单（总览）

- [x] 顶部标题栏（TopAppBar，「图片整理」）+ **设置 icon** → `settings`
- [x] 底部 NavigationBar（待归档 / 已归档 / 回收站）
- [x] 三 Tab 一览与菜单（见下表区域文件）
- [x] 共用列表项（缩略图 + 进详情）
- [x] C3 / **F15**：三 Tab 标签筛选 + 页码分页（默认每页 30，设置可关分页 / 改每页数量）
- [x] 筛选入口改为 navigate → [`filter`](../筛选画面/README.md)；返回展示条件摘要（含拍摄/导入日区间）；**Bug6**：回传写入**进入筛选时所在 Tab**（非回传当时 selectedTab）
- [x] **F14**：三一览编辑态批量套重命名模板（见各 Tab「菜单」）

| 区域 | 文档 |
|---|---|
| 列表项（三 Tab 共用） | [列表项.md](列表项.md) |
| 待归档一览 · 菜单 | [待归档一览/菜单.md](待归档一览/菜单.md) |
| 待归档一览 · 一览 | [待归档一览/一览.md](待归档一览/一览.md) |
| 已归档一览 · 菜单 | [已归档一览/菜单.md](已归档一览/菜单.md) |
| 已归档一览 · 一览 | [已归档一览/一览.md](已归档一览/一览.md) |
| 回收站一览 · 菜单 | [回收站一览/菜单.md](回收站一览/菜单.md) |
| 回收站一览 · 一览 | [回收站一览/一览.md](回收站一览/一览.md) |

## 4. 用户操作（壳层）

1. 从启动画面自动进入
2. 点击标题栏设置进入设置画面；顶栏标题「图片整理」**无导航**（点按无动作；**Bug10**）
3. 点击底部 Tab 切换三个一览
4. 各 Tab 内操作见对应「菜单 / 一览」文档

## 5. UI 壳层

- 顶部：Material 3 `TopAppBar`，标题「图片整理」（无 `onClick`）；右侧设置 icon
- 主体：当前 Tab 的 `ImageListTab`（菜单 + 列表）
- 底部：`NavigationBar` 三个 `NavigationBarItem`
- **Bug6**：`Scaffold.contentWindowInsets` 排除 IME（对照详情 Bug2），避免筛选返回后内容区被吃成白屏
- **Bug10**：子画面离开须安全 pop（勿二次 pop 卸掉根 `main`）；对照 Bug6 / `popRouteIfOnTop`

## 6. 系统返回动作

| 场景 | 行为 |
|---|---|
| 应用根画面 | 返回键退出应用（系统默认） |

## 7. 进入与退出

| 方向 | 说明 |
|---|---|
| 从哪里进入 | [启动画面](../启动画面/README.md) / [教程画面](../教程画面/README.md) |
| 可前往 | [导入画面](../导入画面/README.md)；[图片详细画面](../图片详细画面/README.md)；[设置画面](../设置画面/README.md)；[筛选画面](../筛选画面/README.md)；[图片打包画面](../图片打包画面/README.md)（已归档菜单） |
| 退出去向 | 退出应用 |

## 8. 数据依赖

- UI：`MainScreen` + `ui/main/tab/`
- 逻辑：`MainViewModel`（`MainUiState` / `MainUiEvent` / `MainUiEffect`）；注入 `ImageRepository` + `TagRepository` + `RenameTemplateRepository` + `UserPreferencesRepository`
- 数据：`ImageRepository` ← `RoomImageRepository`；标签库 `TagRepository`；文件 `images/{pending,confirmed,no_modify}/`
- 列表：全量 Flow 按 status → ViewModel 内标签过滤 + **F15** `ListPaging` 切片（读 DataStore：`list_paging_enabled` / `list_page_size`）；关分页不切片、隐藏页码栏；各 Tab 独立保留筛选与页码；偏好变更回第 1 页
- 详见 [架构设计.md](../../架构设计.md)

