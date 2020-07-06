package hp.harsh.tictacbee.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.app.itialltradetest.extensions.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.models.User
import hp.harsh.tictacbee.utils.CommonUtil
import hp.harsh.tictacbee.utils.Constants
import hp.harsh.tictacbee.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_register.*

/**
 * @purpose RegisterActivity - provides firebase authentication using email and password.
 *
 * Once it has registered, it will store basic info of user into firebase realtime database
 *
 * Before registration, app will verify username that is added by user. It must be unique.
 *
 * @author Harsh Patel
 */

class RegisterActivity : BaseActivity(), View.OnClickListener {

    private val TAG = RegisterActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        init()
    }

    private fun init() {
        btnRegister.setOnClickListener(this)
        txtAlreadyBee.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when (v) {
            btnRegister -> {
                checkValidation()
            }

            txtAlreadyBee -> {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    fun checkValidation() {
        if (!CommonUtil.isInternetAvailable(this)) {
            return
        }

        Log.i(TAG,"Validation: Username = " + (edtUsername.isValidUsername()))
        Log.i(TAG,"Validation: UserId = " + (edtUserId.isValidUserId()))
        Log.i(TAG,"Validation: UserId Character = " + (edtUserId.isValidUserIdCharacters()))
        Log.i(TAG,"Validation: Email = " + (edtEmail.isValidEmail()))
        Log.i(TAG,"Validation: Password = " + (edtPassword.isValidPassword()))

        if (!edtUsername.isValidUsername()) {
            ToastUtil.show(this, getString(R.string.toast_username_format_wrong))
            return
        } else if (!edtUserId.isValidUserId()) {
            ToastUtil.show(this, getString(R.string.toast_user_id_length_wrong))
            return
        } else if (!edtUserId.isValidUserIdCharacters()) {
            ToastUtil.show(this, getString(R.string.toast_user_id_format_wrong))
            return
        } else if (!edtEmail.isValidEmail()) {
            ToastUtil.show(this, getString(R.string.toast_email_format_wrong))
            return
        } else if (!edtPassword.isValidPassword()) {
            ToastUtil.show(this, getString(R.string.toast_password_format_wrong))
            return
        }

        val userId = edtUserId.text?.trim().toString()

        firebaseDbRef
            .child(Constants.DbTables.TABLE_USERS)
            .child(userId)
            .addListenerForSingleValueEvent(userIdListener)
    }

    fun doRegister() {
        if (!CommonUtil.isInternetAvailable(this)) {
            return
        }

        createUser()
    }

    private fun createUser() {
        firebaseAuth.createUserWithEmailAndPassword(
            edtEmail.text?.trim().toString(),
            edtPassword.text?.trim().toString()
        )
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser

                    doRegisterInFirebaseDb()
                } else {
                    ToastUtil.show(baseContext, getString(R.string.toast_register_failed))
                }
            }
    }

    fun doRegisterInFirebaseDb() {
        if (!CommonUtil.isInternetAvailable(this)) {
            return
        }

        //Save user Identity in db
        firebaseDbRef
            .child(Constants.DbTables.TABLE_USERS_IDENTITY)
            .child(firebaseAuth.currentUser!!.uid)
            .child("userId")
            .setValue(edtUserId.text?.trim().toString())

        // Create user data
        val user = User(
            edtUsername.text?.trim().toString(),
            edtUserId.text?.trim().toString(),
            edtEmail.text?.trim().toString(),
            Constants.GameDefaults.DEFAULT_USER_AVTAR,
            Constants.GameDefaults.DEFAULT_GAME_CHAR,
            Constants.GameDefaults.DEFAULT_BOARD_BG,
            Constants.GameDefaults.DEFAULT_PREFERRED_LANG
        )

        firebaseDbRef
            .child(Constants.DbTables.TABLE_USERS)
            .child(edtUserId.text?.trim().toString())
            .setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ToastUtil.show(
                        this@RegisterActivity,
                        getString(R.string.toast_register_successful)
                    )

                    // Redirect to login
                    onBackPressed()
                } else {
                    ToastUtil.show(this, getString(R.string.toast_register_failed))
                }
            }
    }

    val userIdListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.getValue() != null) {
                ToastUtil.show(
                    this@RegisterActivity,
                    getString(R.string.toast_userid_not_available)
                )
            } else {
                // No user id saved with this name. So, we can register this user
                doRegister()
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            ToastUtil.show(this@RegisterActivity, getString(R.string.toast_register_failed))
        }
    }
}
