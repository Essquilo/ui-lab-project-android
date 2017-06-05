package com.ivanprymak.uilab.shop

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.ivanprymak.uilab.R
import com.ivanprymak.uilab.auth.Item
import com.ivanprymak.uilab.auth.ShopPresenter
import com.ivanprymak.uilab.auth.ShopView
import com.ivanprymak.uilab.cart.CartActivity
import com.ivanprymak.uilab.cart.CartPresenter
import com.ivanprymak.uilab.common.AppCompatToolbarUI
import com.ivanprymak.uilab.common.ItemsAdapter
import com.ivanprymak.uilab.common.doesListHaveItem
import org.jetbrains.anko.*
import org.jetbrains.anko.design._CoordinatorLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import timber.log.Timber


class ShopActivity : AppCompatActivity(), ShopView {
    var loading: Boolean = false

    override fun onLoadedItems(items: List<Item>) {
        Timber.d("Got ${items.size} items")
        listAdapter.items = items
        if (!loading) {
            refreshLayout.isRefreshing = false
        }
    }

    override fun onErrorLoadingItems(status: Throwable) {
        status.message?.let { toast(it) }
        refreshLayout.isRefreshing = false
        loading = false
    }

    override fun onAddedToCart(id: Long) {
        CartPresenter.instance.getItems()
        refreshLayout.isRefreshing = false
        loading = false
    }

    override fun onErrorAdding(status: Throwable, id: Long?) {
        toast(status.message!!)
        refreshLayout.isRefreshing = false
        loading = false

    }

    lateinit var listAdapter: ItemsAdapter
    lateinit var refreshLayout: SwipeRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = ItemsAdapter(R.string.add_to_cart, ArrayList())
        val ui = ShopActivityUI(listAdapter)
        ui.setContentView(this)
        refreshLayout = ui.refresh
        listAdapter.listener = { id ->
            if (!loading) {
                refreshLayout.isRefreshing = true
                loading = true
                ShopPresenter.instance.addToCart(id)
            }

        }
        setSupportActionBar(ui.toolbar)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        loading = savedInstanceState?.getBoolean("loading") ?: false

    }

    override fun onResume() {
        super.onResume()
        ShopPresenter.instance.subscribe(this)
    }

    override fun onPause() {
        ShopPresenter.instance.unsubscribe()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actions_shop, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.cart -> startActivity(intentFor<CartActivity>())
        }
        return super.onOptionsItemSelected(item)
    }
}

class ShopActivityUI(val listAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>) :
        AppCompatToolbarUI<ShopActivity>(R.string.shop) {
    lateinit var refresh: SwipeRefreshLayout
    override fun createContentView(ui: @AnkoViewDslMarker _CoordinatorLayout) {
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
                refresh = this
                onRefresh {
                    ShopPresenter.instance.getItems()
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
