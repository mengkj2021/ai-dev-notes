# ai-workbench · AI 开发工作台

编排壳在 [`prompts/`](../prompts/)、[`shared/`](../shared/)；**本目录**放宪法、细则、模板与使用指南。

**给人看的整体系整合**（全面、短精确）：[`指南/AI开发体系总览.md`](指南/AI开发体系总览.md)  
**实践定位与边界**（单人 / 不够什么）：[`指南/AI开发实践-定位与现状.md`](指南/AI开发实践-定位与现状.md)

▶ include 语义：[`宪法/include约定.md`](宪法/include约定.md)  
整仓地图：[`指南/AI开发体系总览.md`](指南/AI开发体系总览.md)

```
prompts/ + shared/  ──▶ include──▶  ai-workbench/
        ↓ 读 examples/project-docs / 该车道 status
        ↓ 改 examples/<端>/ 或笔记层
        ↓ 写回
```

## 分区

| 分区 | 用途 |
|---|---|
| [指南/](指南/) | **人读**：体系总览 · 定位 · docs/status 用法 · [通识](指南/AI开发/00-目录.md) · [GitHub收集](指南/GitHub收集/00-目录.md) |
| [体系/](体系/) | 笔记层票库与阶段 |
| [宪法/](宪法/) | 文档分层 · include · 落位 |
| [细则/](细则/) | 接票 / prompts / rules / skills 正文（被 ▶ include） |
| [模板/](模板/) | 画面文档（含区域模板）· 票 |
| [错题本.md](错题本.md) | 跨票教训 |

## 旁系

| 目录 | 关系 |
|---|---|
| [`examples/project-docs/`](../examples/project-docs/) · [`examples/project-status/`](../examples/project-status/) | 共享规格 + 产品进度 |
| [`examples/`](../examples/) | 例子代码（默认 android） |
| [`prompts/`](../prompts/) · [`shared/`](../shared/) | 编排壳 |
