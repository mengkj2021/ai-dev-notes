# 筛选画面

## 1. 画面概述

| 项 | 内容 |
|---|---|
| 画面名称 | 筛选画面 |
| route | `filter?tags={tags}&untagged={untagged}&q={q}&sort={sort}&dateFrom={dateFrom}&dateTo={dateTo}&importFrom={importFrom}&importTo={importTo}` |
| 职责 | 配置标签筛选条件（库标签多选 OR +「未打标签」）、文件名包含、排序、拍摄/导入日期区间；供主画面一览与打包画面复用 |

## 2. 路由定义

- 路由字符串：`filter?tags={tags}&untagged={untagged}&q={q}&sort={sort}&dateFrom={dateFrom}&dateTo={dateTo}&importFrom={importFrom}&importTo={importTo}`（见 `Routes.FILTER`）
- 必选参数：无
- 可选参数：
  - `tags`（多标签以 Unit Separator `\u001F` 拼接后 URL 编码；解析兼容旧版逗号分隔）
  - `untagged`（`true`/`false`，默认 `false`）
  - `q`（文件名包含关键字）
  - `sort`（`ImageListSort` 名，默认 `ImportedAtDesc`）
  - `dateFrom` / `dateTo`（拍摄日本地日历日 epoch day 字符串，空=未设）
  - `importFrom` / `importTo`（导入日本地日历日 epoch day 字符串，空=未设；**F40**）
- 参数说明：进入时带入当前条件以便回显；应用后经上一页 `SavedStateHandle`（`FilterResultKeys`）回传

## 3. 功能清单（总览）

- [x] TopAppBar「筛选」+ 返回（不应用）
- [x] 「未打标签」Checkbox
- [x] 库内标签多选 Checkbox（OR）
- [x] 「应用」回传条件并安全返回（`popFilterIfOnTop` / `popRouteIfOnTop`；**Bug6**）
- [x] 「清除」回传空条件并返回
- [x] 返回/系统返回与「应用」防连点；关闭过程中吞掉二次 back（**Bug6**）
- [x] **F6**：文件名包含（文本框）；排序（导入日期新→旧 / 旧→新 / 文件名 A→Z）
- [x] **F33**：排序增加拍摄日期新→旧 / 旧→新（null 排最后）；拍摄日期起止区间（闭区间，null 不命中，可只填一端）
- [x] **F40**：导入日期起止区间（闭区间，可只填一端；与拍摄日区间 AND；`importedAt` 恒有值）

## 4. 用户操作（总览）

1. 从主画面或打包画面带入当前条件进入
2. 勾选标签 / 未打标签 / 填文件名关键字 / 选排序 / 设拍摄或导入日期区间 →「应用」返回上一画面
3. 「清除」清空条件并返回；返回箭头不改动原条件

## 5. UI 壳层

- 顶部：`TopAppBar` + 返回
- 主体：文件名输入 + 排序单选 + 拍摄/导入日期起止（DatePicker）+ 可滚动 Checkbox 列表（避让由抬升后的底栏 + Scaffold `innerPadding` 承担；**Bug18**）
- 底部：清除 / 应用（`navigationBars` ∪ `ime` 的 `windowInsetsPadding`：无键盘避导航栏 **Bug3**；有键盘贴键盘上沿 **Bug18**）
- **Bug6**：`Scaffold.contentWindowInsets` 排除 IME（对照详情 Bug2）

## 6. 系统返回动作

| 场景 | 返回键 / 返回箭头行为 |
|---|---|
| 有上级画面 | 返回上一画面（**不**回传条件）；与「应用」共用防抖，避免二次 pop 卸掉 Main（**Bug6**） |

## 7. 进入与退出

| 方向 | 说明 |
|---|---|
| 从哪里进入 | [主画面](../主画面/README.md) 三一览菜单「筛选」；[图片打包画面](../图片打包画面/README.md) |
| 可前往 | 无 |
| 退出去向 | 安全 `pop` 回调用方（`popFilterIfOnTop`；非裸 `popBackStack`） |

## 8. 数据依赖

- UI：`FilterScreen`；逻辑：`FilterViewModel`（`FilterUiState` / 事件）
- 数据：`TagRepository.observeTags()`；结果模型 `TagFilterCriteria`（含拍摄日 / **F40** 导入日 epoch day 字段）
- 回传键：`FilterResultKeys`（含 `filter_result_date_*` / `filter_result_import_*` / `filter_result_applied` 等）
