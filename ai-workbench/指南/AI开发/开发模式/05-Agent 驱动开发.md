# 3.5 Agent 驱动开发

Agent-Driven Development：人给**目标 + 约束**，Agent 自主拆解、编码、测试、修复，人最后验收。

## 流程

```
人设定目标 + Rules/MCP 约束
    ↓
Agent 循环：读上下文 → 规划 → 执行 → 验证 → 修复
    ↓
报告结果 → 人验收（Diff + 功能测试）
```

## Agent 信任层级

| 层级 | 模式 | 代表 |
|------|------|------|
| L1 建议 | AI 给建议，人决定 | Chat |
| L2 写代码 | AI 生成，人 Review | Copilot Tab |
| L3 自执行 | 写代码 + 自查 | Cursor Agent、Claude Code、Codex |
| L4 多 Agent | 协调器 + 子代理，人看报告 | Cursor Projects、CodeBuddy Team |
| L5 事件驱动长跑 | 盯 CI/PR/Slack，人抽检 | Projects Subscriptions；仍须人在环 |

## 可控性设计

- **权限有边界**：Rules 限定可改目录
- **可审计**：操作记录在对话/日志
- **自动检查**：Hook 强制 lint/测试
- **人工兜底**：安全、支付等需确认
- **证据优先**：完成以测试/制品/接线检查为准，不以 Agent 自述为准（见 [3.7 GSD](07-GSD%20编排驱动开发.md)）

## 适用 / 不适用

| 适合 | 不适合 |
|------|--------|
| 批量重命名、迁移、格式化 | 关键业务决策 |
| 独立功能模块 | 安全敏感操作 |
| 多模块并行（Team Agent） | 陌生栈首次探索（易跑偏） |

与 [4.2 Team Agent](../协作/02-Team%20Agent%20多智能体.md)、[2.3 Cursor Projects](../工具/03-Cursor%20使用.md) 配合使用。
