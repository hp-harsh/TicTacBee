package hp.harsh.tictacbee.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.itialltradetest.extensions.isEmptyString
import com.app.itialltradetest.extensions.setVisible
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.adapters.SearchAdapter
import hp.harsh.tictacbee.enums.InvitationStatus
import hp.harsh.tictacbee.events.OnBeeInvitationSend
import hp.harsh.tictacbee.models.InvitationData
import hp.harsh.tictacbee.models.User
import hp.harsh.tictacbee.utils.Constants
import hp.harsh.tictacbee.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_user_profile.*

/**
 * @purpose SearchFragment - User can search any user and send request to the other user to play game.
 *
 * However, it checks if user has already sent request before or opponent user is already has request or in ongoing game?
 * If it is, they will show message to send request next time.
 *
 * Or else user can send request and redirected to PlayWithBeeFragment to wait for opponent response to accept or reject.
 *
 * @author Harsh Patel
 */
class SearchFragment : BaseFragment(), View.OnClickListener, TextWatcher {

    val TAG = SearchFragment::class.java.simpleName

    val compositeDisposable = CompositeDisposable()

    var listOfSearchedUser = mutableListOf<User>();

    var searchAdapter: SearchAdapter? = null

    var isViewEnable = false

    fun newInstance(): SearchFragment {
        var searchFragment = SearchFragment()

        return searchFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isViewEnable = true
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
        initSearchList()
        initRxBusListener()
    }

    override fun onDestroyView() {
        isViewEnable = false
        compositeDisposable.clear()
        super.onDestroyView()
    }

    private fun initRxBusListener() {
        compositeDisposable.add(
            rxBus
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { receiverObject ->
                    if (receiverObject != null) {
                        if (receiverObject is OnBeeInvitationSend) {
                            checkAnyExistenceSenderInvitation(receiverObject.invitedUser)
                        }
                    }
                })
        )
    }

    private fun checkAnyExistenceSenderInvitation(invitedUser: User) {
        Log.i(TAG, "checkAnyExistenceSenderInvitation: invitedUser: " + invitedUser.toString())
        // If there is no any invitation available, send new invitation.
        // Or else, inform user to check existing invitation to play or cancel
        firebaseDbRef
            .child(Constants.DbTables.TABLE_INVITATION)
            .child(userId())
            .child("status")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (isViewEnable) {
                        if (dataSnapshot.value != null) {
                            ToastUtil.show(
                                context,
                                getString(R.string.toast_already_sent_invitation_to_bee)
                            )

                        } else {
                            // If there is no existing invitation availble, it means user is free of all the game
                            checkAnyExistingReceiverInvitation(invitedUser)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    ToastUtil.show(context, getString(R.string.toast_error_sending_invitation))
                }
            })
    }

    private fun checkAnyExistingReceiverInvitation(invitedUser: User) {
        Log.i(TAG, "checkAnyExistingReceiverInvitation: invitedUser: " + invitedUser.toString())
        firebaseDbRef
            .child(Constants.DbTables.TABLE_INVITATION)
            .child("" + invitedUser.userId)
            .child("status")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (isViewEnable) {
                        if (dataSnapshot.value != null) {
                            ToastUtil.show(
                                context,
                                getString(R.string.toast_already_have_invitation_to_receiver)
                            )

                        } else {
                            // If there is no existing invitation available, it means invited user is free of all the game
                            addRecordToDbForSenderAndInviteUser(invitedUser)
                            addRecordToDbForReceiver(invitedUser)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    ToastUtil.show(context, getString(R.string.toast_error_sending_invitation))
                }
            })
    }

    private fun addRecordToDbForSenderAndInviteUser(invitedUser: User) {
        val invitationData = InvitationData(
            "" + userName(),
            "" + userId(),
            "" + userAvtar(),
            "" + invitedUser.username,
            "" + invitedUser.userId,
            "" + invitedUser.userAvtar,
            "" + InvitationStatus.SEND_INVITATION.statusName
        )

        firebaseDbRef
            .child(Constants.DbTables.TABLE_INVITATION)
            .child(userId())
            .setValue(invitationData)
    }

    private fun addRecordToDbForReceiver(invitedUser: User) {
        val invitationData = InvitationData(
            "" + userName(),
            "" + userId(),
            "" + userAvtar(),
            "" + invitedUser.username,
            "" + invitedUser.userId,
            "" + invitedUser.userAvtar,
            "" + InvitationStatus.RECEIVE_INVITATION.statusName
        )

        firebaseDbRef
            .child(Constants.DbTables.TABLE_INVITATION)
            .child("" + invitedUser.userId)
            .setValue(invitationData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ToastUtil.show(context, getString(R.string.toast_invitation_sent))

                    // Redirect user to play game
                    removeFragment()
                    addFragment(PlayWithBeeFragment().newInstance())

                } else {
                    ToastUtil.show(context, getString(R.string.toast_error_sending_invitation))
                }
            }
    }


    fun init() {
        imgSearch.setOnClickListener(this)
        edtSearchById.addTextChangedListener(this)

        getLatestData()
    }

    private fun initSearchList() {
        searchRecyclerView.layoutManager = LinearLayoutManager(context)

        searchAdapter = SearchAdapter(context, listOfSearchedUser, rxBus)
        searchRecyclerView.adapter = searchAdapter
    }

    private fun getLatestData() {
        // Create query to get data
        firebaseDbRef
            .child(Constants.DbTables.TABLE_USERS)
            .limitToFirst(100)
            .addListenerForSingleValueEvent(searchedAllDataListener)
    }

    private fun getFilteredData() {
        val searchId = edtSearchById.text.toString().trim()
        if (!searchId.isEmptyString()) {
            // Create query to get filtered data
            firebaseDbRef
                .child(Constants.DbTables.TABLE_USERS)
                .child(searchId)
                .addListenerForSingleValueEvent(searchedDataListener)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            imgSearch -> {
                getFilteredData()
            }
        }
    }

    var searchedDataListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (isViewEnable) {
                if (dataSnapshot.value != null) {
                    Log.i(TAG, "DataSnapshot: " + dataSnapshot.value)
                    listOfSearchedUser.clear()

                    val user = dataSnapshot.getValue(User::class.java)
                    // Do not add current user in search list
                    if (!user?.userId.equals(userId())) {
                        listOfSearchedUser.add(user!!)
                    }

                    initSearchList()

                    txtNoData.setVisible(false)
                    searchRecyclerView.setVisible(true)
                } else {
                    txtNoData.setVisible(true)
                    searchRecyclerView.setVisible(false)
                }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.i(TAG, "onCancelled: " + databaseError.toException())
        }
    }

    var searchedAllDataListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (isViewEnable) {
                if (dataSnapshot.value != null) {
                    Log.i(TAG, "DataSnapshot: " + dataSnapshot.value)
                    listOfSearchedUser.clear()

                    for (userData in dataSnapshot.children) {
                        val user = userData.getValue(User::class.java)

                        // Do not add current user in search list
                        if (!user?.userId.equals(userId())) {
                            listOfSearchedUser.add(user!!)
                        }
                    }

                    initSearchList()

                    txtNoData.setVisible(false)
                    searchRecyclerView.setVisible(true)
                } else {
                    txtNoData.setVisible(true)
                    searchRecyclerView.setVisible(false)
                }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.i(TAG, "onCancelled: " + databaseError.toException())
        }
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (count == 0) {
            getLatestData()
        } else if (count > 3) {
            getFilteredData()
        }
    }
}