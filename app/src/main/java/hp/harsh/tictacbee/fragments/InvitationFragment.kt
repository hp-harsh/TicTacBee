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
import hp.harsh.tictacbee.enums.InvitationStatus
import hp.harsh.tictacbee.models.InvitationData
import hp.harsh.tictacbee.utils.Constants
import kotlinx.android.synthetic.main.fragment_invitation1.*

/**
 * @purpose InvitationFragment - when user has any request for new game, it wil show here.
 *
 * User has an option to accept or reject.
 *
 * If user accept request, app will redirect in to PlayWithBeeFragment view to start game.
 *
 * @author Harsh Patel
 */
class InvitationFragment : BaseFragment(), View.OnClickListener {

    val TAG = InvitationFragment::class.java.simpleName

    var invitationData: InvitationData? = null

    var isViewEnable = false

    fun newInstance(): InvitationFragment {
        var invitationFragment = InvitationFragment()

        return invitationFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isViewEnable = true
        return inflater.inflate(R.layout.fragment_invitation1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkAnyExistenceSenderInvitation()
    }

    override fun onDestroyView() {
        isViewEnable = false
        super.onDestroyView()
    }

    private fun checkAnyExistenceSenderInvitation() {
        // If there is no any invitation available, so empty message
        // Or else, present data to select or delete invitation
        firebaseDbRef
            .child(Constants.DbTables.TABLE_INVITATION)
            .child(userId())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (isViewEnable) {
                        if (dataSnapshot.value != null) {
                            invitationData =
                                dataSnapshot.getValue<InvitationData>(InvitationData::class.java)

                            Log.i(TAG, "onDataChange: invitationData: " + invitationData.toString())

                            if (invitationData?.status.equals(InvitationStatus.RECEIVE_INVITATION.statusName)) {
                                // visible invitation user
                                txtNoData.setVisible(false)
                                lnrInvitationUser.setVisible(true)

                                // Manage accept and reject
                                handleInvitationData()
                            } else {
                                // visible empty message
                                txtNoData.setVisible(true)
                                lnrInvitationUser.setVisible(false)
                            }
                        } else {
                            // visible empty message
                            txtNoData.setVisible(true)
                            lnrInvitationUser.setVisible(false)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    if (isViewEnable) {
                        // visible empty message
                        txtNoData.setVisible(true)
                        lnrInvitationUser.setVisible(false)
                    }
                }
            })
    }

    private fun handleInvitationData() {
        invitationData?.senderAvtar?.let { setImage(it, imgAvtar) }
        txtBeeName.setText("" + invitationData?.senderId)
        imgReject.setOnClickListener(this)
        imgAccept.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgAccept -> {
                updateDbForSenderUserInvitationStatus("" + InvitationStatus.PLAY_GAME.statusName)

            }
            R.id.imgReject -> {
                updateDbForSenderUserInvitationStatus("" + InvitationStatus.DECLINED_INVITATION.statusName)

                // visible empty message
                txtNoData.setVisible(true)
                lnrInvitationUser.setVisible(false)
            }
        }
    }

    private fun updateDbForSenderUserInvitationStatus(status: String) {
        firebaseDbRef
            .child(Constants.DbTables.TABLE_INVITATION)
            .child("" + invitationData?.senderId)
            .child("status")
            .setValue(status)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateDbForUserinvitationStatus("" + status)
                }
            }
    }

    private fun updateDbForUserinvitationStatus(status: String) {
        firebaseDbRef
            .child(Constants.DbTables.TABLE_INVITATION)
            .child(userId())
            .child("status")
            .setValue(status)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (status == InvitationStatus.PLAY_GAME.statusName) {
                        // Send user to play game
                        removeFragment()
                        addFragment(PlayWithBeeFragment().newInstance())
                    }
                }
            }

        if (status == InvitationStatus.DECLINED_INVITATION.statusName) {
            //Remove Invitation
            firebaseDbRef
                ?.child(Constants.DbTables.TABLE_INVITATION)
                ?.child(userId())
                ?.setValue(null)
        }
    }
}