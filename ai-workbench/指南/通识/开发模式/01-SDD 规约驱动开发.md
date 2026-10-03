# 3.1 SDD 规约驱动开发

Spec-Driven Development：人定义**做什么**（Spec），AI 负责**怎么做**（Code）。Spec 是 AI 不可逾越的边界。

## Spec vs 传统需求

| 维度 | 传统需求 | Spec |
|------|----------|------|
| 读者 | 人 | AI + 人 |
| 粒度 | 用户故事 | 页面/接口/数据模型 |
| 可验证 | 依赖人工 | 可逐条校验 |
| 约束力 | 开发中易偏离 | 约束 AI 不跑偏 |

## 流程

```
需求描述 → spec.md → AI 审阅 → plan.md → tasks.md → 逐 Task 执行 → 验收
   ↑人主导        ↑AI+人协作              ↑AI 执行，人验收
```

### 1. 编写 spec.md（人主导）

描述做什么，不写实现细节。

| 要素 | 说明 | 示例 |
|------|------|------|
| 功能概述 | 一句话 | 邮箱注册，含验证码 |
| 页面/路由 | 涉及页面 | /register、/login |
| 数据模型 | 核心结构 | User { email, password_hash } |
| 接口清单 | 端点与 I/O | POST /api/auth/register |
| 验收标准 | 可逐条校验 | 无效邮箱 → 前端提示 |

### 2. AI 审阅 → plan.md

技术选型、组件树、文件清单、依赖关系、风险点；不清楚处 AI 应提问。

### 3. 拆分 tasks.md

```
- [ ] Task 1：User 模型（无依赖 ☆）
- [ ] Task 2：send-code 接口（无依赖 ☆）
- [ ] Task 3：register 接口（依赖 1、2）
- [ ] Task 4：注册页 UI（依赖 2 接口约定）
- [ ] Task 5：联调（依赖 3、4）
```

☆ = 可并行。

### 4–5. 执行与验收

AI 逐 Task：读 Spec → 写代码 → 自测 → 标记完成；人对照验收标准确认。

## 价值

- **防跑偏**：Spec 外发散被约束
- **可并行**：无依赖 Task 分给多 Agent
- **可回溯**：对照 Spec 查漏
- **可复制**：模板跨项目复用

## 适用 / 不适用

| 适合 | 不适合 |
|------|--------|
| 新项目、大型功能 | 快速原型（开销大于收益） |
| 多 Agent 并行 | 极小 Bug 修复 |
| 高质量要求 | 需求仍在剧烈变化 |

小改动不必重走全流程，见 [1.5 避坑手册](../基础/04-AI%20开发避坑手册.md) 问题 8。

## 工具：GitHub Spec Kit

开源脚手架（`github/spec-kit`）：`specify init` 后按 Agent 安装 `/speckit.*` 命令或 Skill。

| 命令 | 作用 |
|------|------|
| `/speckit.constitution` | 项目原则 |
| `/speckit.specify` | 需求与用户故事（写做什么） |
| `/speckit.plan` | 技术方案 |
| `/speckit.tasks` | 可执行任务列表 |
| `/speckit.implement` | 按计划执行 |
| `/speckit.converge` | 对照 spec/plan/tasks 补缺口 |

支持 Cursor、Claude Code、Codex、CodeBuddy、Qoder、Copilot、Trae 等。Spec Kit 管**产物形状**，不替代人写验收标准，也不替代 GSD 的证据链。
