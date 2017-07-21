package com.example.g14.financesdemo

import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Created by Gabriel Fortin
 */


class DataManagerImplTest {
    lateinit var dm: DataManager

    @Before
    fun setUp() {

    }

    @Test
    fun login_success() {
        // PREPARE
        val scheduler: TestScheduler = TestScheduler()
        val networkCall: Single<String> = Single
                .timer(200L, TimeUnit.MILLISECONDS, scheduler)
                .map { "token_1" }
        dm = DataManagerImpl(networkCall)

        // EXECUTE
        val testObserver = dm.logIn().test()

        // VERIFY
        testObserver.assertNoValues()
        scheduler.advanceTimeBy(150, TimeUnit.MILLISECONDS)
        testObserver.assertNoValues()
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)
        testObserver.assertComplete()
    }

    @Test
    fun login_error() {
        // PREPARE
        val networkError = Exception("network error")
        val scheduler: TestScheduler = TestScheduler()
        val networkCall: Single<String> = Single
                .timer(200L, TimeUnit.MILLISECONDS, scheduler)
                .flatMap { Single.error<String>(networkError) }
        dm = DataManagerImpl(networkCall)

        // EXECUTE
        val testObserver = dm.logIn().test()

        // VERIFY
        testObserver.assertNoValues()
        scheduler.advanceTimeBy(150, TimeUnit.MILLISECONDS)
        testObserver.assertNoValues()
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        assertThat(testObserver.errorCount(), `is`(1))
    }

    @Test
    fun reset_concurrent() {
        // PREPARE
        val scheduler: TestScheduler = TestScheduler()
        val networkCall: Single<String> = Single
                .timer(200L, TimeUnit.MILLISECONDS, scheduler)
                .map { "some token" }
        val token = DataManagerImpl.Token(networkCall)

        // EXECUTE
        val testObserver = token.state.test()
        token.reset()
        token.reset()

        // VERIFY
        scheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
        testObserver.assertValues(
                DataManagerImpl.TokenState.NoToken,
                DataManagerImpl.TokenState.Present("some token")
        )
    }
}
