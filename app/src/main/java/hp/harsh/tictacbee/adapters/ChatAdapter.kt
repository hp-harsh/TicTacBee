package hp.harsh.tictacbee.adapters

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.itialltradetest.extensions.setLinearLayoutGravity
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.models.ChatData
import hp.harsh.tictacbee.models.InvitationData

/**
 * @purpose ChatAdapter - Bind user and opponents text messages to the recycler view.
 *
 * It will check if message is sent by loggedIn user, it will set layout right side otherwise it will set layout left side.
 *
 * @author Harsh Patel
 */
class ChatAdapter(
    context: Context?,
    userId: String,
    invitationData: InvitationData?,
    arrChatHistoryData: MutableList<ChatData>
) : RecyclerView.Adapter<ChatAdapter.ChatHolder>() {

    var context: Context?
    var userId: String = ""
    var invitationData: InvitationData?
    var listOfChatData: MutableList<ChatData>

    init {
        this.context = context
        this.userId = userId
        this.invitationData = invitationData
        this.listOfChatData = arrChatHistoryData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        var searchHolder = ChatHolder(
            LayoutInflater.from(parent.context),
            parent
        )

        return searchHolder
    }

    override fun getItemCount(): Int {
        return listOfChatData.size
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        var chat = listOfChatData.get(position)

        holder.txtMessage?.setText("" + chat.message)

        setSender(chat, holder.txtMessage)
    }

    class ChatHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.view_chat_row, parent, false)) {

        var txtMessage: TextView? = null


        init {
            txtMessage = itemView.findViewById(R.id.txtMessage)
        }
    }

    public fun appendNewMessage(newChatList: MutableList<ChatData>) {
        if (listOfChatData != null) {
            listOfChatData = mutableListOf<ChatData>()
        }

        listOfChatData.clear()
        listOfChatData = newChatList
        notifyDataSetChanged()
    }

    private fun setSender(
        chat: ChatData,
        txtMessage: TextView?
    ) {
        if (chat.messageSenderId == userId) {
            txtMessage?.background = context?.getDrawable(R.drawable.view_round_corner_border_sender)
            txtMessage?.setLinearLayoutGravity(Gravity.END)
        } else {
            txtMessage?.background = context?.getDrawable(R.drawable.view_round_corner_border_receiver)
            txtMessage?.setLinearLayoutGravity(Gravity.START)
        }
    }
}