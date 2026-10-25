package com.pengyouxia.app.screenshots

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import com.pengyouxia.app.navigation.AppNavHost
import com.pengyouxia.core.designsystem.theme.PyxTheme
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** M0 骨架验收截图：三个 tab × 浅/深色 + 组件样例页 × 浅/深色。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h915dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class M0ScreenshotsTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private fun shot(name: String) {
        compose.onRoot().captureRoboImage(
            File("build/outputs/roborazzi/$name.png").absolutePath
        )
    }

    private fun app(dark: Boolean, tabLabel: String? = null) {
        compose.setContent { PyxTheme(dark = dark) { AppNavHost() } }
        if (tabLabel != null) {
            // 底栏 item 文字不是 heading，用它与页头区分
            compose.onNode(hasText(tabLabel) and !isHeading()).performClick()
        }
        compose.waitForIdle()
    }

    @Test
    fun friends_light() {
        app(dark = false)
        shot("m0_friends_light")
    }

    @Test
    fun friends_dark() {
        app(dark = true)
        shot("m0_friends_dark")
    }

    @Test
    fun exchanges_light() {
        app(dark = false, tabLabel = "人情")
        shot("m0_exchanges_light")
    }

    @Test
    fun exchanges_dark() {
        app(dark = true, tabLabel = "人情")
        shot("m0_exchanges_dark")
    }

    @Test
    fun mine_light() {
        app(dark = false, tabLabel = "我的")
        shot("m0_mine_light")
    }

    @Test
    fun mine_dark() {
        app(dark = true, tabLabel = "我的")
        shot("m0_mine_dark")
    }

    @Test
    @Config(qualifiers = "w412dp-h1600dp-xxhdpi")
    fun components_light() {
        compose.setContent { PyxTheme(dark = false) { ComponentGallery() } }
        compose.waitForIdle()
        shot("m0_components_light")
    }

    @Test
    @Config(qualifiers = "w412dp-h1600dp-xxhdpi")
    fun components_dark() {
        compose.setContent { PyxTheme(dark = true) { ComponentGallery() } }
        compose.waitForIdle()
        shot("m0_components_dark")
    }
}
