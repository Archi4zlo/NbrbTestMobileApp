package com.archi.tmpnces.data.repository

import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.core.util.runCatchingCancellable
import com.archi.tmpnces.data.local.dao.RateDao
import com.archi.tmpnces.data.local.entity.RateEntity
import com.archi.tmpnces.data.mapper.toDomainModels
import com.archi.tmpnces.data.mapper.toEntities
import com.archi.tmpnces.data.remote.NbrbApiService
import com.archi.tmpnces.domain.model.Rate
import com.archi.tmpnces.domain.repository.RateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

class RateRepositoryImpl @Inject constructor(
	private val api: NbrbApiService, private val dao: RateDao
) : RateRepository {
	
	override fun observeRates(date: LocalDate): Flow<List<Rate>> = dao.observeRatesByDate(date)
		.map { entities -> entities.toDomainModels() }
	
	override suspend fun refreshRates(date: LocalDate): Result<Unit> = runCatchingCancellable {
		fetchAndCache(date)
	}
	
	override suspend fun getRates(date: LocalDate): Result<List<Rate>> = runCatchingCancellable {
		val cached = dao.getRatesByDate(date)
		
		if (cached.isNotEmpty()) {
			Timber.d("Курсы на %s взяты из кэша (%d шт.)", date, cached.size)
			cached.toDomainModels()
		} else {
			Timber.d("Кэш на %s пуст, иду в сеть", date)
			fetchAndCache(date).toDomainModels()
		}
	}
	
	private suspend fun fetchAndCache(date: LocalDate): List<RateEntity> {
		Timber.d("Загружаю курсы на %s", date)
		
		val entities = api.getRates(onDate = date.toString())
			.toEntities()
		dao.insertRates(entities)
		
		Timber.d("Сохранено %d курсов на %s", entities.size, date)
		return entities
	}
}