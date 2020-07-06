package com.app.itialltradetest.extensions

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

/**
 * @purpose ImageView Extension - Tags are set to game board ImageView to determine user moves.
 * To get that tag back in int format this file is used.
 *
 * @author Harsh Patel
 */
fun ImageView.getIntTag(): Int {
    var value = -1
    try {
        value = this.getTag().toString().toInt()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return value
}
