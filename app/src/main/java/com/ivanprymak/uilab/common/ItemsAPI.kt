package com.ivanprymak.uilab.auth

import android.support.v7.util.DiffUtil
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Created by Ivan Prymak on 5/29/2017.
 * Description of API for login in Retrofit
 */
interface ItemsAPI {
    @GET("/api/v1/items/")
    fun getItems(): Call<List<Item>>

    @GET("/api/v1/cart/")
    fun getCart(): Call<List<Item>>

    @POST("/api/v1/cart/add/")
    fun addToCart(@Body body: Id): Call<ResponseBody>

    @POST("/api/v1/cart/remove/")
    fun removeFromCart(@Body body: Id): Call<ResponseBody>
}

data class Item(val id: Long, val name: String, val cost: Int, val img: String)

data class Id(val id: Long)

class ItemDiffer(val old: List<Item>, val new: List<Item>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = old[oldItemPosition].id == new[newItemPosition].id


    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = old[oldItemPosition].name == new[newItemPosition].name
            && old[oldItemPosition].cost == new[newItemPosition].cost
            && old[oldItemPosition].img == new[newItemPosition].img
}