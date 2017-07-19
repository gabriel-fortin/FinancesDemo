package com.example.g14.financesdemo.network

import com.example.g14.financesdemo.BuildConfig
import com.example.g14.financesdemo.model.Transaction
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by Gabriel Fortin
 */

interface ApiService {

    data class TokenResponse(var token: String)

    data class BalanceResponse(
            val balance: String,
            val currency: String)

    data class TransactionsResponse(
            val id: Long,
            val date: String,
            val description: String,
            val amount: String,
            val currency: String)

    // TODO: 'SpendResponse'
    // TODO: refactor: move response models to separate files

    // TODO: when project grows bigger: creating Retrofit should be split and done through DI
    companion object Factory {
        fun create(serverUrl: String = BuildConfig.SERVER_URL): ApiService {
            return Retrofit.Builder()
                    .baseUrl(serverUrl)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
        }
    }

    @POST("login")
    @Headers("Content-Type: application/json")
    fun login(): Single<TokenResponse>  // TODO: return Single<…>

    @GET("balance")
    fun balance(@Header("Authorization") auth: String): Single<BalanceResponse>

    @GET("transactions")
    fun transactions(@Header("Authorization") auth: String): Single<List<TransactionsResponse>>


    // TODO: interface fun declaration for '/spend' API call
//    @POST
//    fun spend



}
