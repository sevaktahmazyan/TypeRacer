package com.webbfontaine.typeracer.extensions

import android.content.Context
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText


fun View.makeVisible() {
    this.visibility = View.VISIBLE
}

fun View.makeGone() {
    this.visibility = View.GONE
}

fun View.makeInVisible() {
    this.visibility = View.INVISIBLE
}

fun EditText.openKeyBoard() {
    Handler().postDelayed(
        {
            val inputMethodManager =
                (this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            inputMethodManager.toggleSoftInputFromWindow(
                this.applicationWindowToken,
                InputMethodManager.SHOW_FORCED,
                0
            )
            this.requestFocus()
        }, 300
    )
}

fun EditText.closeKeyboard() {
    val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}