# Bug14 票：导入中系统返回未拦截

> 全面测试 IM-04 点验；关联 [F10](../功能体验/F10-导入流程体验.md) / [Bug12](Bug12-导入画面返回键无效.md) / [错题本 · BackHandler](../../../../../ai-workbench/错题本.md)。

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | Bug14 |
| 状态 | ✅ |
| 节点 | V |
| 档位 | 绿 |
| 提出 | 用户（画面测试用例 IM-04） |
| 关联画面·实际改动 | 无需改文档（导入 README §6 已正确） |

## 1. 诉求（现象）

1. 复现步骤：
   1. 主画面待归档 →「导入图片」
   2. 选多张图开始导入（出现「正在导入 x/y」遮罩）
   3. 导入尚未结束时按**系统返回键**（或返回手势）
2. 实际结果：导入中系统返回**未被拦截**，可离开导入画面（中断导入）
3. 期望结果：导入中系统返回键 / 手势 / 预测性返回均无响应，画面保持；顶栏返回 `enabled=false`（[导入画面 README §6](../../../../project-docs/画面/导入画面/README.md)；F10）

## 2. 必读路径

- `project-docs/画面/导入画面/README.md` §6（导入中：`BackHandler` **始终启用并吞掉**事件）
- `android/.../ui/importimages/ImportScreen.kt`（修后：始终启用；`isImporting` 时吞掉）
- [F10](../功能体验/F10-导入流程体验.md)、[错题本 2026-09-04](../../../../../ai-workbench/错题本.md)（`enabled=false` ≠ 禁用返回）
- [Bug12](Bug12-导入画面返回键无效.md)（v2 为修 Idle 返回写回了 `enabled = !isImporting`，与 F10 冲突）
- 用例：[IM-04](../../阶段/阶段3-全面测试/画面测试用例/导入画面.md)

## 已锁定（接票时确认；绿档可写「见现象/期望」或省略本节）

- 见现象 / 期望；导入中始终 `BackHandler { if (isImporting) return; requestBack() }`；顶栏 / Idle 逻辑不动

## 实施步骤（接票后填；绿档可写「一步完成」）

| # | 步骤 | 状态 | 备注 |
|---|---|---|---|
| 1 | `ImportScreen` BackHandler 改为始终启用并吞导入中事件 | ✅ | 一步完成 |

## 方案（接票后填）

- 文档改动：无需改文档（README §6 已正确）
- 代码改动点：`ImportScreen.kt` BackHandler
- 验证方式：真机 IM-04 + Idle 回归（Bug12 / Bug10）
- 先测后写：无纯逻辑，未加单测

## 3. 验收

- [x] 导入中：系统返回键、返回手势、预测性返回均不退出画面
- [x] 导入中：顶栏返回不可点（`enabled=false`）
- [x] Idle：顶栏 / 系统 / 手势仍可安全返回 main
- [x] IM-04 可勾选通过

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-10-01 | 修正 BackHandler：导入中始终启用并吞掉；空闲才 `requestBack()` | （待提交） |
| 2026-10-01 | 用户真机验收通过（队列统测） | |

### 验收对照自评（Bug14）

| 验收项 | 结果 | 证据（改了哪 / 验证了什么） |
|---|---|---|
| 导入中：系统返回键、返回手势、预测性返回均不退出画面 | ☑ | `ImportScreen`：`BackHandler { if (isImporting) return@BackHandler; … }`；**用户真机通过** |
| 导入中：顶栏返回不可点（`enabled=false`） | ☑ | `iconBackEnabled = !isImporting && isResumed`；真机确认 |
| Idle：顶栏 / 系统 / 手势仍可安全返回 main | ☑ | Idle 仍走 `requestBack()`；**用户真机通过** |
| IM-04 可勾选通过 | ☑ | 用例已勾 |

## 4. 完结复盘（✅ / ❌ 后）

- 根因：Bug12 v2 误写 `BackHandler(enabled = !isImporting)`，导入中取消拦截
- 为何此前未防住：错题本 / F10 已登记，但 Bug12 补丁回退了正确写法且验收项写「导入中仍吞」时误信 `enabled=false` 双保险
- 新风险：无（与 README §6 / F10 对齐）
- 错题本：已有 2026-09-04 条目，不适用再登

---

## 扩展（仅黄 / 红）

（绿档不填。）
