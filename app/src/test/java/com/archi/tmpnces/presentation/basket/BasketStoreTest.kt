package com.archi.tmpnces.presentation.basket

import app.cash.turbine.test
import com.archi.tmpnces.core.util.Result
import com.archi.tmpnces.domain.model.BasketValue
import com.archi.tmpnces.domain.model.CurrencyBasket
import com.archi.tmpnces.domain.usecase.ObserveBasketUseCase
import com.archi.tmpnces.domain.usecase.RefreshBasketUseCase
import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class BasketStoreTest {

    private val today = LocalDate.of(2026, 9, 2)
    private val otherDate = LocalDate.of(2023, 12, 21)

    private lateinit var observeBasket: ObserveBasketUseCase
    private lateinit var refreshBasket: RefreshBasketUseCase

    private fun basketOn(date: LocalDate) = CurrencyBasket(
        date = date,
        basket = BasketValue(value = 0.1748, sinceYearStart = null, sincePreviousDay = null),
        components = emptyList()
    )

    @Before
    fun setUp() {
        isAssertOnMainThreadEnabled = false
        Dispatchers.setMain(UnconfinedTestDispatcher())

        observeBasket = mockk()
        refreshBasket = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        isAssertOnMainThreadEnabled = true
    }

    private fun createStore(): BasketStore =
        BasketStoreFactory(
            storeFactory = DefaultStoreFactory(),
            observeBasket = observeBasket,
            refreshBasket = refreshBasket
        ).create(today)
    @Test
    fun `при создании сразу загружает данные на текущую дату`() = runTest {
        every { observeBasket(today) } returns flowOf(null)
        coEvery { refreshBasket(today) } returns Result.Success(Unit)

        val store = createStore()

        assertEquals(today, store.state.date)
        coVerify(exactly = 1) { refreshBasket(today) }
    }

    @Test
    fun `корзина из базы попадает в состояние`() = runTest {
        every { observeBasket(today) } returns flowOf(basketOn(today))
        coEvery { refreshBasket(today) } returns Result.Success(Unit)

        val store = createStore()

        assertEquals(0.1748, store.state.basket!!.basket.value, 1e-9)
    }

    @Test
    fun `выбор даты сразу меняет дату и сбрасывает прежние данные`() = runTest {
        every { observeBasket(any()) } returns MutableSharedFlow()
        coEvery { refreshBasket(any()) } returns Result.Success(Unit)

        val store = createStore()
        store.accept(BasketStore.Intent.SelectDate(otherDate))

        assertEquals(otherDate, store.state.date)
        assertNull(store.state.basket)
        coVerify(exactly = 1) { refreshBasket(otherDate) }
    }
    
    @Test
    fun `после смены даты обновления по прежней дате игнорируются`() = runTest {
        val oldDateFlow = MutableSharedFlow<CurrencyBasket?>(replay = 1)
        val newDateFlow = MutableSharedFlow<CurrencyBasket?>(replay = 1)

        every { observeBasket(today) } returns oldDateFlow
        every { observeBasket(otherDate) } returns newDateFlow
        coEvery { refreshBasket(any()) } returns Result.Success(Unit)

        val store = createStore()

        oldDateFlow.emit(basketOn(today))
        assertEquals(today, store.state.basket!!.date)

        store.accept(BasketStore.Intent.SelectDate(otherDate))
        newDateFlow.emit(basketOn(otherDate))
        assertEquals(otherDate, store.state.basket!!.date)

        oldDateFlow.emit(basketOn(today))
        assertEquals(otherDate, store.state.basket!!.date)
    }

    @Test
    fun `кнопка Вывести перезапрашивает данные на текущую дату`() = runTest {
        every { observeBasket(today) } returns flowOf(null)
        coEvery { refreshBasket(today) } returns Result.Success(Unit)

        val store = createStore()
        store.accept(BasketStore.Intent.Reload)
        
        coVerify(exactly = 2) { refreshBasket(today) }
    }

    @Test
    fun `сбой загрузки публикуется событием и не оседает в состоянии`() = runTest {
        every { observeBasket(today) } returns flowOf(null)
        coEvery { refreshBasket(today) } returns Result.Success(Unit)

        val store = createStore()

        coEvery { refreshBasket(today) } returns Result.Error(IOException("Нет сети"))

        store.labels.test {
            store.accept(BasketStore.Intent.Reload)

            val label = awaitItem()
            assertTrue(label is BasketStore.Label.LoadFailed)

            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(!store.state.isLoading)
    }
}