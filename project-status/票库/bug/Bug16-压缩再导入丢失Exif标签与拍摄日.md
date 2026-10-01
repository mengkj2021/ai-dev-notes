# Bug16 票：压缩后再导入丢失 Exif 标签与拍摄日

> 全面测试 IM-16 点验失败；关联 [F7](../功能体验/F7-导入回读EXIF标签.md) / [F24](../功能体验/F24-拍摄日期数据底座.md) / [错题本 · 压缩前读 Exif](../../../ai-workbench/错题本.md)。

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | Bug16 |
| 状态 | ✅ |
| 节点 | V |
| 档位 | 黄 |
| 提出 | 用户（画面测试用例 IM-16） |
| 关联画面·预估 | [导入画面 README §8](../../../project-docs/画面/导入画面/README.md)；[标签.md](../../../project-docs/画面/图片详细画面/标签.md)；`ImportViewModel` / `ImageManager` / `ImageTagMetadata` / `ExifDateTaken` |
| 实际改动 | [导入画面 README §8](../../../project-docs/画面/导入画面/README.md)；[工具类.md](../../../project-docs/工具类.md)；错题本 |

## 1. 诉求（现象）

1. 复现步骤（用户描述）：
   1. 准备**已有本应用标签**的图片（Exif `UserComment` JSON；典型：详情打过标签后的 JPEG，或带样张）
   2. 导入画面开启「按规则压缩」，将该图**重新导入**
   3. 打开新导入记录的详情
2. 实际结果：
   - 先前标签**未**出现在新记录（IM-16 未生效）
   - 拍摄日期显示「拍摄日期未知」（与 IM-15「压缩路径仍应保留拍摄日」对照需一并查清）
3. 期望结果：
   - 源 Uri 上若有本应用 `UserComment` 标签 → 与默认标签并集去重写入 `tagsJson` 并进词表（F7 / IM-16）
   - 源 Uri 上若有 `DateTimeOriginal` → 写入 `dateTakenMillis`，详情非「未知」（F24 / IM-15）
   - 读元数据必须在 `prepareForImport` **之前**（错题本已登记；现行 `ImportViewModel.importSingle` 顺序已是先读后压）

## 2. 必读路径

- 用例：[IM-16](../../阶段/阶段3-全面测试/画面测试用例/导入画面.md)（及对照 IM-15）
- `project-docs/画面/导入画面/README.md` §8（压缩前读 `UserComment` + `DateTimeOriginal`）
- [F7](../功能体验/F7-导入回读EXIF标签.md)（回读语义；**遗留**：开压缩落盘仍剥文件 Exif，Room 为真源）
- [F24](../功能体验/F24-拍摄日期数据底座.md)；[错题本 2026-09-04](../../../ai-workbench/错题本.md)
- 代码：`ImportViewModel.importSingle`；`ImageManager.readUserTagsFromUri` / `readDateTakenMillisFromUri` / `prepareForImport`；`ImageTagMetadata`（`writeUserTags` / `parseUserCommentJson`）；`RoomImageRepository` 打标签双写

## 扩展（仅黄 / 红）

- **根因调查 / 已锁定**：
  1. **① 主因（标签）**：`getAttribute(TAG_USER_COMMENT)` 对 UNICODE 样张常只返回 `"UNICODE"`（字节解码在 `\0` 截断）；带 `ASCII\0\0\0` 前缀时裸 `JSONArray` 也会失败。现行 `parseUserCommentJson` 无 charset 剥离。
  2. **写侧**：`setAttribute`→ASCII `createString`，中文标签被替成 `?`；应用内双写后外拷再导亦不可靠。
  3. **③ 拍摄日 / 再导入**：读序未回退；压缩重编码剥 Exif，且 `insert` 从不回写 `UserComment`/`DateTimeOriginal`→再导「源无 Exif」属 F7 遗留。**本票扩展**：导入落盘后回写标签 JSON + `DateTimeOriginal`（Room 仍真源）。
  4. 否决④；② 保留 PFD 为主，读路径改走 `getAttributeBytes` 解码。
- **步骤 / 方案**：
  1. 先测：`parseUserCommentJson` / `decodeUserCommentBytes` / `asciiSafeJsonArray` / `ExifDateTaken.formatToExif` ✅
  2. `ImageTagMetadata`：字节解码 + 解析容错；写入 ASCII 安全 JSON（`\uXXXX`） ✅
  3. 导入后对落盘文件回写标签 + 拍摄日；文档 / 错题本登记 ✅
  4. 单测绿灯；真机统测 IM-15/16 ✅

## 3. 验收

- [x] 带本应用 `UserComment` JSON 的 JPEG，开压缩再导入：新记录含回读标签（与默认标签并集去重）；IM-16 可勾选
- [x] 源仍含 `DateTimeOriginal` 时：开压缩导入后详情有拍摄日（IM-15 不回退）
- [x] 接票时厘清复现源文件：相册原图 / 本应用私有目录已压缩落盘图 / 详情打标后外拷图——若源文件本身已无 Exif，属 F7 已知「落盘剥 Exif」局限，须在票内写明是否扩展「压缩/打标后回写拍摄日到 Exif」；**不得**把「源无 Exif」误判为读序 bug
- [x] 关压缩路径回归：标签回读与拍摄日仍正常
- [x] 文档与错题本：若有新坑（如 `UserComment` 字符集前缀导致 JSON 解析失败）须登记

## 开发记录

| 日期 | 内容 | 关联提交 |
|---|---|---|
| 2026-10-01 | UserComment 字节解码 + ASCII 安全写入；导入后回写标签/拍摄日；单测绿灯 | （待提交） |
| 2026-10-01 | 用户真机验收通过（队列统测；IM-15/16） | |

### 验收对照自评（Bug16）

| 验收项 | 结果 | 证据 |
|---|---|---|
| 带 UserComment JSON 开压缩再导入有回读标签 | ☑ | `parseUserCommentJson` / `decodeUserCommentBytes` / `writeImportMetadata`；单测 + **用户真机通过**；IM-16 已勾 |
| 源含 DateTimeOriginal 时压缩导入有拍摄日 | ☑ | `formatToExif` + `writeImportMetadata`；IM-15 保持勾选；真机通过 |
| 厘清源无 Exif vs 读序；是否扩展回写 | ☑ | 锁定①+③；**扩展回写**已做并写入票/错题本 |
| 关压缩路径回归 | ☑ | 读仍在 `prepareForImport` 前；关压缩 copy 后仍回写；真机通过 |
| 文档与错题本登记 | ☑ | 导入 §8、工具类、错题本 2026-10-01 |

## 4. 完结复盘（✅ / ❌ 后）

- 根因：① `getAttribute(USER_COMMENT)` 对 UNICODE 常只得 `"UNICODE"` / charset 前缀导致 JSON 解析失败；写侧 ASCII 把中文变 `?`；③ 压缩剥 Exif 且导入从不回写 → 再导「源无 Exif」
- 为何此前未防住：F7 只强调「压缩前读」；未覆盖字节解码 / ASCII 安全写 / 落盘回写
- 新风险：低（Room 仍真源；回写失败不影响入库）
- 错题本：已登记 2026-10-01 Bug16 条
