# project-docs · 共享产品规格（图片整理）

两端**共用**本文档。实现落点分 Android / Windows；冲突以**当前接票例子**源码为准（现默认 [`examples/android/`](../android/)）。Windows 未实现见 [两端差异.md](两端差异.md)。

| 是 | 不是 |
|---|---|
| 功能、包结构、route、Room、画面规格 | Roadmap / 未决（→ [`project-status/<车道>/`](../project-status/README.md)） |
| 对照实现的产品描述 | AI 规范（→ [`shared/`](../../shared/) · [`ai-workbench/`](../../ai-workbench/)） |

Android 根包：`examples/android/app/src/main/java/com/pictureorganizer/`。用法：[如何使用-project-docs](../../ai-workbench/指南/如何使用-project-docs.md)。

## 文档地图

| 分区 | 文档 | 维护什么 | 代码落点 |
|---|---|---|---|
| 总览 | [项目概述.md](项目概述.md) | 产品定位 | — |
|  | [两端差异.md](两端差异.md) | Android / Windows 分叉索引 | — |
|  | [技术栈.md](技术栈.md) | Gradle / BOM / 版号 | Android：`libs.versions.toml` |
| 架构 · 数据 | [架构设计.md](架构设计.md) | 分层 + **§2 包树（Android 权威）** | 根包目录树 |
|  | [数据流.md](数据流.md) | 画面 → VM → Repo | `ui/` · `data/` · `util/` |
|  | [Room.md](Room.md) | 表、DAO | `data/local/` |
|  | [工具类.md](工具类.md) | 小工具 | `util/` · `ui/common/` |
| 导航 · 画面 | [路由设计.md](路由设计.md) | route 表 | `navigation/` |
|  | [画面/README.md](画面/README.md) | 画面清单 | `ui/<screen>/` |
| 主路径 | [核心流程.md](核心流程.md) | 导入→分类→导出 | 相关 `ui/` |
|  | [图片存储管理.md](图片存储管理.md) | 私有目录 | `filesDir/images`、`exports/` |

改清单只改 `画面/README.md`。

## 推荐阅读顺序

1. [项目概述.md](项目概述.md) · [两端差异.md](两端差异.md)  
2. [架构设计.md](架构设计.md) **§2**（Android）  
3. [路由设计.md](路由设计.md)  
4. [画面/README.md](画面/)  
5. 核心流程 · 存储 · 数据流 · Room · 技术栈  

进度属 `project-status/<车道>/`。分层见 [文档分层约定](../../ai-workbench/宪法/文档分层约定.md)。
