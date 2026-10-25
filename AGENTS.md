# 朋友侠（PengYouXia）— 开发约定

朋友侠开发仓库 xiaoHaoZ1/PengYouXia，2026-10-25 克隆时远端为空仓库（无任何提交），
模块结构、构建任务待代码落地后补充进本文档。开发约定与守护侠一致。

- 应用包名：`com.pengyouxia.app`（debug 加 `.debug`），应用名「朋友侠」。
- 设计事实来源：`docs/PRODUCT.md`；实施任务说明：`docs/TASK_*.md`（任务说明与设计冲突时以任务说明为准）。
- 开发方式：审查会话定设计、写任务说明、审查结果；实施模型按任务说明开工，
  每阶段跑完整验证、单独提交、不推送，最后按任务说明第 7 节格式汇报。

## 开发机环境

同一仓库在两台开发机可各有工作副本，路径与工具链不同。用 `hostname` 或仓库根路径区分当前机器。

### 电脑 A：`DESKTOP-3649QRQ`（Windows 10 + Git Bash / MINGW64）

- 仓库根：`D:\AIProject\PengYouXia`
- Android SDK：`D:\AndroidDev\sdk`（`adb` 在 `D:\AndroidDev\sdk\platform-tools\adb.exe`，不在 PATH）
  - 已验证（2026-10-25）：platforms `android-36`；build-tools `35.0.0` / `36.1.0`；adb 1.0.41（37.0.1-15733141）
- JDK：`D:\AndroidDev\jdk17\jdk-17.0.20.1+1`（Temurin 17.0.20.1，不在 PATH，构建前需 `set JAVA_HOME=...`）
- Gradle：独立安装 `D:\AndroidDev\gradle\gradle-8.13`（2026-10-25 已验证可运行）；`D:\AndroidDev\gradle.zip` 为 wrapper 缓存修复备份；wrapper 版本随工程定
- `local.properties`：工程落地后创建 `sdk.dir=D:/AndroidDev/sdk`（不入库）
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

- `.properties` 文件（如 `local.properties`）单反斜杠是转义符会被吞掉：`sdk.dir=D:\AndroidDev\sdk` 被解析成 `D:AndroidDevsdk`，AGP 报「文件名、目录名或卷标语法不正确」。写 `sdk.dir=D:/AndroidDev/sdk`（正斜杠免转义）或 `D:\\AndroidDev\\sdk`。
- Git Bash 里 `cmd.exe /c` 的 `/c` 被 MSYS 转成 `C:\`，命令静默不执行（只见 cmd 横幅）。必须写 `cmd.exe //c`。
- Gradle wrapper 缓存损坏报 `Cannot find JAR 'kotlin-compiler-embeddable-...'`：删 `%USERPROFILE%\.gradle\wrapper\dists\gradle-8.13-bin\<hash>\` 下损坏的 `gradle-8.13/`，把 `D:\AndroidDev\gradle.zip` 解压进去，再 `touch gradle-8.13-bin.zip.ok`。
- `adb`、`java` 不在 PATH：adb 用 `D:\AndroidDev\sdk\platform-tools\adb.exe`；构建命令前缀 `set JAVA_HOME=D:\AndroidDev\jdk17\jdk-17.0.20.1+1&&`（cmd 里 `set X=Y&&` 的 `&&` 前不能留空格，否则值会带尾部空格）。

## 模块结构

待代码落地后补充（参考守护侠：`:<app模块>` 只依赖 `:feature:*` 与 `:core:designsystem`）。

## 构建 / 测试

工程未落地，以下为与守护侠一致的通用约定：

- Gradle 根目录预期为仓库根目录。完整验证任务（单测 + lint + assembleDebug + assembleRelease）待模块确定后在此列出。
- 电脑 A（Git Bash）：`cmd.exe //c "cd /d D:\AIProject\PengYouXia && set JAVA_HOME=D:\AndroidDev\jdk17\jdk-17.0.20.1+1&& gradlew.bat <任务>"`
- `local.properties` 的 `sdk.dir` 指向本机 Android SDK（不入库）。
- `--tests` 模式经 `cmd.exe` 时不要加引号。
- APK 输出：`<app模块>/build/outputs/apk/`。
- 安装到设备：`adb.exe -s <序列号> install -r <apk路径>`；小米/HyperOS 需开「USB 安装」。

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
