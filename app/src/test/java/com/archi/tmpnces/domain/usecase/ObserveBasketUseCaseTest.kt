package com.archi.tmpnces.domain.usecase

import app.cash.turbine.test
import com.archi.tmpnces.domain.model.Rate
import com.archi.tmpnces.domain.model.RubleTrend
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

class ObserveBasketUseCaseTest {
	
	private lateinit var repository: RateRepository
	private lateinit var useCase: ObserveBasketUseCase
	
	// Дата из макета
	private val date = LocalDate.of(2023, 12, 21)
	private val previousDay = LocalDate.of(2023, 12, 20)
	private val yearStart = LocalDate.of(2022, 12, 31)
	
	private fun rate(
		abbreviation: String, name: String, scale: Int, officialRate: Double, curId: Int = 1, onDate: LocalDate = date
	) = Rate(
		curId = curId,
		date = onDate,
		abbreviation = abbreviation,
		name = name,
		scale = scale,
		officialRate = officialRate
	)
	
	private fun mockupRates(onDate: LocalDate = date) = listOf(
		rate("RUB", "Российских рублей", 100, 3.5142, curId = 456, onDate = onDate),
		rate("USD", "Доллар США", 1, 3.1720, curId = 431, onDate = onDate),
		rate("CNY", "Китайских юаней", 10, 4.4403, curId = 508, onDate = onDate)
	)
	
	private fun stub(
		current: List<Rate>, previous: List<Rate> = emptyList(), yearStartRates: List<Rate> = emptyList()
	) {
		every { repository.observeRates(date) } returns flowOf(current)
		every { repository.observeRates(previousDay) } returns flowOf(previous)
		every { repository.observeRates(yearStart) } returns flowOf(yearStartRates)
	}
	
	@Before
	fun setUp() {
		repository = mockk()
		useCase = ObserveBasketUseCase(repository)
	}
	
	@Test
	fun `стоимость корзины совпадает со значением из макета ТЗ`() = runTest {
		stub(current = mockupRates())
		
		useCase(date).test {
			val basket = awaitItem()!!
			
			assertEquals(0.1748, basket.basket.value, 0.0001)
			
			awaitComplete()
		}
	}
	
	@Test
	fun `формула использует курс за единицу, а не за scale единиц`() = runTest {
		stub(current = mockupRates())
		
		useCase(date).test {
			val value = awaitItem()!!.basket.value
			
			assertEquals(true, value < 1.0)
			assertEquals(0.1748, value, 0.0001)
			
			awaitComplete()
		}
	}
	
	@Test
	fun `рост курса валюты показывается как ослабление рубля с минусом`() = runTest {
		stub(
			current = mockupRates(), yearStartRates = listOf(
				rate("RUB", "Российских рублей", 100, 3.7835, onDate = yearStart),
				rate("USD", "Доллар США", 1, 2.7364, onDate = yearStart),
				rate("CNY", "Китайских юаней", 10, 3.8618, onDate = yearStart)
			)
		)
		
		useCase(date).test {
			val usd = awaitItem()!!.components.first { it.abbreviation == "USD" }
			
			assertEquals(-15.92, usd.sinceYearStart!!.percent, 0.01)
			assertEquals(RubleTrend.WEAKENED, usd.sinceYearStart!!.trend)
			
			awaitComplete()
		}
	}
	
	@Test
	fun `падение курса валюты показывается как укрепление рубля с плюсом`() = runTest {
		stub(
			current = mockupRates(), yearStartRates = listOf(
				rate("RUB", "Российских рублей", 100, 3.7835, onDate = yearStart),
				rate("USD", "Доллар США", 1, 2.7364, onDate = yearStart),
				rate("CNY", "Китайских юаней", 10, 3.8618, onDate = yearStart)
			)
		)
		
		useCase(date).test {
			val rub = awaitItem()!!.components.first { it.abbreviation == "RUB" }
			
			assertEquals(7.12, rub.sinceYearStart!!.percent, 0.01)
			assertEquals(RubleTrend.STRENGTHENED, rub.sinceYearStart!!.trend)
			
			awaitComplete()
		}
	}
	
	@Test
	fun `порядок строк соответствует весам корзины RUB USD CNY`() = runTest {
		stub(current = mockupRates().reversed())
		
		useCase(date).test {
			assertEquals(
				listOf("RUB", "USD", "CNY"), awaitItem()!!.components.map { it.abbreviation })
			awaitComplete()
		}
	}
	
	@Test
	fun `подписи собираются из масштаба и названия как в макете`() = runTest {
		stub(current = mockupRates())
		
		useCase(date).test {
			assertEquals(
				listOf("100 Российских рублей", "1 Доллар США", "10 Китайских юаней"),
				awaitItem()!!.components.map { it.label })
			awaitComplete()
		}
	}
	
	@Test
	fun `официальный курс показывается за scale единиц без пересчёта`() = runTest {
		stub(current = mockupRates())
		
		useCase(date).test {
			val rub = awaitItem()!!.components.first { it.abbreviation == "RUB" }
			assertEquals(3.5142, rub.officialRate, 0.0)
			awaitComplete()
		}
	}
	
	@Test
	fun `без одной из трёх валют корзина не считается`() = runTest {
		stub(
			current = listOf(
				rate("RUB", "Российских рублей", 100, 3.5142), rate("USD", "Доллар США", 1, 3.1720)
				// CNY отсутствует
			)
		)
		
		useCase(date).test {
			assertNull(awaitItem())
			awaitComplete()
		}
	}
	
	@Test
	fun `без баз сравнения корзина считается, а проценты равны null`() = runTest {
		stub(current = mockupRates())
		
		useCase(date).test {
			val basket = awaitItem()!!
			
			assertEquals(0.1748, basket.basket.value, 0.0001)
			assertNull(basket.basket.sinceYearStart)
			assertNull(basket.basket.sincePreviousDay)
			assertNull(basket.components.first().sinceYearStart)
			
			awaitComplete()
		}
	}
	
	@Test
	fun `базой для годовой колонки служит 31 декабря предыдущего года`() {
		assertEquals(
			LocalDate.of(2022, 12, 31), ObserveBasketUseCase.yearStartOf(LocalDate.of(2023, 12, 21))
		)
		assertEquals(
			LocalDate.of(2025, 12, 31), ObserveBasketUseCase.yearStartOf(LocalDate.of(2026, 1, 3))
		)
	}
	
	@Test
	fun `веса корзины соответствуют документу НБРБ`() {
		assertEquals(
			listOf("RUB" to 0.6, "USD" to 0.3, "CNY" to 0.1), ObserveBasketUseCase.WEIGHTS
		)
	}
}