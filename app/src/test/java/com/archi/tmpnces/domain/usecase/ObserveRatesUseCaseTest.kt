package com.archi.tmpnces.domain.usecase

import app.cash.turbine.test
import com.archi.tmpnces.domain.model.ChangeDirection
import com.archi.tmpnces.domain.model.Rate
import com.archi.tmpnces.domain.repository.RateRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ObserveRatesUseCaseTest {
	
	private lateinit var repository: RateRepository
	private lateinit var useCase: ObserveRatesUseCase
	
	private val today = LocalDate.of(2026, 8, 31)
	private val yesterday = LocalDate.of(2026, 8, 30)
	
	private fun rate(
		curId: Int = 431,
		date: LocalDate = today,
		abbreviation: String = "USD",
		name: String = "Доллар США",
		scale: Int = 1,
		officialRate: Double = 3.1234
	) = Rate(
		curId = curId,
		date = date,
		abbreviation = abbreviation,
		name = name,
		scale = scale,
		officialRate = officialRate,
	)
	
	private fun stubRepository(current: List<Rate>, previous: List<Rate>) {
		every { repository.observeRates(today) } returns flowOf(current)
		every { repository.observeRates(yesterday) } returns flowOf(previous)
	}
	
	@Before
	fun setUp() {
		repository = mockk()
		useCase = ObserveRatesUseCase(repository)
	}
	
	@Test
	fun `изменение считается как разница курсов за два дня`() = runTest {
		stubRepository(
			current = listOf(rate(officialRate = 3.1234)),
			previous = listOf(rate(date = yesterday, officialRate = 3.1000))
		)
		
		useCase(today).test {
			val item = awaitItem().single()
			
			assertEquals(0.0234, item.change!!, 1e-9)
			assertEquals(ChangeDirection.UP, item.direction)
			
			awaitComplete()
		}
	}
	
	@Test
	fun `падение курса отмечается направлением DOWN`() = runTest {
		stubRepository(
			current = listOf(rate(officialRate = 3.1000)),
			previous = listOf(rate(date = yesterday, officialRate = 3.1234))
		)
		
		useCase(today).test {
			val item = awaitItem().single()
			
			assertEquals(-0.0234, item.change!!, 1e-9)
			assertEquals(ChangeDirection.DOWN, item.direction)
			
			awaitComplete()
		}
	}
	
	@Test
	fun `без данных за вчера изменение равно null и направление UNKNOWN`() = runTest {
		stubRepository(
			current = listOf(rate()), previous = emptyList()
		)
		
		useCase(today).test {
			val item = awaitItem().single()
			
			assertNull(item.change)
			assertEquals(ChangeDirection.UNKNOWN, item.direction)
			
			awaitComplete()
		}
	}
	
	@Test
	fun `смена scale между днями не искажает изменение`() = runTest {
		stubRepository(
			current = listOf(rate(scale = 100, officialRate = 3.6000)),
			previous = listOf(rate(date = yesterday, scale = 1, officialRate = 0.0350))
		)
		
		useCase(today).test {
			val item = awaitItem().single()
			
			assertEquals(0.0010, item.change!!, 1e-9)
			assertEquals(ChangeDirection.UP, item.direction)
			
			awaitComplete()
		}
	}
	
	@Test
	fun `валюта находится по буквенному коду даже при смене curId`() = runTest {
		stubRepository(
			current = listOf(rate(curId = 456, abbreviation = "RUB", scale = 100, officialRate = 3.4567)),
			previous = listOf(
				rate(
					curId = 298, date = yesterday, abbreviation = "RUB", scale = 100, officialRate = 3.4000
				)
			)
		)
		
		useCase(today).test {
			val item = awaitItem().single()
			
			assertEquals(0.000567, item.change!!, 1e-9)
			
			awaitComplete()
		}
	}
	
	@Test
	fun `неизменившийся курс отмечается направлением UNCHANGED`() = runTest {
		stubRepository(
			current = listOf(rate(officialRate = 3.1234)),
			previous = listOf(rate(date = yesterday, officialRate = 3.1234))
		)
		
		useCase(today).test {
			assertEquals(ChangeDirection.UNCHANGED, awaitItem().single().direction)
			awaitComplete()
		}
	}
	
	@Test
	fun `каждая валюта сопоставляется со своей парой`() = runTest {
		stubRepository(
			current = listOf(
				rate(curId = 431, abbreviation = "USD", officialRate = 3.1234),
				rate(curId = 451, abbreviation = "EUR", officialRate = 3.5678)
			), previous = listOf(
				rate(curId = 451, date = yesterday, abbreviation = "EUR", officialRate = 3.5000),
				rate(curId = 431, date = yesterday, abbreviation = "USD", officialRate = 3.1000)
			)
		)
		
		useCase(today).test {
			val items = awaitItem().associateBy { it.rate.abbreviation }
			
			assertEquals(0.0234, items.getValue("USD").change!!, 1e-9)
			assertEquals(0.0678, items.getValue("EUR").change!!, 1e-9)
			
			awaitComplete()
		}
	}
	
	@Test
	fun `новая валюта без вчерашней пары не ломает остальной список`() = runTest {
		stubRepository(
			current = listOf(
				rate(curId = 431, abbreviation = "USD", officialRate = 3.1234),
				rate(curId = 999, abbreviation = "XXX", officialRate = 1.0000)
			),
			previous = listOf(rate(curId = 431, date = yesterday, abbreviation = "USD", officialRate = 3.1000)),
		)
		
		useCase(today).test {
			val items = awaitItem().associateBy { it.rate.abbreviation }
			
			assertEquals(0.0234, items.getValue("USD").change!!, 1e-9)
			assertNull(items.getValue("XXX").change)
			
			awaitComplete()
		}
	}

	@Test
	fun `валюта с большим scale не считается неизменившейся`() = runTest {
		stubRepository(
			current = listOf(rate(abbreviation = "VND", name = "Донгов", scale = 100000, officialRate = 11.7476)),
			previous = listOf(rate(date = yesterday, abbreviation = "VND", name = "Донгов", scale = 100000, officialRate = 11.7280))
		)
		
		useCase(today).test {
			val item = awaitItem().single()
			
			assertEquals(0.0196, item.changeInQuoteUnits!!, 1e-9)
			assertEquals(ChangeDirection.UP, item.direction)
			
			awaitComplete()
		}
	}
}