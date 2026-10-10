# F1 票：Windows 立项与 CMP Desktop 骨架

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | F1 |
| 状态 | 🚧 |
| 节点 | B |
| 档位 | 黄 |
| 关联 | 两端差异 / 技术栈 |

## 1. 诉求

- 问题或场景：Windows 车道未立项，无工程
- 预期效果：`examples/windows/` 可 Gradle 构建并启动桌面窗口；栈写进 project-docs

## 2. 必读路径

- `examples/project-docs/两端差异.md`
- `examples/project-docs/技术栈.md`
- `examples/project-docs/架构设计.md`
- `examples/android/`（对照包树，勿套 Compose 技能）

## 3. 验收

- [x] `examples/windows/` 工程可 `./gradlew :composeApp:compileKotlin`（2026-10-10 已过）
- [ ] 桌面窗口可启动（`run`，待你本机点验）
- [x] `技术栈.md` / `两端差异.md` 标明 CMP Desktop
- [x] 车道票库与阶段1 已铺

## 4. 完结复盘（✅ / ❌ 后）

- 落地效果：
- 代价与遗留：
- 错题本：不适用

---

## 扩展

- **已锁定**：Kotlin JVM + Compose Multiplatform Desktop（非 WinUI / 非 Avalonia）；包名 `com.pictureorganizer`
- **步骤**：铺车道 → 写栈文档 → Gradle 骨架 → 空窗可跑
- **方案**：单模块 `composeApp`；Gradle Wrapper 复用 9.7.1
- **开发记录**：2026-10-10 · 立项开工
