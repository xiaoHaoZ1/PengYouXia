package com.pengyouxia.app.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pengyouxia.core.model.MemoCategory
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** lastMemoCategory：默认 RECENT、写入读回、未知值回退。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsRepositoryTest {

    private fun newDataStore(dir: File, scope: TestScope) =
        PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { File(dir, "settings.preferences_pb") },
        )

    private fun tempDir(): File = Files.createTempDirectory("ds-test").toFile()

    @Test
    fun `默认 RECENT`() = runTest {
        val r = SettingsRepositoryImpl(newDataStore(tempDir(), this))
        assertEquals(MemoCategory.RECENT, r.lastMemoCategory.first())
    }

    @Test
    fun `写入后读回`() = runTest {
        val r = SettingsRepositoryImpl(newDataStore(tempDir(), this))
        r.setLastMemoCategory(MemoCategory.WANTS)
        assertEquals(MemoCategory.WANTS, r.lastMemoCategory.first())
    }

    @Test
    fun `未知值回退 RECENT`() = runTest {
        val ds = newDataStore(tempDir(), this)
        // 直接写一个不合法的 name 进底层 DataStore（同一实例，避免多实例冲突）
        ds.edit { it[stringPreferencesKey("last_memo_category")] = "BOGUS" }
        val r = SettingsRepositoryImpl(ds)
        assertEquals(MemoCategory.RECENT, r.lastMemoCategory.first())
    }
}
