package hp.harsh.tictacbee.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.app.itialltradetest.extensions.isValidEmail
import com.app.itialltradetest.extensions.isValidPassword
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.utils.CommonUtil
import hp.harsh.tictacbee.utils.Constants
import hp.harsh.tictacbee.utils.RxUtil
import hp.harsh.tictacbee.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_login.*

/**
 * @purpose LoginActivity - provides firebase authentication using registered email and password
 *
 * SharedPreference will store loggedIn user id to perform auto login when user open app next time.
 *
 * @author Harsh Patel
 */
class LoginActivity : BaseActivity(), View.OnClickListener {

    private val TAG = LoginActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        init()
    }

    private fun init() {
        btnLogin.setOnClickListener(this)
        txtForgotPassword.setOnClickListener(this)
        txtNewBee.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            btnLogin -> {
                doLogin()
            }

            txtForgotPassword -> {
                startActivity(Intent(this, ForgotPasswordActivity::class.java))
                finish()
            }

            txtNewBee -> {
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
        }
    }

    fun doLogin() {
        if (!CommonUtil.isInternetAvailable(this)) {
            return
        }

        if (!edtEmail.isValidEmail()) {
            ToastUtil.show(this, getString(R.string.toast_email_format_wrong))
            return
        } else if(!edtPassword.isValidPassword()) {
            ToastUtil.show(this, getString(R.string.toast_password_format_wrong))
            return
        }

        firebaseAuth.signInWithEmailAndPassword(edtEmail.text.toString().trim(), edtPassword.text.toString().trim())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    // get user id of logged in user
                    firebaseDbRef
                        .child(Constants.DbTables.TABLE_USERS_IDENTITY)
                        .child(firebaseAuth.currentUser!!.uid).child("userId")
                        .addListenerForSingleValueEvent(identityListener)
                } else {
                    ToastUtil.show(this, getString(R.string.toast_login_failed))
                }
            }
    }

    val identityListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.getValue() != null) {
                Log.i(TAG,"data: " + dataSnapshot.getValue())
                // Save in shared preference
                sharedPrefsHelper.set(Constants.Keys.KEY_USER_ID, "" + dataSnapshot.value)

                val user = firebaseAuth.currentUser

                //firebaseAuth.signOut()

                ToastUtil.show(this@LoginActivity, getString(R.string.toast_login_successful))

                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }
    }

    override fun onBackPressed() {
        RxUtil.quitApp(this)
    }
}
