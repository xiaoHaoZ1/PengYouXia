package com.pengyouxia.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.pengyouxia.app.data.FriendRepository
import com.pengyouxia.app.data.FriendRepositoryImpl
import com.pengyouxia.app.data.IdGenerator
import com.pengyouxia.app.data.SystemTimeSource
import com.pengyouxia.app.data.TimeSource
import com.pengyouxia.app.data.UuidIdGenerator
import com.pengyouxia.app.data.db.AppDatabase
import com.pengyouxia.app.data.settings.SettingsRepository
import com.pengyouxia.app.data.settings.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "pengyouxia.db")
            .build()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("settings") }
        )

    @Provides
    @Singleton
    fun provideIdGenerator(): IdGenerator = UuidIdGenerator()

    @Provides
    @Singleton
    fun provideTimeSource(): TimeSource = SystemTimeSource()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModule {
    @Binds
    @Singleton
    abstract fun bindFriendRepository(impl: FriendRepositoryImpl): FriendRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
