package hp.harsh.tictacbee.utils

import android.app.Activity
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.utils.ToastUtil.show
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @purpose RxUtil - Common functionality for RxJava and RxAndroid defined here
 * such as double press to exist app
 *
 * @author Harsh Patel
 */
object RxUtil {
    var TAG = RxUtil::class.java.simpleName
    private var mDoubleBackToExitPressedOnce = false

    fun quitApp(activity: Activity?) {
        if (activity == null) {
            return
        }
        if (mDoubleBackToExitPressedOnce) {
            activity.finish()
        }
        if (!mDoubleBackToExitPressedOnce) {
            mDoubleBackToExitPressedOnce = true
            show(activity, activity.getString(R.string.toast_press_to_close))
        }
        checkDoubleBackPressTime()
    }

    private fun checkDoubleBackPressTime() {
        Single.just(Any())
            .delay(2, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    private val observer: SingleObserver<Any?>
        private get() = object : SingleObserver<Any?> {
            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {}
            override fun onSuccess(t: Any) {
                mDoubleBackToExitPressedOnce = false
            }
        }
}