# 4.2 协作：Team Agent 多智能体

多 Agent 分工：主控负责规划与调度，执行 Agent / Subagent 各自完成子 Task。依据含 Anthropic《When to use multi-agent systems》。

## 架构

```
用户指令
    ↓
主控 / 协调器（拆 Task、画依赖、分配；Projects 里协调器不写代码）
    ├── Subagent A（模块 1，独立上下文）─┐
    ├── Subagent B（模块 2）────────────┼→ 并行
    └── 验证 Subagent（只测不改史）─────┘
    ↓
主控汇总、冲突仲裁 → 人验收
```

## 什么时候拆、什么时候不拆

| 该拆 | 不该拆 |
|------|--------|
| 上下文能真正隔离（前后端已定接口） | 改功能的人还要写它的测试——同上下文更省 |
| 检索/探索会污染主窗口 | 任务短、单 Agent 一轮能完 |
| 需要独立验收（验证子代理） | 没有 I/O 契约，子代理只会把混乱摘要传回 |

验证子代理有效，因为验收几乎不需要「电话传话」式的建造史。

## 关键设计

| 点 | 说明 |
|----|------|
| 独立上下文 | 各 Agent 独立 Rules/Skill，避免污染 |
| 星型通信 | 执行端只与主控通信，避免 N×N |
| 冲突仲裁 | 同文件冲突由主控合并或交人；或 Git worktree 隔离 |
| 进度透明 | 主控维护全局 TODO；完工写 SUMMARY |
| 成本 | 多 Agent 常多消耗数倍 token，用隔离换质量，不是默认加速 |

## 典型角色

| 角色 | 职责 | 注入 |
|------|------|------|
| 架构师 / 协调器 | Spec/Plan/Tasks | 架构 Rules |
| 前端 | UI 与组件 | 组件库 Skill |
| 后端 | API 与模型 | 数据库 Rules |
| 测试 / 验证 | 黑盒验收 | 测试框架 Skill |
| 审查 | 质量与安全 | Lint/安全 Rules |

## 与其他模式关系

- **SDD** 产出 Tasks → Team Agent 按 Task 分配
- **Agent-Driven L4/L5** ≈ Team Agent / Projects
- Team Agent 是**执行引擎**，不替代 SDD/TDD；长项目仍要 GSD 式证据
- Skill 进主上下文 vs Skill 当子代理调用：有清晰契约时后者更耐长任务
