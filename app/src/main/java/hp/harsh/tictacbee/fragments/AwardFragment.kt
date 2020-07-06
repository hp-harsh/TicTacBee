package hp.harsh.tictacbee.fragments

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
import hp.harsh.tictacbee.enums.GameStatus
import hp.harsh.tictacbee.models.InvitationData
import hp.harsh.tictacbee.utils.Constants
import kotlinx.android.synthetic.main.fragment_award.*

/**
 * @purpose AwardFragment - List of awards are displayed by this fragment
 * and also calculate user award according to the number of game user win
 *
 * @author Harsh Patel
 */
class AwardFragment : BaseFragment() {

    val TAG = AwardFragment::class.java.simpleName

    var isViewEnable = false

    fun newInstance(): AwardFragment {
        var awardFragment = AwardFragment()

        return awardFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isViewEnable = true
        return inflater.inflate(R.layout.fragment_award, container, false)
    }

    override fun onDestroy() {
        isViewEnable = false
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getWinningHistory()
    }

    private fun getWinningHistory() {
        // Add history
        firebaseDbRef
            .child(Constants.DbTables.TABLE_HISTORY)
            .child("" + userId())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null && isViewEnable) {

                        var totalWin = 0
                        var totalLost = 0
                        var totalDraw = 0

                        for (child in dataSnapshot.children) {
                            val result: InvitationData? = child.getValue<InvitationData>(
                                InvitationData::class.java)

                            if (result?.status == GameStatus.WIN.statusName) {
                                totalWin += 1
                            } else if (result?.status == GameStatus.LOST.statusName) {
                                totalLost += 1
                            } else {
                                totalDraw += 1
                            }

                        }

                        calculateAward(totalWin)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
    }

    private fun calculateAward(totalWin: Int) {

        when(totalWin) {
            in 1..10 -> rltAward1.setBackgroundResource(R.drawable.view_trophy_border_golden)
            in 11..25 -> rltAward2.setBackgroundResource(R.drawable.view_trophy_border_golden)
            in 26..50 -> rltAward3.setBackgroundResource(R.drawable.view_trophy_border_golden)
            in 51..100 -> rltAward4.setBackgroundResource(R.drawable.view_trophy_border_golden)
            in 101..200 -> rltAward5.setBackgroundResource(R.drawable.view_trophy_border_golden)
            in 201..300 -> rltAward6.setBackgroundResource(R.drawable.view_trophy_border_golden)
            in 301..500 -> rltAward7.setBackgroundResource(R.drawable.view_trophy_border_golden)
            in 501..Int.MAX_VALUE -> rltAward8.setBackgroundResource(R.drawable.view_trophy_border_golden)
        }
    }

}