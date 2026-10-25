package com.pengyouxia.app.navigation

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.pengyouxia.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** 端到端：添加朋友 → 记备忘 → 返回列表 → 搜索 → 删除朋友回到空状态。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FriendFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun waitNode(matcher: androidx.compose.ui.test.SemanticsMatcher) {
        compose.waitUntil(5_000) {
            compose.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `完整朋友流程`() {
        // 空状态 → 添加第一位朋友
        waitNode(hasText("添加第一位朋友"))
        compose.onNodeWithText("添加第一位朋友").performClick()

        // 输入名字 → 保存
        waitNode(hasText("名字"))
        compose.onNodeWithText("名字").performTextInput("王小明")
        compose.onNodeWithText("保存").performClick()

        // 落在详情页（页头标题=朋友名）
        waitNode(hasText("记一条"))
        compose.onNodeWithText("记一条").performClick()

        // 选「喜好」→ 输入内容 → 保存
        waitNode(hasText("喜好"))
        compose.onNodeWithText("喜好").performClick()
        compose.onNodeWithText("").performTextInput("喜欢露营")
        compose.onNodeWithText("保存").performClick()

        // 详情里显示备忘
        waitNode(hasText("喜欢露营"))

        // 返回列表
        compose.onNodeWithContentDescription("返回").performClick()
        waitNode(hasText("王小明"))
        waitNode(hasText("喜欢露营"))

        // 搜索「露营」→ 命中（片段含分类前缀）
        compose.onNode(hasText("搜名字、昵称、备忘")).performTextInput("露营")
        waitNode(hasText("备忘·喜好", substring = true))
        waitNode(hasText("王小明"))

        // 清除搜索
        compose.onNodeWithContentDescription("清除").performClick()

        // 进详情 → 编辑 → 删除
        compose.onNodeWithText("王小明").performClick()
        waitNode(hasText("编辑"))
        compose.onNodeWithText("编辑").performClick()
        waitNode(hasText("删除这位朋友"))
        compose.onNodeWithText("删除这位朋友").performClick()
        waitNode(hasText("将同时删除 1 条备忘，无法恢复。"))
        compose.onNodeWithText("删除").performClick()

        // 回到空状态
        waitNode(hasText("还没有朋友"))
    }

    @Test
    fun `添加页有改动返回先确认`() {
        waitNode(hasText("添加第一位朋友"))
        compose.onNodeWithText("添加第一位朋友").performClick()
        waitNode(hasText("名字"))
        compose.onNodeWithText("名字").performTextInput("王小明")

        compose.onNodeWithContentDescription("返回").performClick()
        waitNode(hasText("放弃修改？"))
        compose.onNodeWithText("继续编辑").performClick()
        waitNode(hasText("名字"))

        compose.onNodeWithContentDescription("返回").performClick()
        waitNode(hasText("放弃修改？"))
        compose.onNodeWithText("放弃").performClick()
        waitNode(hasText("还没有朋友"))
    }
}
