package com.archi.tmpnces.data.repository

import app.cash.turbine.test
import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.data.local.dao.RateDao
import com.archi.tmpnces.data.local.entity.RateEntity
import com.archi.tmpnces.data.remote.NbrbApiService
import com.archi.tmpnces.data.remote.dto.RateDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class RateRepositoryImplTest {
	
	private lateinit var api: NbrbApiService
	private lateinit var dao: RateDao
	private lateinit var repository: RateRepositoryImpl
	
	private val date = LocalDate.of(2026, 8, 31)
	
	private fun dto(
		curId: Int = 431,
		abbreviation: String = "USD",
		name: String = "Доллар США",
		scale: Int = 1,
		officialRate: Double = 3.1234
	) = RateDto(
		curId = curId,
		date = "2026-08-31T00:00:00",
		abbreviation = abbreviation,
		scale = scale,
		name = name,
		officialRate = officialRate
	)
	
	private fun entity(
		curId: Int = 431,
		abbreviation: String = "USD",
		name: String = "Доллар США",
		scale: Int = 1,
		officialRate: Double = 3.1234
	) = RateEntity(
		date = date, curId = curId, abbreviation = abbreviation, name = name, scale = scale, officialRate = officialRate
	)
	
	@Before
	fun setUp() {
		api = mockk()
		dao = mockk(relaxed = true)
		repository = RateRepositoryImpl(api, dao)
	}
	
	@Test
	fun `getRates возвращает данные из кэша и не ходит в сеть`() = runTest {
		coEvery { dao.getRatesByDate(date) } returns listOf(entity())
		
		val result = repository.getRates(date)
		
		assertTrue(result is Result.Success)
		assertEquals(1, (result as Result.Success).data.size)
		
		coVerify(exactly = 0) { api.getRates(any(), any()) }
	}
	
	@Test
	fun `getRates при пустом кэше идёт в сеть и сохраняет результат`() = runTest {
		coEvery { dao.getRatesByDate(date) } returns emptyList()
		coEvery { api.getRates(any(), any()) } returns listOf(dto(), dto(curId = 451, abbreviation = "EUR"))
		
		val result = repository.getRates(date)
		
		assertTrue(result is Result.Success)
		assertEquals(2, (result as Result.Success).data.size)
		
		coVerify(exactly = 1) { api.getRates(0, "2026-08-31") }
		coVerify(exactly = 1) { dao.insertRates(match { it.size == 2 }) }
	}
	
	@Test
	fun `getRates оборачивает ошибку сети в Result Error`() = runTest {
		coEvery { dao.getRatesByDate(date) } returns emptyList()
		coEvery { api.getRates(any(), any()) } throws IOException("Нет соединения")
		
		val result = repository.getRates(date)
		
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).exception is IOException)
		
		coVerify(exactly = 0) { dao.insertRates(any()) }
	}
	
	@Test
	fun `getRates корректно переводит DTO в доменную модель`() = runTest {
		coEvery { dao.getRatesByDate(date) } returns emptyList()
		coEvery { api.getRates(any(), any()) } returns listOf(
			dto(curId = 298, abbreviation = "RUB", name = "Российский рубль", scale = 100, officialRate = 3.4567)
		)
		
		val rate = (repository.getRates(date) as Result.Success).data.single()
		
		assertEquals(298, rate.curId)
		assertEquals("RUB", rate.abbreviation)
		assertEquals(date, rate.date)
		assertEquals(100, rate.scale)
		assertEquals(3.4567, rate.officialRate, 0.0)
		assertEquals(0.034567, rate.ratePerUnit, 1e-9)
	}
	
	@Test
	fun `refreshRates всегда обращается к сети минуя кэш`() = runTest {
		coEvery { api.getRates(any(), any()) } returns listOf(dto())
		
		val result = repository.refreshRates(date)
		
		assertTrue(result is Result.Success)
		coVerify(exactly = 1) { api.getRates(0, "2026-08-31") }
		coVerify(exactly = 1) { dao.insertRates(any()) }
		
		coVerify(exactly = 0) { dao.getRatesByDate(any()) }
	}
	
	@Test
	fun `refreshRates возвращает Result Error при сбое сети`() = runTest {
		coEvery { api.getRates(any(), any()) } throws IOException("Таймаут")
		
		val result = repository.refreshRates(date)
		
		assertTrue(result is Result.Error)
		coVerify(exactly = 0) { dao.insertRates(any()) }
	}
	
	@Test
	fun `observeRates отдаёт доменные модели из потока базы`() = runTest {
		every { dao.observeRatesByDate(date) } returns flowOf(
			listOf(entity(curId = 431, abbreviation = "USD"), entity(curId = 451, abbreviation = "EUR"))
		)
		
		repository.observeRates(date)
			.test {
				val rates = awaitItem()
				
				assertEquals(2, rates.size)
				assertEquals(listOf("USD", "EUR"), rates.map { it.abbreviation })
				
				awaitComplete()
			}
		
		coVerify(exactly = 0) { api.getRates(any(), any()) }
	}
}