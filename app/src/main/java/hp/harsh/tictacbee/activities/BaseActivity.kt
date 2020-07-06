package hp.harsh.tictacbee.activities

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.app.itialltradetest.extensions.isEmptyString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import hp.harsh.tictacbee.AppController
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.enums.UserLanguage
import hp.harsh.tictacbee.fragments.HomeFragment
import hp.harsh.tictacbee.fragments.NoInternetConnectionFragment
import hp.harsh.tictacbee.receivers.ConnectivityReceiver
import hp.harsh.tictacbee.utils.CommonUtil.updateLocale
import hp.harsh.tictacbee.utils.Constants
import hp.harsh.tictacbee.utils.RxBus
import hp.harsh.tictacbee.utils.RxUtil
import hp.harsh.tictacbee.utils.SharedPrefsHelper

/**
 * @purpose BaseActivity - Provides base functionality and common functionalities for derived activities.
 * It contains Shared Preference Helper, Firebase authentication reference, RxBus for even triggering,
 * fragment transactions and network change functionality
 *
 * @author Harsh Patel
 */
open class BaseActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    private val TAG = BaseActivity::class.java.simpleName

    val sharedPrefsHelper : SharedPrefsHelper = AppController.getSharedPrefsHelper()
    val firebaseAuth : FirebaseAuth = AppController.getFirebaseAuthInstance()
    val firebaseDb : FirebaseDatabase = AppController.getFirebaseDbInstance()
    val firebaseDbRef : DatabaseReference = AppController.getFirebaseDbRef()
    val rxBus : RxBus = AppController.getRxBus()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerReceiver(ConnectivityReceiver(),
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

        var savedLanguage = sharedPrefsHelper.get(Constants.Keys.KEY_USER_GAME_LANGUAGE)

        if (savedLanguage.isEmptyString()) {
            // Default Language
            updateLocale(this, UserLanguage.ENGLISH.languageCode)
        } else {
            updateLocale(this, savedLanguage)
        }
    }

    override fun onResume() {
        super.onResume()

        ConnectivityReceiver.connectivityReceiverListener = this
    }

    fun replaceFragment(fragment : Fragment) {
        Log.i(TAG, "replaceFragment: " + currentFragmentTag())
        //removeFragment()
        //clearBackStack()

        supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragmentContainer, fragment, fragment::class.java.simpleName)
            //?.addToBackStack(fragment::class.java.simpleName)
            ?.commitAllowingStateLoss()
    }
    fun addFragment(fragment : Fragment) {
        supportFragmentManager?.beginTransaction()
            ?.add(R.id.fragmentContainer, fragment, fragment::class.java.simpleName)
            //?.addToBackStack(fragment::class.java.simpleName)
            ?.commitAllowingStateLoss()
    }

    fun removeFragment() {
        if (currentFragmentTag() != HomeFragment::class.java.simpleName) {
            supportFragmentManager?.beginTransaction()?.remove(supportFragmentManager?.findFragmentByTag(currentFragmentTag())!!)?.commitAllowingStateLoss()
        } else {
            RxUtil.quitApp(this)
        }

        /*val backStackEntryCount : Int = fragManager?.backStackEntryCount!!

        if (backStackEntryCount > 1) {
            fragManager?.beginTransaction()?.remove(fragManager?.findFragmentByTag(currentFragmentTag())!!)?.commitAllowingStateLoss()
            fragManager?.popBackStack()
        } else {
            RxUtil.quitApp(this)
        }*/
    }

    fun clearBackStack() {
        val backStackEntryCount : Int = supportFragmentManager?.backStackEntryCount!!

        if (backStackEntryCount > 0) {
            val first = supportFragmentManager?.getBackStackEntryAt(0)
            supportFragmentManager?.popBackStack(first!!.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    fun showFragment(fragmentTag: String) {
        val fragment: Fragment? = supportFragmentManager?.findFragmentByTag(fragmentTag)

        fragment?.let { supportFragmentManager?.beginTransaction()?.show(it)?.commitAllowingStateLoss()
        }
    }

    fun hideFragment(fragmentTag: String) {
        val fragment: Fragment? = supportFragmentManager?.findFragmentByTag(fragmentTag)

        supportFragmentManager?.findFragmentByTag(fragmentTag)

        fragment?.let {
            supportFragmentManager?.beginTransaction()?.hide(it)?.commitAllowingStateLoss()

        }
    }

    fun isFragmentExist(fragmentTag: String) : Boolean {
        return supportFragmentManager?.findFragmentByTag(fragmentTag) != null
    }

    fun isFragmentHidden(fragmentTag: String) : Boolean {
        val fragment: Fragment? = supportFragmentManager?.findFragmentByTag(fragmentTag)

        return fragment?.isHidden!!
    }

    fun currentFragmentTag() : String {
        return "" + supportFragmentManager?.findFragmentById(R.id.fragmentContainer)?.tag
    }

    fun userId() : String {
        return sharedPrefsHelper.get(Constants.Keys.KEY_USER_ID)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (!isConnected) {
            addFragment(NoInternetConnectionFragment().newInstance())
        } else {
            if (!currentFragmentTag().isEmptyString() && currentFragmentTag().equals(
                    NoInternetConnectionFragment::class.java.simpleName)) {
                removeFragment()
            }
        }
    }
}