# 贡献指南

本仓两块：**AI 开发搭法**（优先）与 **Android App**（产物）。改什么，写回哪里，按下面走。

## 改 AI 体系（prompts / shared / ai-workbench）

1. 先改 `shared/` 或 `ai-workbench/`（细则正文）；`prompts/` 只做编排壳。  
2. 改 `shared/rules` 或 `shared/skills` 后，**必须**同步到 `.cursor/` 与 `.codebuddy/`（见 [双工具同步](shared/rules/sync-convention.md)）。  
3. 较大清理可走票库 **FN（节点 P）**，或在 [票库附记](project-status/票库/README.md) 留一行。  
4. 入口：[AI 开发体系总览](ai-workbench/指南/AI开发体系总览.md) · [用 AI 开发本仓](ai-workbench/指南/用AI开发本仓.md)

## 改 App（android / 规格 / 进度）

1. **起票** → 拉 [票_接票开发](prompts/票_接票开发.md)；业务码只改 `android/`。  
2. 规格写回 `project-docs/`；进度只认票 + [阶段待对应](project-status/阶段/阶段4-维护与迭代/待对应.md)。  
3. 构建见 [android/README.md](android/README.md)。

## Git

- 服从 [git-convention](shared/rules/git-convention.md)：中文说明用 `-F`、禁止 `git add .`、禁止擅自 `--force`。  
- 克隆后执行一次：`git config core.hooksPath .githooks`。  
- 授权提交推送可拉 [prompts/git_提交推送.md](prompts/git_提交推送.md)。

## 安全与隐私

- App **不联网**、无服务端；导入副本在应用私有目录。  
- 勿提交 `local.properties`、密钥、`*.jks` / `*.keystore`、构建产物（见根 README「勿提交」）。  
- 发现敏感误提交：开 Issue 或直接联系维护者处理。

## 许可

[MIT License](LICENSE)。
