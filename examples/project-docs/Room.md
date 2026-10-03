# Room

> **Android 现状**：`data/local/` + `data/repository/`。Windows：未实现，见 [两端差异.md](两端差异.md)。

| 项 | 值 |
|---|---|
| 类 / 文件 | `AppDatabase` / `picture_organizer.db` |
| 版本 | **5**（→2 标签；→3 重命名模板；→4 `originalName`；→5 `dateTakenMillis`） |
| 初始化 | `PictureOrganizerApplication.database`（lazy） |

## `images`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | String (PK) | |
| `filePath` | String | 相对 `images/`，如 `pending/…` |
| `fileName` | String | 显示名 |
| `description` | String | |
| `status` | String | Pending / Confirmed / NoModify |
| `importedAt` | Long | |
| `tagsJson` | String | JSON 数组；**仅用户标签** |
| `originalName` | String? | 导入 DISPLAY_NAME；历史可 null |
| `dateTakenMillis` | Long? | Exif 拍摄日；无则 null |

## `tags` / `tag_templates` / `rename_templates`

| 表 | 要点 |
|---|---|
| `tags` | `id`、`name`（唯一）、`sortOrder`、`createdAt` |
| `tag_templates` | `name`、`tagNamesJson`（名数组，非外键）、`isDefault` |
| `rename_templates` | `name`、`pattern`（`{name}` `{date}` `{yyyy}` `{mm}` `{dd}` `{time}` `{tag}` `{tags}` `{n}` 等）、`isDefault` |

标签库 / 模板与 `images.tagsJson` **解耦**（删库标签不自动改旧图）。

## Repository

| 接口 | 职责 |
|---|---|
| `ImageRepository` | 列表 / 移动 / 删除 / 重命名 / 标签；按标签计数与批量剔除；**F48** `renameTagInAllImages`（原位替换词） |
| `TagRepository` | 标签库与标签模板 CRUD / 默认 |
| `RenameTemplateRepository` | 重命名模板 CRUD / 默认 |
| `UserPreferencesRepository` | DataStore：教程、默认标签、压缩、重名询问、`list_paging_enabled`、`list_page_size`（1～500） |

Mapper：`ImageMappers` / `TagMappers`（含 RenameTemplate）。映射层共享 `DateTimeFormatter`（minSdk 26+，可在 Room Flow 复用）。
