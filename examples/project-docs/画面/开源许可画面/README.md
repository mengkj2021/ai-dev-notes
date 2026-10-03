# 开源许可画面

## 1. 画面概述

| 项 | 内容 |
|---|---|
| 画面名称 | 开源许可画面 |
| route | `oss-licenses` |
| 职责 | 展示 App runtime 第三方库清单及许可证文本（合规 / 开源观感） |

## 2. 路由定义

- `oss-licenses`；无参数

## 3. 功能清单

- [x] TopAppBar「开源许可」+ 返回
- [x] 第三方库列表（构建期 aboutlibraries 扫描生成）
- [x] 点击库项查看许可证详情（库方 `LibrariesContainer` 默认交互）

## 4. 用户操作

1. 设置 →「开源许可」进入
2. 浏览列表；点库项查看许可
3. 返回设置

## 5. UI 壳层

- `TopAppBar`「开源许可」+ 返回
- 主体：`produceLibraries(R.raw.aboutlibraries)` + `LibrariesContainer`（aboutlibraries-compose-m3）
- SystemBars：`Scaffold` 消费 statusBars / navigationBars

## 6. 系统返回动作

| 场景 | 行为 |
|---|---|
| 有上级 | 安全 pop（`popRouteIfOnTop`；非裸 `popBackStack`） |

## 7. 进入与退出

| 方向 | 说明 |
|---|---|
| 从哪里进入 | [设置画面](../设置画面/README.md)「开源许可」行 |
| 可前往 | 无 |
| 退出去向 | 回 `settings` |

## 8. 数据依赖

- 无 ViewModel；元数据由 Gradle 插件 `com.mikepenz.aboutlibraries.plugin.android` 构建期写入 `R.raw.aboutlibraries`
