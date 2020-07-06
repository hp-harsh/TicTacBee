package hp.harsh.tictacbee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.itialltradetest.extensions.isEmptyString
import com.app.itialltradetest.extensions.isValidPassword
import com.app.itialltradetest.extensions.isValidUsername
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.activities.HomeActivity
import hp.harsh.tictacbee.events.OnUserAvtarChanged
import hp.harsh.tictacbee.models.User
import hp.harsh.tictacbee.utils.CommonUtil
import hp.harsh.tictacbee.utils.Constants
import hp.harsh.tictacbee.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_user_profile.*

/**
 * @purpose UpdateProfileFragment - Provides functionality of update its Avtar, basic info and password
 *
 * Everything is changed via firebase Authentication and Realtime database.
 *
 * @author Harsh Patel
 */
class UpdateProfileFragment : BaseFragment(), View.OnClickListener {

    val TAG = UpdateProfileFragment::class.java.simpleName

    val compositeDisposable = CompositeDisposable()

    fun newInstance(): UpdateProfileFragment {
        var userProfileFragment = UpdateProfileFragment()

        return userProfileFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        init()
        initRxBusListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun initRxBusListener() {
        compositeDisposable.add(
            rxBus
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { receiverObject ->
                    if (receiverObject != null) {
                        if (receiverObject is OnUserAvtarChanged) {
                            // Update firebase db
                            firebaseDbRef
                                .child(Constants.DbTables.TABLE_USERS)
                                .child(userId())
                                .child("userAvtar")
                                .setValue("" + receiverObject.avtarData.name)

                            // Update user avtar
                            setImage(userAvtar(), imgUserAvtar)
                        }
                    }
                })
        )
    }

    private fun init() {
        edtEmail.setText(userEmail())
        edtUsername.setText(userName())
        txtUserId.setText(userId())

        imgUserAvtar.setOnClickListener(this)
        btnUpdateProfile.setOnClickListener(this)
        btnUpdatePassword.setOnClickListener(this)


        // Set user avtar
        setImage(userAvtar(), imgUserAvtar)
    }

    override fun onClick(v: View?) {
        when (v) {
            imgUserAvtar -> {
                addFragment(UpdateAvtarFragment().newInstance())
            }

            btnUpdateProfile -> {
                updateBasicInfo()
            }

            btnUpdatePassword -> {
                updatePasswordInfo()
            }
        }
    }

    private fun updateBasicInfo() {
        if (!CommonUtil.isInternetAvailable(context as HomeActivity)) {
            return
        }

        if (!edtUsername.isValidUsername()) {
            ToastUtil.show(context, getString(R.string.toast_username_format_wrong))
            return
        }

        // Create user data
        val user = User(
            edtUsername.text?.trim().toString(),
            txtUserId.text?.trim().toString(),
            edtEmail.text?.trim().toString(),
            userAvtar(),
            gameChar(),
            boardBg(),
            userPreferredLanguage())

        firebaseDbRef
            .child(Constants.DbTables.TABLE_USERS)
            .child(userId())
            .setValue(user)!!.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                ToastUtil.show(context, getString(R.string.toast_user_profile_updated))

                // Update shared preference
                sharedPrefsHelper.set(Constants.Keys.KEY_USER_NAME, edtUsername.text?.trim().toString())
                sharedPrefsHelper.set(Constants.Keys.KEY_USER_ID, txtUserId.text?.trim().toString())
                sharedPrefsHelper.set(Constants.Keys.KEY_USER_EMAIL, edtEmail.text?.trim().toString())
            } else {
                ToastUtil.show(context, getString(R.string.toast_user_profile_update_failed))
            }
        }
    }

    private fun updatePasswordInfo() {
        if (!CommonUtil.isInternetAvailable(context as HomeActivity)) {
            return
        }

        if (!edtOldPassword.isValidPassword()) {
            ToastUtil.show(context, getString(R.string.toast_password_format_wrong))
            return
        } else if (!edtNewPassword.isValidPassword()) {
            ToastUtil.show(context, getString(R.string.toast_password_format_wrong))
            return
        }

        if (!edtOldPassword.text.toString().isEmptyString() && !edtNewPassword.text.toString().isEmptyString()) {

            Firebase.auth.signOut()

            Firebase.auth.signInWithEmailAndPassword(userEmail(), edtOldPassword.text.toString().trim())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = Firebase.auth.currentUser

                        user!!.updatePassword(edtNewPassword.text.toString())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    ToastUtil.show(context, getString(R.string.toast_user_password_updated))
                                } else {
                                    ToastUtil.show(context, getString(R.string.toast_user_password_update_failed))
                                }
                            }
                    } else {
                        ToastUtil.show(context, getString(R.string.toast_user_old_password_failed))
                    }
                }
        }
    }
}