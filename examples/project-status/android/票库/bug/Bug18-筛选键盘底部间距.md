# Bug18 票：筛选画面软键盘弹出时底部间距不对

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | Bug18 |
| 状态 | ✅ |
| 节点 | V |
| 档位 | 绿 |
| 提出 | 用户（全面测试 · 筛选画面） |
| 关联画面·预估 | [筛选画面](../../../../project-docs/画面/筛选画面/README.md) §5；`FilterScreen`（`bottomBar` + 内容 `imePadding`）；对照 [Bug3](Bug3-筛选底栏被切.md) / [Bug2](Bug2-详情软键盘顶栏消失.md) / [Bug6](Bug6-筛选后回收站白屏.md) IME 壳层；用例 FT-30 |
| 实际改动 | [筛选画面 README §5](../../../../project-docs/画面/筛选画面/README.md)；`FilterScreen.kt` |

## 1. 诉求（现象）

1. 复现步骤：进入筛选画面 → 点「文件名包含」等输入框 → 软键盘弹出
2. 实际结果：底部区域间距不对（底栏「清除 / 应用」相对键盘 / 内容的空隙异常；与无键盘时观感不一致）
3. 期望结果：键盘弹出时底栏贴合键盘上沿（或按确认方案合理避让）；内容可滚入视野；无多余大空隙、不被键盘/导航栏裁切

## 2. 必读路径

- 用例：[FT-30](../../阶段/阶段3-全面测试/画面测试用例/筛选画面.md)
- `project-docs/画面/筛选画面/README.md` §5
- 代码：`examples/android/app/src/main/java/com/pictureorganizer/ui/filter/FilterScreen.kt`
- 对照：Bug3 / Bug2 / Bug6

## 已锁定（接票时确认）

- 根因：Scaffold 已排除 IME；底栏只吃导航栏、**不**随键盘抬升；内容区再 `imePadding` → 键盘升起时底栏仍贴屏底（易被键盘挡 / 与内容空隙异常）
- 修法：底栏改为 `navigationBars` ∪ `ime` 的 `windowInsetsPadding`；内容区去掉多余 `imePadding`；保留 Scaffold 排除 IME
- 先测后写：否（纯 UI inset，无 JVM 纯逻辑）
- 拆票：未达门槛；档位 **绿**；合入 `direct-main`

## 实施步骤

| # | 步骤 | 状态 | 备注 |
|---|---|---|---|
| 1 | 改 `FilterScreen` 底栏 inset；内容去 `imePadding` | ✅ | `windowInsetsPadding(navigationBars ∪ ime)` |
| 2 | 对齐筛选 README §5；compileDebugKotlin；真机 FT-30 | ✅ | 用户真机通过 |

## 方案

- 文档：筛选 README §5 改为底栏吃 `navigationBars` ∪ `ime`；内容避让由抬升底栏 + `innerPadding` 承担
- 代码：`FilterScreen.kt` 底栏 / 内容 modifier
- 验证：`compileDebugKotlin` 绿；真机 FT-30 用户通过
- 先测后写：无纯逻辑，未加单测

## 3. 验收

- [x] 软键盘弹出：底栏与键盘之间间距正常；清除 / 应用可见可点
- [x] 内容区仍可滚；顶栏不被顶走（Bug2 不回退）
- [x] 无键盘时底栏仍不被导航栏裁切（Bug3 不回退）；应用 / 清除 / 返回行为正常
- [x] FT-30 可勾选通过

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-10-01 | 底栏 `navigationBars` ∪ `ime`；内容去 `imePadding`；README §5 / FT-30 文案对齐；compileDebugKotlin 绿 | （待提交） |
| 2026-10-01 | 用户真机验收通过（FT-30） | |

### 验收对照自评（Bug18）

| 验收项 | 结果 | 证据（改了哪 / 验证了什么） |
|---|---|---|
| 软键盘弹出：底栏与键盘之间间距正常；清除 / 应用可见可点 | ☑ | `FilterScreen` 底栏 `windowInsetsPadding(nav ∪ ime)`；**用户真机通过** |
| 内容区仍可滚；顶栏不被顶走（Bug2 不回退） | ☑ | 内容 `verticalScroll`；Scaffold 仍排除 IME；真机通过 |
| 无键盘时底栏仍不被导航栏裁切（Bug3 不回退）；应用 / 清除 / 返回行为正常 | ☑ | `union(navigationBars)`；返回逻辑未改；真机抽查通过 |
| FT-30 可勾选通过 | ☑ | 用例已勾；用户确认 |

## 4. 完结复盘（✅ / ❌ 后）

- 根因：有自定义 bottomBar 时只加 `navigationBarsPadding`、内容再单独 `imePadding`，键盘升起底栏不抬升、内容多垫一层 → 底部间距错乱
- 为何此前未防住：Bug3 只修导航栏裁切；Bug2/错题本写「内容区 imePadding」，未覆盖「含自定义 bottomBar + 输入」的抬升职责归属
- 新风险：低（union 无键盘时等同导航栏；有键盘贴上沿）
- 错题本：已登记
