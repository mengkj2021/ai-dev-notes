# F50 票：P · clone 后可构建冒烟

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | F50 |
| 类型 | 功能 / 体验票 |
| 类型细分 | 体验增强 |
| 标题 | P · 全新 clone → Open examples/android/ 可构建（wrapper + 冒烟） |
| 状态 | ✅ 已完成 |
| 起票日期 | 2026-09-16 |
| 所属节点 | P |
| 涉及节点 | P |
| 关联 | [技术栈.md](../../../../project-docs/技术栈.md)；根 README 构建节；近源 [F51](F51-公开首版版本号.md)；[阶段2](../../阶段/阶段2-填充与打磨/) |
| 档位 | 黄 |

## 关联画面（路径级）

- **预估**：
  - 无画面（构建 / wrapper / 文档冒烟）
- **实际改动**：
  - 无需改画面文档；根 README + 技术栈构建说明

## 用户诉求 / 场景

- 要解决的问题或场景：当前 `gradle-wrapper.properties` 偏本地 zip；公开后陌生人 clone 易卡在发行包。计划要求「按公开 README：全新 clone → Open `examples/android/` 可构建」。
- 预期效果：`distributionUrl` 改为官方 URL（或 README 明确双路径且默认外人可走通）；本机按公开 README 跑通 `assembleDebug`；JDK / AGP / SDK 要求写清。
- 是否已有式样依据：是（[阶段2](../../阶段/阶段2-填充与打磨/)；[技术栈](../../../../project-docs/技术栈.md)）

## 需求确认（接票前若为「待定」先在此澄清）

- wrapper：优先改官方 URL 入库；若保留本地 zip 路径须在公开 README 写清「首选改 URL」。

## 已锁定（接票时确认；绿档可写「见诉求」或省略本节）

- `distributionUrl` 改为官方 `https://services.gradle.org/distributions/gradle-9.7.1-bin.zip`
- 顺带修 `gradlew` / `gradlew.bat` 中 `DEFAULT_JVM_OPTS` 嵌套引号导致无法启动 Wrapper

## 实施步骤（接票后填；绿档可写「一步完成」）

| # | 步骤 | 状态 | 备注 |
|---|---|---|---|
| 1 | wrapper properties → 官方 URL | ✅ | |
| 2 | 修 gradlew JVM opts 引号 | ✅ | 冒烟阻塞 |
| 3 | README / 技术栈对齐 | ✅ | |
| 4 | `./gradlew assembleDebug` | ✅ | BUILD SUCCESSFUL |

## 方案（接票后填）

- 文档先行：根 README 构建节；技术栈 Wrapper 行
- 代码改动点：`gradle-wrapper.properties`；`gradlew` / `gradlew.bat`
- 验证方式：本机 `assembleDebug`

## 验收标准

- [x] 按公开 README：干净环境可 Sync + `assembleDebug`（或记录已知环境门槛）
- [x] wrapper 策略对外人可用；勿提交 zip
- [x] 技术栈 / README 构建节与现状一致
- [x] 票内开发记录（带本票号）已写

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-09-16 | 官方 distributionUrl；修 gradlew 引号；assembleDebug 通过；文档对齐 | （本次提交） |

### 验收对照自评（F50）

| 验收项 | 结果 | 证据（改了哪 / 验证了什么） |
|---|---|---|
| 按公开 README：干净环境可 Sync + `assembleDebug` | ☑ | `JAVA_HOME=corretto-19`；`./gradlew assembleDebug` → BUILD SUCCESSFUL |
| wrapper 策略对外人可用；勿提交 zip | ☑ | `distributionUrl=https://services.gradle.org/.../gradle-9.7.1-bin.zip`；zip 仍 ignore |
| 技术栈 / README 构建节与现状一致 | ☑ | README「Gradle Wrapper」节；技术栈矩阵加 Wrapper 行 |
| 票内开发记录（带本票号）已写 | ☑ | 本票 |

## 完结复盘（✅ / ❌ 后填；票与台账行保留，不删除）

- **落地效果**：外人 clone 后可由 Wrapper 拉官方 Gradle；本机冒烟通过。顺带修了 Wrapper 脚本引号 bug（否则 macOS `/bin/sh` 无法启动）。
- **代价与遗留**：首次构建需下载发行包（网络）；本地旧 zip 可删。JDK 17+ 门槛仍写在 README。
- **错题本登记**：不适用
