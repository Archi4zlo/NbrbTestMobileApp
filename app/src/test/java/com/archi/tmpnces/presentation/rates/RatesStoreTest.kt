package com.archi.tmpnces.presentation.rates

import app.cash.turbine.test
import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.domain.model.Rate
import com.archi.tmpnces.domain.model.RateWithChange
import com.archi.tmpnces.domain.usecase.ObserveRatesUseCase
import com.archi.tmpnces.domain.usecase.RefreshRatesUseCase
import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RatesStoreTest {
	
	private val date = LocalDate.of(2026, 8, 31)
	
	private lateinit var observeRates: ObserveRatesUseCase
	private lateinit var refreshRates: RefreshRatesUseCase
	
	private val sampleRate = RateWithChange(
		rate = Rate(
			curId = 431, date = date, abbreviation = "USD", name = "Доллар США", scale = 1, officialRate = 3.1234
		), previousRatePerUnit = 3.1000
	)
	
	@Before
	fun setUp() {
		isAssertOnMainThreadEnabled = false
		Dispatchers.setMain(UnconfinedTestDispatcher())
		observeRates = mockk()
		refreshRates = mockk()
	}
	
	@After
	fun tearDown() {
		Dispatchers.resetMain()
		isAssertOnMainThreadEnabled = true
	}
	
	private fun createStore(): RatesStore = RatesStoreFactory(
		storeFactory = DefaultStoreFactory(), observeRates = observeRates, refreshRates = refreshRates
	).create(date)
	
	private fun stubSuccess(rates: List<RateWithChange> = emptyList()) {
		every { observeRates(date) } returns flowOf(rates)
		coEvery { refreshRates(date) } returns Result.Success(Unit)
	}
	
	@Test
	fun `при создании сразу загружает курсы из сети`() = runTest {
		stubSuccess()
		
		createStore()
		
		coVerify(exactly = 1) { refreshRates(date) }
	}
	
	@Test
	fun `курсы из базы попадают в состояние`() = runTest {
		stubSuccess(rates = listOf(sampleRate))
		
		val store = createStore()
		
		assertEquals(1, store.state.rates.size)
		assertEquals("USD", store.state.rates.first().rate.abbreviation)
		assertEquals(date, store.state.date)
	}
	
	@Test
	fun `нажатие Обновить запускает повторную загрузку`() = runTest {
		stubSuccess()
		val store = createStore()
		
		store.accept(RatesStore.Intent.Refresh)
		coVerify(exactly = 2) { refreshRates(date) }
	}
	
	@Test
	fun `после завершения загрузки индикатор гаснет`() = runTest {
		stubSuccess()
		
		val store = createStore()
		assertFalse(store.state.isLoading)
	}
	
	@Test
	fun `сбой загрузки публикуется событием и не оседает в состоянии`() = runTest {
		stubSuccess()
		val store = createStore()
		
		coEvery { refreshRates(date) } returns Result.Error(IOException("Нет соединения"))
		
		store.labels.test {
			store.accept(RatesStore.Intent.Refresh)
			
			val label = awaitItem()
			assertTrue(label is RatesStore.Label.RefreshFailed)
			assertTrue((label as RatesStore.Label.RefreshFailed).throwable is IOException)
			
			cancelAndIgnoreRemainingEvents()
		}
		
		assertFalse(store.state.isLoading)
	}
}