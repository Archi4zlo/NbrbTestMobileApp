package com.archi.tmpnces.di

import android.content.Context
import androidx.room.Room
import com.archi.tmpnces.data.local.AppDatabase
import com.archi.tmpnces.data.local.dao.RateDao
import com.archi.tmpnces.data.local.dao.RateDynamicsDao
import com.archi.tmpnces.data.local.migration.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
	
	@Provides
	@Singleton
	fun provideAppDatabase(
		@ApplicationContext context: Context
	): AppDatabase = Room.databaseBuilder(
		context, AppDatabase::class.java,
		AppDatabase.DATABASE_NAME,
	)
		.addMigrations(MIGRATION_1_2)
		.build()
	
	@Provides
	fun provideRateDao(database: AppDatabase): RateDao = database.rateDao()

	@Provides
	fun provideRateDynamicsDao(database: AppDatabase): RateDynamicsDao = database.rateDynamicsDao()
}