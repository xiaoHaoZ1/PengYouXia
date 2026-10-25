package com.pengyouxia.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pengyouxia.core.model.MemoCategory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 应用设置（DataStore）。 */
interface SettingsRepository {
    /** 上次记备忘使用的分类，默认「近况」。 */
    val lastMemoCategory: Flow<MemoCategory>

    suspend fun setLastMemoCategory(category: MemoCategory)
}

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private val keyLastMemoCategory = stringPreferencesKey("last_memo_category")

    override val lastMemoCategory: Flow<MemoCategory> =
        dataStore.data.map { prefs ->
            prefs[keyLastMemoCategory]
                ?.let { runCatching { MemoCategory.valueOf(it) }.getOrNull() }
                ?: MemoCategory.RECENT
        }

    override suspend fun setLastMemoCategory(category: MemoCategory) {
        dataStore.edit { it[keyLastMemoCategory] = category.name }
    }
}
