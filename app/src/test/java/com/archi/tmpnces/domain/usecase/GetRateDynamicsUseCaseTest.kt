package com.archi.tmpnces.domain.usecase

import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.domain.model.CurrencyVersion
import com.archi.tmpnces.domain.model.RatePoint
import com.archi.tmpnces.domain.repository.RateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class GetRateDynamicsUseCaseTest {
	
	private lateinit var repository: RateRepository
	private lateinit var useCase: GetRateDynamicsUseCase
	
	private val rub190 = CurrencyVersion(
		curId = 190,
		parentId = 190,
		abbreviation = "RUB",
		name = "Российский рубль",
		scale = 1,
		validFrom = LocalDate.of(2003, 1, 1),
		validTo = LocalDate.of(2016, 6, 30)
	)
	private val rub298 = CurrencyVersion(
		curId = 298,
		parentId = 190,
		abbreviation = "RUB",
		name = "Российских рублей",
		scale = 100,
		validFrom = LocalDate.of(2016, 7, 1),
		validTo = LocalDate.of(2021, 7, 8)
	)
	private val rub456 = CurrencyVersion(
		curId = 456,
		parentId = 190,
		abbreviation = "RUB",
		name = "Российских рублей",
		scale = 100,
		validFrom = LocalDate.of(2021, 7, 9),
		validTo = LocalDate.of(2050, 1, 1)
	)
	private val usd431 = CurrencyVersion(
		curId = 431,
		parentId = 431,
		abbreviation = "USD",
		name = "Доллар США",
		scale = 1,
		validFrom = LocalDate.of(2016, 7, 1),
		validTo = LocalDate.of(2050, 1, 1)
	)
	
	private val allVersions = listOf(rub190, rub298, rub456, usd431)
	
	private fun point(day: Int, month: Int = 7, value: Double = 0.035) =
		RatePoint(LocalDate.of(2021, month, day), value)
	
	@Before
	fun setUp() {
		repository = mockk()
		useCase = GetRateDynamicsUseCase(repository)
		coEvery { repository.getCurrencyVersions() } returns Result.Success(allVersions)
	}
	
	@Test
	fun `период со сменой Cur_ID разбивается на два запроса по границе версий`() = runTest {
		val from = LocalDate.of(2021, 7, 1)
		val to = LocalDate.of(2021, 7, 31)
		
		coEvery { repository.getRateDynamics(rub298, any(), any()) } returns Result.Success(listOf(point(1), point(8)))
		coEvery { repository.getRateDynamics(rub456, any(), any()) } returns Result.Success(listOf(point(9), point(31)))
		
		val result = useCase("RUB", from, to)
		
		assertTrue(result is Result.Success)
		
		coVerify(exactly = 1) {
			repository.getRateDynamics(rub298, LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 8))
		}
		coVerify(exactly = 1) {
			repository.getRateDynamics(rub456, LocalDate.of(2021, 7, 9), LocalDate.of(2021, 7, 31))
		}
		
		coVerify(exactly = 0) { repository.getRateDynamics(rub190, any(), any()) }
	}
	
	@Test
	fun `точки обеих версий склеиваются в один упорядоченный список`() = runTest {
		coEvery { repository.getRateDynamics(rub298, any(), any()) } returns Result.Success(listOf(point(1), point(8)))
		coEvery { repository.getRateDynamics(rub456, any(), any()) } returns Result.Success(listOf(point(9), point(31)))
		
		val dynamics = (useCase("RUB", LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 31)) as Result.Success).data
		
		assertEquals(4, dynamics.points.size)
		assertEquals(
			listOf(1, 8, 9, 31), dynamics.points.map { it.date.dayOfMonth })
	}
	
	@Test
	fun `период внутри одной версии обходится одним запросом`() = runTest {
		val from = LocalDate.of(2021, 7, 10)
		val to = LocalDate.of(2021, 7, 20)
		
		coEvery { repository.getRateDynamics(rub456, any(), any()) } returns Result.Success(
			listOf(
				point(10),
				point(20)
			)
		)
		
		useCase("RUB", from, to)
		
		coVerify(exactly = 1) { repository.getRateDynamics(rub456, from, to) }
		coVerify(exactly = 0) { repository.getRateDynamics(rub298, any(), any()) }
	}
	
	@Test
	fun `версии находятся по буквенному коду, а не по ссылке друг на друга`() = runTest {
		assertEquals(190, rub298.parentId)
		assertEquals(190, rub456.parentId)
		assertEquals(rub298.parentId, rub456.parentId)
		
		coEvery { repository.getRateDynamics(any(), any(), any()) } returns Result.Success(listOf(point(1)))
		
		useCase("RUB", LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 31))
		
		coVerify(exactly = 1) { repository.getRateDynamics(rub298, any(), any()) }
		coVerify(exactly = 1) { repository.getRateDynamics(rub456, any(), any()) }
	}
	
	@Test
	fun `чужие валюты в выборку не попадают`() = runTest {
		coEvery { repository.getRateDynamics(any(), any(), any()) } returns Result.Success(emptyList())
		
		useCase("RUB", LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 31))
		
		coVerify(exactly = 0) { repository.getRateDynamics(usd431, any(), any()) }
	}
	
	@Test
	fun `период через несколько границ покрывается всеми версиями`() = runTest {
		coEvery { repository.getRateDynamics(any(), any(), any()) } returns Result.Success(emptyList())
		
		useCase("RUB", LocalDate.of(2015, 1, 1), LocalDate.of(2022, 1, 1))
		
		val versionSlot = mutableListOf<CurrencyVersion>()
		coVerify(exactly = 3) { repository.getRateDynamics(capture(versionSlot), any(), any()) }
		
		assertEquals(listOf(190, 298, 456), versionSlot.map { it.curId })
	}
	
	@Test
	fun `неизвестная валюта возвращает ошибку`() = runTest {
		val result = useCase("XXX", LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 31))
		
		assertTrue(result is Result.Error)
	}
	
	@Test
	fun `сбой загрузки справочника возвращается наверх`() = runTest {
		coEvery { repository.getCurrencyVersions() } returns Result.Error(IOException("Нет сети"))
		
		val result = useCase("RUB", LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 31))
		
		assertTrue(result is Result.Error)
		coVerify(exactly = 0) { repository.getRateDynamics(any(), any(), any()) }
	}
	
	@Test
	fun `сбой загрузки одного отрезка прекращает работу с ошибкой`() = runTest {
		coEvery { repository.getRateDynamics(rub298, any(), any()) } returns Result.Success(listOf(point(1)))
		coEvery { repository.getRateDynamics(rub456, any(), any()) } returns Result.Error(IOException("Таймаут"))
		
		val result = useCase("RUB", LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 31))
		
		assertTrue(result is Result.Error)
	}
}