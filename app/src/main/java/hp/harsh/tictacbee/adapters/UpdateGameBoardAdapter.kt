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
import hp.harsh.tictacbee.events.OnGameBoardChanged
import hp.harsh.tictacbee.models.BoardData
import hp.harsh.tictacbee.models.CharaterData
import hp.harsh.tictacbee.utils.Constants
import hp.harsh.tictacbee.utils.RxBus

/**
 * @purpose UpdateAvtarAdapter - Bind list of game board background that is stored in drawable folder to make it selected by user.
 *
 * @author Harsh Patel
 */
class UpdateGameBoardAdapter(
    context: Context?,
    listOfBoards: MutableList<BoardData>,
    rxBus: RxBus
) : RecyclerView.Adapter<UpdateGameBoardAdapter.BoardHolder>() {

    var context : Context?
    var listOfBoards : MutableList<BoardData>
    var res : Resources? = null
    var rxBus : RxBus? = null

    init {
        this.context = context
        this.listOfBoards = listOfBoards
        this.rxBus = rxBus;

        res = (context as HomeActivity).resources
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardHolder {
        var BoardHolder = BoardHolder(LayoutInflater.from(parent.context), parent)
        return BoardHolder
    }

    override fun getItemCount(): Int {
        return listOfBoards.size
    }

    override fun onBindViewHolder(holder: BoardHolder, position: Int) {
        var boardData = listOfBoards.get(position)

        var resId : Int? = res?.getIdentifier(boardData.name,"drawable", context?.packageName)

        holder.imgGameBoard?.setImageResource(resId!!)

        if (boardData.isSelected) {
            holder.imgGameBoard?.setBackgroundResource(R.drawable.view_trophy_border_golden)
        } else {
            holder.imgGameBoard?.setBackgroundResource(R.drawable.view_trophy_border_transparent)
        }

        holder.imgGameBoard?.setOnClickListener(View.OnClickListener { view ->
            for (i in 0..(listOfBoards.size - 1)) {
                if (listOfBoards.get(i).isSelected) {
                    listOfBoards.get(i).isSelected = false
                    break
                }
            }

            // Update adapter view
            boardData.isSelected = true
            notifyDataSetChanged()

            // Save user avtar in SharedPref
            (context as HomeActivity).sharedPrefsHelper.set(Constants.Keys.KEY_USER_GAME_BOARD, "" + boardData.name)

            // Notify to previous fragment (user profile fragment)
            rxBus?.send(OnGameBoardChanged(boardData))

            // Remove this fragment
            (context as HomeActivity).removeFragment()
        })
    }

    class BoardHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.view_game_board_row, parent, false)) {

        var imgGameBoard: ImageView? = null

        init {
            imgGameBoard = itemView.findViewById(R.id.imgGameBoard)
        }
    }
}