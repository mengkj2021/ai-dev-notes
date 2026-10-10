# 2.4 DeepSeek Harness（DSH）使用

本文是**本仓的运行口径**（不是官方全貌）：DSH 怎么读工作区指令、怎么发现技能、与本仓三工具同步的关系。
官方仓库：https://github.com/deepseek-ai/deepseek-harness （文档在 `docs/`，技能系统见 `docs/subsystems/skills.md`）。

## 产品形态

| 项 | 说明 |
|---|---|
| 形态 | 本地 Agent（CLI `dsh`）+ 桌面 / Web GUI；同一套 session 与工具面 |
| 模型 | DeepSeek V4 系（Flash 便宜量大 / Pro 强推理），按 API 计费 |
| 能力面 | 文件读写、shell、子代理、工作流编排、后台任务、沙箱、技能、网络检索、计划/目标、持久会话 |
| 项目根 | 最近的、含 `.git` 的祖先目录；没有则当前工作目录 |
| 用户级位置 | `$DSH_HOME`（默认 `~/.dsh`）：会话、凭据、用户级技能等 |

## 工作区指令（本仓最要紧的一条）

**只读 `AGENTS.md` / `CLAUDE.md`**，不读其他规则格式：

| 项 | 值 |
|---|---|
| 候选文件名 | `AGENTS.md`、`CLAUDE.md`（同级内容一致时只渲染一次） |
| 本仓约定 | **正文只维护 `AGENTS.md`**；根 `CLAUDE.md` 为短转发桩（指向 AGENTS），禁止双份全文 |
| 本地叠加 | `AGENTS.local.md`、`CLAUDE.local.md` |
| 加载范围 | 用户级 `$DSH_HOME/AGENTS.md` + 项目链：项目根 → 会话工作目录，逐级（宽 → 具体） |
| 生效时机 | 首次请求注入一次；在更深目录成功读写后，下一次请求补入该目录的指令 |
| 预算 | 由 `maxBytes` 限制整条渲染结果，超出时**优先丢宽泛的**，保留具体的 |

**推论（本仓据此设计）**：DSH **没有** CodeBuddy 那种「会话开始注入常驻 rules」，也**没有** Cursor 的 `globs` / `alwaysApply` 路径触发。所以：

- 根 `AGENTS.md` 是纪律正文；`CLAUDE.md` 只转发，避免双份维护与双倍 token；
- `.dsh/rules/` 下的三份规则**不会自动进上下文**，要靠 `AGENTS.md` 里的指针、或 Agent 主动打开；
- 长规格、画面文档继续按需读（与「规则瘦身」一致）。

## 技能

| 项 | 值 |
|---|---|
| 位置 | `<项目根>/.dsh/skills/<name>/SKILL.md`，或扁平 `<name>.md` |
| frontmatter | 必填 `name`（kebab-case）、`description`；可选 `whenToUse`、`metadata`、`disable-model-invocation`、`user-invocable` |
| 描述即路由 | 模型先只看到 name + description；触发条件写进 description，别只写在正文 |
| 资源 | `references/`、`scripts/`、`assets/` 放在技能目录里，按需打开 |
| 门槛 | 不支持递归 `**/SKILL.md`；改正文即时生效，改 frontmatter 才会刷新目录 |

**项目级技能解析顺序**（前者遮蔽后者，同名时后者永不生效）：

```
1. <项目根>/.dsh/skills/
2. <项目根>/.agents/skills/
3. 自定义目录（若配置）
4. $DSH_HOME/skills
```

本仓的三个技能：`create-screen-doc`、`implement-screen`、`sync-docs`，统一源 `shared/skills/`，同步到 `.codebuddy/` · `.cursor/` · `.dsh/` 三处。
**不要**在 `.agents/skills/` 放同名技能——那里排在第 2 位，会被 `.dsh/skills/` 遮蔽，改了不生效也不报错（`.agents/skills/` 只保留一份说明）。

## 与本仓的落位

| 本仓文件 | 在 DSH 里的角色 |
|---|---|
| `AGENTS.md` | 唯一常驻指令：6 条纪律 + 三工具总表 + DSH 须知 |
| `.dsh/rules/dev-convention` · `git-convention` | 细则的**位置约定**，按需打开（内容与 `shared/rules/` 逐字一致） |
| `.dsh/rules/sync-convention` | 同上，且由 `AGENTS.md` 直接指向 |
| `.dsh/skills/<name>` | 项目技能，自动发现、可直接 `/技能名` 或由模型自选 |

同步是否漂移不靠眼睛：`node scripts/check-shell-sync.mjs`（`.githooks/pre-push` 每次 push 自动跑，漂移即拒绝推送）。细则见 [`细则/rules/三工具同步.md`](../../../细则/rules/三工具同步.md)。
## 隐私 / 成本

- 会话与凭据在 `$DSH_HOME`（本机），**不要**提交；本仓 `.gitignore` 也只放行 `.dsh/rules`、`.dsh/skills`。
- 按量计费：长任务用子代理 / 后台任务并行更省时，但 token 仍按实际消耗计；`.dsh/` 之外的会话数据不入库。
