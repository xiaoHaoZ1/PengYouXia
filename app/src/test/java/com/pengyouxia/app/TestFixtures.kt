package com.pengyouxia.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pengyouxia.app.data.FriendRepositoryImpl
import com.pengyouxia.app.data.IdGenerator
import com.pengyouxia.app.data.TimeSource
import com.pengyouxia.app.data.db.AppDatabase
import com.pengyouxia.app.data.settings.SettingsRepository
import com.pengyouxia.core.model.MemoCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** 测试基建：Main 调度器、可推进时间、递增 ID、内存仓库、内存设置。 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = StandardTestDispatcher()) :
    TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeTimeSource(var now: Long = 1_000L) : TimeSource {
    override fun now(): Long = now
}

class FakeIdGenerator(private var next: Int = 0) : IdGenerator {
    override fun newId(): String = "id-${++next}"
}

class FakeSettingsRepository(
    initial: MemoCategory = MemoCategory.RECENT,
) : SettingsRepository {
    private val flow = MutableStateFlow(initial)
    override val lastMemoCategory: Flow<MemoCategory> = flow
    override suspend fun setLastMemoCategory(category: MemoCategory) {
        flow.value = category
    }
}

fun newTestDb(): AppDatabase = Room.inMemoryDatabaseBuilder(
    ApplicationProvider.getApplicationContext(), AppDatabase::class.java
).allowMainThreadQueries().build()

fun newRepo(db: AppDatabase, time: TimeSource, ids: IdGenerator) =
    FriendRepositoryImpl(db, ids, time)
