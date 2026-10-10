# windows · 图片整理（Compose Desktop）

与 Android 共用 [project-docs](../project-docs/)，差异见 [两端差异](../project-docs/两端差异.md)。  
进度：[windows 车道](../project-status/windows/README.md)。

## 要求

- JDK 17+（建议 `JAVA_HOME` 指向 JBR / Temurin 17）
- 打开本目录为 Gradle 工程根（勿与 `examples/android/` 混开）

## 常用命令

```bat
set JAVA_HOME=C:\Users\…\.jdks\jbr-17.0.9
gradlew.bat :composeApp:compileKotlin
gradlew.bat :composeApp:run
```

## 打包成 exe

工程已配置 Compose Desktop `nativeDistributions`（Exe / Msi）。

| 产物 | 命令 | 输出 |
|---|---|---|
| 可运行目录（内含启动 exe，免安装） | `gradlew.bat :composeApp:createDistributable` | `composeApp/build/compose/binaries/main/app/PictureOrganizer/PictureOrganizer.exe` |
| 安装包 exe | `gradlew.bat :composeApp:packageExe` | `composeApp/build/compose/binaries/main/exe/` |
| 安装包 msi | `gradlew.bat :composeApp:packageMsi` | `composeApp/build/compose/binaries/main/msi/` |

**路径注意**：仓库若在含中文的目录（如 `项目列表`）下，`jlink`/`jpackage` 可能因参数文件编码失败。请用英文路径的目录联接再打包，例如：

```bat
mklink /J C:\dev\AI-DEV-windows "%CD%"
cd /d C:\dev\AI-DEV-windows
gradlew.bat :composeApp:createDistributable
```

安装包任务首次会下载 WiX（需能访问 GitHub）。`description` / `vendor` 请保持 ASCII，避免 jpackage `Input length = 1`。

## 数据目录

`%LOCALAPPDATA%\PictureOrganizer\`（`images/` · `exports/` · `images.json` · `catalog.json` · `prefs.json`）

## 包结构

`composeApp/src/main/kotlin/com/pictureorganizer/` — 对照 [架构设计 §2](../project-docs/架构设计.md)（桌面适配，无 Android 框架依赖）。
