package com.ivanprymak.uilab.util

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.design.widget.TextInputEditText
import android.util.TypedValue
import android.util.TypedValue.complexToDimensionPixelSize
import android.view.ViewManager
import android.widget.Button
import com.ivanprymak.uilab.R
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.include

/**
 * Created by Ivan Prymak on 5/28/2017.
 * Utils for Anko's DSL
 */
fun Context.attribute(value: Int): TypedValue {
    val ret = TypedValue()
    theme.resolveAttribute(value, ret, true)
    return ret
}

fun Context.attrDimen(@DimenRes value: Int): Int {
    return complexToDimensionPixelSize(attribute(value).data, resources.displayMetrics)
}

fun Context.attrColor(@ColorRes value: Int): Int {
    return attribute(value).data
}

fun ViewManager.borderlessColoredButton(textRes: Int = 0) =
        borderlessColoredButton(textRes) { }

fun ViewManager.borderlessColoredButton(textRes: Int = 0, init: Button.() -> Unit) =
        include<Button>(R.layout.borderless_button) {
            if (textRes != 0) setText(textRes)
            init()
        }

fun ViewManager.borderlessColoredButton(text: String, init: Button.() -> Unit) =
        include<Button>(R.layout.borderless_button) {
            setText(text)
            init()
        }

fun ViewManager.materialColoredButton(textRes: Int = 0) =
        materialColoredButton(textRes) { }

fun ViewManager.materialColoredButton(textRes: Int = 0, init: Button.() -> Unit) =
        include<Button>(R.layout.colored_button) {
            if (textRes != 0) setText(textRes)
            init()
        }

inline fun ViewManager.textInputEditText() = textInputEditText {}
inline fun ViewManager.textInputEditText(theme: Int = 0, init: TextInputEditText.() -> Unit) = ankoView({ TextInputEditText(it) }, theme, init)