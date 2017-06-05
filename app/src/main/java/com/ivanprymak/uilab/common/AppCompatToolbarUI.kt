package com.ivanprymak.uilab.common

import android.support.annotation.StringRes
import android.support.v7.widget.Toolbar
import com.ivanprymak.uilab.R
import com.ivanprymak.uilab.util.attrDimen
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.themedToolbar
import org.jetbrains.anko.appcompat.v7.titleResource
import org.jetbrains.anko.design._CoordinatorLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.themedAppBarLayout

/**
 * Created by Ivan Prymak on 6/2/2017.
 * View with predefined {@link CoordinatorView} + {@link Toolbar} wrapper.
 */

abstract class AppCompatToolbarUI<in AppCompatActivity>(@StringRes val titleRes: Int) : AnkoComponent<AppCompatActivity> {
    lateinit var toolbar: Toolbar
    override fun createView(ui: AnkoContext<AppCompatActivity>) = with(ui) {
        coordinatorLayout {
            themedAppBarLayout(R.style.ThemeOverlay_AppCompat_Dark_ActionBar) {
                lparams(matchParent, wrapContent)
                themedToolbar(R.style.ThemeOverlay_AppCompat_Dark_ActionBar) {
                    toolbar = this
                    popupTheme = R.style.ThemeOverlay_AppCompat_Light
                    titleResource = titleRes
                }.lparams(width = matchParent, height = ctx.attrDimen(R.attr.actionBarSize))
            }
            createContentView(this)
        }
    }

    abstract fun createContentView(ui: @AnkoViewDslMarker _CoordinatorLayout)
}