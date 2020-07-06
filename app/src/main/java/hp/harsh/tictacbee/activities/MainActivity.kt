package hp.harsh.tictacbee.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.app.itialltradetest.extensions.isEmptyString
import hp.harsh.tictacbee.AppController
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.enums.UserLanguage
import hp.harsh.tictacbee.extensions.rotate
import hp.harsh.tictacbee.utils.CommonUtil
import hp.harsh.tictacbee.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_no_internet_connection.*

class MainActivity : AppCompatActivity() {

    private var mDelayHandler: Handler = Handler()

    internal val mRunnable: Runnable = Runnable {
        // Check if user is already logged in, redirect to Home page
        if (CommonUtil.isInternetAvailable(this)) {
            if (AppController.getFirebaseAuthInstance().currentUser != null) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                // Redirect user to Login page
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        manageLocale()

        setContentView(R.layout.activity_main)

        mDelayHandler.postDelayed(mRunnable, 3000)

        startProgress()
    }

    private fun manageLocale() {
        var savedLanguage = AppController.getSharedPrefsHelper().get(Constants.Keys.KEY_USER_GAME_LANGUAGE)

        if (savedLanguage.isEmptyString()) {
            // Default Language
            CommonUtil.updateLocale(this@MainActivity, UserLanguage.ENGLISH.languageCode)
        } else {
            CommonUtil.updateLocale(this@MainActivity, savedLanguage)
        }
    }

    private fun startProgress() {
        imgProgress.rotate()
    }

    override fun onDestroy() {
        imgProgress.clearAnimation()
        super.onDestroy()
    }
}
