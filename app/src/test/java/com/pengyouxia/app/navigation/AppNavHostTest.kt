package com.pengyouxia.app.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pengyouxia.core.designsystem.theme.PyxTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** 三个一级 tab 的默认页与切换行为。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppNavHostTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private fun setContent() {
        compose.setContent { PyxTheme(dark = false) { AppNavHost() } }
    }

    @Test
    fun `默认停在朋友页`() {
        setContent()
        compose.waitForIdle()
        assertTrue(
            compose.onAllNodes(hasText("朋友") and isHeading()).fetchSemanticsNodes().isNotEmpty()
        )
        compose.onNodeWithText("还没有朋友").assertExists()
    }

    @Test
    fun `点底栏人情切到人情页`() {
        setContent()
        // 底栏 item 文字不是 heading，用它区分页头标题
        compose.onNode(hasText("人情") and !isHeading()).performClick()
        compose.waitForIdle()
        assertTrue(
            compose.onAllNodes(hasText("人情") and isHeading()).fetchSemanticsNodes().isNotEmpty()
        )
        compose.onNodeWithText("还没有往来记录").assertExists()
    }

    @Test
    fun `点底栏我的切到我的页`() {
        setContent()
        compose.onNode(hasText("我的") and !isHeading()).performClick()
        compose.waitForIdle()
        assertTrue(
            compose.onAllNodes(hasText("我的") and isHeading()).fetchSemanticsNodes().isNotEmpty()
        )
        compose.onNodeWithText("导出备份").assertExists()
    }
}
