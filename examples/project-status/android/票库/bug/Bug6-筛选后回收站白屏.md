# Bug6 票：V · 筛选应用后切回收站白屏

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | Bug6 |
| 类型 | Bug 票 |
| 标题 | V · 筛选应用后切回收站一览全白 |
| 状态 | ✅ 已完成 |
| 起票日期 | 2026-09-08 |
| 所属节点 | V |
| 涉及节点 | V |
| 提出 | 用户 |
| 关联 | [主画面/回收站一览](../../../../project-docs/画面/主画面/回收站一览/)；[筛选画面](../../../../project-docs/画面/筛选画面/)；[F47](../功能体验/F47-Debug专用AppLog.md) |
| 档位 | 黄 |

## 关联画面（路径级）

- **预估**：主画面回收站一览；筛选画面
- **实际改动**：
  - `project-docs/画面/主画面/README.md`（SOURCE_TAB；Scaffold 排除 IME）
  - `project-docs/画面/主画面/回收站一览/菜单.md`
  - `project-docs/画面/筛选画面/README.md`（IME + 安全 pop / 防连点）

## 现象（用户可观察）

1. 复现：主画面 → 筛选 → 应用 →（或回收站进筛选再返回）→ 白屏
2. 实际：全白
3. 期望：列表或空态正常

## 根因（调查后填）

- F47 log：返回后仍有 `pageItems=1`，随后 Main `ON_DESTROY`，系统返回直接 `Activity.finish` → **二次 `popBackStack` 把根 Main 弹出**，NavHost 空窗呈白屏（非筛空、非写错 Tab）。
- 早期 A（回传 Tab）/ B（IME）加固保留，但未盖住主因。

## 已锁定

- 离开筛选不得把 Main 弹出栈；回传仍绑进入时 Tab
- 先测后写：否

## 实施步骤

| # | 步骤 | 状态 | 备注 |
|---|---|---|---|
| 1 | F47 log 定位 | ☑ | 双 pop / 空栈 |
| 2 | SOURCE_TAB + IME | ☑ | 早期加固 |
| 3 | `popFilterIfOnTop` + 防连点 + BackHandler + queue 埋点 | ☑ | 主修 |
| 4 | 真机点验 | ☑ | 2026-09-14：原白屏手顺已恢复；`popFilter done … queue=?>main`，Main `ON_RESUME` |

## 方案

- `popFilterIfOnTop`；Filter `closing` + `BackHandler`；Nav `size/queue` 日志

## 验收标准

- [x] 现象消失，期望结果达成（真机）
- [x] 其它 Tab / 无筛选路径进回收站回归
- [x] 票内开发记录已写

### 验收对照自评（Bug6）

| 验收项 | 结果 | 证据 |
|---|---|---|
| 现象消失，期望结果达成 | ☑ | 用户确认原手顺正常；log：`popFilter done … size=2 queue=?>main`，Main `ON_RESUME`，无再 DESTROY 白屏 |
| 其它 Tab / 无筛选路径进回收站回归正常 | ☑ | `SelectTab` 路径未改；同会话 `SelectTab NoModify -> NoModify` 正常 |
| 票内开发记录已写 | ☑ | 本票开发记录 |

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-09-09 | SOURCE_TAB + Main/Filter 排除 IME | a3cad4b |
| 2026-09-14 | 真机仍白屏；拆 F47 |  |
| 2026-09-14 | log 定位双 pop；`popFilterIfOnTop` + 防连点 | f2d6888 |
| 2026-09-14 | 真机确认原手顺已恢复，关票 |  |

## 完结复盘（✅ / ❌ 后填；票与台账行保留，不删除）

- **此 bug 的产生原因**：筛选离开时二次 `popBackStack` 弹出根 Main → NavHost 空窗白屏；表面像「回收站白屏」
- **为什么此前没被发现 / 防不住**：先按条件写错 Tab / IME 修；缺 back stack 日志；有数据仍白易误判为空态
- **修改是否带来新风险**：非 filter 上 skip pop；关闭中吞 back；SOURCE_TAB/IME 仍保留
- **错题本登记**：已登记
