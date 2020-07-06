package hp.harsh.tictacbee.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.app.itialltradetest.extensions.isEmptyString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import hp.harsh.tictacbee.AppController
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.activities.HomeActivity
import hp.harsh.tictacbee.utils.Constants
import hp.harsh.tictacbee.utils.RxBus
import hp.harsh.tictacbee.utils.SharedPrefsHelper

/**
 * @purpose BaseFragment - Provides base functionality and common functionalities for derived activities.
 * It contains Shared Preference Helper, Firebase authentication reference, RxBus for even triggering,
 * fragment transactions and network change functionality
 *
 * It also return logged in user information and some common functionality to set image, set image background and board background
 *
 * @author Harsh Patel
 */
open class BaseFragment : Fragment() {
    private val TAG = BaseFragment::class.java.simpleName

    val sharedPrefsHelper : SharedPrefsHelper = AppController.getSharedPrefsHelper()
    val firebaseAuth : FirebaseAuth = AppController.getFirebaseAuthInstance()
    val firebaseDb : FirebaseDatabase = AppController.getFirebaseDbInstance()
    val firebaseDbRef : DatabaseReference = AppController.getFirebaseDbRef()
    val rxBus : RxBus = AppController.getRxBus()

    var context : HomeActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context = (activity as HomeActivity)
    }

    fun addFragment(fragment : Fragment) {
        (context as AppCompatActivity).supportFragmentManager?.beginTransaction()
            ?.add(R.id.fragmentContainer, fragment, fragment::class.java.simpleName)
            //?.addToBackStack(fragment::class.java.simpleName)
            ?.commitAllowingStateLoss()
    }

    fun addHiddenFragment(fragment : Fragment) {
        (context as AppCompatActivity).supportFragmentManager?.beginTransaction()
            ?.add(R.id.fragmentContainer, fragment, fragment::class.java.simpleName)
            ?.hide(fragment)
            //?.addToBackStack(fragment::class.java.simpleName)
            ?.commitAllowingStateLoss()
    }

    fun replaceFragment(fragment : Fragment) {
        Log.i(TAG, "replaceFragment: " + currentFragmentTag())
        //clearBackStack()
        (context as AppCompatActivity).supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragmentContainer, fragment, fragment::class.java.simpleName)
            //?.addToBackStack(fragment::class.java.simpleName)
            ?.commitAllowingStateLoss()
    }

    fun removeFragment() {
        if (currentFragmentTag() != HomeFragment::class.java.simpleName) {
            (context as AppCompatActivity).supportFragmentManager?.beginTransaction()?.remove((context as AppCompatActivity).supportFragmentManager?.findFragmentByTag(currentFragmentTag())!!)?.commitAllowingStateLoss()
        }

        /*val backStackEntryCount : Int = fragManager?.backStackEntryCount!!

        if (backStackEntryCount > 1) {
            fragManager?.beginTransaction()?.remove(fragManager?.findFragmentByTag(currentFragmentTag())!!)?.commitAllowingStateLoss()
            //fragManager?.popBackStack()
        } */
    }

    fun currentFragmentTag() : String {
        return "" + (context as AppCompatActivity).supportFragmentManager?.findFragmentById(R.id.fragmentContainer)?.tag
    }

    fun clearBackStack() {
        val backStackEntryCount : Int = (context as AppCompatActivity).supportFragmentManager?.backStackEntryCount!!

        if (backStackEntryCount > 0) {
            val first = (context as AppCompatActivity).supportFragmentManager?.getBackStackEntryAt(0)
            (context as AppCompatActivity).supportFragmentManager?.popBackStack(first!!.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    fun showFragment(fragmentTag: String) {
        val fragment: Fragment? = (context as AppCompatActivity).supportFragmentManager?.findFragmentByTag(fragmentTag)

        fragment?.let { (context as AppCompatActivity).supportFragmentManager?.beginTransaction()?.show(it)?.commitAllowingStateLoss()
        }
    }

    fun hideFragment(fragmentTag: String) {
        val fragment: Fragment? = (context as AppCompatActivity).supportFragmentManager?.findFragmentByTag(fragmentTag)

        (context as AppCompatActivity).supportFragmentManager?.findFragmentByTag(fragmentTag)

        fragment?.let {
            (context as AppCompatActivity).supportFragmentManager?.beginTransaction()?.hide(it)?.commitAllowingStateLoss()

        }
    }

    fun isFragmentExist(fragmentTag: String) : Boolean {
        return (context as AppCompatActivity).supportFragmentManager?.findFragmentByTag(fragmentTag) != null
    }

    fun isFragmentHidden(fragmentTag: String) : Boolean {
        val fragment: Fragment? = (context as AppCompatActivity).supportFragmentManager?.findFragmentByTag(fragmentTag)

        return fragment?.isHidden!!
    }

    fun firebaseUserId() : String {
        return firebaseAuth.currentUser!!.uid
    }

    fun userName() : String {
        return sharedPrefsHelper.get(Constants.Keys.KEY_USER_NAME)
    }

    fun userId() : String {
        return sharedPrefsHelper.get(Constants.Keys.KEY_USER_ID)
    }

    fun userEmail() : String {
        return sharedPrefsHelper.get(Constants.Keys.KEY_USER_EMAIL)
    }

    fun userAvtar() : String {
        return sharedPrefsHelper.get(Constants.Keys.KEY_USER_AVTAR, Constants.GameDefaults.DEFAULT_USER_AVTAR)
    }

    fun gameChar() : String {
        return sharedPrefsHelper.get(Constants.Keys.KEY_USER_GAME_CHARACTER, Constants.GameDefaults.DEFAULT_GAME_CHAR)
    }

    fun boardBg() : String {
        return sharedPrefsHelper.get(Constants.Keys.KEY_USER_GAME_BOARD, Constants.GameDefaults.DEFAULT_BOARD_BG)
    }

    fun userPreferredLanguage() : String {
        return sharedPrefsHelper.get(Constants.Keys.KEY_USER_GAME_LANGUAGE, Constants.GameDefaults.DEFAULT_PREFERRED_LANG)
    }

    fun setImage(imageName : String, imgUser: ImageView) {
        if (!imageName.isEmptyString()) {
            val res = (context as HomeActivity).resources
            var resId: Int? = res?.getIdentifier(imageName, "drawable", context?.packageName)
            imgUser.setImageResource(resId!!)
        }
    }

    fun setImageBg(colorName : Int, img: ImageView) {
        img.setBackgroundColor(colorName)
    }

    fun setBackground(backgroundName : String, lnrView: LinearLayout) {
        if (!userAvtar().isEmptyString()) {
            val res = (context as HomeActivity).resources
            var resId: Int? = res?.getIdentifier(backgroundName, "drawable", context?.packageName)
            lnrView.setBackgroundResource(resId!!)
        }
    }
}