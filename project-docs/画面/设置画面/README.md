# 设置画面

## 1. 画面概述

| 项 | 内容 |
|---|---|
| 画面名称 | 设置画面 |
| route | `settings` |
| 职责 | App 设置入口：标签管理、重命名模板、默认标签、导入重名询问、**列表分页**（开关 + 每页数量）、已打包文件、查看教程、开源许可；底部显示 versionName |

## 2. 路由定义

- `settings`；无参数

## 3. 功能清单

- [x] TopAppBar + 返回
- [x] 「标签管理」→ `tag-manage`
- [x] 「重命名模板」→ `rename-template-manage`
- [x] 「默认标签」→ `default-tags`
- [x] 「导入重名询问」Switch（默认开；关则导入不询问）
- [x] 「分页显示」Switch（默认开）+「每页数量」预设 20/30/50/100 + **自定义**（1～500，默认 30；关分页时置灰）（**F38**）
- [x] 「已打包文件」→ `export-manage`（F43）
- [x] 「查看教程」→ `tutorial`（`fromSettings=true`，返回设置）
- [x] 「开源许可」→ `oss-licenses`
- [x] 底部显示版本号（`versionName`，只读）

## 4. 用户操作

1. 主画面齿轮进入
2. 点各列表项进入对应画面；切换「导入重名询问」/「分页显示」/「每页数量」即时写入 DataStore
3. 「每页数量」对话框：点预设立即生效并关闭；点「自定义」后输入整数，确定写入（非法则提示、不改上次有效值）
4. 返回主画面（底部版本号只读，无交互）

## 5. UI 壳层

- `TopAppBar`「设置」+ 返回
- `LazyColumn`：导航行 `ListItem` + 「导入重名询问」Switch 行 + 「分页显示」Switch 行 + 「每页数量」行（点开对话框：预设单选 + 自定义输入；关分页时不可点、置灰）+「已打包文件」等导航行
- 列表底部一行 caption 灰字居中：「版本 {versionName}」（来自 `PackageManager`，与 `build.gradle.kts` 一致；空则「版本 —」）
- SystemBars：`Scaffold` 消费 statusBars；列表内容注意 navigationBars

## 6. 系统返回动作

| 场景 | 行为 |
|---|---|
| 有上级 | 安全 pop（`popRouteIfOnTop`；非裸 `popBackStack`） |

## 7. 进入与退出

| 方向 | 说明 |
|---|---|
| 从哪里进入 | [主画面](../主画面/README.md) TopAppBar 设置 |
| 可前往 | 标签管理 / 重命名模板管理 / 默认标签 / 已打包文件管理 / 教程 / 开源许可 |
| 退出去向 | 回 `main` |

## 8. 数据依赖

- `SettingsViewModel` + `UserPreferencesRepository`：
  - `importDuplicateAskEnabled`（`import_duplicate_ask_enabled`，默认 true）
  - `listPagingEnabled`（`list_paging_enabled`，默认 true）
  - `listPageSize`（`list_page_size`，默认 30；合法 **1～500**，非法回落默认；预设 UI 为 20/30/50/100）
