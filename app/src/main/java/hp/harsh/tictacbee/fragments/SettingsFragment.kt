package hp.harsh.tictacbee.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.itialltradetest.extensions.isEmptyString
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.activities.HomeActivity
import hp.harsh.tictacbee.enums.UserLanguage
import hp.harsh.tictacbee.events.OnGameBoardChanged
import hp.harsh.tictacbee.events.OnGameCharacterChanged
import hp.harsh.tictacbee.utils.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_settings.*

/**
 * @purpose SettingsFragment - provide an option to change board bg, game character and language.
 *
 * @author Harsh Patel
 */
class SettingsFragment : BaseFragment(), View.OnClickListener {

    val TAG = SettingsFragment::class.java.simpleName

    val compositeDisposable = CompositeDisposable()

    var isViewEnable = false

    fun newInstance(): SettingsFragment {
        var settingsFragment = SettingsFragment()

        return settingsFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isViewEnable = true
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
        initRxBusListener()
        setLanguage()
        setGameChar()
        setGameBoard()
    }

    override fun onDestroy() {
        isViewEnable = false
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun initRxBusListener() {
        compositeDisposable.add(rxBus
            .toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer { receiverObject ->
                if (receiverObject != null) {
                    if (receiverObject is OnGameCharacterChanged) {
                        // Update firebase db
                        firebaseDbRef
                            .child(Constants.DbTables.TABLE_USERS)
                            .child(userId())
                            .child("gameChar")
                            .setValue("" + receiverObject.characterData.name)

                        // Update game character
                        setGameChar()
                    } else if (receiverObject is OnGameBoardChanged) {
                        // Update firebase db
                        firebaseDbRef
                            .child(Constants.DbTables.TABLE_USERS)
                            .child(userId())
                            .child("boardBackground")
                            .setValue("" + receiverObject.boardData.name)

                        // Update game board
                        setGameBoard()
                    }
                }
            }))
    }

    private fun setGameChar() {
        // Get selected character from shared preference
        val savedCharName : String = sharedPrefsHelper.get(Constants.Keys.KEY_USER_GAME_CHARACTER)

        if (!savedCharName.isEmptyString()) {
            val res = (context as HomeActivity).resources
            var resId : Int? = res?.getIdentifier(savedCharName,"drawable", context?.packageName)
            imgGameCharacter.setImageResource(resId!!)
        }
    }

    private fun setGameBoard() {
        // Get selected board from shared preference
        val savedBoardName : String = sharedPrefsHelper.get(Constants.Keys.KEY_USER_GAME_BOARD)

        if (!savedBoardName.isEmptyString()) {
            val res = (context as HomeActivity).resources
            var resId : Int? = res?.getIdentifier(savedBoardName,"drawable", context?.packageName)
            imgGameBoard.setImageResource(resId!!)
        }
    }

    private fun setLanguage() {
        var savedLanguage = sharedPrefsHelper.get(Constants.Keys.KEY_USER_GAME_LANGUAGE)

        updateLanguageSetting(savedLanguage)
    }

    fun init() {
        imgGameCharacter.setOnClickListener(this)
        imgGameBoard.setOnClickListener(this)

        txtEnglish.setOnClickListener(this)
        txtFrench.setOnClickListener(this)
        txtHindi.setOnClickListener(this)
        txtChinese.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            imgGameCharacter -> {
                addFragment(UpdateGameCharacterFragment().newInstance())
            }

            imgGameBoard -> {
                addFragment(UpdateGameBoardFragment().newInstance())
            }

            txtEnglish -> {
                updateLanguageSetting(UserLanguage.ENGLISH.languageCode)
                restartActivity()
            }

            txtFrench -> {
                updateLanguageSetting(UserLanguage.FRENCH.languageCode)
                restartActivity()
            }

            txtHindi -> {
                updateLanguageSetting(UserLanguage.HINDI.languageCode)
                restartActivity()
            }

            txtChinese -> {
                updateLanguageSetting(UserLanguage.CHINESE.languageCode)
                restartActivity()
            }
        }
    }

    private fun updateLanguageSetting(preferredLanguageCode : String) {
        // Update shared preference
        sharedPrefsHelper.set(Constants.Keys.KEY_USER_GAME_LANGUAGE, "" + preferredLanguageCode)

        // Update firebase db
        firebaseDbRef
            .child(Constants.DbTables.TABLE_USERS)
            .child(userId())
            .child("preferredLanguage")
            .setValue("" + preferredLanguageCode)

        // Update UI
        when (preferredLanguageCode) {
            UserLanguage.ENGLISH.languageCode -> {
                txtEnglish.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_check_white, 0)
                txtFrench.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                txtHindi.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                txtChinese.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)

                txtEnglish.setBackgroundResource(R.drawable.view_user_border_golden)
                txtFrench.setBackgroundResource(R.drawable.view_trophy_border_transparent)
                txtHindi.setBackgroundResource(R.drawable.view_trophy_border_transparent)
                txtChinese.setBackgroundResource(R.drawable.view_trophy_border_transparent)
            }

            UserLanguage.FRENCH.languageCode -> {
                txtEnglish.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                txtFrench.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_check_white, 0)
                txtHindi.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                txtChinese.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)

                txtEnglish.setBackgroundResource(R.drawable.view_trophy_border_transparent)
                txtFrench.setBackgroundResource(R.drawable.view_user_border_golden)
                txtHindi.setBackgroundResource(R.drawable.view_trophy_border_transparent)
                txtChinese.setBackgroundResource(R.drawable.view_trophy_border_transparent)
            }

            UserLanguage.HINDI.languageCode -> {
                txtEnglish.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                txtFrench.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                txtHindi.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_check_white, 0)
                txtChinese.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)

                txtEnglish.setBackgroundResource(R.drawable.view_trophy_border_transparent)
                txtFrench.setBackgroundResource(R.drawable.view_trophy_border_transparent)
                txtHindi.setBackgroundResource(R.drawable.view_user_border_golden)
                txtChinese.setBackgroundResource(R.drawable.view_trophy_border_transparent)
            }

            UserLanguage.CHINESE.languageCode -> {
                txtEnglish.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                txtFrench.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                txtHindi.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                txtChinese.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_check_white, 0)

                txtEnglish.setBackgroundResource(R.drawable.view_trophy_border_transparent)
                txtFrench.setBackgroundResource(R.drawable.view_trophy_border_transparent)
                txtHindi.setBackgroundResource(R.drawable.view_trophy_border_transparent)
                txtChinese.setBackgroundResource(R.drawable.view_user_border_golden)
            }
        }
    }

    private fun restartActivity() {
        // Notify Home activity that language has been changed
        sharedPrefsHelper.set(Constants.Keys.KEY_IS_LANGUAGE_CHANGED, true)

        // Restart Activity to make language effect
        context?.startActivity(Intent(context, HomeActivity::class.java))
        (context as HomeActivity)?.finish()
    }
}