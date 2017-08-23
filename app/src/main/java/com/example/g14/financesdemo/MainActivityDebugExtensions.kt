package com.example.g14.financesdemo

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.g14.financesdemo.model.Currency
import com.example.g14.financesdemo.model.MoneyAmount
import com.example.g14.financesdemo.model.Transaction
import com.example.g14.financesdemo.network.ApiService
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Gabriel Fortin
 */

fun MainActivity.performRetrofitLogin() {
    val longSnack = { msg: String ->
        Snackbar.make(fab, msg, Snackbar.LENGTH_LONG).show()
    }

    ApiService.create()
            .login()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    { longSnack("TOKEN: ${it.token}"); Log.d("A", "$it") },
                    { longSnack("ERROR: ${it.message}"); Log.e("A", "$it") }
            )
}

val MainActivity.dataManager by lazy {
    val loginNetworkCall = ApiService.create()
            .login()
            .map { it.token }
    DataManagerImpl(loginNetworkCall)
}

fun MainActivity.loginViaDataManager() {
//    val loginNetworkCall = ApiService.create()
//            .login()
//            .map { it.token }
//    val dataManager: DataManager = DataManagerImpl(loginNetworkCall)

    dataManager.logIn()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    { Toast.makeText(this@loginViaDataManager, "login success", Toast.LENGTH_LONG).show() },
                    { Toast.makeText(this@loginViaDataManager, "login ERROR: $it", Toast.LENGTH_LONG).show() }
            )
}

