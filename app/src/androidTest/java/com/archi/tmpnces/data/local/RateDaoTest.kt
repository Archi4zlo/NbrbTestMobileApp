package com.archi.tmpnces.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.archi.tmpnces.data.local.dao.RateDao
import com.archi.tmpnces.data.local.entity.RateEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RateDaoTest {
	
	private lateinit var database: AppDatabase
	private lateinit var dao: RateDao
	
	private val today = LocalDate.of(2026, 8, 31)
	private val yesterday = LocalDate.of(2026, 8, 30)
	
	private fun rate(
		date: LocalDate = today,
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
		database = Room.inMemoryDatabaseBuilder(
			ApplicationProvider.getApplicationContext(), AppDatabase::class.java
		)
			.build()
		dao = database.rateDao()
	}
	
	@After
	fun tearDown() {
		database.close()
	}
	
	@Test
	fun insertRates_thenReadByDate_returnsInserted() = runTest {
		val rates = listOf(
			rate(curId = 431, abbreviation = "USD", name = "Доллар США"),
			rate(curId = 451, abbreviation = "EUR", name = "Евро")
		)
		
		dao.insertRates(rates)
		val result = dao.getRatesByDate(today)
		
		assertEquals(2, result.size)
	}
	
	@Test
	fun getRatesByDate_sortsByNameAscending() = runTest {
		dao.insertRates(
			listOf(
				rate(curId = 431, abbreviation = "USD", name = "Доллар США"),
				rate(curId = 456, abbreviation = "RUB", name = "Российский рубль"),
				rate(curId = 451, abbreviation = "EUR", name = "Евро")
			)
		)
		
		val names = dao.getRatesByDate(today)
			.map { it.name }
		
		assertEquals(listOf("Доллар США", "Евро", "Российский рубль"), names)
	}
	
	@Test
	fun insertRates_withSameKey_replacesInsteadOfDuplicating() = runTest {
		dao.insertRates(listOf(rate(officialRate = 3.1234)))
		dao.insertRates(listOf(rate(officialRate = 3.9999)))
		
		val result = dao.getRatesByDate(today)
		
		assertEquals(1, result.size)
		assertEquals(3.9999, result.first().officialRate, 0.0)
	}
	
	@Test
	fun ratesForDifferentDates_areStoredIndependently() = runTest {
		dao.insertRates(listOf(rate(date = today, officialRate = 3.1234)))
		dao.insertRates(listOf(rate(date = yesterday, officialRate = 3.1000)))
		
		assertEquals(
			3.1234,
			dao.getRatesByDate(today)
				.single().officialRate,
			0.0
		)
		assertEquals(
			3.1000,
			dao.getRatesByDate(yesterday)
				.single().officialRate,
			0.0
		)
	}
	
	@Test
	fun countByDate_reflectsStoredRows() = runTest {
		assertEquals(0, dao.countByDate(today))
		
		dao.insertRates(
			listOf(
				rate(curId = 431, abbreviation = "USD"), rate(curId = 451, abbreviation = "EUR")
			)
		)
		
		assertEquals(2, dao.countByDate(today))
		assertEquals(0, dao.countByDate(yesterday))
	}
	
	@Test
	fun getRateByAbbreviation_findsMatchingCurrency() = runTest {
		dao.insertRates(
			listOf(
				rate(curId = 431, abbreviation = "USD", officialRate = 3.1234),
				rate(curId = 451, abbreviation = "EUR", officialRate = 3.5678)
			)
		)
		
		val eur = dao.getRateByAbbreviation(today, "EUR")
		
		assertEquals(3.5678, eur!!.officialRate, 0.0)
	}
	
	@Test
	fun getRateByAbbreviation_returnsNullWhenAbsent() = runTest {
		dao.insertRates(listOf(rate(abbreviation = "USD")))
		
		assertNull(dao.getRateByAbbreviation(today, "GBP"))
		assertNull(dao.getRateByAbbreviation(yesterday, "USD"))
	}
	
	@Test
	fun observeRatesByDate_emitsAgainWhenDataChanges() = runTest {
		dao.observeRatesByDate(today)
			.test {
				assertEquals(emptyList<RateEntity>(), awaitItem())
				
				dao.insertRates(listOf(rate(curId = 431, abbreviation = "USD")))
				assertEquals(1, awaitItem().size)
				
				dao.insertRates(listOf(rate(curId = 451, abbreviation = "EUR")))
				assertEquals(2, awaitItem().size)
				
				cancelAndIgnoreRemainingEvents()
			}
	}
}