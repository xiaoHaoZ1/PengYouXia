# 实施任务 M1：朋友档案 + 备忘 + 搜索

> 本文件是交给实施模型的完整任务说明。开工前先通读本文件和下列必读文件，
> 有冲突时以本文件为准；本文件没写到的细节，按"简单、可维护、与家人侠习惯一致"的原则自行决定，并在最终汇报里说明。
> 本批不做：生日（M2）、人情往来的界面与汇总（M3）、备份（M4）。

---

## 0. 开工前必读

1. `AGENTS.md`（尤其「测试分工（项目规则）」「Git / 提交」）
2. `docs/PRODUCT.md` 第 2、4.1–4.3、4.6、5、8 节
3. 现有代码：`core/model/`（5 个枚举）、`core/designsystem/`（`PyxTheme`、`PageHeader`、`PyxCard`、`SectionHeader`、`EmptyState`、`FriendAvatar`、`PyxDimens`）、
   `app/.../navigation/AppNavHost.kt`、`app/.../ui/`、`app/src/test/`
4. 家人侠参考（只读，不得修改；路径相对 `D:\AIProject\JiaRenXia\apps\android\app\src\main\java\io\github\xiaohaoz1\jiarenxia\`）：
   - `domain/pinyin/PinyinIndex.kt`（拼音，本批复制改造）、`domain/PersonSearch.kt`（搜索写法）、`domain/FamilyOrdering.kt`（中文 Collator 排序）
   - `data/db/`（`AppDatabase`、`Entities`、`Daos`、`Mappers`）、`data/FamilyRepository*.kt`、`data/FamilyResult.kt`、`data/IdGenerator.kt`、`data/TimeSource.kt`
   - `di/AppModule.kt`、`app/build.gradle.kts` 里 Room schema 导出（`ksp { arg("room.schemaLocation", …) }`）的配置

---

## 1. 目标

完成后在手机上能：添加朋友 → 进朋友详情 → 记各类备忘 → 编辑 / 删除备忘、勾"已送出" → 编辑 / 删除朋友；
首页按拼音分组列出朋友，能按名字、昵称、拼音、怎么认识的、备忘内容搜索。debug 包能一键填充演示数据。

---

## 2. 需求规格

### 2.1 领域模型（`:core:model`，纯 Kotlin）

- `Friend(id, name, nickname?, metNote?, birthday: Birthday?, createdAt, updatedAt)`
- `Birthday(calendar: CalendarType, year: Int?, month: Int, day: Int, leapMonth: Boolean)`：构造时校验 month 1..12；
  公历 day 1..31、农历 day 1..30；`leapMonth` 只允许农历。（日期是否真实存在、农历换算在 M2 做；本批不做生日界面，但模型和表结构先定下来。）
- `Memo(id, friendId, category: MemoCategory, content, done, createdAt, updatedAt)`
- 输入规范化放在纯函数里并单测：名字、昵称、怎么认识的、备忘内容一律 `trim`；昵称 / 怎么认识的 trim 后为空存 null；
  长度上限：名字 30、昵称 30、怎么认识的 100、备忘 500（按 Unicode 码点数计）。

### 2.2 数据库（Room，`:app` 的 `data/db/`）

- `AppDatabase` 版本 **1**，`exportSchema = true`，schema 导出到 `app/schemas/` 并**入库**。
- 三张表**一次建全**，严格按 `docs/PRODUCT.md` 第 5 节（`sort_key` 已从设计中删除，不要建）：`friends`、`memos`、`exchanges`。
  `exchanges` 本批只用于"删除朋友时显示将删除几条往来"与级联删除；M3 再写它的业务。这样 M2、M3 不需要改表。
- 外键：`memos.friend_id`、`exchanges.friend_id` → `friends.id` 级联删除；`exchanges.source_memo_id` → `memos.id` 置空。外键列都建索引。
- 枚举以 `name` 字符串存储（`MemoCategory` 等），读到未知值时不能崩溃：备忘归入 `OTHER` 并记日志。
- **禁止** `fallbackToDestructiveMigration*`。
- 时间与 ID 经可注入的 `TimeSource` / `IdGenerator`（UUID）产生，测试里可替换。

### 2.3 仓库（`data/FriendRepository`）

接口 + 实现，是**唯一写入路径**；多表写入用 `db.withTransaction`。返回值用密封结果类型（参考家人侠 `FamilyResult`），校验失败返回错误而不是抛异常。

- 查询（`Flow`）：全部朋友（附带每人最新一条备忘的内容，用于列表第二行）；单个朋友详情（朋友 + 全部备忘）；删除某朋友时的影响计数（备忘数、往来数）。
- 写入：新增 / 修改 / 删除朋友；新增 / 修改 / 删除备忘；设置「想要」备忘的已送出。
- 规则：
  - 名字规范化后为空 → 错误。重名允许（朋友里可能有两个"小王"）。
  - 修改备忘可以改分类；从 `WANTS` 改成别的分类时 `done` 清为 false。
  - `done` 只对 `WANTS` 有效，对其他分类设置返回错误。
  - 任何写入都更新对应行的 `updatedAt`；写备忘同时更新所属朋友的 `updatedAt`。

设置（DataStore，`data/settings/`）：`lastMemoCategory`（默认 `RECENT`），保存备忘后更新为本次使用的分类。

### 2.4 排序、分组与搜索（`domain/`）

- `PinyinIndex`：从家人侠复制，包名改为本工程，保持 API 29+ 判断与 `Han-Latin/Names` 优先的逻辑。
- `FriendOrdering`：按名字用 `Collator.getInstance(Locale.CHINA)` 排序；分组键 = 名字拼音全拼首字母（A–Z），其他（数字、表情、拼音不可用的字符）归 `#`，`#` 组排最后。
  拼音不可用（API < 29）时不分组：返回单一无标题分组。
- `FriendSearch`：
  - 查询 `trim` + 小写；空白查询返回空列表（界面显示正常列表，不显示搜索结果）。
  - 文字子串匹配：名字、昵称、怎么认识的、备忘内容（不分大小写）。
  - 查询为纯 ASCII 字母（忽略空格）时，再对名字、昵称做拼音匹配：全拼包含，或声母缩写以查询开头（`lw` 命中「老王」，`laowang` 命中「老王」）。
  - 结果每个朋友一条，按 `FriendOrdering` 排序；带"命中片段"：名字 / 昵称命中时片段为空（界面显示普通第二行）；
    否则取第一个命中字段：「怎么认识的：…」或「备忘·喜好：…」，内容截取命中处前后各约 12 个字符，前后被截断时加「…」，并给出高亮区间。
  - 本批不搜往来（M3 加入）。

### 2.5 界面与导航

**导航**：新增路由 朋友详情 `friend/{id}`、添加朋友 `friend/new`、编辑朋友 `friend/{id}/edit`。二级页**隐藏底栏**。
添加成功后用详情页**替换**添加页（返回直接回列表）。删除朋友成功后回到列表。

**`PageHeader` 扩展**：增加可选 `onBack`（左侧返回箭头，`contentDescription = "返回"`）。有返回箭头时标题紧随其后；
没有时保持现在的 16dp 对齐与 64dp 高度不变。

**朋友页（首页）**（`docs/PRODUCT.md` 4.1，本批不含"近期生日"）：
- 页头「朋友」+「+」→ 添加朋友。
- 搜索框：占位「搜名字、昵称、备忘」；右侧有清除按钮；输入时下方换成搜索结果。搜索在 ViewModel 里做，输入防抖约 150ms。
- 列表：字母分组标题 + 行（`FriendAvatar` 40dp、名字 + 灰色昵称、第二行灰色单行截断：最新备忘 → 怎么认识的 → 不显示）。点击进详情。
- 搜索结果：同样的行，第二行换成命中片段（命中词用主色加粗）。无结果：「没有找到“xxx”」。
- 空状态：保持 M0 文案，按钮「添加第一位朋友」→ 添加朋友。

**添加 / 编辑朋友**（4.2，本批不含生日）：
- 页头：返回 + 标题「添加朋友」/「编辑朋友」+ 右侧「保存」文字按钮（名字为空时禁用）。
- 字段：名字（必填，自动获取焦点并弹出键盘）、昵称、怎么认识的（单行）。超长时输入框下方提示并阻止保存。
- 有未保存改动时按返回 → 确认框「放弃修改？」（放弃 / 继续编辑）。
- 编辑页底部「删除这位朋友」（error 色文字按钮）→ 确认框正文「将同时删除 N 条备忘、M 条往来记录，无法恢复。」，为 0 的项不写；按钮「删除」（error 色）/「取消」。

**朋友详情**（4.3，本批不含生日行与往来区块）：
- 页头：返回 + 朋友名字 + 右侧「编辑」。
- 头部卡：`FriendAvatar` 56dp、名字、灰色昵称、怎么认识的。
- 「备忘」区块：按 家人 → 喜好 → 想要 → 近况 → 其他 分组，没有内容的分类不显示；组内按 `createdAt` 倒序（新的在上），
  「想要」组里已送出的排到组末、灰显加删除线。每条：内容（多行完整显示）+ 灰色日期（今年显示「10月25日」，往年「2025年10月25日」）。
  「想要」每条右侧一个勾选框（`contentDescription`「已送出」），点勾选框只切换状态，不打开编辑。
- 没有任何备忘时区块内显示「还没有备忘，记下他的家人、喜好、想要的东西吧」。
- 区块底部「记一条」按钮 → 记备忘弹层。

**记备忘弹层**（`ModalBottomSheet`）：
- 顶部 5 个分类胶囊（单选），默认 `lastMemoCategory`；输入框多行（最少 3 行高），自动获取焦点；「保存」按钮（内容为空禁用）。
- 点已有备忘 → 同一弹层预填内容与分类，额外显示「删除」（确认框「删除这条备忘？」）。
- 保存后弹层关闭，列表即时更新。

**我的页**：新增「测试工具」分组，**只在 debug 包显示**（`BuildConfig.DEBUG`）：
- 「填充演示数据」：追加约 12 位合成朋友（含中文名、英文名 `Alex`、表情开头的名字、`#` 组的数字开头名字、重名两位「小王」），
  各类备忘齐全（含已送出 / 未送出的「想要」、超长备忘一条）。完成后 Toast「已添加 N 位演示朋友」。
- 「清空全部数据」：确认框后清空三张表（不清设置）。
- 演示数据里不得出现真实人名；名字示例：王小明、李雷、韩梅梅、陈晓、Alex、😀小明、3班老同学、小王（两位）等。
- 其他「即将推出」占位行保持不变。

### 2.6 M0 遗留的两处小调整

- `LightColors` / `DarkColors` 重命名为 `PyxLightColorScheme` / `PyxDarkColorScheme`（public 的通用名容易与其他库冲突），同步测试。
- 删除不再使用的 `ui/ComingSoon.kt` 用法中已被真功能替代的部分；仍有占位行（提醒设置、导出备份、从备份恢复）则保留该工具。

---

## 3. 测试

- `:core:model`：`Birthday` 校验边界；规范化与长度上限（含表情、生僻字按码点计数）。
- `:app`（Robolectric，`@Config(sdk = [34])`）：
  1. **仓库**（in-memory Room）：增删改朋友与备忘；删除朋友级联删除备忘与往来（往来行在测试里直接用 DAO 插入）；影响计数；
     改分类清 `done`；对非 `WANTS` 设 `done` 返回错误；`updatedAt` 更新；未知枚举值读取不崩溃。
  2. **`FriendOrdering`**：拼音分组与组内顺序、`#` 组在最后；拼音不可用时单一分组（把"拼音是否可用"做成可注入，便于测试）。
  3. **`FriendSearch`**：中文子串、昵称、`lw` / `laowang`、大小写、英文名、怎么认识的、备忘片段截取与「…」、高亮区间、空白查询。
  4. **ViewModel**：编辑页名字为空不可保存、未保存改动判定；记备忘默认分类取自设置、保存后更新设置。
  5. **端到端导航**（Compose UI 测试）：空状态点「添加第一位朋友」→ 输入名字 → 保存 → 落在详情页 → 记一条备忘 → 返回列表能看到该朋友与备忘摘要 → 搜索能搜到 → 编辑页删除 → 回到空状态。
  6. **截图**（Roborazzi，前缀 `m1_`，浅 / 深各一张，使用演示数据）：朋友列表、搜索结果（拼音查询）、无搜索结果、朋友详情（含已送出的「想要」）、
     添加朋友（空）、编辑朋友（含删除按钮）、记备忘弹层、删除确认框、我的页（debug 测试工具可见）。
  7. `ManifestPermissionsTest` 与 `ColorContrastTest` 保持通过。
- M0 的 `m0_*` 截图可以删除或保留，汇报中说明。

---

## 4. 架构硬约束（违反即返工）

- 沿用 `docs/TASK_M0_SKELETON.md` 第 4 节全部约束（不联网、不新增第三方依赖、不硬编码颜色、合成名字、中文注释、不用 heredoc 写源码）。
- 仓库是唯一写入路径；Composable 里不写业务规则；ViewModel + `StateFlow`；DAO 不直接暴露给界面。
- Room schema 文件入库；禁止破坏性迁移。
- 新增依赖只能来自现有版本目录（Room、DataStore、hilt-navigation-compose、lifecycle、coroutines-test、room-testing 等）。

---

## 5. 分阶段实施（每阶段单独提交，不推送）

每个阶段完成后：跑完整验证任务全部通过 → 提交（中文提交信息，作者 `git -c user.name=yangyonghao -c user.email=2107290326@qq.com commit ...`）→ 进入下一阶段。

1. **数据与领域**：2.1–2.4 + 对应单测；2.6 的重命名。
2. **界面与导航**：2.5（不含测试工具）+ ViewModel 测试、端到端导航测试。
3. **测试工具、截图与文档**：2.5 的测试工具 + 截图测试；更新 `AGENTS.md`（模块与包结构、数据库与 schema 位置、截图前缀）。
   如实施中发现设计问题，在汇报中提出，**不要**自行修改 `docs/PRODUCT.md`。

---

## 6. 验证与测试分工

完整验证任务（命令见 `AGENTS.md`「构建 / 测试」）：

```
:core:model:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease
```

- 你必须自己跑完并全部通过（lint 0 error）。
- 不要用 adb 操作手机。release 包已开启 R8：确认 Room / Hilt 在 release 构建下无需额外 keep 规则即可通过构建，如需要请说明。

---

## 7. 交付物（最终汇报格式）

1. 提交列表（哈希 + 一句话）。
2. 改动摘要（按模块 / 包分组）。
3. 与本文件的偏差及理由；发现的设计问题。
4. 自动化测试结果：BUILD SUCCESSFUL 输出、测试数量与新增测试清单、lint 情况。
5. 截图清单（`m1_*.png` 完整路径）。
6. 交给用户的真机测试清单（编号；「操作 → 预期 → 不符合时记录什么」），至少覆盖：
   - 覆盖安装 debug 包后应用正常打开（M0 装过的直接 `install -r`）；
   - 我的 → 测试工具 → 填充演示数据，回到朋友页看分组与第二行摘要；
   - 搜索：中文、拼音全拼、声母缩写、备忘里的词；清除搜索；
   - 添加朋友、未保存返回的确认、保存后落在详情页；
   - 记备忘（每类至少一条）、默认分类记住上次、编辑、删除、「想要」勾已送出；
   - 编辑朋友、删除朋友（确认框里的数量是否正确）；
   - 杀掉应用后重新打开，数据仍在；
   - 深色模式下各页面可读；
   - 我的 → 测试工具 → 清空全部数据。
7. 已知风险与未决问题。

---

## 8. 不要做的事

- 不要推送；不要修改 git config；不要改签名配置。
- 不要做生日编辑、近期生日、提醒、往来界面、备份。
- 不要修改家人侠、守护侠工程。
- 不要删除或改写 `AGENTS.md` 中「测试分工（项目规则）」一节。
