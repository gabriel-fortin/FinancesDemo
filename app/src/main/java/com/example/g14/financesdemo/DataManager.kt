package com.example.g14.financesdemo

import com.example.g14.financesdemo.model.Currency
import com.example.g14.financesdemo.model.MoneyAmount
import com.example.g14.financesdemo.model.Transaction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by Gabriel Fortin
 */

interface DataManager {
    fun logIn(): Completable
    fun logOut(): Completable
    fun getBalance(): Single<Pair<MoneyAmount, Currency>>
    fun getTransactions(): Single<List<Transaction>>
    fun spend(description: String, amount: MoneyAmount, currency: Currency): Completable

    fun observe(): Observable<DataManagerEvent>
}
