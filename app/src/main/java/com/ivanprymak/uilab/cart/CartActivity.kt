package com.ivanprymak.uilab.cart

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import com.ivanprymak.uilab.R
import com.ivanprymak.uilab.auth.Item
import com.ivanprymak.uilab.common.AppCompatToolbarUI
import com.ivanprymak.uilab.common.ItemsAdapter
import com.ivanprymak.uilab.common.doesListHaveItem
import org.jetbrains.anko.*
import org.jetbrains.anko.design._CoordinatorLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.swipeRefreshLayout

class CartActivity : AppCompatActivity(), CartListener {
    var loading: Boolean = false
    lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onLoadedCart(items: List<Item>) {
        listAdapter.items = items
        swipeRefresh.isRefreshing = false
    }

    override fun onErrorLoadingCart(status: Throwable) {
        toast(status.message!!)
        swipeRefresh.isRefreshing = false
    }

    override fun onRemoved(id: Long) {
        listAdapter.remove(id)
        swipeRefresh.isRefreshing = false
        loading = false

    }

    override fun onErrorRemoving(status: Throwable, id: Long?) {
        toast(status.message!!)
        swipeRefresh.isRefreshing = false
        loading = false
    }

    lateinit var listAdapter: ItemsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = ItemsAdapter(R.string.remove_from_cart, ArrayList())
        val ui = CartActivityUI(listAdapter)
        ui.setContentView(this)
        swipeRefresh = ui.swipeRefresh
        listAdapter.listener = { id ->
            toast("Clicked $id and refresh is ${swipeRefresh.isRefreshing}")
            if (!loading) {
                CartPresenter.instance.removeFromCart(id)
                loading = true
            }
        }
        setSupportActionBar(ui.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        loading = savedInstanceState?.getBoolean("loading") ?: false
    }

    override fun onResume() {
        super.onResume()
        CartPresenter.instance.subscribe(this)
    }

    override fun onPause() {
        CartPresenter.instance.unsubscribe()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean("loading", loading)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        listAdapter.listener = null
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}

class CartActivityUI(val listAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>)
    : AppCompatToolbarUI<CartActivity>(R.string.cart) {
    lateinit var swipeRefresh: SwipeRefreshLayout
    override fun createContentView(ui: _CoordinatorLayout) {
        with(ui) {
            val emptyView = frameLayout {
                textView(R.string.no_items).lparams(wrapContent, wrapContent, Gravity.CENTER)

            }.lparams(matchParent, matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }

            fun updateEmptyViewVisibility(recyclerView: RecyclerView) {
                if (doesListHaveItem(recyclerView)) {
                    emptyView.visibility = View.GONE
                } else {
                    emptyView.visibility = View.VISIBLE
                }
            }
            swipeRefreshLayout {
                swipeRefresh = this
                onRefresh {
                    CartPresenter.instance.getItems()
                }
                recyclerView {
                    lparams(matchParent, matchParent)
                    val orientation = LinearLayoutManager.VERTICAL
                    layoutManager = LinearLayoutManager(context, orientation, false)
                    overScrollMode = View.OVER_SCROLL_NEVER
                    adapter = listAdapter
                    adapter.registerAdapterDataObserver(
                            object : RecyclerView.AdapterDataObserver() {
                                override fun onChanged() {
                                    super.onChanged()
                                    updateEmptyViewVisibility(this@recyclerView)
                                }
                            })

                    updateEmptyViewVisibility(this)
                }
            }.lparams(matchParent, matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
        }
    }
}