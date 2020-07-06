package hp.harsh.tictacbee.utils

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.util.DisplayMetrics
import hp.harsh.tictacbee.AppController
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.activities.BaseActivity
import hp.harsh.tictacbee.activities.HomeActivity
import hp.harsh.tictacbee.activities.MainActivity
import java.util.*

/**
 * @purpose CommonUtil - All common methods those are used through out the app are defined here.
 *
 * @author Harsh Patel
 */
object CommonUtil {

    fun updateLocale(context: BaseActivity, languageCodeToLoad: String) {
        val locale = Locale(languageCodeToLoad)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        context.getBaseContext().getResources().updateConfiguration(
            config,
            context.getBaseContext().getResources().getDisplayMetrics()
        )
    }

    fun updateLocale(context: MainActivity, languageCodeToLoad: String) {
        val locale = Locale(languageCodeToLoad)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        context.getBaseContext().getResources().updateConfiguration(
            config,
            context.getBaseContext().getResources().getDisplayMetrics()
        )
    }

    fun isInternetAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            if (netInfo != null && netInfo.isConnectedOrConnecting) {
                return true
            } else {
                ToastUtil.show(context, context.getString(R.string.toast_no_internet))
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getDeviceWidth(context: Context) : Int {
        val displayMetrics = DisplayMetrics()
        (context as HomeActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)

        return displayMetrics.widthPixels
    }

    fun getDeviceHeight(context: Context) : Int {
        val displayMetrics = DisplayMetrics()
        (context as HomeActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)

        return displayMetrics.heightPixels
    }
}