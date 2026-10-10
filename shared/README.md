# AI 执行件入口（shared/）

本目录维护 **rules / skills 编排壳**（frontmatter + ▶ include），并同步到 CodeBuddy / Cursor / DeepSeek Harness（DSH）。  
细则在 [`ai-workbench/细则/rules/`](../ai-workbench/细则/rules/) · [`ai-workbench/细则/skills/`](../ai-workbench/细则/skills/)。  
include 语义：[`ai-workbench/宪法/include约定.md`](../ai-workbench/宪法/include约定.md)。

提示词编排壳在仓库根 [`prompts/`](../prompts/)。

```
prompts/ + shared/  ←▶ include→  ai-workbench/
    ↓ 读取 examples/project-docs / 产品 status 或 ai-workbench/体系
    ↓ 变更 examples/<端>/ 或笔记层
    ↓ 回写 docs / 该车道 status
```

## 本目录有什么

| 路径 | 用途 |
|---|---|
| [rules/](rules/) | 常驻纪律编排壳 → include `ai-workbench/细则/rules/` |
| [skills/](skills/) | 技能编排壳 → include `ai-workbench/细则/skills/` |

## 维护流程

| 改动内容 | 维护入口 | 同步目标 |
|----------|----------|----------|
| include 约定 / 细则正文 | `ai-workbench/` | 无需同步到工具目录 |
| 提示词编排壳 | `prompts/` | 无需同步 |
| 规则编排壳 | `shared/rules/<name>.md` | `.codebuddy/rules/` + `.cursor/rules/` + `.dsh/rules/` |
| 技能编排壳 | `shared/skills/<name>/` | 各工具技能目录（`.codebuddy/skills/` · `.cursor/skills/` · `.dsh/skills/`） |

改细则：先改 `ai-workbench/` 分册；若编排壳路径不变，只需同步壳文件（若壳有改）到三个工具目录。

## 注意事项

- 规则修改后需**新建对话**才生效（CodeBuddy / Cursor）；DSH 的 `.dsh/skills/` 改后即时生效
- 首次构建 Android 例子见仓库根 [README.md](../README.md)（Open `examples/android/`）
