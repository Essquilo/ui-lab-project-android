package com.ivanprymak.uilab.auth

import android.content.SharedPreferences
import com.ivanprymak.uilab.common.Cache
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Ivan Prymak on 5/29/2017.
 * Singleton responsible for handling background login tasks.
 */
private val KEY_USER_TOKEN = "userToken"

class AuthManager private constructor(val api: AuthAPI, val preferences: SharedPreferences) : Callback<LoginResponse> {
    var observer: AuthListener? = null
    var cachedLogin: Cache<LoginResponse> = Cache(false)

    companion object {
        fun initialize(api: AuthAPI, preferences: SharedPreferences) {
            instance = AuthManager(api, preferences)
        }

        lateinit var instance: AuthManager
    }

    fun login(email: String, password: String) {
        api.login(email, password).enqueue(this)
    }


    fun isLoggedIn(): Boolean {
        return preferences.contains(KEY_USER_TOKEN)
    }

    fun getToken(): String {
        return preferences.getString(KEY_USER_TOKEN, null)
    }

    override fun onResponse(call: Call<LoginResponse>?, result: Response<LoginResponse>?) {
        cachedLogin.onResponse(call, result)
        if (!cachedLogin.isEmpty())
            preferences.edit().putString(KEY_USER_TOKEN, cachedLogin.cached!!.token).apply()
        publishResults()
    }

    @Synchronized
    private fun publishResults() {
        observer?.let {
            cachedLogin.publish(observer!!::onSuccess, observer!!::onError)
        }
    }

    override fun onFailure(call: Call<LoginResponse>?, error: Throwable?) {
        cachedLogin.onFailure(call, error)
        publishResults()
    }

    fun subscribe(observer: AuthListener) {
        this.observer = observer
        if (isLoggedIn()) {
            observer.onSuccess(LoginResponse(getToken()))
        }
    }

    fun unsubscribe() {
        this.observer = null
    }
}

interface AuthListener {
    fun onSuccess(loginResponse: LoginResponse)
    fun onError(status: Throwable)
}
