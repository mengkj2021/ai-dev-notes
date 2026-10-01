# 教程画面

## 1. 画面概述

| 项 | 内容 |
|---|---|
| 画面名称 | 教程画面 |
| route | `tutorial?fromSettings={fromSettings}` |
| 职责 | 首次启动引导（3 页）；设置内可再次查看 |

## 2. 路由定义

- 路由字符串：`tutorial?fromSettings={fromSettings}`（见 `Routes.TUTORIAL`）
- 可选参数：`fromSettings`（默认 `false`）
- 参数说明：
  - `false`（首次）：完成或跳过 → 写入 DataStore `tutorial_completed` → 导航 `main`（替换栈，不回 splash）
  - `true`（设置重看）：完成或跳过 → **不**改写完成标记 → 安全 pop（`popRouteIfOnTop`）回设置

## 3. 功能清单（总览）

- [x] 3 页横向滑动（`HorizontalPager`）+ 页指示
- [x] 「下一步」/ 末页主按钮（首次「开始使用」；设置重看「返回」）
- [x] 顶栏退出（首次「跳过」；设置重看「关闭」）；任意页可退出
- [x] 首次完成/跳过写入 `tutorial_completed`（`fromSettings=false`）
- [x] 设置内再次查看（`fromSettings=true`）；文案与行为见 §2 / §5
- [x] 首次流程不留在 back stack（与 splash 一致）
- [x] 页内正文可纵向滚动（文案加长后；**F12**）

## 4. 用户操作（总览）

1. 首次：`splash` → 本画面 → 浏览 / 跳过 → `main`
2. 设置：「查看教程」→ 本画面 → 返回设置

## 5. UI 壳层

- 布局：整页包 M3 `Scaffold`，内容消费系统栏 inset（修复 Bug1）
- 顶部右侧：`fromSettings=false` →「跳过」；`true` →「关闭」（`tutorial_skip` / `tutorial_close`）
- 主体：当前页标题 + 说明文案（正文左对齐；页内可纵向滚动）；**无配图**（F12 裁定）
- 正文呈现（**S6**）：每页仍一条 `tutorial_pageN_body`。可选引言段后接 `N. 短标题` 条目（短标题为重点，说明另起行）。`parseTutorialBody` 解析；引言 `bodyLarge`，短标题 `titleSmall`，说明 `bodyMedium`。不使用 Markdown / `AnnotatedString`
- 底部：页点指示 +「下一步」；末页主按钮：`false` →「开始使用」；`true` →「返回」（`tutorial_start` / `tutorial_return`）

### 三页文案（与 `values*/strings.xml` 一致）

简中摘要；日/英见 `values-ja` / `values-en`。**F12** 扩写 + **S6** 分段短标题；三语条目数：页1 引言+3 / 页2 引言+4 / 页3 无引言 5 条。

| 页 | 标题 | 说明要点 |
|---|---|---|
| 1 | 主体流程 | 引言「核心三步」→ 导入 → 打标签/重命名 → 按标签 ZIP 导出 |
| 2 | 使用说明 | 本机不联网；只复制不改原图；操作只影响副本；状态名≠标签 |
| 3 | 更多说明 | 三状态；筛选共用；标签来源；已打包管理；设置与重看教程 |

## 6. 系统返回动作

| 场景 | 行为 |
|---|---|
| 首次教程（根引导） | 返回键退出应用（不回 splash） |
| 从设置进入 | 返回键 / 完成 → 安全 pop（`popRouteIfOnTop`；非裸 `popBackStack`）回设置 |

## 7. 进入与退出

| 方向 | 说明 |
|---|---|
| 从哪里进入 | [启动画面](../启动画面/README.md)（未完成教程）；[设置画面](../设置画面/README.md)「查看教程」 |
| 可前往 | [主画面](../主画面/README.md)（首次）；或返回设置 |
| 退出去向 | 见 §2 |

## 8. 数据依赖

- `UserPreferencesRepository`（键 `tutorial_completed`；仅首次流程写入）
- UI：`TutorialScreen`；完成回调由 `PictureOrganizerNavHost` 按 `fromSettings` 分支处理
