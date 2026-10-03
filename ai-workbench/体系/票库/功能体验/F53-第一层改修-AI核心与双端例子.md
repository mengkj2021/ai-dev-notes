# F53 票：第一层改修 · AI 核心 + 双端例子

> 复制落点：`ai-workbench/体系/票库/功能体验/`。改宪法第一层。

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | F53 |
| 状态 | ✅ |
| 节点 | P |
| 档位 | 红 |
| 关联 | [文档分层约定](../../../宪法/文档分层约定.md) · 根 README |

## 1. 诉求

- 问题或场景：笔记是核心；图片整理只是例子。规格和票应进 `examples/`；打包脚本跟着 Android 例子；仓库名不要叫 picture-organizer。体系进度不应挂在 examples 下。
- 预期效果：`examples/{project-docs,project-status/android|windows,android,windows}`；根上是 prompts/shared/workbench；体系票在 `ai-workbench/体系/`；GitHub 名 **ai-dev-notes**。

## 2. 必读路径

- `ai-workbench/宪法/文档分层约定.md`
- `ai-workbench/宪法/AI配置落位.md`
- `ai-workbench/指南/AI开发体系总览.md`
- `README.md`

## 3. 验收

- [x] 宪法为「笔记 + 例子」；产品 docs/status 在 `examples/`
- [x] `examples/android/`；windows 空位；发版脚本在 `examples/android/scripts/`
- [x] 已删 `prompts/脚手架_生成AI开发结构.md`；根上无 `project-docs/` / `project-status/` 跳转目录
- [x] 门面用个人笔记口径；远端已改名 **ai-dev-notes**
- [x] 体系车道在 `ai-workbench/体系/`
- [x] 历史票相对链（android 票库 → `examples/project-docs`）可用

## 4. 完结复盘（✅ / ❌ 后）

- 落地效果：第一层改为笔记核心；例子进 `examples/`；仓库改名；体系票搬出 examples；指南入口收束。
- 代价与遗留：Windows 未实现；画面文档未逐篇改两端；skills 仍三份手同步；本地文件夹名 `图片整理` 需关 Cursor 后手改 `ai-dev-notes`。
- 错题本：不适用

---

## 扩展（红）

- **已锁定**：共享 docs；产品票按端分开；体系单独车道；本轮不写 Windows 码。
- **步骤**：宪法 → 搬工程 → 拆 status → 钩子与 prompts → 门面指南 → 抽查链接 → 收空目录。
- **开发记录**：2026-10-03 按计划实施并收口。
