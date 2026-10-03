# Agent Skills 标准（agentskills）

**分区**：仓库 · **收集**：2026-10-03

## 简介

Agent Skills 开放格式：一个技能就是含 `SKILL.md` 的目录。YAML 至少要 `name`、`description`；正文是步骤说明，可选 `scripts/`、`references/`、`assets/`。按「发现 → 激活 → 执行」渐进加载，避免整份技能常驻上下文。

## URL

https://github.com/agentskills/agentskills

规格页：https://agentskills.io/specification

## 使用效果

按规格：agent 启动只读名称与描述；任务匹配后再读完整 `SKILL.md`，需要时才跑脚本或打开参考文件。可用仓库里的 `skills-ref validate` 校验 frontmatter。本仓**未跑校验**。

## 对本仓

写 `.cursor/skills/` 时可对照字段与目录习惯，不覆盖 create-screen-doc 等本仓细则。

上一级：[GitHub收集](../../00-目录.md)
