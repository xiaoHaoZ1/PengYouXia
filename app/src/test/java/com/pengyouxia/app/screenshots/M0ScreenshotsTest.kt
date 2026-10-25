package com.pengyouxia.app.screenshots

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
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
