# Bug17 票：教程关闭按钮刚进屏时无效

> 全面测试 TU-10 点验；同族 [Bug12](Bug12-导入画面返回键无效.md) / [Bug10](Bug10-主画面标题点击白屏.md) / [错题本 · Bug12 v3 全局扫描](../../../ai-workbench/错题本.md)。

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | Bug17 |
| 状态 | ☐ |
| 节点 | V |
| 档位 | 绿 |
| 提出 | 用户（画面测试用例 教程画面 · 关闭延迟生效） |
| 关联画面·预估 | [教程画面](../../../project-docs/画面/教程画面/README.md) §5/§6；`TutorialScreen`；`PictureOrganizerNavHost` tutorial `onFinished`；[路由设计 §5](../../../project-docs/路由设计.md) |
| 实际改动 | |

## 1. 诉求（现象）

1. 复现步骤：
   1. 设置 →「查看教程」进入教程（`fromSettings=true`，顶栏为「关闭」）
   2. **刚进入**后立刻点顶栏「关闭」
2. 实际结果：关闭**不生效**；等一会儿再点才回设置
3. 期望结果：进入后即可关闭 / 末页「返回」/ 系统返回回设置；**不**改写 `tutorial_completed`（TU-10）
4. 对照：与此前导入等「返回键刚进无效、稍后才好」同族（Bug12：`popRouteIfOnTop` 在 lifecycle 未 RESUMED 时跳过；顶栏应用 `BackNavIconButton` 等守卫）。教程顶栏是 `TextButton`，**未**纳入 Bug12 v3 helper，易漏修。

## 2. 必读路径

- 用例：[TU-09 / TU-10](../../阶段/阶段3-全面测试/画面测试用例/教程画面.md)
- `project-docs/画面/教程画面/README.md` §2 / §5 / §6；[路由设计.md](../../../project-docs/路由设计.md) §5
- [Bug12](Bug12-导入画面返回键无效.md)（transition / RESUMED / `onPopFailed`）；错题本 Bug12 v3（同类用法先全局扫）
- 代码：`TutorialScreen.kt`（`TextButton(onClick = onFinished)`）；`PictureOrganizerNavHost` tutorial 分支（`fromSettings` → `popRouteIfOnTop("tutorial", …)`，含 lifecycle 非 RESUMED → `return false`）
- 首次路径（「跳过」/`navigate(MAIN)`）若同类延迟失效一并回归

## 3. 验收

- [ ] 设置进教程：刚进屏立刻点「关闭」即可回设置（不必干等）
- [ ] 末页「返回」、系统返回 / 手势同效；不改 `tutorial_completed`
- [ ] 首次教程「跳过」/「开始使用」不引入同类卡死；TU-05～TU-07 不回退
- [ ] TU-10 可勾选通过；若抽 helper（如 lifecycle 安全的 Text 退出钮）或复用既有守卫，错题本「全局扫描」不留同族漏网
- [ ] Bug12 / 路由 §5 行为不回退

## 4. 完结复盘（✅ / ❌ 后）

- 根因：
- 为何此前未防住：
- 新风险：
- 错题本：已登记 ／ 不适用

---

## 扩展（仅黄 / 红）

（绿档不填；定点提示：`onFinished` 在未 RESUMED 时 `popRouteIfOnTop` 静默 false——应对齐 Bug12：transition 中禁用或失败可再点 / 等 RESUMED 再 pop；系统返回若裸 `popBackStack` 也要与设置路径一致。）
