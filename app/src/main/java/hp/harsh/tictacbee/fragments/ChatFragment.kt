package hp.harsh.tictacbee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.itialltradetest.extensions.isEmptyString
import com.app.itialltradetest.extensions.isEmptyText
import com.app.itialltradetest.extensions.setVisible
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.adapters.ChatAdapter
import hp.harsh.tictacbee.events.OnNewMessageReceived
import hp.harsh.tictacbee.models.ChatData
import hp.harsh.tictacbee.models.InvitationData
import hp.harsh.tictacbee.utils.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_chat.*

/**
 * @purpose ChatFragment - Provides base functionality to initiate chat.
 *
 * It also ask firebase to have any previous chat message if any it will get it as history and set in the chat list.
 *
 * This fragment is popped over ongoing game and it has alpha background. So user can see what opponent player is moved
 *
 * This will send and receive only text message.
 *
 * Last message that is send or received is bind to the ChatAdapter.
 *
 * @author Harsh Patel
 */
class ChatFragment : BaseFragment(), View.OnClickListener {

    val TAG = ChatFragment::class.java.simpleName

    val compositeDisposable = CompositeDisposable()

    var chatId = "";
    var invitationData: InvitationData? = null;

    var arrChatHistoryData = mutableListOf<ChatData>()
    var chatAdapter: ChatAdapter? = null

    // Because first time you will get all data from getHistory func
    // So this flag avoid to add duplicate message when you call getLastMessage func
    var areYouGettingLastMessageFirstTime = true

    var isViewEnable = false

    fun newInstance(
        chatId: String,
        invitationData: InvitationData?
    ): ChatFragment {
        var playWithBeeFragment = ChatFragment()

        playWithBeeFragment.chatId = chatId
        playWithBeeFragment.invitationData = invitationData

        return playWithBeeFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isViewEnable = true
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
        initRxBusListener()
    }

    override fun onDestroyView() {
        isViewEnable = false
        compositeDisposable.clear()
        super.onDestroyView()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgSend -> {
                if (!edtMessage.isEmptyText()) {
                    sendMessage()
                }
            }

            R.id.emptyView -> {
                hideFragment(ChatFragment::class.java.simpleName)
            }
        }
    }

    private fun init() {
        emptyView.setOnClickListener(this)
        imgSend.setOnClickListener(this)

        initChatList()

        getHistory()
        getLastMessage()
    }

    private fun initChatList() {
        listChat.layoutManager = LinearLayoutManager(context)
        chatAdapter = ChatAdapter(context, userId(), invitationData, arrChatHistoryData)

        listChat.adapter = chatAdapter
        listChat.adapter?.notifyDataSetChanged()

        scrollToBottom()
    }

    private fun initRxBusListener() {
        compositeDisposable.add(
            rxBus
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { receiverObject ->
                    if (receiverObject != null) {

                    }
                })
        )
    }

    private fun sendMessage() {

        if (!chatId.isEmptyString()) {

            var messageSenderId = ""
            var messageReceiverId = ""

            if (userId().equals("" + invitationData?.senderId)) {
                messageSenderId = "" + invitationData?.senderId
                messageReceiverId = "" + invitationData?.receiverId
            } else {
                messageSenderId = "" + invitationData?.receiverId
                messageReceiverId = "" + invitationData?.senderId
            }

            val newChatMessage = ChatData(
                messageSenderId,
                messageReceiverId,
                edtMessage.text.toString().trim(),
                System.currentTimeMillis()
            )

            firebaseDbRef
                .child(Constants.DbTables.TABLE_CHAT)
                .child(chatId)
                .child("" + System.currentTimeMillis())
                .setValue(newChatMessage)

            edtMessage.setText("")
        }
    }

    private fun getHistory() {
        firebaseDbRef
            .child(Constants.DbTables.TABLE_CHAT)
            .child(chatId)
            .orderByChild("messageTimestamp")
            .limitToLast(100)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (isViewEnable) {
                        if (dataSnapshot.value != null) {
                            arrChatHistoryData = mutableListOf<ChatData>()

                            arrChatHistoryData.clear()

                            for (child in dataSnapshot.children) {
                                val chat: ChatData? = child.getValue<ChatData>(ChatData::class.java)
                                arrChatHistoryData.add(chat!!)

                            }

                            if (arrChatHistoryData.size <= 0) {
                                txtChatData.setVisible(true)
                            } else {
                                txtChatData.setVisible(false)
                            }

                            initChatList()
                        } else {
                            txtChatData.setVisible(true)
                        }
                }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
    }

    private fun getLastMessage() {
        firebaseDbRef
            .child(Constants.DbTables.TABLE_CHAT)
            .child(chatId)
            .limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (isViewEnable) {
                        if (!areYouGettingLastMessageFirstTime) {
                            if (dataSnapshot.value != null) {
                                // Get last messsage
                                for (child in dataSnapshot.children) {
                                    val chat: ChatData? =
                                        child.getValue<ChatData>(ChatData::class.java)
                                    arrChatHistoryData.add(chat!!)
                                }

                                // If this fragment is not visible,
                                // notify PlayWithBeeFragment that new message has been arrived.
                                // So, that fragment start blinking chat icon
                                if (isFragmentHidden(ChatFragment::class.java.simpleName)) {
                                    rxBus.send(OnNewMessageReceived())
                                }

                                // Update adapter
                                chatAdapter?.appendNewMessage(arrChatHistoryData)

                                txtChatData.setVisible(false)

                                scrollToBottom()
                            }
                        } else {
                            // From next time you need new messages that has been sent or received
                            areYouGettingLastMessageFirstTime = false
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
    }

    fun scrollToBottom() {
        if (arrChatHistoryData != null && arrChatHistoryData.size > 0) {
            listChat.smoothScrollToPosition(arrChatHistoryData.size - 1)
        }
    }
}