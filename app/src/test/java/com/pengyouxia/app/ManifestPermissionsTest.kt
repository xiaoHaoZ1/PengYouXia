package com.pengyouxia.app

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 清单权限回归：朋友侠默认不需要任何系统权限。
 * 以后确实需要权限（如 POST_NOTIFICATIONS）时把它加进 [allowed]。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ManifestPermissionsTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    /**
     * 允许存在的权限白名单。
     * `*.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` 是 androidx.core 注入的
     * 应用私有签名级权限，不算对外索权。
     */
    private val allowed = setOf(
        "${context.packageName}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
    )

    @Test
    fun `不声明白名单以外的权限`() {
        val info = context.packageManager.getPackageInfo(
            context.packageName, PackageManager.GET_PERMISSIONS
        )
        val requested = info.requestedPermissions.orEmpty().toSet()
        assertEquals(emptySet<String>(), requested - allowed)
    }

    @Test
    fun `不声明 INTERNET`() {
        val info = context.packageManager.getPackageInfo(
            context.packageName, PackageManager.GET_PERMISSIONS
        )
        assertFalse(
            info.requestedPermissions.orEmpty().contains("android.permission.INTERNET")
        )
    }
}
