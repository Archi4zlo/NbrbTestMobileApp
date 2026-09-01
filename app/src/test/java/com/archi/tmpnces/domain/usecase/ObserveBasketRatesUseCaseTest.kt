package com.archi.tmpnces.domain.usecase

import app.cash.turbine.test
import com.archi.tmpnces.domain.model.Rate
import com.archi.tmpnces.domain.repository.RateRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ObserveBasketRatesUseCaseTest {
	
	private lateinit var repository: RateRepository
	private lateinit var useCase: ObserveBasketRatesUseCase
	
	private val date = LocalDate.of(2026, 8, 31)
	
	private fun rate(
		curId: Int, abbreviation: String, name: String, officialRate: Double = 3.0
	) = Rate(
		curId = curId, date = date, abbreviation = abbreviation, name = name, scale = 1, officialRate = officialRate
	)
	
	private val allRates = listOf(
		rate(440, "AUD", "Австралийский доллар"),
		rate(431, "USD", "Доллар США"),
		rate(451, "EUR", "Евро"),
		rate(456, "RUB", "Российский рубль"),
		rate(508, "CNY", "Китайский юань")
	)
	
	@Before
	fun setUp() {
		repository = mockk()
		useCase = ObserveBasketRatesUseCase(repository)
	}
	
	@Test
	fun `из общего списка остаются только валюты корзины`() = runTest {
		every { repository.observeRates(date) } returns flowOf(allRates)
		
		useCase(date).test {
			val basket = awaitItem()
			
			assertEquals(3, basket.size)
			assertTrue(basket.none { it.abbreviation == "AUD" })
			assertTrue(basket.none { it.abbreviation == "CNY" })
			
			awaitComplete()
		}
	}
	
	@Test
	fun `порядок соответствует корзине, а не сортировке базы`() = runTest {
		every { repository.observeRates(date) } returns flowOf(allRates)
		
		useCase(date).test {
			assertEquals(listOf("USD", "EUR", "RUB"), awaitItem().map { it.abbreviation })
			awaitComplete()
		}
	}
	
	@Test
	fun `отсутствующая валюта пропускается, остальные показываются`() = runTest {
		every { repository.observeRates(date) } returns flowOf(
			listOf(
				rate(431, "USD", "Доллар США"),
                rate(456, "RUB", "Российский рубль"),
			)
		)
		
		useCase(date).test {
			assertEquals(
				listOf("USD", "RUB"), awaitItem().map { it.abbreviation })
			awaitComplete()
		}
	}
	
	@Test
	fun `пустая база даёт пустую корзину без падения`() = runTest {
		every { repository.observeRates(date) } returns flowOf(emptyList())
		
		useCase(date).test {
			assertTrue(awaitItem().isEmpty())
			awaitComplete()
		}
	}
}