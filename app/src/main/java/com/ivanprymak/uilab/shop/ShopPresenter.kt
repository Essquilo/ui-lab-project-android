package com.ivanprymak.uilab.auth

import com.ivanprymak.uilab.common.CacheWithId
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Ivan Prymak on 5/29/2017.
 * Singleton responsible for handling background login tasks.
 */
class ShopPresenter private constructor(val api: ItemsAPI) : Callback<List<Item>> {
    var observer: ShopView? = null
    var latestItems: List<Item>? = null
    var latestError: Throwable? = null
    val addToCartCache: CacheWithId<ResponseBody> = CacheWithId()

    companion object {
        fun initialize(api: ItemsAPI) {
            instance = ShopPresenter(api)
        }

        lateinit var instance: ShopPresenter
    }

    fun addToCart(id: Long) {
        addToCartCache.id = id
        api.addToCart(Id(id)).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, error: Throwable?) {
                addToCartCache.onFailure(call, error)
                observer?.let {
                    addToCartCache.publish(it::onAddedToCart, it::onErrorAdding)
                }
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                addToCartCache.onResponse(call, response)
                observer?.let {
                    addToCartCache.publish(it::onAddedToCart, it::onErrorAdding)
                }
            }

        })
    }


    fun getItems() = api.getItems().enqueue(this)

    @Synchronized
    private fun publishResults() {
        if (observer != null) {
            if (latestError != null) {
                // method is synchronized, and we are changing latest to null only here
                observer?.onErrorLoadingItems(latestError!!)
                latestItems = null
            }
            if (latestItems != null) {
                // method is synchronized, and we are changing latest to null only here
                observer?.onLoadedItems(latestItems!!)
            }
            latestError = null
        }
    }

    override fun onResponse(call: Call<List<Item>>?, response: Response<List<Item>>?) {
        if (response != null && response.body() != null) {
            latestItems = response.body()
        }
        publishResults()
    }

    override fun onFailure(call: Call<List<Item>>?, error: Throwable?) {
        latestError = error ?: NullPointerException("Unknown error occured")
        publishResults()
    }

    fun subscribe(observer: ShopView) {
        this.observer = observer
        if (latestItems == null) {
            getItems()
        } else {
            observer.onLoadedItems(latestItems!!)
        }
        addToCartCache.publishIfNotEmpty(observer::onAddedToCart, observer::onErrorAdding)

    }

    fun unsubscribe() {
        this.observer = null
    }
}

interface ShopView {
    fun onLoadedItems(items: List<Item>)
    fun onErrorLoadingItems(status: Throwable)
    fun onAddedToCart(id: Long)
    fun onErrorAdding(status: Throwable, id: Long?)
}
