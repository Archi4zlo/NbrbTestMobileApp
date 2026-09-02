package com.archi.tmpnces.data.repository

import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.core.util.runCatchingCancellable
import com.archi.tmpnces.data.local.dao.RateDao
import com.archi.tmpnces.data.local.dao.RateDynamicsDao
import com.archi.tmpnces.data.local.entity.RateEntity
import com.archi.tmpnces.data.mapper.toDomain
import com.archi.tmpnces.data.mapper.toDomainModels
import com.archi.tmpnces.data.mapper.toEntities
import com.archi.tmpnces.data.mapper.toEntity
import com.archi.tmpnces.data.remote.NbrbApiService
import com.archi.tmpnces.domain.model.CurrencyVersion
import com.archi.tmpnces.domain.model.Rate
import com.archi.tmpnces.domain.model.RatePoint
import com.archi.tmpnces.domain.repository.RateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class RateRepositoryImpl @Inject constructor(
	private val api: NbrbApiService,
	private val dao: RateDao,
	private val dynamicsDao: RateDynamicsDao,
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
	
	override suspend fun getCurrencyVersions(): Result<List<CurrencyVersion>> = runCatchingCancellable {
		Timber.d("Загружаю справочник валют")
		api.getCurrencies()
			.map { it.toDomain() }
	}
	
	override suspend fun getRateDynamics(
		version: CurrencyVersion, from: LocalDate, to: LocalDate
	): Result<List<RatePoint>> = runCatchingCancellable {
		val cached = dynamicsDao.getPoints(version.curId, from, to)
		val expected = java.time.temporal.ChronoUnit.DAYS.between(from, to)
			.toInt() + 1
		
		if (cached.size >= expected) {
			Timber.d("Динамика %s (id %d) за %s..%s взята из кэша", version.abbreviation, version.curId, from, to)
			cached.map { it.toDomain(version.scale) }
		} else {
			Timber.d("Загружаю динамику %s (id %d) за %s..%s", version.abbreviation, version.curId, from, to)
			
			val entities = api.getRateDynamics(
				curId = version.curId, startDate = from.format(ApiDateFormat), endDate = to.format(ApiDateFormat)
			)
				.map { it.toEntity() }
			
			dynamicsDao.insertPoints(entities)
			entities.map { it.toDomain(version.scale) }
		}
	}
	
	private companion object {
		val ApiDateFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
	}
}