# F42 票：V · 打包可改 zip 文件名

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | F42 |
| 类型 | 功能 / 体验票 |
| 类型细分 | 体验增强 |
| 标题 | V · 打包画面可重命名打包后的文件名 |
| 状态 | ✅ 已完成 |
| 起票日期 | 2026-09-08 |
| 所属节点 | V |
| 涉及节点 | V |
| 关联 | [图片打包画面](../../../../project-docs/画面/图片打包画面/)；同主题 [F41](F41-打包预览排除.md) · [F43](F43-已打包文件管理.md) |
| 档位 | 黄 |

## 关联画面（路径级）

- **预估**：
  - `project-docs/画面/图片打包画面/README.md`
- **实际改动**：
  - `project-docs/画面/图片打包画面/README.md`
  - `project-docs/画面/图片打包画面/输出文件名.md`（新建）
  - `project-docs/核心流程.md`、`数据流.md`、`架构设计.md` §2、`工具类.md`

## 用户诉求 / 场景

- 要解决的问题或场景：打包生成的 zip 名不可自定义
- 预期效果：打包前或结果处可指定/修改输出文件名（多标签多包时命名规则接票锁定）
- 是否已有式样依据：否

## 需求确认（接票前若为「待定」先在此澄清）

- 改名时机：开始打包前统一前缀 / 每包单独改 / 仅结果列表重命名 → **打包前每包单独改 stem**
- 非法名、重名冲突策略 → 见已锁定

## 已锁定（接票时确认；绿档可写「见诉求」或省略本节）

- 开始打包前按即将生成的每一包改主文件名；扩展名固定 `.zip`（对齐 F2）
- 默认仍 `export_{标签}_{日期}`；用户覆盖按标签存在会话内；改筛选清空覆盖
- 不做统一前缀；不做结果列表事后改名（历史文件归 F43）
- 非法名（空、`/ \ : * ? " < > |`）Snackbar，不开始打包；不静默替换
- 本次多包 stem 冲突：Snackbar，不打包；与 `exports/` 已有文件冲突：自动 `_2` `_3`
- 分享与结果列表用最终落盘名
- 先测后写：抽出 `ExportZipNames`
- 档位：黄；`direct-main`

## 实施步骤（接票后填；绿档可写「一步完成」）

| # | 步骤 | 状态 | 备注 |
|---|---|---|---|
| 1 | 文档：输出文件名 + README / 核心流程 / 架构 | ☑ | |
| 2 | 先测：默认名 / 校验 / 冲突 | ☑ | ExportZipNamesTest |
| 3 | VM / 文件名 UI / 三语文案 | ☑ | |
| 4 | 跑相关单测 + 收尾 | ☑ | testDebugUnitTest / ktlintCheck 绿 |

## 方案（接票后填）

- 文档先行：`画面/图片打包画面/输出文件名.md` + README；核心流程 / 数据流 / 架构 §2 / 工具类
- 代码改动点：`util/file/ExportZipNames.kt` + Test；`ExportZipViewModel` / `ExportZipUi` / `ExportZipScreen`；三语 strings
- 验证方式：单测绿灯；开始打包落盘名与输入一致；非法名 / 批次冲突有提示

## 验收标准

- [x] 用户可指定或修改本次输出 zip 文件名且落盘名一致
- [x] 分享仍指向正确文件；非法名有提示
- [x] 票内开发记录（带本票号）已写

### 验收对照自评（F42）

| 验收项 | 结果 | 证据（改了哪 / 验证了什么） |
|---|---|---|
| 用户可指定或修改本次输出 zip 文件名且落盘名一致 | ☑ | `ExportZipScreen` 每包 stem 输入；`ZipStemChanged` → `stemOverrides`；`startExport` 用 `planZipFileNames` 得到最终名再 `ZipExporter.zipFiles`；`ExportZipNamesTest.planZipFileNames_ready_usesCustomStems` / `uniqueZipFileName_*` 通过 |
| 分享仍指向正确文件；非法名有提示 | ☑ | 结果 `fileName`/`absolutePath` 为最终落盘名；`requestShare` 未改路径来源；非法名 / 批次重名在打包前 `error()` → Snackbar（`export_zip_name_empty` / `_illegal` / `_collision`）；`validateZipStem_*` / `planZipFileNames_invalidStem_*` / `duplicateStems_rejected` 通过 |
| 票内开发记录（带本票号）已写 | ☑ | 本票 |

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-09-11 | 打包前每包可改 zip 主名；抽出 `ExportZipNames`；三语文案；`testDebugUnitTest`（ExportZipNamesTest + ExportZipPackTest）/ `ktlintCheck` 绿 | cc68ad1 |

## 完结复盘（✅ / ❌ 后填；票与台账行保留，不删除）

- **落地效果**：打包画面可在开始前改每包主文件名；默认名、非法名拦截、批次冲突、与已有 exports 自动后缀均有单测。
- **代价与遗留**：结果列表事后改名与历史 zip 管理仍属 F43；真机点验输入框 / IME / 多包分享未在本环境做。同目录覆盖改为自动 `_2`（比原先同名删除更安全）。
- **错题本登记**：不适用
