package hp.harsh.tictacbee.adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.activities.HomeActivity
import hp.harsh.tictacbee.events.OnUserAvtarChanged
import hp.harsh.tictacbee.models.AvtarData
import hp.harsh.tictacbee.utils.Constants
import hp.harsh.tictacbee.utils.RxBus


/**
 * @purpose UpdateAvtarAdapter - Bind list of avtars that is stored in drawable folder to make it selected by user.
 *
 * @author Harsh Patel
 */
class UpdateAvtarAdapter(
    context: Context?,
    listOfAvtar: MutableList<AvtarData>,
    rxBus: RxBus
) : RecyclerView.Adapter<UpdateAvtarAdapter.AvtarHolder>() {

    var context : Context?
    var listOfAvtar : MutableList<AvtarData>
    var res : Resources? = null
    var rxBus : RxBus? = null

    init {
        this.context = context
        this.listOfAvtar = listOfAvtar
        this.rxBus = rxBus;

        res = (context as HomeActivity).resources
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvtarHolder {
        var avtarHolder = AvtarHolder(
                LayoutInflater.from(parent.context),
                parent)

        return avtarHolder
    }

    override fun getItemCount(): Int {
        return listOfAvtar.size
    }

    override fun onBindViewHolder(holder: AvtarHolder, position: Int) {
        var avtarData = listOfAvtar.get(position)

        var resId : Int? = res?.getIdentifier(avtarData.name,"drawable", context?.packageName)

        holder.imgUserAvtar?.setImageResource(resId!!)

        if (avtarData.isSelected) {
            holder.imgUserAvtar?.setBackgroundResource(R.drawable.view_trophy_border_golden)
        } else {
            holder.imgUserAvtar?.setBackgroundResource(R.drawable.view_trophy_border_transparent)
        }

        holder.imgUserAvtar?.setOnClickListener(View.OnClickListener {view ->
            for (i in 0..(listOfAvtar.size - 1)) {
                if (listOfAvtar.get(i).isSelected) {
                    listOfAvtar.get(i).isSelected = false
                    break
                }
            }

            // Update adapter view
            avtarData.isSelected = true
            notifyDataSetChanged()

            // Save user avtar in SharedPref
            (context as HomeActivity).sharedPrefsHelper.set(Constants.Keys.KEY_USER_AVTAR, "" + avtarData.name)

            // Notify to previous fragment (user profile fragment)
            rxBus?.send(OnUserAvtarChanged(avtarData))

            // Remove this fragment
            (context as HomeActivity).removeFragment()
        })

    }

    class AvtarHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.view_avtart_row, parent, false)) {

        var imgUserAvtar: ImageView? = null


        init {
            imgUserAvtar = itemView.findViewById(R.id.imgUserAvtar)
        }
    }
}