package com.ivanprymak.uilab

import android.app.Application
import android.content.Context
import com.ivanprymak.uilab.auth.AuthAPI
import com.ivanprymak.uilab.auth.AuthManager
import com.ivanprymak.uilab.auth.ItemsAPI
import com.ivanprymak.uilab.auth.ShopPresenter
import com.ivanprymak.uilab.cart.CartPresenter
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber


/**
 * Created by Ivan Prymak on 5/29/2017.
 * Applications entry point.
 */
val PREFERENCE_NAME = "UIApp.prefs"

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Picasso.with(this).isLoggingEnabled = true
        val httpLogger = HttpLoggingInterceptor()
        httpLogger.level = HttpLoggingInterceptor.Level.BODY
        val http = OkHttpClient.Builder()
                .addInterceptor({
                    val ongoing = it.request().newBuilder()
                    ongoing.addHeader("Accept", "application/json;versions=1")
                    if (AuthManager.instance.isLoggedIn()) {
                        ongoing.addHeader("Authorization", "Token ${AuthManager.instance.getToken()}")
                    }
                    it.proceed(ongoing.build())
                })
                .addInterceptor(httpLogger)
                .build()
        val retrofit = Retrofit.Builder()
                .client(http)
                .baseUrl(BuildConfig.API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        AuthManager.initialize(retrofit.create(AuthAPI::class.java), getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE))
        ShopPresenter.initialize(retrofit.create(ItemsAPI::class.java))
        CartPresenter.initialize(retrofit.create(ItemsAPI::class.java))
    }
}