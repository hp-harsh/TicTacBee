package hp.harsh.tictacbee.adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.activities.HomeActivity
import hp.harsh.tictacbee.events.OnGameCharacterChanged
import hp.harsh.tictacbee.models.CharaterData
import hp.harsh.tictacbee.utils.Constants
import hp.harsh.tictacbee.utils.RxBus

/**
 * @purpose UpdateAvtarAdapter - Bind list of game characters that is stored in drawable folder to make it selected by user.
 *
 * @author Harsh Patel
 */
class UpdateGameCharacterAdapter(
    context: Context?,
    listOfCharacters: MutableList<CharaterData>,
    rxBus: RxBus
) : RecyclerView.Adapter<UpdateGameCharacterAdapter.CharacterHolder>() {

    var context : Context?
    var listOfCharacters : MutableList<CharaterData>
    var res : Resources? = null
    var rxBus : RxBus? = null

    init {
        this.context = context;
        this.listOfCharacters = listOfCharacters
        this.rxBus = rxBus

        res = (context as HomeActivity).resources
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterHolder {
        var avtarHolder = CharacterHolder(LayoutInflater.from(parent.context), parent)
        return avtarHolder
    }

    override fun getItemCount(): Int {
        return listOfCharacters.size
    }

    override fun onBindViewHolder(holder: CharacterHolder, position: Int) {
        var charaterData = listOfCharacters.get(position)

        var resId : Int? = res?.getIdentifier(charaterData.name,"drawable", context?.packageName)

        holder.imgGameCharacter?.setImageResource(resId!!)

        if (charaterData.isSelected) {
            holder.imgGameCharacter?.setBackgroundResource(R.drawable.view_trophy_border_golden)
        } else {
            holder.imgGameCharacter?.setBackgroundResource(R.drawable.view_trophy_border_transparent)
        }

        holder.imgGameCharacter?.setOnClickListener(View.OnClickListener { view ->
            for (i in 0..(listOfCharacters.size - 1)) {
                if (listOfCharacters.get(i).isSelected) {
                    listOfCharacters.get(i).isSelected = false
                    break
                }
            }

            // Update adapter view
            charaterData.isSelected = true
            notifyDataSetChanged()

            // Save user avtar in SharedPref
            (context as HomeActivity).sharedPrefsHelper.set(Constants.Keys.KEY_USER_GAME_CHARACTER, "" + charaterData.name)

            // Notify to previous fragment (user profile fragment)
            rxBus?.send(OnGameCharacterChanged(charaterData))

            // Remove this fragment
            (context as HomeActivity).removeFragment()
        })
    }

    class CharacterHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.view_game_character_row, parent, false)) {

        var imgGameCharacter: ImageView? = null


        init {
            imgGameCharacter = itemView.findViewById(R.id.imgGameCharacter)
        }
    }
}