# F7 票：导入时从 Exif 回读标签

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | F7 |
| 类型 | 功能 / 体验票 |
| 类型细分 | 新功能 |
| 标题 | 导入时从 Exif 回读标签并入库（文件内标签副本可迁移恢复） |
| 状态 | ✅ 已完成 |
| 起票日期 | 2026-09-03 |
| 关联 | [标签.md](../../../../project-docs/画面/图片详细画面/标签.md) 6 待定「导入时从 Exif 回读」；[ImageTagMetadata.kt](../../../../android/app/src/main/java/com/pictureorganizer/util/image/ImageTagMetadata.kt)（`readUserTags` 现成但无调用方） |

## 用户诉求 / 场景

- **现状**：给图片打标签时，归属真源写 Room `images.tagsJson`，同时同步写 JPEG Exif `UserComment`（JSON 数组）作可迁移副本；配套的 `readUserTags()` 已实现但**无任何调用**（死代码）。文件存于 App 私有目录 `filesDir/images/{pending,confirmed,no_modify}`。
- **要解决的问题**：图片拷出 App 长期保存（或换机 / 重装 / 清数据）后再导入，文件里虽带着 Exif 标签，App 却从不读它 → 标签只能重打。
- **预期效果**：导入时若图片 Exif `UserComment` 含本应用写入的 JSON 标签数组，自动解析、与默认标签合并去重后写入 `tagsJson`，实现「标签跟文件走」的恢复闭环。
- **是否已有式样依据**：画面文档 [标签.md](../../../../project-docs/画面/图片详细画面/标签.md) 6 待定事项「导入时从 Exif 回读」（未勾选）即本票来源，无独立式样章。

## 需求确认（接票前若为「待定」先在此澄清）

- **回读范围**：仅 JPEG（`ExifInterface` 局限，与写入侧同宽）；非 JPEG / 无 `UserComment` / 内容非本应用 JSON 格式 → 一律忽略，按普通导入处理，不报错不打断（`readUserTags` 现有 `runCatching` 已容错）。
- **合并语义（用户拍板：并集去重）**：导入已写入默认标签 → 回读结果与默认标签**并集去重**；EXIF 标签视为「额外恢复项」，不覆盖用户输入。
- **标签词表入库（用户拍板：进词表，对齐 S2）**：回读出的标签进全局词表 `tags`（与「添加后自动入库」一致），使恢复的标签可在候选标签 / 筛选复用。
- **边界（不做）**：维持「EXIF = 单向副本」定位——不把 Exif 当真源覆盖库、不做删除标签时反向清理 Exif 等扩展；重命名 / 压缩类重写 JPEG 丢失 Exif 属已知局限，库内 `tagsJson` 不受影响。

## 方案（接票后填）

- 文档先行（画面文档 / 式样书 / 架构 / 路由，按需）：`标签.md` 6、详情 README 9、导入 README、式样 4.1、架构 3.2
- 代码改动点：
  - `ImageTagMetadata`：`parseUserCommentJson` / `mergeImportTags` / `readUserTags(InputStream)`
  - `ImageManager.readUserTagsFromUri`（压缩前读源）
  - `ImportViewModel`：合并后 insert + `TagRepository` 词表入库
  - 单测：`ImageTagMetadataTest`（`mergeImportTags`，F20）
- 验证方式：`testDebugUnitTest`（含 ImageTagMetadataTest 5 用例）；真机：带 Exif 标签 JPEG 再导入核对 tagsJson / 词表

## 验收标准

- [x] 导入含 Exif 标签的 JPEG：`tagsJson` 含回读标签且与默认标签去重
- [x] 非 JPEG / 无标签 / 坏 JSON：导入照常，不报错
- [x] 文档与代码对齐：勾选 [标签.md](../../../../project-docs/画面/图片详细画面/标签.md) 6 与图片详细画面 README 9 的「导入时从 Exif 回读」待办
- [x] 开发日志（文件名带 F7）已写

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-09-04 | 先测后写 mergeImportTags；压缩前回读 + 词表；文档勾选 | c6ba4a8 |

## 完结复盘（✅ / ❌ 后填；票与台账行保留，不删除）

- **落地效果**：导入闭环「标签跟文件走」：压缩前读源 Exif → 与默认标签并集去重写库 → 进词表；`readUserTags` 不再是死代码。
- **代价与遗留**：开压缩落盘仍会剥落盘文件 Exif（已知局限，`tagsJson` 不受影响）；`parseUserCommentJson` 依赖 `org.json`，JVM 单测未覆盖（对齐 F16 裁剪，仅测 merge）。
- **错题本登记**：已登记 [ai-workbench/错题本.md](../../../../../ai-workbench/错题本.md)（压缩前读 Exif）
