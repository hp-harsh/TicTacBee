package com.app.itialltradetest.extensions

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*

/**
 * @purpose ViewExtensions - required extension for visibility change of view,
 * keyboard show up and close down and other common view operations are done here
 *
 * @author Harsh Patel
 */
fun View?.setVisible(visible: Boolean) {
    if (this != null) {
        visibility = if (visible) View.VISIBLE else View.GONE
    }
}

fun View?.setInvisible(invisible: Boolean) {
    if (this != null) {
        visibility = if (invisible) View.VISIBLE else View.INVISIBLE
    }
}


fun View?.isVisible(): Boolean {
    if (this != null)
        return visibility == View.VISIBLE
    return false
}

fun TextView.setTextOrHide(value: String?) {
    setVisible(!value.isNullOrEmpty())
    text = value
}

fun TextView.setTextOrInvisible(value: String?) {
    visibility = if (value.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
    text = value
}

fun ProgressBar.setAnimatedProgress(value: Int) {
    val animation = ObjectAnimator.ofInt(this, "progress", this.progress, value)
    animation.duration = 500
    animation.interpolator = DecelerateInterpolator()
    animation.start()
}

/*Recyclerview Extensions*/
fun RecyclerView.setItemSpacing(left: Int, right: Int = left, top: Int = left, bottom: Int = left) {
    this.addItemDecoration(object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.left = left
            outRect.top = top
            outRect.right = right
            outRect.bottom = bottom
        }
    })
}

fun View.showKeyBoard() {
    this.postDelayed({
        val inputManager = this.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(this, InputMethodManager.HIDE_NOT_ALWAYS)
    }, 400)
}

fun View.hideKeyBoard() {
    val inputManager = this.context
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

fun View.hideKeyboardOnTouch() {
    setOnTouchListener { _, _ ->
        hideKeyBoard()
        false
    }
}

fun EditText.onRightDrawableClickListener(threshHold: Int = 0, func: () -> Unit) {
    this.setOnTouchListener(OnTouchListener { v, event ->
        val DRAWABLE_LEFT = 0
        val DRAWABLE_TOP = 1
        val DRAWABLE_RIGHT = 2
        val DRAWABLE_BOTTOM = 3

        if (event.action == MotionEvent.ACTION_UP) {
            val bound = this.getCompoundDrawables()[DRAWABLE_RIGHT]
            if (event.rawX >= this.getRight() - (bound?.getBounds()?.width() ?: 0) - threshHold) {
                func()
                return@OnTouchListener false
            }
        }
        false
    })
}

fun View.setMargin(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0, start: Int = 0, end: Int = 0) {
    if (this.getLayoutParams() is ViewGroup.MarginLayoutParams) {
        val params = this.getLayoutParams() as ViewGroup.MarginLayoutParams
        params.leftMargin = dpToPx(left.toFloat())
        params.topMargin = dpToPx(top.toFloat())
        params.rightMargin = dpToPx(right.toFloat())
        params.bottomMargin = dpToPx(bottom.toFloat())
        params.marginStart = dpToPx(start.toFloat())
        params.marginEnd = dpToPx(end.toFloat())
        this.requestLayout();
    }
}

fun View.setLinearLayoutGravity(value: Int = Gravity.LEFT) {
    val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        gravity = value
    }

    this.layoutParams = params
}

fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)

fun Context.dpToPx(dp: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

fun ImageView?.setTint(color: Int) {
    this?.setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY)
}
