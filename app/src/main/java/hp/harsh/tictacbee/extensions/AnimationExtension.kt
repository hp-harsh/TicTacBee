package hp.harsh.tictacbee.extensions

import android.animation.ObjectAnimator
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import androidx.animation.Infinite

/**
 * @purpose Animation Extension - all the required view animations for scaling, fading, rotating etc are comes from this file
 *
 * @author Harsh Patel
 */
fun View.fade(from: Float, to: Float, duration: Long, func: (Animation) -> Unit = {}) {
    val fadeIn = AlphaAnimation(from, to)
    fadeIn.duration = duration
    func(fadeIn)
    startAnimation(fadeIn)
}

fun View.scale(fromX: Float, toX: Float, fromY: Float, toY: Float, pivotX: Float, pivotY: Float, duration: Long, isRepeat: Boolean, func: (Animation) -> Unit = {}) {
    val anim = ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY)
    anim.duration = duration

    if (isRepeat) {
        anim.repeatCount = -1
        anim.repeatMode = ObjectAnimator.REVERSE
    }
    func(anim)
    startAnimation(anim)
}

fun View.scale(fromX: Float, toX: Float, fromY: Float, toY: Float, duration: Long, func: (Animation) -> Unit = {}) {
    val anim = ScaleAnimation(fromX, toX, fromY, toY)
    anim.duration = duration
    func(anim)
    startAnimation(anim)
}

fun View.rotate() {
    val anim = ObjectAnimator.ofFloat(this, View.ROTATION, 0f, 360f)
    anim.duration = 1000
    anim.repeatCount = -1
    anim.repeatMode = ObjectAnimator.RESTART
    anim.start()

}

fun ViewGroup.runLayoutAnimation(resourceId: Int) {
    val context = this.context
    val controller = AnimationUtils.loadLayoutAnimation(context, resourceId)

    this.layoutAnimation = controller
    (this as? RecyclerView)?.adapter?.notifyDataSetChanged()
    this.scheduleLayoutAnimation()
}