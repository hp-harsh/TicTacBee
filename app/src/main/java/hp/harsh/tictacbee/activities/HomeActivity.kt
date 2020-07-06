package hp.harsh.tictacbee.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.app.itialltradetest.extensions.isEmptyString
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.events.OnGameBackPressedEvent
import hp.harsh.tictacbee.fragments.*
import hp.harsh.tictacbee.models.User
import hp.harsh.tictacbee.utils.Constants

/**
 * @purpose HomeActivity - Once user successfully logged In, app will be redirect to this activity.
 * If user is already logged in user lands here after splash.
 *
 * This functionality achieved using SharedPreference and Firebase current user status.
 *
 * This class is also responsible for device back pressed event.
 *
 * When user change language, this activity will be refreshed and open setting fragment by default.
 *
 * @author Harsh Patel
 */
class HomeActivity : BaseActivity() {

    private val TAG = HomeActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Load HomeFragment
        Log.i(TAG, "replaceFragment: 1")
        replaceFragment(HomeFragment().newInstance())

        // Check if language has been changed or not.
        // If it is changed recently from SettingFragment. Open that fragment again
        checkLanguageSettings()

        init()
    }

    private fun init() {
        // Get User Details
        getUserDetailsFromFirebaseDb()
    }

    private fun getUserDetailsFromFirebaseDb() {
        firebaseDbRef
            .child(Constants.DbTables.TABLE_USERS)
            .child(userId())
            .addListenerForSingleValueEvent(defaultUserDetailsListener)
    }

    private fun checkLanguageSettings() {
        if (sharedPrefsHelper.getBoolean(Constants.Keys.KEY_IS_LANGUAGE_CHANGED)) {
            // Update flag so SettingFragment will not open again and again
            sharedPrefsHelper.set(Constants.Keys.KEY_IS_LANGUAGE_CHANGED, false)

            addFragment(SettingsFragment().newInstance())
        }
    }

    override fun onBackPressed() {
        Log.i(TAG, "onBackPressed: " + currentFragmentTag());

        if (!currentFragmentTag().isEmptyString() && currentFragmentTag().equals(SettingsFragment::class.java.simpleName)) {
            // Restart Activity to make language effect
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else if (!currentFragmentTag().isEmptyString() && currentFragmentTag().equals(ChatFragment::class.java.simpleName)) {
            if (isFragmentHidden(ChatFragment::class.java.simpleName)) {
                // It means it is PlayWithBeeFragment
                rxBus.send(OnGameBackPressedEvent(true))
            } else {
                // Just hide Chat fragment
                hideFragment(ChatFragment::class.java.simpleName)
            }
        } else if (!currentFragmentTag().isEmptyString() && currentFragmentTag().equals(ChatFragment::class.java.simpleName)) {
            hideFragment(ChatFragment::class.java.simpleName)
        } else if (!currentFragmentTag().isEmptyString() && currentFragmentTag().equals(
                PlayWithAppFragment::class.java.simpleName)
        ) {
            Log.i(TAG, "replaceFragment: 2")
            replaceFragment(HomeFragment().newInstance())
        } else if (!currentFragmentTag().isEmptyString() && currentFragmentTag().equals(
                PlayAloneFragment::class.java.simpleName
            )
        ) {
            Log.i(TAG, "replaceFragment: 3")
            replaceFragment(HomeFragment().newInstance())
        } else if (!currentFragmentTag().isEmptyString() && currentFragmentTag().equals(NoInternetConnectionFragment::class.java.simpleName)){
            // Do nothing, it will manage automatically when internet will receive again
        } else {
            removeFragment()
        }
    }

    val defaultUserDetailsListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.getValue() != null) {
                val user = dataSnapshot.getValue(User::class.java)
                Log.i(TAG, "User: " + user.toString())

                if (user != null) {
                    sharedPrefsHelper.set(Constants.Keys.KEY_USER_NAME, user.username!!)
                    sharedPrefsHelper.set(Constants.Keys.KEY_USER_ID, user.userId!!)
                    sharedPrefsHelper.set(Constants.Keys.KEY_USER_EMAIL, user.emailId!!)
                    sharedPrefsHelper.set(Constants.Keys.KEY_USER_AVTAR, user.userAvtar!!)
                    sharedPrefsHelper.set(Constants.Keys.KEY_USER_GAME_CHARACTER, user.gameChar!!)
                    sharedPrefsHelper.set(
                        Constants.Keys.KEY_USER_GAME_BOARD,
                        user.boardBackground!!
                    )
                    sharedPrefsHelper.set(
                        Constants.Keys.KEY_USER_GAME_LANGUAGE,
                        user.preferredLanguage!!
                    )
                }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }
}
