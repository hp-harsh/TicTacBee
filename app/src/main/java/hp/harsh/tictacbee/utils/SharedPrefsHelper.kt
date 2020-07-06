package hp.harsh.tictacbee.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * @purpose SharedPrefsHelper - Provides basic functionality for storing and retrieving temporary data from/to SharedPreference.
 *
 * @author Harsh Patel
 */
class SharedPrefsHelper(context : Context) {

    private val sharedPreferences: SharedPreferences
    private val prefsEditor: SharedPreferences.Editor

    init {
        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCE, Context.MODE_PRIVATE)
        prefsEditor = sharedPreferences.edit()
    }

    fun set(key : String, value: String) {
        prefsEditor.putString(key, value).commit()
    }

    fun set(key : String, value: Boolean) {
        prefsEditor.putBoolean(key, value).commit()
    }

    fun get(key : String, defaultValue : String = "") : String {
        return sharedPreferences.getString(key, defaultValue)!!
    }

    fun getBoolean(key : String, defaultValue : Boolean = false) : Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)!!
    }

    fun remove(key : String) {
        prefsEditor.remove(key).commit()
    }

    fun clearAll() {
        prefsEditor.clear().commit()
    }


}