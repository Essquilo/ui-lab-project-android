package com.ivanprymak.uilab.cart

import com.ivanprymak.uilab.auth.Id
import com.ivanprymak.uilab.auth.Item
import com.ivanprymak.uilab.common.Cache
import com.ivanprymak.uilab.common.CacheWithId
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Ivan Prymak on 5/29/2017.
 * Singleton responsible for handling background login tasks.
 */

class CartPresenter private constructor(val api: com.ivanprymak.uilab.auth.ItemsAPI) : retrofit2.Callback<List<Item>> {
    var observer: CartListener? = null
    val cartCache: Cache<List<Item>> = Cache(true)
    val removeFromCartCache: CacheWithId<ResponseBody> = CacheWithId()

    companion object {
        fun initialize(api: com.ivanprymak.uilab.auth.ItemsAPI) {
            com.ivanprymak.uilab.cart.CartPresenter.Companion.instance = com.ivanprymak.uilab.cart.CartPresenter(api)
        }

        lateinit var instance: com.ivanprymak.uilab.cart.CartPresenter
    }

    fun getItems() = api.getCart().enqueue(this)

    fun removeFromCart(id: Long) {
        removeFromCartCache.id = id
        api.removeFromCart(Id(id)).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, error: Throwable?) {
                removeFromCartCache.onFailure(call, error)
                observer?.let {
                    removeFromCartCache.publish(it::onRemoved, it::onErrorRemoving)
                }
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                removeFromCartCache.onResponse(call, response)
                observer?.let {
                    removeFromCartCache.publish(it::onRemoved, it::onErrorRemoving)
                }
            }

        })
    }

    @Synchronized
    private fun publishResults() {
        observer?.let {
            cartCache.publish(observer!!::onLoadedCart, observer!!::onErrorLoadingCart)
        }
    }

    override fun onResponse(call: Call<List<Item>>?, response: Response<List<Item>>?) {
        cartCache.onResponse(call, response)
        publishResults()
    }

    override fun onFailure(call: Call<List<Item>>?, error: Throwable?) {
        cartCache.onFailure(call, error)
        publishResults()
    }

    fun subscribe(observer: CartListener) {
        this.observer = observer
        if (cartCache.isEmpty()) {
            getItems()
        } else {
            cartCache.publish(observer::onLoadedCart, observer::onErrorLoadingCart)
        }

        //removeFromCartCache.publishIfNotEmpty(observer::onRemoved, observer::onErrorRemoving)
    }

    fun unsubscribe() {
        this.observer = null
    }
}

interface CartListener {
    fun onLoadedCart(items: List<Item>)
    fun onErrorLoadingCart(status: Throwable)
    fun onRemoved(id: Long)
    fun onErrorRemoving(status: Throwable, id: Long?)
}