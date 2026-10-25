# 实施任务 M0：工程骨架

> 本文件是交给实施模型的完整任务说明。开工前先通读本文件和下列必读文件，
> 有冲突时以本文件为准；本文件没写到的细节，按"简单、可维护、与家人侠/守护侠习惯一致"的原则自行决定，并在最终汇报里说明。
> 本批**只搭骨架，不做业务功能**（朋友、备忘、生日、人情、备份都在后续批次）。

---

## 0. 开工前必读

1. `AGENTS.md`（项目约定，**尤其是「开发机环境」「测试分工（项目规则）」「Git / 提交」**；先用 `hostname` 判断所在电脑，按对应命令构建）
2. `docs/PRODUCT.md`（产品设计，重点读第 4 节页面、第 10 节视觉、第 11 节技术架构）
3. 参考工程（只读，不要修改）：
   - 家人侠 `D:\AIProject\JiaRenXia\apps\android\`：`gradle/libs.versions.toml`、`settings.gradle.kts`、`build.gradle.kts`、`app/build.gradle.kts`（Hilt / KSP / Room schema 的配置方式）、`core/model/build.gradle.kts`（纯 JVM 模块写法）
   - 守护侠 `D:\AIProject\ShouHuXia-dev\`：`gradlew`、`gradlew.bat`、`gradle/wrapper/`、`gradle.properties`、`.gitignore`、`.gitattributes`、`guardianapp/build.gradle.kts`（签名配置、testOptions）、`core/designsystem/`（主题与组件，本批要复制改造）

---

## 1. 目标

仓库根目录即 Gradle 根目录。完成后：

- 有 `:app`、`:core:model`、`:core:designsystem` 三个模块，完整验证任务全部通过；
- 装到手机上能打开，看到「朋友 / 人情 / 我的」三个 tab，每个 tab 是设计好的空状态页；
- 主题是 `docs/PRODUCT.md` 第 10 节的青绿 / 暖橙配色，浅色深色都有；
- 有截图测试产出各页面渲染图，供审查方看配色与版式。

## 2. 现状（已核实）

- 仓库只有 `AGENTS.md` 和 `docs/`，没有任何代码。
- 电脑 A 工具链已验证：SDK `D:\AndroidDev\sdk`（platforms android-36）、JDK 17 `D:\AndroidDev\jdk17\jdk-17.0.20.1+1`、Gradle 8.13。
- 守护侠的 `libs.versions.toml` **缺少 Hilt 插件条目**，以家人侠的为准（家人侠有 `kotlin-jvm`、`ksp`、`hilt` 插件）。

---

## 3. 需求规格

### 3.1 Gradle 工程

- 复制守护侠的 `gradlew`、`gradlew.bat`、`gradle/wrapper/`（wrapper 8.13）、`gradle.properties`、`.gitignore`、`.gitattributes`。`.gitignore` 必须包含 `local.properties`、`keystore.properties`、`*.jks`、`*.keystore`。
- `gradle/libs.versions.toml`：版本**与家人侠完全一致**，只保留本工程会用到的条目（Compose、Material3、图标、Activity、Lifecycle、Navigation、Hilt（含 hilt-navigation-compose、hilt-work）、Room、WorkManager、DataStore、协程、core-ktx、JUnit、Robolectric、Roborazzi、androidx-test、compose-ui-test、room-testing、work-testing）。Room / WorkManager / DataStore 本批可以先不接入模块，但条目放进目录，后续批次直接用。不要 glance、baselineprofile、benchmark、uiautomator、profileinstaller。
- `settings.gradle.kts`：`rootProject.name = "PengYouXia"`，`include(":app", ":core:model", ":core:designsystem")`。
- 创建 `local.properties`（`sdk.dir=D:/AndroidDev/sdk`，**不入库**）。

### 3.2 `:core:model`（纯 Kotlin/JVM）

- 写法参照家人侠 `core/model`，包 `com.pengyouxia.core.model`，JVM 17。
- 本批只放已经定了的枚举（`docs/PRODUCT.md` 第 5 节），每个都带中文显示名：
  - `MemoCategory`：`FAMILY 家人 / LIKES 喜好 / WANTS 想要 / RECENT 近况 / OTHER 其他`，枚举声明顺序就是界面顺序
  - `ExchangeDirection`：`OUT 我给 / IN 他给`
  - `ExchangeKind`：`MONEY 钱 / GIFT 礼物 / MEAL 请吃饭`
  - `ExchangeOccasion`：`BIRTHDAY 生日 / WEDDING 结婚 / BABY 生孩子 / HOUSEWARMING 乔迁 / SICK_VISIT 探病 / FESTIVAL 过节 / OTHER 其他`
  - `CalendarType`：`SOLAR 公历 / LUNAR 农历`
- 这些枚举名会写进数据库和备份文件，**以后不得改名**，在 KDoc 里写明这一点。
- JUnit 测试：顺序与显示名符合上表；所有枚举名唯一。

### 3.3 `:core:designsystem`（Android 库）

- 从守护侠 `core/designsystem` 复制，包名改为 `com.pengyouxia.core.designsystem`（主题放 `.theme` 子包）。
- 命名前缀 `Jrx` 一律改成 `Pyx`（如 `PyxTheme`、`PyxColors`、`LocalPyxColors`、`PyxCard`）。
- 颜色按 `docs/PRODUCT.md` 第 10 节重写，浅色 / 深色两套，映射到 Material 3 `ColorScheme`（primary、primaryContainer=primarySoft、secondary=accent、secondaryContainer=accentSoft、background、surface / surfaceContainer*=card 系、outlineVariant=outline、onSurfaceVariant=textSecondary、error）。扩展 token（`PyxColors`）至少包含：`accent`、`accentSoft`、`give`（我给 = accent）、`receive`（他给 = primary）、`pageHeaderBackground`、`cardBorder`。
- 保留并改造的组件：`PageHeader`（平底）、`PyxCard`（白底、1dp 描边、14dp 圆角、无阴影）、`SectionHeader`、`EmptyState`、`DateTile`、`CountdownBadge`（今天用强调色）。`TitleChip`、`IconInfoRow`、`QuickAction` 如本工程用不上就删掉，不留死代码。
- 新增 `FriendAvatar(name: String, size: Dp)`：圆形，primarySoft 底，显示名字第一个字符（按 Unicode 码点取，不能把表情或生僻字截成半个）；名字为空时显示「?」。
- 主题跟随系统深浅色；不做动态取色（Material You）。
- 任何组件内不得硬编码颜色，全部取 token。

### 3.4 `:app`

- 插件：application、kotlin-android、kotlin-compose、ksp、hilt（配置方式参照家人侠 `app/build.gradle.kts`）。
- `namespace` / `applicationId` = `com.pengyouxia.app`；debug `applicationIdSuffix = ".debug"`、`versionNameSuffix = "-debug"`；`versionCode = 1`、`versionName = "0.1.0"`；minSdk 26、compileSdk / targetSdk 36；JVM 17。
- release：`isMinifyEnabled = true`、`isShrinkResources = true`；签名配置照抄守护侠（读根目录 `keystore.properties`，**缺失时保持未签名，绝不回落到 debug 签名**）。
- `buildConfig = true`；testOptions 照抄守护侠（`isIncludeAndroidResources = true`，Roborazzi record 模式）。
- `PengYouXiaApp`：`@HiltAndroidApp`。`MainActivity`：`ComponentActivity` + `@AndroidEntryPoint`，edge-to-edge，`setContent { PyxTheme { AppNavHost() } }`。
- 应用名「朋友侠」；图标先做一个简单的自适应矢量图标（主色底 + 白色「友」字或简洁图形），不要用 Android 默认机器人。
- **Manifest 不声明 `INTERNET` 及任何其他权限**（本批一个权限都不需要）。
- 导航（Navigation Compose）：底部导航三个 tab，图标用 Material Icons：
  | tab | 页头 | 空状态 |
  |---|---|---|
  | 朋友 | 「朋友」，右侧「+」（本批点了无反应或弹「即将推出」均可，汇报里说明） | 标题「还没有朋友」，说明「记下朋友的生日、喜好和往来，下次见面不用再想」，按钮「添加第一位朋友」 |
  | 人情 | 「人情」，右侧「+」 | 标题「还没有往来记录」，说明「随礼、送礼物、请吃饭，都可以记在这里」 |
  | 我的 | 「我的」 | 不是空状态，而是三个分组的占位行：「生日提醒」「数据」（导出备份 / 从备份恢复）「关于」（版本号取 `BuildConfig.VERSION_NAME`；隐私说明文案见 `docs/PRODUCT.md` 4.6）。本批行可点但不跳转 |
- 「朋友」页搜索框本批不放（M1 做）。
- 包结构先建好 `ui/friends`、`ui/exchanges`、`ui/mine`、`navigation`，`data` / `domain` / `reminder` 等后续批次再加，不要建空包。

### 3.5 测试

- `:core:model`：3.2 所述 JUnit。
- `:app`（Robolectric，`@Config(sdk = [34])`）：
  1. **导航测试**：启动后默认在「朋友」tab；点「人情」「我的」能切过去并看到对应页头。
  2. **权限测试**：读取打包后的 `PackageInfo.requestedPermissions`，断言不含 `android.permission.INTERNET`（本批断言为空或不含任何危险权限；写成以后加通知权限也好改的形式）。
  3. **截图测试**（Roborazzi，输出到 `app/build/outputs/roborazzi/`，文件名前缀 `m0_`）：三个 tab × 浅色 / 深色 = 6 张；外加一张**组件样例页**（浅 / 深各 1 张），把 `PageHeader`、`PyxCard`、`SectionHeader`、`EmptyState`、`DateTile`、`CountdownBadge`（今天 / 明天 / 12 天后）、`FriendAvatar`（中文名、英文名、表情开头的名字、空名字）、「我给 ¥800」「他给 ¥600」两种颜色的示例文字排在一页上。样例页只放在测试源码里，不进正式代码。
  - 截图测试随 `:app:testDebugUnitTest` 一起运行（不设 opt-in 开关）。

### 3.6 文档

更新 `AGENTS.md`（**不要改动「测试分工（项目规则）」一节**）：
- 开头的说明改为：应用 `com.pengyouxia.app`（debug 加 `.debug`），设计文档 `docs/PRODUCT.md`，任务说明 `docs/TASK_*.md`。
- 「模块结构」写实际的三个模块及依赖关系。
- 「构建 / 测试」写出完整验证任务（见第 6 节）、APK 实际输出路径、截图产物路径。
- 实施过程中踩到的新坑补进「电脑 A 踩坑速查」。

---

## 4. 架构硬约束（违反即返工）

- 不声明 `INTERNET` 权限；不引入任何统计 / 广告 / 崩溃上报等联网 SDK；不引入版本目录以外的第三方依赖（确需新增，先停下来在汇报中说明理由）。
- 依赖版本与家人侠一致，不要自行升级或降级。
- `:core:model` 不依赖 Android；`:core:designsystem` 不依赖 `:core:model` 以外的业务代码，不依赖数据层。
- 界面里不硬编码颜色、不写死任何真实人名；示例与测试一律用合成名字（如「王小明」「Alex」）。
- 代码注释用中文：类和公开函数写 KDoc，非显而易见的逻辑写注释；风格紧凑，不留死代码。
- 源码一律用编辑工具写入，不要用 shell heredoc / echo 生成 Kotlin 文件（反引号与 `$` 会被展开）。

---

## 5. 分阶段实施（每阶段单独提交）

每个阶段完成后：跑完整验证任务全部通过 → 提交（中文提交信息，作者用 `git -c user.name=yangyonghao -c user.email=2107290326@qq.com commit ...`，**不要推送**）→ 再进入下一阶段。

**阶段 1：Gradle 工程 + `:core:model`**
3.1、3.2。此阶段 `:app` 可以只有最小的 Activity，能 assemble 即可。

**阶段 2：`:core:designsystem` + `:app` 三个 tab**
3.3、3.4、3.5。

**阶段 3：文档**
3.6。

---

## 6. 测试分工（项目规则，务必遵守）

- **你必须自己完成**：单元测试、Robolectric / 截图测试、lint、debug 与 release 构建。每阶段结束都要亲自跑完整验证任务并全部通过；失败就自己修，不要把"请用户跑构建 / 单测"当成交付。
- **你不要自己做**：安装 APK 到手机、用 adb 操作手机。这些整理成清单交给用户。

完整验证任务：

```
:core:model:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease
```

电脑 A（Git Bash）：

```
cmd.exe //c "cd /d D:\AIProject\PengYouXia && set JAVA_HOME=D:\AndroidDev\jdk17\jdk-17.0.20.1+1&& gradlew.bat :core:model:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease"
```

Lint 要求 0 error；warning 能修的修掉，修不了的在汇报里列出原因。

---

## 7. 交付物（最终汇报格式）

1. **提交列表**：每个阶段的提交哈希与一句话说明。
2. **改动摘要**：按模块分组。
3. **与本文件的偏差**：没按本文件做的、或本文件没写到而你自行决定的地方，逐条列出并说明理由。
4. **自动化测试结果**：完整验证任务的执行结果（贴最后的 BUILD SUCCESSFUL 及任务列表）；lint 的 warning 情况；新增了哪些测试。
5. **截图清单**：`app/build/outputs/roborazzi/` 下每个 `m0_*.png` 的完整路径（审查方会逐张查看）。
6. **交给用户的真机测试清单**：编号的逐步操作，每步写清「操作 → 预期现象 → 不符合时请记录什么」。至少覆盖：
   - 安装 debug 包（给出 `adb.exe` 完整路径与命令，序列号见 `AGENTS.md`；小米需开「USB 安装」）；
   - 桌面图标与应用名是否正常；
   - 三个 tab 切换、空状态文案；
   - 手机切换深色模式后各页颜色是否正常；
   - 系统设置 → 应用 → 朋友侠 → 权限：确认没有任何权限项（尤其没有网络）。
7. **已知风险与未决问题**。

---

## 8. 不要做的事

- 不要推送到远程；不要修改 git config；不要改签名口令或放入签名文件。
- 不要做任何业务功能（数据库表、朋友编辑、提醒、备份），也不要为它们预留空类。
- 不要修改家人侠、守护侠两个参考工程里的任何文件。
- 不要删除或改写 `AGENTS.md` 中「测试分工（项目规则）」一节。
