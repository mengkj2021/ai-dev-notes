# F31 票：project-docs 去污染与包树补映射

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | F31 |
| 类型 | 功能 / 体验票 |
| 类型细分 | 工程质量（文档对齐） |
| 标题 | 清洗 project-docs 票号/AI 渗入；架构 §2 / 工具类补 BatchRenamePlanner、ImageTagFilter、分页偏好 |
| 状态 | ✅ 已完成 |
| 起票日期 | 2026-09-07 |
| 关联 | 全局扫描 A；[架构设计 §2](../../../../project-docs/架构设计.md)；[工具类](../../../../project-docs/工具类.md)；结构1（完结即删） |
| 档位 | 绿 |

## 关联画面（路径级）

- **预估**：`project-docs/画面/` 索引与若干 README；架构/工具类/Room/技术栈
- **实际改动**：
  - `project-docs/架构设计.md`
  - `project-docs/工具类.md`
  - `project-docs/Room.md`
  - `project-docs/技术栈.md`（删「开发方式」）
  - `project-docs/README.md`
  - `project-docs/画面/README.md`
  - `project-docs/画面/启动画面/README.md`
  - `project-docs/画面/设置画面/README.md`
  - `project-docs/画面/开源许可画面/README.md`
  - `project-docs/画面/导入画面/README.md`
  - `project-docs/画面/教程画面/README.md`

## 验收标准

- [x] 画面索引与技术栈无「AI 开发方式 / skills 链 / 搁置票当规格」渗入
- [x] 架构 §2 与工具类含 `BatchRenamePlanner`、`ImageTagFilter`；偏好含分页两键
- [x] 启动/设置/开源许可 §5 标题为「UI 壳层」
- [x] Room 偏好表述与设置对齐
- [x] 用户 Review 后标 ✅

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-09-07 | 起票并实施文档清洗与包树补映射 | 592f288 |
| 2026-09-07 | 用户验收 ✅；台账同步 | d7a2db8 |

## 完结复盘

- **落地效果**：project-docs 分层边界更清晰；包树与源码对齐，接票少踩「缺工具类」坑。
- **代价与遗留**：部分画面正文仍可能残留历史票号锚点，可按需随 sync-docs 继续 scrub；非本票阻塞。
- **错题本登记**：不适用（结构1 侧登记门禁教训）
