package com.ivanprymak.uilab.common

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Ivan Prymak on 6/2/2017.
 */

open class Cache<T>(val keepOnSuccess: Boolean) : Callback<T> {
    var error: Throwable? = null
    var cached: T? = null

    override fun onResponse(call: Call<T>?, result: Response<T>?) {
        cached = null
        error = null
        if (result == null) {
            error = NullPointerException("Empty response returned")
        } else {
            if (result.body() == null) {
                error = NullPointerException("Empty response body returned")
            } else {
                cached = result.body()
            }
        }
    }

    override fun onFailure(call: Call<T>?, error: Throwable?) {
        cached = null
        this.error = error ?: NullPointerException("Unknown error occured")
    }

    fun publish(onSuccess: (T) -> Any, onFailure: (Throwable) -> Any) {
        if (error != null) {
            onFailure(error!!)
        } else {
            if (cached != null) {
                onSuccess(cached!!)
            }
        }
        error = null
        if (!keepOnSuccess) cached = null
    }

    fun publishIfNotEmpty(onSuccess: (T) -> Any, onFailure: (Throwable) -> Any) {
        if (error != null || cached != null) {
            publish(onSuccess, onFailure)
        }
    }

    fun isEmpty(): Boolean = cached == null
}

class CacheWithId<T> : Cache<T>(false) {
    var id: Long? = null
        set(value) {
            if (field != null && value != null) {
                throw Throwable("Trying to init new cache for $value when $id is not yet retrieved")
            }
            field = value
        }

    fun publish(onSuccess: (Long) -> Any, onFailure: (Throwable, Long?) -> Any) {
        publish({ onSuccess(id!!) }, { err ->
            with(err) {
                if (id == null) {
                    onFailure(NotCachedException(), id)
                } else {
                    onFailure(err, id)
                }
            }
        })
        id = null
    }

    fun publishIfNotEmpty(onSuccess: (Long) -> Any, onFailure: (Throwable, Long?) -> Any) {
        if (error != null || cached != null) {
            publish(onSuccess, onFailure)
        }
    }
}

class NotCachedException : Throwable("Cache was empty.")