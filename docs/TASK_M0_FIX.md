# 实施任务 M0-FIX：M0 评审返工

> 本文件是交给实施模型的完整任务说明。M0（`fc4e13f` / `683ac8e` / `657b474`）已通过评审的主体部分，
> 以下四项需要返工。开工前先读 `AGENTS.md` 与 `docs/PRODUCT.md` 第 9、10 节（本次评审已更新）。
> 本文件没写到的细节，按"简单、与现有代码习惯一致"自行决定，并在汇报里说明。

---

## 1. 配色对比度（必须）

**问题**（WCAG 对比度实测）：

| 组合 | 现值 | 要求 |
|---|---|---|
| 白字 on 暖橙 `#E8792F`（「今天」徽章） | 2.91 | ≥ 4.5 |
| 暖橙 `#E8792F` 文字 on 背景 `#F6F8F7`（「我给 ¥800」） | 2.73 | ≥ 4.5 |
| 白字 on 主色 `#0E8A7D`（主按钮） | 4.24 | ≥ 4.5 |
| 次要文字 `#6A7875` on 背景 `#F6F8F7` | 4.32 | ≥ 4.5 |

**改法**（按 `docs/PRODUCT.md` 第 10 节新表）：
- 浅色 primary 改为 `#0B7A6F`；浅色 textSecondary（`onSurfaceVariant`）改为 `#5F6E6B`。
- `PyxColors` 新增 `accentStrong` / `onAccentStrong`：浅色 `#B4500F` / `#FFFFFF`，深色 `#FFB07A` / `#4A2200`。
- `give`（我给）改为取 `accentStrong`；`CountdownBadge` 的「今天」改为 `accentStrong` 底 + `onAccentStrong` 字。
- `accent`（`#E8792F` / `#FFB07A`）保留，KDoc 写明"只做装饰（图标、描边、点缀），不得作为小字颜色或白字的底色"。
- `colorScheme.secondary` 是否改映射到 `accentStrong` 由你判断（目标：Material 组件默认用到 secondary 时也满足对比度），汇报里说明。

**新增单元测试** `ColorContrastTest`（放 `:app` 测试源集，与 `AvatarInitialTest` 同处）：
实现 WCAG 2.x 相对亮度与对比度计算，对**浅色、深色两套**逐一断言 ≥ 4.5：
- `onBackground` on `background`、`onSurface` on `surface`（卡片）
- `onSurfaceVariant` on `background`、on `surface`
- `onPrimary` on `primary`
- `onPrimaryContainer` on `primaryContainer`
- `give` on `background`、on `surface`；`receive` on `background`、on `surface`
- `onAccentStrong` on `accentStrong`
- `onSecondaryContainer` on `secondaryContainer`

断言失败信息里要写出组合名与实际比值。测试直接读 `LightColors` / `DarkColors` / `LightPyxColors` / `DarkPyxColors`
（`LightColors` / `DarkColors` 现为 `private`，可改为 `internal` 并通过合适方式让测试访问，或在 designsystem 暴露只读的方案对象；选一种并说明）。

## 2. 页头高度与左边距（必须）

**问题**（见 `m0_friends_light.png` 与 `m0_mine_light.png`）：
- 「朋友」「人情」页头有右侧「+」按钮（48dp），「我的」没有，导致标题纵向位置不同，切 tab 时标题会跳动。
- 页头标题左边距 20dp，页面内容（`SectionHeader`、卡片）左边距 16dp，「我的」页上标题与「生日提醒」没对齐。

**改法**：
- `PageHeader` 标题行固定高度 64dp（不含状态栏），标题垂直居中；有无 actions 标题位置一致。
- 在 designsystem 新增统一间距常量（如 `PyxDimens.pageHorizontal = 16.dp`），`PageHeader` 的起始边距与各页面内容的水平边距都用它。
- 右侧操作按钮的最右边缘与内容右边缘对齐的方式自行决定（`IconButton` 自带 12dp 内边距，注意视觉上「+」图形右缘对齐到 16dp）。

## 3. 系统备份规则（必须）

**问题**：当前 `allowBackup="false"` + 只排除 `root` 域。按官方文档，target 31+ 的应用在部分厂商设备上
`allowBackup="false"` **不会**禁止设备间迁移；而 `root` 域并不覆盖 `file` / `database` / `sharedpref` 等域，
所以实际行为因设备而异、与注释说的"排除全部数据"不符。

**改法**（与家人侠一致，见 `docs/PRODUCT.md` 第 9 节）：
- `android:allowBackup="true"`。
- `full_backup_content.xml`（Android 11 及以下）：排除全部数据域 —— `root`、`file`、`database`、`sharedpref`、`external`、
  `device_root`、`device_file`、`device_database`、`device_sharedpref`。
- `data_extraction_rules.xml`（Android 12+）：`<cloud-backup>` 排除上述全部数据域；`<device-transfer>` 不排除任何内容（换机带走全部数据）。
- 参照：`D:\AIProject\JiaRenXia\apps\android\app\src\main\res\xml\backup_rules.xml`、`data_extraction_rules.xml`（只读）。
- 注释写清楚"云备份全部排除、换机直连迁移保留"的理由。

## 4. 截图与文档（必须）

- 重新生成全部 `m0_*.png`；组件样例页「人情用色」一节同时展示：`我给 ¥800`（accentStrong）、`他给 ¥600`（receive），
  以及 accent 装饰色的一个色块示例（标注"仅装饰"）。
- `AGENTS.md` 无需大改；如本次有新的踩坑，补进「电脑 A 踩坑速查」。

---

## 5. 约束

- 沿用 `docs/TASK_M0_SKELETON.md` 第 4 节的全部架构硬约束。
- 不做任何业务功能；不改图标（图标留待用户看真机效果后再定）。
- 一次提交即可（中文提交信息，作者用 `git -c user.name=yangyonghao -c user.email=2107290326@qq.com commit ...`），**不要推送**。

## 6. 验证与测试分工

提交前亲自跑完整验证任务并全部通过（命令见 `AGENTS.md`「构建 / 测试」）：

```
:core:model:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease
```

不要用 adb 操作手机。

## 7. 汇报格式

1. 提交哈希与一句话说明。
2. 改动摘要（按文件）。
3. 与本文件的偏差及理由（含第 1 节 secondary 映射、测试访问方式的选择）。
4. 验证结果：BUILD SUCCESSFUL 输出；`ColorContrastTest` 各组合的实际比值表（浅 / 深）。
5. 重新生成的截图完整路径列表。
6. 真机清单增补（编号步骤，「操作 → 预期 → 不符合时记录什么」）：只需覆盖本次改动——
   切换三个 tab 时页头标题不跳动、与下方内容左对齐；深浅色下按钮文字清晰。
