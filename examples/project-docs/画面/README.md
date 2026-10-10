# 画面 · 文档索引

一层目录 = 一个 **已实现** route。Android：`examples/android/.../ui/<screen>/`。Windows：`examples/windows/.../ui/<screen>/`（开源许可仍缺）。路由表：[路由设计.md](../路由设计.md)。两端：[两端差异.md](../两端差异.md)。

> **文言**：用户可见文案以 `res/values*/strings.xml` 为准（简中 + ja/en；跟随系统，缺译回退简中）。画面文档中文示例即可，不逐画面维护三语。教程正文：**F12** 扩写、**S6** 分段，三语同步。

## 约定

- 文档：`examples/project-docs/画面/<名>/`（总览 `README.md` + 按需区域文件）；新建复制 [画面文档模板](../../../ai-workbench/模板/画面文档/)
- 本文档描述两端共用产品意图；实现落点见各端代码与 [两端差异.md](../两端差异.md)，不在本表逐行拆端。
- 代码：`ui/<screen>/` + 同包 ViewModel；route 在 `Routes.kt`
- 有 FAB 的可滚列表：底边留空（Bug9；`FabOverlayListContentPadding`）
- **区域深度**：默认一画面一 README；复杂再加同级 md。**例外 · 主画面**可按 Tab 分子目录（`一览.md` / `菜单.md`）+ 共用 `列表项.md`；其它画面勿无故三级嵌套

## 当前画面清单

| 目录 | route | `ui/` 包 | 说明 | 状态 |
|------|-------|----------|------|------|
| [启动画面/](启动画面/) | `splash` | `splash` | → tutorial / main | ✅ |
| [主画面/](主画面/) | `main` | `main` | 壳 + 三 Tab；筛选举 filter | ✅ |
| [导入画面/](导入画面/) | `import-images` | `importimages` | 来源 / 进度；压缩开关 | ✅ |
| [图片详细画面/](图片详细画面/) | `image-detail/{imageId}` | `imagedetail` | 主图、重命名、标签、移动/删除 | ✅ |
| [设置画面/](设置画面/) | `settings` | `settings` | 标签 / 模板 / 默认标签 / 已打包 / 教程 / 许可 | ✅ |
| [开源许可画面/](开源许可画面/) | `oss-licenses` | `osslicenses` | 第三方库一览 | ✅ |
| [标签管理画面/](标签管理画面/) | `tag-manage` | `tagmanage` | 标签库 + 标签模板 | ✅ |
| [重命名模板管理画面/](重命名模板管理画面/) | `rename-template-manage` | `renametemplate` | CRUD | ✅ |
| [重命名模板编辑画面/](重命名模板编辑画面/) | `rename-template-edit` / `…/{templateId}` | `renametemplate` | 添加/编辑表单 | ✅ |
| [默认标签画面/](默认标签画面/) | `default-tags` | `defaulttags` | 导入默认标签 | ✅ |
| [图片打包画面/](图片打包画面/) | `export-zip` | `exportzip` | 已归档打**一个** zip（S7） | ✅ |
| [已打包文件管理画面/](已打包文件管理画面/) | `export-manage` | `exportmanage` | `exports/` 历史（列表/分享/删/改名） | ✅ |
| [筛选画面/](筛选画面/) | `filter?…` | `filter` | 一览 / 打包复用；拍摄/导入日 | ✅ |
| [教程画面/](教程画面/) | `tutorial?…` | `tutorial` | 首次引导；设置可重看 | ✅ |

## 新增画面

1. 复制模板 → `examples/project-docs/画面/<名>/`，填 README（按需区域文件）
2. 登记 [路由设计.md](../路由设计.md) 导航表一行 + 本表一行（含 `ui/` 包；新建多为 `☐`）
3. `examples/android/.../ui/<screen>/` 实现并注册 route（仅 Android 例子）  

SOP：`create-screen-doc` / `implement-screen`（属 AI 世界，非本目录规格正文）。
