# 朋友侠（PengYouXia）— 开发约定

朋友侠开发仓库 xiaoHaoZ1/PengYouXia。开发约定与守护侠一致。

- 应用：`com.pengyouxia.app`（debug 加 `.debug`），应用名「朋友侠」。
- 设计事实来源：`docs/PRODUCT.md`；实施任务说明：`docs/TASK_*.md`（任务说明与设计冲突时以任务说明为准）。
- 开发方式：审查会话定设计、写任务说明、审查结果；实施模型按任务说明开工，
  每阶段跑完整验证、单独提交、不推送，最后按任务说明第 7 节格式汇报。
- 交接记录：`docs/HANDOFF.md`（当前进度、待办顺序、换电脑注意事项、真机清单）。换电脑或新开会话先读它。

## 开发机环境

同一仓库在两台开发机可各有工作副本，路径与工具链不同。用 `hostname` 或仓库根路径区分当前机器。

### 电脑 A：`DESKTOP-3649QRQ`（Windows 10 + Git Bash / MINGW64）

- 仓库根：`D:\AIProject\PengYouXia`
- Android SDK：`D:\AndroidDev\sdk`（`adb` 在 `D:\AndroidDev\sdk\platform-tools\adb.exe`，不在 PATH）
  - 已验证（2026-10-25）：platforms `android-36`；build-tools `35.0.0` / `36.1.0`；adb 1.0.41（37.0.1-15733141）
- JDK：`D:\AndroidDev\jdk17\jdk-17.0.20.1+1`（Temurin 17.0.20.1，不在 PATH，构建前需 `set JAVA_HOME=...`）
- Gradle：独立安装 `D:\AndroidDev\gradle\gradle-8.13`（2026-10-25 已验证可运行）；`D:\AndroidDev\gradle.zip` 为 wrapper 缓存修复备份；wrapper 版本随工程定
- `local.properties`：`sdk.dir=D\:/AndroidDev/sdk`（冒号需转义，见踩坑速查；不入库）
- 代理（V2rayN）：HTTP `127.0.0.1:10819`，SOCKS `127.0.0.1:10818`；GitHub 直连不通，仓库已设 local `http(s).proxy`
  - 代理由远端节点转发，隧道建立可能需 ~10 秒：`curl`/`git` 的 `--connect-timeout` 别设太短，否则误判为不通
- 终端是 Git Bash：调 `cmd.exe` 须用 `//c`（`/c` 会被 MSYS 转成 `C:\`）
- 签名文件：未放置 `keystore.properties` / `jiarenxia-release.jks` → release 保持未签名
- 常用测试机：Redmi 2407FRK8EC（rothko / K70 至尊版），Android 14（API 34），adb 序列号 `MJDA5LPF6LPVGQRO`（2026-10-25 在线）

### 电脑 B

暂无朋友侠工作副本。如需在 `E:\` / WSL2 环境克隆，参照守护侠 AGENTS.md 的电脑 B 约定
（Devin 会话在 WSL2 内、Windows 工具链经 `cmd.exe /c` 调用、代理 `127.0.0.1:10808`、
推送用 Windows git），克隆后把实际路径补进本节。

### 电脑 A 踩坑速查

- `.properties` 文件（如 `local.properties`）单反斜杠是转义符会被吞掉：`sdk.dir=D:\AndroidDev\sdk` 被解析成 `D:AndroidDevsdk`，AGP 报「文件名、目录名或卷标语法不正确」。写 `sdk.dir=D\:/AndroidDev/sdk`。`D:/AndroidDev/sdk`（冒号未转义）构建能过，但 lint 报 `PropertyEscape` error。
- 资源目录改名（如 `mipmap-anydpi-v26` → `mipmap-anydpi`）后 `mergeDebugResources` 可能命中旧的构建缓存产物，新目录资源丢失、报 `resource mipmap/ic_launcher not found`，`clean` 也无效：改名后跑一次加 `--no-build-cache` 的构建即可。
- `kspDebugKotlin` 与 `kspReleaseKotlin` 并行写同一 Room schema 目录，偶发读到半截 JSON 报 `Expected end of the object '}'`：整条命令重跑即可。
- Robolectric `@Config(qualifiers)` 里 `night` 必须写在密度之前（`w412dp-h915dp-night-xxhdpi`），否则 QualifierParser 抛异常；Robolectric 非触摸模式会把焦点给第一个输入框，截图前先点一下空白处进入触摸模式。
- 测试进程超时残留的 `Gradle Test Executor` 会锁住 `app/build/test-results`（`Unable to delete output.bin`）：结束该残留 java 进程后重跑。
- lint 固定有 `NewerVersionAvailable` warning（kotlin / hilt / roborazzi 有新版）：依赖版本必须与家人侠一致，不升级、也不用 baseline 压掉。
- Git Bash 里 `cmd.exe /c` 的 `/c` 被 MSYS 转成 `C:\`，命令静默不执行（只见 cmd 横幅）。必须写 `cmd.exe //c`。
- Gradle wrapper 缓存损坏报 `Cannot find JAR 'kotlin-compiler-embeddable-...'`：删 `%USERPROFILE%\.gradle\wrapper\dists\gradle-8.13-bin\<hash>\` 下损坏的 `gradle-8.13/`，把 `D:\AndroidDev\gradle.zip` 解压进去，再 `touch gradle-8.13-bin.zip.ok`。
- `adb`、`java` 不在 PATH：adb 用 `D:\AndroidDev\sdk\platform-tools\adb.exe`；构建命令前缀 `set JAVA_HOME=D:\AndroidDev\jdk17\jdk-17.0.20.1+1&&`（cmd 里 `set X=Y&&` 的 `&&` 前不能留空格，否则值会带尾部空格）。

## 模块结构

- `:app` — 应用模块（Compose + Hilt + Navigation Compose），包 `com.pengyouxia.app`；依赖 `:core:model`、`:core:designsystem`。
  内部按包划分：
  - `navigation/AppNavHost` — 全部路由：一级 `friends` / `exchanges` / `mine`（显示底栏），二级 `friend/new`、`friend/{id}`、`friend/{id}/edit`（隐藏底栏；`friend/new` 必须声明在 `friend/{id}` 之前）。
  - `data/` — `FriendRepository`（唯一写入路径，校验失败返回 `FriendResult.Failure`，多表写入走 `db.withTransaction`）、`IdGenerator` / `TimeSource`（`SystemTimeSource` 进程内严格递增）；`data/db/`（Room：`AppDatabase`、`Entities`、`Daos`、`Mappers`，枚举以 name 字符串存，未知值读取不崩溃）；`data/settings/`（DataStore：上次备忘分类）；`data/demo/DemoDataSeeder`（debug 测试工具的合成演示数据）。
  - `di/AppModule` — Room（`pengyouxia.db`）、DataStore、仓库绑定。
  - `domain/` — `pinyin/PinyinIndex`（自家人侠复制，API 29+，`PinyinProvider` 可注入）、`FriendOrdering`（Collator 排序 + 拼音首字母分组，`#` 组最后）、`FriendSearch`（文字 / 拼音 / 怎么认识的 / 备忘片段与高亮）。
  - `ui/friends`（列表与搜索）、`ui/friends/edit`（添加 / 编辑朋友）、`ui/frienddetail`（详情与记备忘弹层）、`ui/exchanges`、`ui/mine`（debug 包显示「测试工具」）；ViewModel + StateFlow，业务规则不进 Composable。
  - `reminder` 等随后续批次加入。
- `:core:model` — 纯 Kotlin/JVM 库（不依赖 Android），领域模型与纯规则，包 `com.pengyouxia.core.model`。枚举名会写进数据库与备份，不得改名。
- `:core:designsystem` — Android 库，主题 token（`.theme`：`PyxTheme`、`PyxColors`/`LocalPyxColors`）与通用组件（`PageHeader`、`PyxCard`、`SectionHeader`、`EmptyState`、`DateTile`、`CountdownBadge`、`FriendAvatar`），包 `com.pengyouxia.core.designsystem`；不依赖数据层与业务代码。界面颜色一律取 token，不硬编码。初始自守护侠复制，与之各自演进。

## 构建 / 测试

Gradle 根目录就是仓库根目录。完整验证任务：

```
:core:model:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease
```

- 电脑 A（Git Bash）：`cmd.exe //c "cd /d D:\AIProject\PengYouXia && set JAVA_HOME=D:\AndroidDev\jdk17\jdk-17.0.20.1+1&& gradlew.bat <任务>"`
- `local.properties` 的 `sdk.dir` 指向本机 Android SDK（不入库）。
- 单元测试：`:app` 用 Robolectric（`@Config(sdk = [34])`），`:core:model` 为纯 JUnit。
- `--tests` 模式经 `cmd.exe` 时不要加引号。
- 已知偶发：lint 在 `lintAnalyze*` 阶段出现 Kotlin 分析器内部崩溃（`KotlinIllegalArgumentExceptionWithAttachments`）时单独重跑即可。
- Lint 报告：`app/build/reports/lint-results-debug.html`，要求 0 error。
- 数据库：Room 版本 1，schema 导出到 `app/schemas/com.pengyouxia.app.data.db.AppDatabase/<版本>.json` 并入库；以后每次改表都写显式 Migration + 迁移测试，禁止 `fallbackToDestructiveMigration`。
- 截图测试（Roborazzi record 模式）随 `:app:testDebugUnitTest` 一起运行，产物在 `app/build/outputs/roborazzi/`，文件名前缀 = 批次：`m0_components_*`（组件样例页，只在测试源码 `screenshots/ComponentGallery.kt`）、`m1_*`（朋友 / 搜索 / 详情 / 编辑 / 弹层 / 我的页，浅深各一张，用 `DemoDataSeeder` 演示数据）。弹层与对话框在独立窗口，用 `captureScreenRoboImage` 截全屏。
- Robolectric 测试要点：`MainActivity` 可直接用 `createAndroidComposeRule<MainActivity>()` 启动（用真实 Hilt 图，每个测试是全新的空库）；Room / DataStore 走真实线程，用 `waitUntil` 等节点，不要 sleep；LazyColumn 屏幕外的项不在语义树里，不能当等待条件。
- 清单权限由 `ManifestPermissionsTest` 守护：只允许白名单内权限（目前仅 androidx.core 注入的 `<applicationId>.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`），以后加权限须同步白名单。
- APK 输出：`app/build/outputs/apk/debug/app-debug.apk`、`app/build/outputs/apk/release/app-release-unsigned.apk`（有签名配置时为 `app-release.apk`）。
- 安装到设备：`D:\AndroidDev\sdk\platform-tools\adb.exe -s <序列号> install -r app/build/outputs/apk/debug/app-debug.apk`；小米/HyperOS 需开「USB 安装」。

## 测试分工（项目规则）

- **代码测试由模型自己完成**：单元测试、Robolectric/截图测试、lint、debug/release 构建，改完代码必须亲自跑完整验证任务并全部通过，失败要自己修，不得把"请用户跑一下构建/单测"当作交付。
- **真机上一步一步的操作测试交给用户**：安装 APK、锁屏静置、观察界面表现、去系统设置里开关权限、重启手机等，模型不要自己用 adb 或其他方式代为操作手机。模型的职责是把这些整理成**编号的逐步操作清单**交给用户：每步写清楚操作、预期现象、不符合时记录什么（截图 / 日志内容）。
- 真机步骤清单随交付一起给出；用户反馈结果后，模型再根据反馈定位和修复。

## 正式版签名

与家人侠共用同一把密钥（沿用守护侠约定）：工程根目录放 `keystore.properties` 与
`jiarenxia-release.jks`（均应 .gitignore；工作副本在电脑 B 的 `E:\JiaRenXia\apps\android\`，
离线备份在 `W:\20_家人侠重要信息`；电脑 A 当前未放置）。
文件缺失时 release 保持未签名，绝不回落到 debug 签名。
证书 SHA-256：`00:15:22:AE:2C:13:CB:3B:BD:2F:38:CB:FA:49:FB:32:2D:5B:59:91:85:93:F5:67:A2:C0:65:E2:61:F8:75:D0`。

## Git / 提交

- 提交作者必须是用户本人：`git -c user.name=yangyonghao -c user.email=2107290326@qq.com commit ...`；不要把身份信息写进 git config。
- 提交信息用中文；代码注释用中文。
- 直连 GitHub 不通，推拉走代理（见「开发机环境」）：电脑 A 仓库已设 local `http(s).proxy=127.0.0.1:10819`，Git Bash 里直接 `git pull/push`。
