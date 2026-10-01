# Bug16 票：压缩后再导入丢失 Exif 标签与拍摄日

> 全面测试 IM-16 点验失败；关联 [F7](../功能体验/F7-导入回读EXIF标签.md) / [F24](../功能体验/F24-拍摄日期数据底座.md) / [错题本 · 压缩前读 Exif](../../../ai-workbench/错题本.md)。

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | Bug16 |
| 状态 | ☐ |
| 节点 | V |
| 档位 | 黄 |
| 提出 | 用户（画面测试用例 IM-16） |
| 关联画面·预估 | [导入画面 README §8](../../../project-docs/画面/导入画面/README.md)；[标签.md](../../../project-docs/画面/图片详细画面/标签.md)；`ImportViewModel` / `ImageManager` / `ImageTagMetadata` / `ExifDateTaken` |
| 实际改动 | |

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

## 3. 验收

- [ ] 带本应用 `UserComment` JSON 的 JPEG，开压缩再导入：新记录含回读标签（与默认标签并集去重）；IM-16 可勾选
- [ ] 源仍含 `DateTimeOriginal` 时：开压缩导入后详情有拍摄日（IM-15 不回退）
- [ ] 接票时厘清复现源文件：相册原图 / 本应用私有目录已压缩落盘图 / 详情打标后外拷图——若源文件本身已无 Exif，属 F7 已知「落盘剥 Exif」局限，须在票内写明是否扩展「压缩/打标后回写拍摄日到 Exif」；**不得**把「源无 Exif」误判为读序 bug
- [ ] 关压缩路径回归：标签回读与拍摄日仍正常
- [ ] 文档与错题本：若有新坑（如 `UserComment` 字符集前缀导致 JSON 解析失败）须登记

## 4. 完结复盘（✅ / ❌ 后）

- 根因：
- 为何此前未防住：
- 新风险：
- 错题本：已登记 ／ 不适用

---

## 扩展（仅黄 / 红）

- **根因调查 / 已锁定**：（接票填）候选——① `ExifInterface` 读回 `UserComment` 带 charset 前缀导致 `parseUserCommentJson` 失败；② PFD/`FileDescriptor` 读属性与 `File` 路径不一致；③ 复现用的是压缩落盘后已无 Exif 的文件（标签未双写成功或拍摄日从未回写）；④ 读序回退（与错题本相反，现行代码似未回退）
- **步骤 / 方案**：
  1. 用可控样张（已知 `UserComment` + `DateTimeOriginal`）分别走开/关压缩导入，对照 Room 与详情
  2. 文档先行（若改回写策略或解析容错）→ 改 `ImageTagMetadata` / 导入管线 → 单测能测的先测
  3. 点验 IM-15 / IM-16；更新用例勾选
- **开发记录**：
