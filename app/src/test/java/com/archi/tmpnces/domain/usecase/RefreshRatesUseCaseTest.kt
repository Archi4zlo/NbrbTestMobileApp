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

class RefreshRatesUseCaseTest {
	
	private lateinit var repository: RateRepository
	private lateinit var useCase: RefreshRatesUseCase
	
	private val today = LocalDate.of(2026, 8, 31)
	private val yesterday = LocalDate.of(2026, 8, 30)
	
	@Before
	fun setUp() {
		repository = mockk()
		useCase = RefreshRatesUseCase(repository)
	}
	
	@Test
	fun `загружаются обе даты - запрошенная и предыдущая`() = runTest {
		coEvery { repository.getRates(yesterday) } returns Result.Success(emptyList<Rate>())
		coEvery { repository.refreshRates(today) } returns Result.Success(Unit)
		
		val result = useCase(today)
		
		assertTrue(result is Result.Success)
		coVerify(exactly = 1) { repository.getRates(yesterday) }
		coVerify(exactly = 1) { repository.refreshRates(today) }
	}
	
	@Test
	fun `предыдущий день берётся через кэш, а запрошенная дата обновляется`() = runTest {
		coEvery { repository.getRates(yesterday) } returns Result.Success(emptyList<Rate>())
		coEvery { repository.refreshRates(today) } returns Result.Success(Unit)
		
		useCase(today)
		
		coVerify(exactly = 0) { repository.refreshRates(yesterday) }
		coVerify(exactly = 0) { repository.getRates(today) }
	}
	
	@Test
	fun `сбой на предыдущем дне не роняет обновление`() = runTest {
		coEvery { repository.getRates(yesterday) } returns Result.Error(IOException("Нет соединения"))
		coEvery { repository.refreshRates(today) } returns Result.Success(Unit)
		
		val result = useCase(today)
		
		assertTrue(result is Result.Success)
		coVerify(exactly = 1) { repository.refreshRates(today) }
	}
	
	@Test
	fun `сбой на запрошенной дате возвращается наверх`() = runTest {
		coEvery { repository.getRates(yesterday) } returns Result.Success(emptyList<Rate>())
		coEvery { repository.refreshRates(today) } returns Result.Error(IOException("Таймаут"))
		
		val result = useCase(today)
		
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).exception is IOException)
	}
	
	@Test
	fun `предыдущий день загружается до запрошенной даты`() = runTest {
		coEvery { repository.getRates(yesterday) } returns Result.Success(emptyList<Rate>())
		coEvery { repository.refreshRates(today) } returns Result.Success(Unit)
		
		useCase(today)
		
		coVerify(ordering = Ordering.ORDERED) {
			repository.getRates(yesterday)
			repository.refreshRates(today)
		}
	}
}