# 画面文档模板

> **这是什么**：新建 Compose **画面规格文档**时用的目录模板（一 route 一目录）。  
> **不是**：票模板（在 [`票/`](../票/)）、也不是代码脚手架。

## 怎么用

1. 复制本目录 `ai-workbench/模板/画面文档/` → `examples/project-docs/画面/<画面名>/`
2. 填写本目录中的 `README.md`（十节结构）
3. 按需复制 [画面区域文档模板.md](画面区域文档模板.md) 为区域文件（如 `菜单.md`、`一览.md`）
4. 登记 [`examples/project-docs/路由设计.md`](../../../examples/project-docs/路由设计.md) 第 3 节；更新 [`examples/project-docs/画面/README.md`](../../../examples/project-docs/画面/README.md) 清单
5. 再按 `create-screen-doc` / `implement-screen` 技能继续

约定：一层目录 = 一个 route；总览始终为 `README.md`。服从 [`文档分层约定.md`](../../宪法/文档分层约定.md)。

---

## 1. 画面概述

| 项 | 内容 |
|---|---|
| 画面名称 | （如：导入画面） |
| route | （如：`import-images`，见路由设计） |
| 职责 | （一句话） |

## 2. 路由定义

| 项 | 内容 |
|---|---|
| route 字符串 | |
| 参数 | 无 / 路径参 / query |

## 3. 功能清单

- [ ] …

## 4. 用户操作

主路径步骤。

## 5. UI 壳层

- [ ] 整页使用 M3 `Scaffold`（或等价），内容消费 **SystemBars insets**（防系统栏裁切，Bug1）
- [ ] 若自定义 `bottomBar`（非 `BottomAppBar`）：已规划 `navigationBarsPadding`（Bug3）
- [ ] 若有 FAB（或其它叠在列表上的控件）：可滚列表 `contentPadding` 底边留空（防末项被挡，Bug9）
- [ ] 其它壳层说明：…

## 6. 系统返回

对照路由设计 §5；有未保存态写清拦截。

## 7. 进入与退出

从哪进、去哪。

## 8. 数据依赖

有 ViewModel 的画面请写明：`*UiState` / `*UiEvent` / `*UiEffect`、Repository。架构见 [架构设计.md](../../../examples/project-docs/架构设计.md)。

## 9. 待定事项

- [ ] …

## 10. 开发记录

新画面的过程写入**相关票**复盘即可，本 README 只留跳转，不堆流水账。
