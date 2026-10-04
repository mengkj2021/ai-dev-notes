# F56 票：接入 DeepSeek Harness（DSH）为第三工具

> 复制落点：`ai-workbench/体系/票库/功能体验/`。改 AI 核心落位（宪法四象限的 rules / skills 同步面）。

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | F56 |
| 状态 | ✅ |
| 节点 | P |
| 档位 | 绿 |
| 关联 | [AI配置落位](../../../宪法/AI配置落位.md) · [三工具同步](../../../细则/rules/三工具同步.md) · 根 `AGENTS.md` · F53（三份手同步的遗留） · DSH 手册 [2.4](../../../指南/通识/工具/04-DeepSeek%20Harness%20使用.md) |

## 1. 诉求

- 问题或场景：本仓原先只服务 CodeBuddy 与 Cursor（rules / skills 手同步两份）；现在改为**用 DeepSeek Harness（DSH）**开发，第三套工具目录与同步纪律缺位，`shared/` 统一源没有第三落点。
- 预期效果：DSH 从本仓获得与另两个工具同等的常驻纪律与技能：`.dsh/rules/` + `.dsh/skills/` 就位；`AGENTS.md` 明确三工具与同步入口；文档全部由「双工具」改为「三工具」。

## 2. 必读路径

- `ai-workbench/宪法/AI配置落位.md`、`ai-workbench/宪法/include约定.md`
- `shared/README.md`、`shared/skills/README.md`、`shared/rules/sync-convention.md`
- 根 `AGENTS.md`、`CONTRIBUTING.md`

## 3. 验收

- [x] `.dsh/rules/{dev-convention,git-convention,sync-convention}/RULE.mdc` 与 `shared/rules/` 壳逐字一致
- [x] `.dsh/skills/{create-screen-doc,implement-screen,sync-docs}/SKILL.md` 与 `shared/skills/` 壳逐字一致；DSH 会话技能目录已能列出三个技能
- [x] 细则改名 `细则/rules/三工具同步.md`，四个壳（shared / cursor / codebuddy / dsh）include 同步更新
- [x] `AGENTS.md` 增「本仓使用哪三个 AI 工具」表；README / CONTRIBUTING / 落位 / 指南 / 通识工具表同步
- [x] `.gitignore` 放行 `.dsh/rules` / `.dsh/skills`，其余 `.dsh/*` 忽略
- [x] 业务代码零改动（`examples/` 未改）
- [x] **复核轮**：新增 `scripts/check-shell-sync.mjs`（6 组壳 × 4 份 = 24 份，行尾归一化后比内容；默认工作区、`--root` 比快照），接进 `.githooks/pre-push`（工作区口径漂移即拦；已提交口径只警告）；脚本兼检 `.agents/skills/` 同名技能
- [x] **复核轮**：`.agents/skills/` 只留防重复说明 + `.gitignore` 挡散落副本；`AGENTS.md` / 落位 / 三工具同步 / 总览统一口径为「DSH 只自动注入 `AGENTS.md`，`.dsh/rules/` 须按指针读」
- [x] **复核轮**：新增 DSH 运行手册 [通识/工具/04](../../../指南/通识/工具/04-DeepSeek%20Harness%20使用.md)；两个转发桩页改写为「有意的转发桩 + 下钻入口」
- [x] **复核轮撤回**：原报「`.gitignore` 注释双层编码乱码」为**误判**（原始字节即合法 UTF-8，是 PowerShell 控制台按 GBK 显示的假象），未改文件

## 4. 完结复盘（✅ / ❌ 后）

- 落地效果：DSH 三件套落位；三工具同步从「约定」变成有细则、有校验脚本、有 push 门禁、有 `.gitignore` 边界。
- 代价与遗留：**手同步仍未自动化**（本票只加校验与门禁，不改「先改 shared 再复制三处」的流程）；DSH 的 rules 需按指针读，纪律强度弱于 CodeBuddy 的会话注入；`.agents/skills/` 的遮蔽风险已用说明 + ignore + 脚本三重挡住，但机制本身仍存在。
- 错题本：不适用

---

## 扩展（仅黄 / 红）

- **已锁定**：只扩同步面，不改业务代码；DSH 落位采用与 CodeBuddy 相同的 `<name>/RULE.mdc` 结构，减少一类工具特例；校验脚本不依赖 git 子进程与 sha256sum，受限沙箱下同样可跑。
- **步骤**：脚手架 → 壳同步 → 细则改名 → 门面与索引 → 通识 → gitignore → 校验脚本与 hook → 复核轮（DSH 读法口径、防重复、手册、转发桩）。
- **开发记录**：
  - 2026-10-04 一次落位并自检（`git status` 无业务代码改动）。
  - 2026-10-04 复核轮：修 5 条巡检问题（同步门禁 / DSH 读法口径 / 同名技能防重 / DSH 手册 / 转发桩），撤回 1 条误判；正反用例：24 份一致 → 0，改坏任一份 → 1，仅 HEAD 快照缺 `.dsh` → 1 且只报缺失项。
  - 2026-10-04 复第二轮：修编号冲突（Android 车道下一号 F54 → F55，与体系票库附记对齐）、3 条失效相对链接、3 处表格渲染（未转义 `|` / 行内代码多余转义）；另外去除 `阶段3/画面测试用例/README.md` 的全仓唯一 UTF-8 BOM（3032 → 3029 字节，正文与行尾不变）。全仓复检：链接 1115 条 0 失效、include 73 条 0 缺失、表格/frontmatter/围栏 0 问题、编码 0 异常。
