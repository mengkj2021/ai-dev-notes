# 4.3 协作：Skill 与 Rules 驱动

三层约束：Rules（全局边界）→ Skill（领域方法，渐进加载）→ Template（单任务格式）。Skill 格式以 Agent Skills 为准；MCP 只负责运输（SEP-2640，`skill://`）。

## 三层架构

```
Rules（项目级）  → 编码规范、技术栈、禁止项     ← 全局
    ↓
Skill（领域级）  → 组件库、业务 API 用法       ← 按需加载
    ↓
Template（任务级）→ Spec/Task 模板、代码骨架   ← 单任务
```

## 渐进披露

1. 宿主只索引 `name` + `description`（何时用）
2. 命中后再读 `SKILL.md`
3. 正文引用的参考文件、脚本：**脚本跑出来的输出**进上下文，源码不必进

与「把所有 Skill 拼进 System Prompt」相反。全量加载见 [1.5](../基础/04-AI%20开发避坑手册.md) 问题 3。

## Rules 分类示例

| 类型 | 示例 |
|------|------|
| 编码风格 | TS 严格模式，禁止 any |
| 架构 | 业务逻辑只在 service 层 |
| 框架 | 使用 React Router v6 |
| 安全 | 用户输入 XSS 过滤 |
| 文件级 | `api/*.ts` 必须 async/await |

## 协作流程

1. 读 Rules → 确定编码约束
2. 查 Skill 索引 → 只加载相关流程
3. 读 Template → 按骨架填充

## 各模式中的角色

| 模式 | Rules/Skill 作用 |
|------|------------------|
| SDD | 约束在 Spec 框架内按规范编码 |
| TDD+AI | 代码符合测试框架 API |
| PDD | 轻量 Rules 防风格漂移 |
| Review-Driven | 审查后自动修复遵循 Rules |
| Agent-Driven | Rules 为 Agent 不可逾越边界 |

配置路径见 [2.3 Cursor 使用](../工具/03-Cursor%20使用.md)。隐性知识写不成 Skill，见 [4.7](07-退化为验收者的风险.md)。
