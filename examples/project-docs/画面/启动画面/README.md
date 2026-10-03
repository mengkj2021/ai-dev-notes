# 启动画面

## 1. 画面概述

| 项 | 内容 |
|---|---|
| 画面名称 | 启动画面 |
| route | `splash` |
| 职责 | App 启动首屏；短暂展示后按教程完成状态跳转 |

## 2. 路由定义

- `splash`；无参数

## 3. 功能清单

- [x] 显示应用名称（静态文字）
- [x] 读取 DataStore：未完成教程 → `tutorial`；已完成 → `main`
- [x] 跳转后 splash 不在 back stack

## 4. 用户操作

1. 打开 App 进入本画面
2. 短暂展示后自动跳转（无需用户操作）

## 5. UI 壳层

- 主体：居中应用名称（`headlineMedium`）
- 背景：Material 3 默认；edge-to-edge
- SystemBars：随系统默认 insets（无额外手写 padding 时以 Scaffold / 系统默认为准）

## 6. 系统返回动作

| 场景 | 行为 |
|---|---|
| splash 展示期间 | 返回键退出应用（系统默认） |
| 跳转后 | splash 不在 back stack |

## 7. 进入与退出

| 方向 | 说明 |
|---|---|
| 从哪里进入 | App 启动（MainActivity） |
| 可前往 | [教程画面](../教程画面/README.md) 或 [主画面](../主画面/README.md) |
| 退出去向 | 跳转后不在 back stack |

## 8. 数据依赖

- `UserPreferencesRepository.isTutorialCompleted`

## 9. 待定事项

- [ ] 启动过渡动画（如需要）
- [ ] splash 展示时长是否可配置

