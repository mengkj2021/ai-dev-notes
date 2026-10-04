# 贡献指南

本仓**核心**是个人 AI 开发笔记；图片整理是例子。

## 改 AI 体系（prompts / shared / ai-workbench）

1. 先改 `shared/` 或 `ai-workbench/`；`prompts/` 只做编排壳。  
2. 改 rules / skills 后同步 `.cursor/`、`.codebuddy/` 与 `.dsh/`（[三工具同步](shared/rules/sync-convention.md)）；`node scripts/check-shell-sync.mjs` 校验，push 前门禁会自动跑。  
3. 起票挂 **[体系车道](ai-workbench/体系/README.md)**（FN · 节点 P），或极简附记。  
4. 入口：[体系总览](ai-workbench/指南/AI开发体系总览.md)

## 改例子（examples / 规格 / 该车道进度）

1. 选 [android](examples/project-status/android/) 或 [windows](examples/project-status/windows/) → 起票 → 拉 [票_接票开发](prompts/票_接票开发.md)。  
2. 业务码只改 `examples/<端>/`；规格写回共享 `examples/project-docs/`（两端差见 [两端差异](examples/project-docs/两端差异.md)）。  
3. Android 构建：[examples/android/README.md](examples/android/README.md)。Windows 尚未开工。  
4. Compose 画面技能仅用于 Android 例子。

## Git

- [git-convention](shared/rules/git-convention.md)：中文用 `-F`、禁止 `git add .`、禁止擅自 `--force`。  
- `git config core.hooksPath .githooks`  
- 提交推送可拉 [prompts/git_提交推送.md](prompts/git_提交推送.md)

## 安全与隐私

- Android 例子不联网、无服务端。  
- 勿提交 `local.properties`、密钥、构建产物。

## 许可

[MIT License](LICENSE)。
