package com.ivanprymak.uilab.auth

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by Ivan Prymak on 5/29/2017.
 * Description of API for login in Retrofit
 */
interface AuthAPI {
    @POST("/api/v1/login/")
    @FormUrlEncoded
    fun login(@Field("username") after: String, @Field("password") limit: String)
            : Call<LoginResponse>
}

data class LoginResponse(val token: String)