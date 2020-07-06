package com.app.itialltradetest.extensions

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import java.util.regex.Pattern

/**
 * @purpose TextExtensions - checks if TextView is empty or not. It also determine valid email and user Id pattern
 *
 * @author Harsh Patel
 */
fun TextView.getTrimmedText(): String {
    return this.text.toString().trim()
}

fun TextView.isEmptyText(): Boolean {
    return TextUtils.isEmpty(this.getTrimmedText())
}

fun TextView.isValidEmail(): Boolean {
    return if (this.getTrimmedText().isEmpty()) {
        false
    } else {
        Patterns.EMAIL_ADDRESS.matcher(this.getTrimmedText()).matches()
    }
}

fun TextView.isValidPassword(): Boolean {
    return if (this.getTrimmedText().isEmpty()) {
        false
    } else if (this.getTrimmedText().length < 8 || this.getTrimmedText().length > 20) {
        false
    } else {
        true
    }
}

fun TextView.isValidUsername(): Boolean {
    return if (this.getTrimmedText().isEmpty()) {
        false
    } else if (this.getTrimmedText().length < 6 || this.getTrimmedText().length > 20) {
        false
    } else {
        true
    }
}

fun TextView.isValidUserIdCharacters(): Boolean {
    val regex = "^[a-zA-Z0-9_]+$"
    val pattern = Pattern.compile(regex)

    return if (this.getTrimmedText().isEmpty()) {
        false
    } else {
        pattern.matcher(this.text).matches()
    }
}

fun TextView.isValidUserId(): Boolean {
    return if (this.getTrimmedText().isEmpty()) {
        false
    } else if (this.getTrimmedText().length < 6 || this.getTrimmedText().length > 15) {
        false
    } else {
        true
    }
}
