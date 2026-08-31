package com.archi.tmpnces.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.archi.tmpnces.data.local.entity.RateEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface RateDao {
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertRates(rates: List<RateEntity>)
	@Query("SELECT * FROM rates WHERE date = :date ORDER BY name ASC")
	suspend fun getRatesByDate(date: LocalDate): List<RateEntity>
	@Query("SELECT * FROM rates WHERE date = :date ORDER BY name ASC")
	fun observeRatesByDate(date: LocalDate): Flow<List<RateEntity>>
	@Query("SELECT COUNT(*) FROM rates WHERE date = :date")
	suspend fun countByDate(date: LocalDate): Int
	@Query("SELECT * FROM rates WHERE date = :date AND abbreviation = :abbreviation LIMIT 1")
	suspend fun getRateByAbbreviation(date: LocalDate, abbreviation: String): RateEntity?
}