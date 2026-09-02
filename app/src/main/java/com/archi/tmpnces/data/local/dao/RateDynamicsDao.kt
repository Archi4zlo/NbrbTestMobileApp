package com.archi.tmpnces.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.archi.tmpnces.data.local.entity.RateDynamicsEntity
import java.time.LocalDate

@Dao
interface RateDynamicsDao {
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertPoints(points: List<RateDynamicsEntity>)
	
	@Query(
		"""
		SELECT * FROM rate_dynamics
		WHERE curId = :curId AND date BETWEEN :from AND :to
		ORDER BY date ASC
		"""
	)
	suspend fun getPoints(curId: Int, from: LocalDate, to: LocalDate): List<RateDynamicsEntity>
	
	@Query("SELECT COUNT(*) FROM rate_dynamics WHERE curId = :curId AND date BETWEEN :from AND :to")
	suspend fun countPoints(curId: Int, from: LocalDate, to: LocalDate): Int
}