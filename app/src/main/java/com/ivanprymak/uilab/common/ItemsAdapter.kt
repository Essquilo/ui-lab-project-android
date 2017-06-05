package com.ivanprymak.uilab.common

import android.support.annotation.StringRes
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.ivanprymak.uilab.auth.Item
import com.ivanprymak.uilab.auth.ItemDiffer
import com.ivanprymak.uilab.util.borderlessColoredButton
import com.squareup.picasso.Picasso
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView
import timber.log.Timber
import java.math.BigDecimal

/**
 * Created by Ivan Prymak on 5/31/2017.
 */

class ItemsAdapter(@StringRes val btnText: Int, items: List<Item>) : RecyclerView.Adapter<ItemViewHolder>() {
    var listener: ((Long) -> Any)? = null

    var items: List<Item> = items
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun remove(id: Long) {
        val new = items.filterNot { it.id == id }
        val differ = ItemDiffer(items, new)
        items = new
        DiffUtil.calculateDiff(differ).dispatchUpdatesTo(this)
    }

    fun add(item: Item) {
        val new = items.plus(item)
        val differ = ItemDiffer(items, new)
        items = new
        DiffUtil.calculateDiff(differ).dispatchUpdatesTo(this)
    }

    override fun onBindViewHolder(holder: ItemViewHolder?, position: Int) {
        val item = items[position]
        val cost = BigDecimal(item.cost).movePointLeft(2)
        holder?.cost?.text = "$$cost".format(cost.toString())
        Picasso.with(holder?.itemView?.context)
                .load(item.img)
                .into(holder?.img)
        holder?.name?.text = item.name
        holder?.btn?.setOnClickListener {
            listener?.invoke(item.id)
            Timber.d("Clicked ${item.id} listener ${listener != null}")
        }
        Timber.d("Bound view holder for ${item.id}")
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val ui = ItemUI(btnText)
        return ItemViewHolder(ui.createView(AnkoContext.Companion.create(parent.context, parent)), ui.name, ui.img, ui.cost, ui.btn)
    }
}

class ItemUI(@StringRes val btnText: Int) : AnkoComponent<ViewGroup> {
    lateinit var name: TextView
    lateinit var img: ImageView
    lateinit var cost: TextView
    lateinit var btn: Button

    override fun createView(ui: AnkoContext<ViewGroup>): View {
        return with(ui) {
            verticalLayout {
                lparams(width = matchParent, height = wrapContent) {
                    padding = dip(10)
                }
                cardView {
                    lparams(width = matchParent, height = wrapContent) {
                        cardElevation = dip(4).toFloat()
                        useCompatPadding = true
                        radius = dip(2).toFloat()
                    }
                    verticalLayout {
                        lparams(width = matchParent, height = wrapContent)
                        imageView {
                            img = this
                            lparams(width = matchParent, height = dip(320))
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                        textView {
                            lparams(matchParent, wrapContent) {
                                setPadding(dip(15), dip(10), dip(15), dip(10))
                            }
                            name = this
                            textSize = 22f
                        }
                        relativeLayout {
                            lparams(matchParent, wrapContent) {
                                setPadding(dip(15), dip(0), dip(5), dip(10))
                            }
                            textView {
                                cost = this // Save a reference as you create the object
                                textSize = 18f
                            }.lparams(wrapContent, wrapContent) {
                                alignParentLeft()
                                centerVertically()
                            }
                            borderlessColoredButton(btnText) {
                                btn = this // Save a reference as you create the object
                                textSize = 18f
                            }.lparams(wrapContent, wrapContent) {
                                alignParentRight()
                                centerVertically()
                            }
                        }
                    }
                }
            }
        }
    }
}

class ItemViewHolder(itemView: View, val name: TextView, val img: ImageView, val cost: TextView,
                     val btn: Button) : RecyclerView.ViewHolder(itemView)

fun doesListHaveItem(list: RecyclerView?) = list?.adapter?.itemCount!! > 0
