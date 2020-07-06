package hp.harsh.tictacbee.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.app.itialltradetest.extensions.isValidEmail
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.utils.CommonUtil
import hp.harsh.tictacbee.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_forgot_password.*

/**
 * @purpose ForgotPasswordActivity - When user do forgot password, firebase send resent link to registered email address
 *
 * @author Harsh Patel
 */
class ForgotPasswordActivity : BaseActivity(), View.OnClickListener {

    private val TAG = ForgotPasswordActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        init()
    }

    private fun init() {
        btnResetPassword.setOnClickListener(this)
        txtRememberPassword.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            btnResetPassword -> {
                doResetPassword()
            }

            txtRememberPassword -> {
                onBackPressed()
            }
        }
    }

    private fun doResetPassword() {
        if (!CommonUtil.isInternetAvailable(this)) {
            return
        }

        if (!edtEmail.isValidEmail()) {
            ToastUtil.show(this, getString(R.string.toast_email_format_wrong))
            return
        }

        firebaseAuth.sendPasswordResetEmail(edtEmail.text.toString().trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ToastUtil.show(this, getString(R.string.toast_reset_email_sent_successful))

                    onBackPressed()
                } else {
                    ToastUtil.show(this, getString(R.string.toast_reset_email_sent_failed))
                }
            }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
