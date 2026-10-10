# AI 如何使用 project-docs

> `examples/project-docs/` = 图片整理 **共享规格**。实现落点分端；冲突以**当前接票例子**源码为准。  
> 入口：[README.md](../../examples/project-docs/README.md) · 差异：[两端差异.md](../../examples/project-docs/两端差异.md)

## 定位

| 是 | 不是 |
|---|---|
| 产品行为、包结构（Android）、route、画面规格、Room | Roadmap（→ 对应车道 `examples/project-status/`） |
| 与代码冲突时以当前例子源码为准，再回写 | AI 纪律（→ `shared/` · `prompts/` · `ai-workbench/`） |

## 何时打开

| 场景 | 先读 |
|---|---|
| 接票改功能 | 票「关联画面」→ `画面/<名>/`；按需核心流程 / Room / 架构 §2 |
| 新画面（Android） | 路由 §4 → 复制画面文档模板 |
| 改包结构 | 架构 **§2**（先文档再代码） |
| 依赖版本 | [技术栈.md](../../examples/project-docs/技术栈.md) ↔ `examples/android/gradle/libs.versions.toml` |
| 只问不改 | `prompts/问答_式样问答.md` |

Windows 未实现：不要把 Compose 路径写成桌面真相。

## 读写纪律

1. 新画面 / 改交互先改 `画面/<名>/`  
2. 冲突：代码为准 → 改文档，或回票  
3. 未决不要写成已确认规格  
4. 过程写在票内  

与 status：docs=什么样，status=该车道做到哪。
