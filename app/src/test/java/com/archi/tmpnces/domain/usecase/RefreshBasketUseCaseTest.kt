package com.archi.tmpnces.domain.usecase

import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.domain.model.Rate
import com.archi.tmpnces.domain.repository.RateRepository
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class RefreshBasketUseCaseTest {
	
	private lateinit var repository: RateRepository
	private lateinit var useCase: RefreshBasketUseCase
	
	private val date = LocalDate.of(2023, 12, 21)
	private val previousDay = LocalDate.of(2023, 12, 20)
	private val yearStart = LocalDate.of(2022, 12, 31)
	
	private fun stubSuccess() {
		coEvery { repository.getRates(any()) } returns Result.Success(emptyList<Rate>())
		coEvery { repository.refreshRates(any()) } returns Result.Success(Unit)
	}
	
	@Before
	fun setUp() {
		repository = mockk()
		useCase = RefreshBasketUseCase(repository)
	}
	
	@Test
	fun `загружаются все три даты - выбранная, предыдущий день и конец прошлого года`() = runTest {
		stubSuccess()
		
		val result = useCase(date)
		
		assertTrue(result is Result.Success)
		coVerify(exactly = 1) { repository.getRates(previousDay) }
		coVerify(exactly = 1) { repository.getRates(yearStart) }
		coVerify(exactly = 1) { repository.refreshRates(date) }
	}
	
	@Test
	fun `базы сравнения берутся через кэш, а запрошенная дата обновляется`() = runTest {
		stubSuccess()
		
		useCase(date)
		
		coVerify(exactly = 0) { repository.refreshRates(previousDay) }
		coVerify(exactly = 0) { repository.refreshRates(yearStart) }
		coVerify(exactly = 0) { repository.getRates(date) }
	}
	
	@Test
	fun `сбой на базах сравнения не роняет обновление`() = runTest {
		coEvery { repository.getRates(any()) } returns Result.Error(IOException("Нет сети"))
		coEvery { repository.refreshRates(date) } returns Result.Success(Unit)
		
		val result = useCase(date)
		
		assertTrue(result is Result.Success)
		coVerify(exactly = 1) { repository.refreshRates(date) }
	}
	
	@Test
	fun `сбой на запрошенной дате возвращается наверх`() = runTest {
		coEvery { repository.getRates(any()) } returns Result.Success(emptyList<Rate>())
		coEvery { repository.refreshRates(date) } returns Result.Error(IOException("Таймаут"))
		
		val result = useCase(date)
		
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).exception is IOException)
	}
	
	@Test
	fun `базы сравнения загружаются до запрошенной даты`() = runTest {
		stubSuccess()
		
		useCase(date)
		
		coVerify(ordering = Ordering.ORDERED) {
			repository.getRates(previousDay)
			repository.getRates(yearStart)
			repository.refreshRates(date)
		}
	}
}