package hp.harsh.tictacbee.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.itialltradetest.extensions.setVisible
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.activities.LoginActivity
import hp.harsh.tictacbee.enums.GameStatus
import hp.harsh.tictacbee.models.InvitationData
import hp.harsh.tictacbee.utils.CommonUtil
import hp.harsh.tictacbee.utils.Constants
import hp.harsh.tictacbee.utils.ToastUtil
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * @purpose HomeFragment - Start game menu options
 * and other navigation option for Settings, Profile, Award, Request and Logout is initiated from this class
 *
 * User's number of winning, lost and draw games are also displayed in this class
 *
 * @author Harsh Patel
 */
class HomeFragment : BaseFragment(), View.OnClickListener {

    val TAG = HomeFragment::class.java.simpleName

    fun newInstance(): HomeFragment {
        var homeFragment = HomeFragment()

        return homeFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "onViewCreated")
        init()

        getWinningHistory()
    }

    private fun getWinningHistory() {
        Log.i(TAG, "getWinningHistory")
        // Add history
        firebaseDbRef
            .child(Constants.DbTables.TABLE_HISTORY)
            .child("" + userId())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null) {

                        var totalWin = 0
                        var totalLost = 0
                        var totalDraw = 0

                        for (child in dataSnapshot.children) {
                            val result: InvitationData? = child.getValue<InvitationData>(InvitationData::class.java)

                            if (result?.status == GameStatus.WIN.statusName) {
                                totalWin += 1
                            } else if (result?.status == GameStatus.LOST.statusName) {
                                totalLost += 1
                            } else {
                                totalDraw += 1
                            }

                        }

                        // Set result
                        txtTotalWin?.text = "" + totalWin
                        txtTotalLoose?.text = "" + totalLost
                        txtTotalDraw?.text = "" + totalDraw

                        calculateAward(totalWin)

                        Log.i(TAG, "onDataChange: Win: $totalWin, Lost: $totalLost, Draw: $totalDraw")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
    }

    private fun calculateAward(totalWin: Int) {
        imgBeeAward.setVisible(true)

        when(totalWin) {
            in 1..10 -> imgBeeAward.setImageResource(R.drawable.ic_trophy_1)
            in 11..25 -> imgBeeAward.setImageResource(R.drawable.ic_trophy_2)
            in 26..50 -> imgBeeAward.setImageResource(R.drawable.ic_trophy_3)
            in 51..100 -> imgBeeAward.setImageResource(R.drawable.ic_trophy_4)
            in 101..200 -> imgBeeAward.setImageResource(R.drawable.ic_trophy_5)
            in 201..300 -> imgBeeAward.setImageResource(R.drawable.ic_trophy_6)
            in 301..500 -> imgBeeAward.setImageResource(R.drawable.ic_trophy_7)
            in 501..Int.MAX_VALUE -> imgBeeAward.setImageResource(R.drawable.ic_trophy_8)
            else -> imgBeeAward.setVisible(false)
        }
    }

    private fun init() {
        txtPlayWithUser.setOnClickListener(this)
        txtPlayWithApp.setOnClickListener(this)
        txtPlayAlone.setOnClickListener(this)

        btnAward.setOnClickListener(this)
        btnInvitation.setOnClickListener(this)
        btnSettings.setOnClickListener(this)
        btnProfile.setOnClickListener(this)
        btnLogout.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            txtPlayWithUser -> {
                addFragment(SearchFragment().newInstance())
            }

            txtPlayWithApp -> {
                addFragment(PlayWithAppFragment().newInstance())
            }

            txtPlayAlone -> {
                addFragment(PlayAloneFragment().newInstance())
            }

            btnAward -> {
                addFragment(AwardFragment().newInstance())
            }

            btnInvitation -> {
                addFragment(InvitationFragment().newInstance())
            }

            btnSettings -> {
                addFragment(SettingsFragment().newInstance())
            }

            btnProfile -> {
                addFragment(UpdateProfileFragment().newInstance())
            }

            btnLogout -> {
                dpLogout()
            }
        }
    }

    private fun dpLogout() {
        if (!CommonUtil.isInternetAvailable(context!!)) {
            return
        }

        // Signout from firebase
        firebaseAuth.signOut()

        sharedPrefsHelper.clearAll()

        ToastUtil.show(context, getString(R.string.toast_logout_successful))

        // Redirect to login screen
        context!!.startActivity(Intent(context, LoginActivity::class.java))
        context!!.finish()
    }
}