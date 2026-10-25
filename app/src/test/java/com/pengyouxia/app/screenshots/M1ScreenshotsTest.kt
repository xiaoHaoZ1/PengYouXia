package com.pengyouxia.app.screenshots

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.pengyouxia.app.MainActivity
import com.pengyouxia.app.data.FriendRepositoryImpl
import com.pengyouxia.app.data.SystemTimeSource
import com.pengyouxia.app.data.UuidIdGenerator
import com.pengyouxia.app.data.db.AppDatabase
import com.pengyouxia.app.data.demo.DemoDataSeeder
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** M1 截图：朋友列表/搜索/详情/编辑/备忘弹层/删除确认/我的页，浅深各一张。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class M1ScreenshotsTest {

    // 不自动启动 Activity：先往 pengyouxia.db 写演示数据，再启动 MainActivity
    @get:Rule
    val compose = createEmptyComposeRule()

    private fun shot(name: String) {
        compose.onRoot().captureRoboImage(
            File("build/outputs/roborazzi/$name.png").absolutePath
        )
    }

    /** 弹层/对话框在独立窗口，截全屏。 */
    @OptIn(ExperimentalRoborazziApi::class)
    private fun shotScreen(name: String) {
        captureScreenRoboImage(
            File("build/outputs/roborazzi/$name.png").absolutePath
        )
    }

    private fun waitText(text: String) {
        compose.waitUntil(15_000) {
            compose.onAllNodes(hasText(text, substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** 启动 App 前把 12 位演示朋友写进库文件，启动后即可见、无需等待。 */
    private fun launchSeeded() {
        val context = RuntimeEnvironment.getApplication()
        val db = Room.databaseBuilder(context, AppDatabase::class.java, "pengyouxia.db").build()
        runBlocking {
            DemoDataSeeder(FriendRepositoryImpl(db, UuidIdGenerator(), SystemTimeSource())).seed()
        }
        db.close()
        ActivityScenario.launch(MainActivity::class.java)
        waitText("王小明")
    }

    /** 不填数据直接启动（添加朋友空页、我的页用）。 */
    private fun launchEmpty() {
        ActivityScenario.launch(MainActivity::class.java)
        compose.waitForIdle()
    }

    /**
     * 进入触摸模式。注意：Robolectric 启动时不在触摸模式，首个输入框（朋友页搜索框）
     * 会拿到初始焦点，这一步并不能让它失焦，列表截图里的搜索框聚焦态是测试环境产物。
     */
    private fun touchMode() {
        compose.onRoot().performTouchInput {
            down(Offset(1f, 1f))
            up()
        }
    }

    private fun openSearch(text: String) {
        compose.onNode(hasText("搜名字、昵称、备忘")).performTextInput(text)
    }

    private fun assertNotExists(text: String) {
        org.junit.Assert.assertTrue(
            "不期望出现：$text",
            compose.onAllNodes(hasText(text, substring = true))
                .fetchSemanticsNodes().isEmpty()
        )
    }

    /** 等到某文字消失（防抖生效、结果替换列表的判据）。 */
    private fun waitGone(text: String) {
        compose.waitUntil(15_000) {
            compose.onAllNodes(hasText(text, substring = true))
                .fetchSemanticsNodes().isEmpty()
        }
    }

    /** 「lw」命中老王：先等全量列表退场（李雷消失），再断言。 */
    private fun verifyPinyinResult() {
        openSearch("lw")
        waitGone("李雷")
        waitText("老王")
        assertNotExists("王小明")
        compose.waitForIdle()
    }

    private fun gotoMine() {
        compose.onNode(hasText("我的") and !isHeading()).performClick()
        waitText("填充演示数据")
    }

    // ---------- 朋友列表 ----------
    @Test
    @Config(qualifiers = "w412dp-h2000dp-xxhdpi")
    fun friends_list_light() {
        launchSeeded(); touchMode(); compose.waitForIdle()
        shot("m1_friends_list_light")
    }

    @Test
    @Config(qualifiers = "w412dp-h2000dp-night-xxhdpi")
    fun friends_list_dark() {
        launchSeeded(); touchMode(); compose.waitForIdle()
        shot("m1_friends_list_dark")
    }

    // ---------- 搜索：拼音（等防抖出结果再截） ----------
    @Test
    @Config(qualifiers = "w412dp-h915dp-xxhdpi")
    fun search_pinyin_light() {
        launchSeeded(); touchMode(); verifyPinyinResult()
        shot("m1_search_pinyin_light")
    }

    @Test
    @Config(qualifiers = "w412dp-h915dp-night-xxhdpi")
    fun search_pinyin_dark() {
        launchSeeded(); touchMode(); verifyPinyinResult()
        shot("m1_search_pinyin_dark")
    }

    // ---------- 搜索：无结果 ----------
    @Test
    @Config(qualifiers = "w412dp-h915dp-xxhdpi")
    fun search_empty_light() {
        launchSeeded(); touchMode(); openSearch("张三丰")
        waitText("没有找到“张三丰”")
        compose.waitForIdle()
        shot("m1_search_empty_light")
    }

    @Test
    @Config(qualifiers = "w412dp-h915dp-night-xxhdpi")
    fun search_empty_dark() {
        launchSeeded(); touchMode(); openSearch("张三丰")
        waitText("没有找到“张三丰”")
        compose.waitForIdle()
        shot("m1_search_empty_dark")
    }

    // ---------- 备忘搜索（附赠，不计入要求） ----------
    @Test
    @Config(qualifiers = "w412dp-h915dp-xxhdpi")
    fun search_memo_light() {
        launchSeeded(); touchMode(); openSearch("露营")
        waitText("备忘·喜好")
        waitText("王小明")
        compose.waitForIdle()
        shot("m1_search_memo_light")
    }

    @Test
    @Config(qualifiers = "w412dp-h915dp-night-xxhdpi")
    fun search_memo_dark() {
        launchSeeded(); touchMode(); openSearch("露营")
        waitText("备忘·喜好")
        waitText("王小明")
        compose.waitForIdle()
        shot("m1_search_memo_dark")
    }

    // ---------- 朋友详情 ----------
    @Test
    @Config(qualifiers = "w412dp-h915dp-xxhdpi")
    fun friend_detail_light() {
        launchSeeded(); touchMode()
        openDetail()
        compose.waitForIdle()
        shot("m1_friend_detail_light")
    }

    @Test
    @Config(qualifiers = "w412dp-h915dp-night-xxhdpi")
    fun friend_detail_dark() {
        launchSeeded(); touchMode()
        openDetail()
        compose.waitForIdle()
        shot("m1_friend_detail_dark")
    }

    // ---------- 添加朋友（空页保持聚焦态） ----------
    @Test
    @Config(qualifiers = "w412dp-h915dp-xxhdpi")
    fun friend_add_light() {
        launchEmpty()
        compose.onNodeWithContentDescription("添加朋友").performClick()
        waitText("添加朋友")
        compose.waitForIdle()
        shot("m1_friend_add_light")
    }

    @Test
    @Config(qualifiers = "w412dp-h915dp-night-xxhdpi")
    fun friend_add_dark() {
        launchEmpty()
        compose.onNodeWithContentDescription("添加朋友").performClick()
        waitText("添加朋友")
        compose.waitForIdle()
        shot("m1_friend_add_dark")
    }

    // ---------- 编辑朋友 ----------
    @Test
    @Config(qualifiers = "w412dp-h915dp-xxhdpi")
    fun friend_edit_light() {
        launchSeeded(); touchMode(); openEdit()
        shot("m1_friend_edit_light")
    }

    @Test
    @Config(qualifiers = "w412dp-h915dp-night-xxhdpi")
    fun friend_edit_dark() {
        launchSeeded(); touchMode(); openEdit()
        shot("m1_friend_edit_dark")
    }

    // ---------- 记备忘弹层 ----------
    @Test
    @Config(qualifiers = "w412dp-h915dp-xxhdpi")
    fun memo_sheet_light() {
        launchSeeded(); touchMode(); openDetail()
        openSheet()
        shotScreen("m1_memo_sheet_light")
    }

    @Test
    @Config(qualifiers = "w412dp-h915dp-night-xxhdpi")
    fun memo_sheet_dark() {
        launchSeeded(); touchMode(); openDetail()
        openSheet()
        shotScreen("m1_memo_sheet_dark")
    }

    // ---------- 删除确认框 ----------
    @Test
    @Config(qualifiers = "w412dp-h915dp-xxhdpi")
    fun delete_confirm_light() {
        launchSeeded(); touchMode(); openEdit()
        compose.onNodeWithText("删除这位朋友").performClick()
        waitText("将同时删除 5 条备忘，无法恢复。")
        compose.waitForIdle()
        shotScreen("m1_delete_confirm_light")
    }

    @Test
    @Config(qualifiers = "w412dp-h915dp-night-xxhdpi")
    fun delete_confirm_dark() {
        launchSeeded(); touchMode(); openEdit()
        compose.onNodeWithText("删除这位朋友").performClick()
        waitText("将同时删除 5 条备忘，无法恢复。")
        compose.waitForIdle()
        shotScreen("m1_delete_confirm_dark")
    }

    // ---------- 我的页（测试工具） ----------
    @Test
    @Config(qualifiers = "w412dp-h915dp-xxhdpi")
    fun mine_light() {
        launchEmpty()
        gotoMine()
        compose.waitForIdle()
        shot("m1_mine_debug_light")
    }

    @Test
    @Config(qualifiers = "w412dp-h915dp-night-xxhdpi")
    fun mine_dark() {
        launchEmpty()
        gotoMine()
        compose.waitForIdle()
        shot("m1_mine_debug_dark")
    }

    private fun openDetail() {
        compose.onNodeWithText("王小明").performClick()
        waitText("记一条")
        waitText("降噪耳机")
    }

    private fun openSheet() {
        compose.onNodeWithText("记一条").performClick()
        waitText("家人")  // FilterChip 组
        compose.waitForIdle()
    }

    private fun openEdit() {
        openDetail()
        compose.onNodeWithText("编辑").performClick()
        waitText("编辑朋友")
        compose.waitForIdle()
    }
}
