# Cursor · Agent Skills 文档

**分区**：资讯 · **收集**：2026-10-03

## 简介

Cursor 官方技能说明：发现路径、`SKILL.md` frontmatter（含 Cursor 扩展字段 `paths`、`disable-model-invocation`）、内置技能表、从 GitHub 经 plugin marketplace 安装、以及 `/migrate-to-skills`。

## URL

https://cursor.com/docs/skills

帮助页：https://cursor.com/help/customization/skills

## 使用效果

按文档：技能会从 `.cursor/skills/`、`.agents/skills/`（及 Claude/Codex 兼容目录）自动发现；可用 `/技能名` 点名，或做成 Custom Mode 整段会话挂上。从 GitHub 拉技能需带 `.cursor-plugin/marketplace.json` 的 plugin，不是随便 clone 一个仓就会出现。本仓已有项目 skills（如 create-screen-doc），与该机制一致。

## 对本仓

与「prompts 壳 + ▶ include 细则」并存：技能管某一类活的 SOP，prompts 管这次怎么开干。不互相替代。

上一级：[GitHub收集](../../00-目录.md)
