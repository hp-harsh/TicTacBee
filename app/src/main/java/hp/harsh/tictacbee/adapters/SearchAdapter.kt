package hp.harsh.tictacbee.adapters

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.activities.HomeActivity
import hp.harsh.tictacbee.events.OnBeeInvitationSend
import hp.harsh.tictacbee.events.OnUserAvtarChanged
import hp.harsh.tictacbee.models.AvtarData
import hp.harsh.tictacbee.models.User
import hp.harsh.tictacbee.utils.Constants
import hp.harsh.tictacbee.utils.RxBus

/**
 * @purpose SearchAdapter - Bind list of users that is searched before sending game request to the other user.
 *
 * @author Harsh Patel
 */
class SearchAdapter(
    context: Context?,
    listOfSearchedUser: MutableList<User>,
    rxBus: RxBus
) : RecyclerView.Adapter<SearchAdapter.SearchHolder>() {

    var context: Context?
    var listOfSearchedUser: MutableList<User>
    var res: Resources? = null
    var rxBus: RxBus? = null

    init {
        this.context = context
        this.listOfSearchedUser = listOfSearchedUser
        this.rxBus = rxBus;

        res = (context as HomeActivity).resources
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHolder {
        var searchHolder = SearchHolder(
            LayoutInflater.from(parent.context),
            parent
        )

        return searchHolder
    }

    override fun getItemCount(): Int {
        return listOfSearchedUser.size
    }

    override fun onBindViewHolder(holder: SearchHolder, position: Int) {
        var searchData = listOfSearchedUser.get(position)

        var resId: Int? = res?.getIdentifier(searchData.userAvtar, "drawable", context?.packageName)

        holder.imgAvtar?.setImageResource(resId!!)
        holder.txtBeeName?.setText("" + searchData.userId)

        holder.imgSendInvitation?.setOnClickListener(View.OnClickListener { view: View? ->
            rxBus?.send(OnBeeInvitationSend(searchData))
        })
    }

    class SearchHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.view_search_row, parent, false)) {

        var imgAvtar: ImageView? = null
        var imgSendInvitation: ImageView? = null
        var txtBeeName: TextView? = null


        init {
            imgAvtar = itemView.findViewById(R.id.imgAvtar)
            imgSendInvitation = itemView.findViewById(R.id.imgSendInvitation)
            txtBeeName = itemView.findViewById(R.id.txtBeeName)
        }
    }
}