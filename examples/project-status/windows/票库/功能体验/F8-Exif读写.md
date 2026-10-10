# F8 票：Exif 读写（Windows）

## 票信息

| 项 | 内容 |
|---|---|
| 票号 | F8 |
| 状态 | 🚧 |
| 节点 | B |
| 档位 | 黄 |
| 关联 | F7 · 核心流程 · ImageTagMetadata |

## 1. 诉求

- 问题或场景：Windows 端未接 Exif；并非平台不能，是未接线
- 预期效果：导入回读 UserComment 标签与拍摄日；落盘/改标签后回写（JPEG）；压缩后补写；改名标签联动写盘

## 2. 必读路径

- `examples/android/.../util/image/ImageTagMetadata.kt`
- `examples/android/.../util/image/ExifDateTaken.kt`
- `examples/project-docs/核心流程.md`

## 3. 验收

- [x] 导入可读 Exif 标签并与默认标签合并
- [x] 导入可读/写入 `dateTakenMillis`
- [x] 详情改标签后写回 JPEG UserComment
- [x] 非 JPEG 优雅跳过不崩

## 4. 完结复盘（✅ / ❌ 后）

- 落地效果：
- 代价与遗留：读用 metadata-extractor；写用 commons-imaging（仅 JPEG）
- 错题本：不适用
