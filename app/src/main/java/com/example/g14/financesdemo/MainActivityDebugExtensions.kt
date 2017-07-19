package com.example.g14.financesdemo

import android.support.design.widget.Snackbar
import android.util.Log
import com.example.g14.financesdemo.network.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

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
