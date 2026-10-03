# git 操作约定 · 细则

> 供 `shared/rules/git-convention.md` ▶ include。

本仓库文档与提交信息均为中文。Windows 上错误编码会造成中文**真乱码且不可逆**。

面向人的 onboarding（账号、克隆、勿提交明细、Gradle zip）见仓库根 `README.md`，此处不重复。

## 提交编码（防乱码）

1. 提交信息 **UTF-8（无 BOM）**，用文件提交，**禁止** `git commit -m "中文"`：
   ```
   git commit -F 提交说明.txt
   ```
   提交后删除临时文件。
2. 中文路径勿直接作 git 参数；改用 `git add project-status/` 或 `git add "*.md"` 等。
3. 提交前 `git log -1` 检查中文；已乱码未 push 用 `git commit --amend -F 提交说明.txt`。

## 提交流程

1. `git status`
2. `git add <文件>`（**禁止** `git add .`）
3. `git commit -F 提交说明.txt`（带票号）
4. `git push`（**禁止** `--force`，除非用户明确确认）
5. commit / push 由用户决定，不主动越权

## 合入方式（开关 · F31）

| 模式 | 行为 |
|---|---|
| `direct-main` | 可在 `main` 直接改、commit、push；不强制 ticket/ / PR |
| `via-mr` | 走 `ticket/` → PR/MR → 合入 `main`；用户要求合入时默认开 PR |

**当前：`direct-main`（不需要提 MR）**

切换：改本表「当前」一行，或口头「合入方式改成 via-mr / direct-main」由 AI 回写（须同步 `.cursor/` 与 `.codebuddy/`）。

`direct-main` 下黄/红档分支为可选；`--force` 仍禁止。

## 工作分支（拆票档位）

命名：`ticket/票号-简述`。档位见 `ai-workbench/细则/接票/拆票门槛与档位.md`。受合入方式开关约束。

| 档 | 策略 |
|---|---|
| 绿 | 不强制开分支 |
| 黄 | **建议**开 `ticket/…` |
| 红 / 多子票并行 | **应当**开分支（`via-mr` 时；`direct-main` 可选） |

例外：本票已在当前分支提交过半不必中途分叉。禁止擅自 `--force`。

## 勿提交内容（摘要）

`build/`、`.gradle/`、`.kotlin/`、`.idea/`、`*.iml`、`local.properties`、wrapper zip、`.DS_Store`、密钥、`*.log`、`.codebuddy/plans/` 等（完整见根 README）。

## push 前门禁（产品票 F19 · ktlint）

克隆后执行一次：`git config core.hooksPath .githooks`。此后 push 时：若本次推送范围内有 `examples/android/` 下 `.kt` / `.kts` 变更则跑 `ktlintCheck`，失败拒绝推送；**仅文档等非 Kotlin 变更则跳过**（秒级结束）。紧急跳过：`git push --no-verify`（不推荐）。

`.githooks/pre-push` **须为可执行**（Git 索引 `100755`）。若 hook 不跑，先查：`git ls-files -s .githooks/pre-push` 是否为 `100755`，以及 `git config --get core.hooksPath` 是否为 `.githooks`。

## push 前自检

- [ ] hooksPath 已启用或知悉未启用；hook 文件为可执行
- [ ] 无预期外本地产物
- [ ] `git log -1` 中文正常
- [ ] 提交说明含改了什么 / 为什么与票号
- [ ] 规则已从 `shared/rules/` 同步到两工具目录
