package com.archi.tmpnces.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.archi.tmpnces.data.local.dao.RateDao
import com.archi.tmpnces.data.local.dao.RateDynamicsDao
import com.archi.tmpnces.data.local.entity.RateDynamicsEntity
import com.archi.tmpnces.data.local.entity.RateEntity

@Database(
	entities = [RateEntity::class, RateDynamicsEntity::class],
	version = 2,
	exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun rateDao(): RateDao
	abstract fun rateDynamicsDao(): RateDynamicsDao
	
	companion object {
		const val DATABASE_NAME = "tmpnces.db"
	}
}