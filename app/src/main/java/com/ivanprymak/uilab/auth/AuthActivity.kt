package com.ivanprymak.uilab.auth

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import com.ivanprymak.uilab.R
import com.ivanprymak.uilab.common.AppCompatToolbarUI
import com.ivanprymak.uilab.shop.ShopActivity
import com.ivanprymak.uilab.util.materialColoredButton
import com.ivanprymak.uilab.util.textInputEditText
import org.jetbrains.anko.*
import org.jetbrains.anko.design._CoordinatorLayout
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.support.v4.nestedScrollView

class AuthActivity : AppCompatActivity(), AuthListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthActivityUI().setContentView(this)
    }

    override fun onResume() {
        super.onResume()
        AuthManager.instance.subscribe(this)
    }

    override fun onError(status: Throwable) {
        toast(status.message ?: "Unknown error occured")
    }

    override fun onSuccess(loginResponse: LoginResponse) {
        toast("Login successful!")
        startActivity(intentFor<ShopActivity>())
        finish()
    }

    override fun onPause() {
        AuthManager.instance.unsubscribe()
        super.onPause()
    }
}

class AuthActivityUI : AppCompatToolbarUI<AuthActivity>(R.string.login) {
    override fun createContentView(ui: _CoordinatorLayout) {
        with(ui) {
            nestedScrollView {
                verticalLayout {
                    lparams(matchParent, wrapContent) {
                        horizontalGravity = right
                    }
                    val email = textInputLayout {
                        textInputEditText {
                            lparams(matchParent, wrapContent)
                        }
                        lparams(matchParent, wrapContent) {
                            topMargin = dip(20)
                            leftMargin = dip(10)
                            rightMargin = dip(10)
                        }
                        hint = context.getString(R.string.email)
                    }
                    val password = textInputLayout {
                        textInputEditText {
                            lparams(matchParent, wrapContent)
                            inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
                        }
                        lparams(matchParent, wrapContent) {
                            topMargin = dip(20)
                            leftMargin = dip(10)
                            rightMargin = dip(10)
                        }
                        hint = context.getString(R.string.password)
                    }

                    materialColoredButton(R.string.login) {
                        setOnClickListener {
                            AuthManager.instance.login(email.editText!!.text.toString(),
                                    password.editText!!.text.toString())
                        }
                    }.lparams(dip(120), wrapContent) {
                        horizontalGravity = right
                        verticalGravity = right
                        topMargin = dip(20)
                        leftMargin = dip(10)
                        rightMargin = dip(10)
                    }
                }
            }.lparams(matchParent, wrapContent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
        }
    }
}