package hp.harsh.tictacbee.utils

import android.content.Context
import android.graphics.Typeface
import android.widget.Toast

/**
 * @purpose SharedPrefsHelper - Common class to show toast message
 *
 * @author Harsh Patel
 */
object ToastUtil {
    fun show(context: Context?, message: String?) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}