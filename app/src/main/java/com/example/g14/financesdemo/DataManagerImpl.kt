package com.example.g14.financesdemo

import com.example.g14.financesdemo.model.Currency
import com.example.g14.financesdemo.model.MoneyAmount
import com.example.g14.financesdemo.model.Transaction
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

/**
 * Created by Gabriel Fortin
 */

class DataManagerImpl(observableLoginNetworkCall: Single<String>) : DataManager {

    sealed class TokenState {
        data class Present(val token: String) : TokenState()
        data class Error(val reason: String) : TokenState()
        object NoToken : TokenState()
            { override fun toString() = "NoToken" }
    }

    class Token(val observableLoginNetworkCall: Single<String>) {
        private val lock: Any = Any()
        private var doingResetRightNow: Boolean = false
        private var doingRemoveRightNow: Boolean = false
        private var networkRequest: Disposable? = null

        private val tokenStream: BehaviorSubject<TokenState> = BehaviorSubject.create()

        /** State is always active and caches last value */
        val state: Observable<TokenState> = tokenStream
                .startWith(TokenState.NoToken)
                .distinctUntilChanged()
                .replay(1).autoConnect()

        /** Acquires or re-acquires token */
        fun reset() {
            synchronized(lock) {
                if (doingResetRightNow) return
                doingResetRightNow = true
            }

            tokenStream.onNext(TokenState.NoToken)
            networkRequest = callForToken()
                    .doFinally {
                        networkRequest = null
                        synchronized(lock) {
                            doingResetRightNow = false
                        }
                    }
                    .subscribe(
                            { token -> tokenStream.onNext(TokenState.Present(token)) },
                            { error ->
                                tokenStream.onNext(TokenState.Error(
                                        error.message ?: "network error"))
                                tokenStream.onNext(TokenState.NoToken)
                            }
                    )
        }

        /** Removes token (e.g. for logout) */
        fun remove() {
            synchronized(lock) {
                // prevent running "remove()" in parallel
                if (doingRemoveRightNow) return
                doingRemoveRightNow = true

                // prevent a new concurrent "reset()" from starting
                doingResetRightNow = true
            }

            // if network request is on-going then cancel it
            networkRequest?.dispose()  // this triggers the ".doFinally" operator

            tokenStream.onNext(TokenState.NoToken)

            synchronized(lock) {
                doingRemoveRightNow = false
            }
        }

        fun callForToken(): Single<String> {
            return observableLoginNetworkCall
        }
    }


    private val token = Token(observableLoginNetworkCall)
    private val events = BehaviorSubject.create<DataManagerEvent>()

    override fun observe(): Observable<DataManagerEvent> = events

    override fun logOut(): Completable {
        token.remove()
        return Completable.complete()
    }

    /** Returns a completable which will perform log-in upon subscription */
    override fun logIn(): Completable {
        return token.state
                .doOnSubscribe {
                    events.onNext(DataManagerEvent.Requesting(ENDPOINT_LOGIN))
                }
                // if NoToken then request for one and "wait"
                // otherwise proceed
                .filter {
                    when (it) {
                        is TokenState.NoToken -> { token.reset(); false }
                        else -> true
                    }
                }
                .take(1)
                // map: token => complete, error => error
                .flatMapCompletable {
                    when (it) {
                        is TokenState.Present -> {
                            events.onNext(DataManagerEvent.RequestSuccess(ENDPOINT_LOGIN))
                            Completable.complete()
                        }
                        is TokenState.Error -> {
                            // TODO: become more precise about the problem
                            events.onNext(DataManagerEvent.UnknownProblem(it.reason))

                            // returning error will trigger the back-off mechanism
                            Completable.error(Exception("$ENDPOINT_LOGIN: $it.reason"))
                        }
                        is TokenState.NoToken -> {
                            // this case is filtered out by the '.filter' operator
                            Completable.error(IllegalArgumentException("SHOULD NOT HAPPEN"))
                        }
                    }
                }
                // TODO: add exponential back-off to login (see "ExponentialBackOff" class)
                //       but first, write tests for the back-off feature
//                .compose(ExponentialBackOff())
    }

    // TODO: finish back-off implementation and use in "logIn()"
    inner class ExponentialBackOff : CompletableTransformer {
        val RETRY_COUNT = 3  // must be at least '1' for ".scan" to work properly

        override fun apply(upstream: Completable): CompletableSource {
            return upstream
                    .retryWhen { throwables ->
                        throwables
                                .zipWith(1..RETRY_COUNT, { thr, i ->
                                    // trigger retry (this makes "token.state" to emit an item)
                                    token.reset()
                                    // compute back-off time: 2^i
                                    Pair(thr, Math.pow(2.0, i.toDouble()))
                                })
                                .materialize()
                                // when no more retries ("onComplete" event) then emit last error
                                .scan { previousPair, currentPair ->
                                    if (currentPair.isOnNext) {
                                        // here, "value" is known to be non-null
                                        Notification.createOnNext(currentPair.value!!)
                                    } else if (currentPair.isOnComplete) {
                                        // previously, "value" was known to be non-null
                                        val throwable = previousPair.value!!.first
                                        Notification.createOnError(throwable)
                                    } else {
                                        // "onError" never happens
                                        throw NotImplementedError("unexpected case in exponential" +
                                                " back-off; message: ${currentPair.error?.message}")
                                    }
                                }
                                .dematerialize<Double>()
                                .map { retryDelay ->
                                    // apply back-off time
                                    Observable.timer(retryDelay.toLong(), TimeUnit.SECONDS)
                                }
                    }
        }

    }

    override fun getBalance(): Single<Pair<MoneyAmount, Currency>> {
        TODO("not implemented: DataManagerImpl.getBalance")
    }

    override fun getTransactions(): Single<List<Transaction>> {
        TODO("not implemented: DataManagerImpl.getTransactions")
    }

    override fun spend(description: String, amount: MoneyAmount, currency: Currency): Completable {
        TODO("not implemented: DataManagerImpl.spend")
    }
}
