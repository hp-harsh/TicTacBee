package com.app.itialltradetest.extensions

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import android.util.Patterns
import android.view.View
import hp.harsh.tictacbee.utils.Constants

/**
 * @purpose ValidationUtil - Extension that is used to check string common requirement
 *
 * @author Harsh Patel
 */
fun String.isEmptyString(): Boolean {
    return (this == null || this.trim().length == 0)
}

fun String.isValidEmail(): Boolean {
    /*val pattern = Constants.EMAIL_PATTERN
    val matcher = pattern.matcher(this)
    return matcher.matches()*/
    return false
}

fun String.isValidPhoneNumber(): Boolean {
    return this.matches("[0-9]*".toRegex()) && this.length in 9..12
}

fun String.isValidPassword(): Boolean {
    return this.length in 6..15
}

fun String. isValidFirstName(): Boolean {
    return this.isNotEmpty()
}

fun String.isValidLastName(): Boolean {
    return this.isNotEmpty()
}

fun String.isValidText(): Boolean {
    return this.isNotEmpty()
}

fun String.isValidEnrollmentNumber(): Boolean {
    return this.isNotEmpty()
}

fun String.isValidInstituteName(): Boolean {
    return this.isNotEmpty()
}