package hp.harsh.tictacbee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.adapters.UpdateGameBoardAdapter
import hp.harsh.tictacbee.models.BoardData
import hp.harsh.tictacbee.utils.Constants
import kotlinx.android.synthetic.main.fragment_update_game_board.*

/**
 * @purpose UpdateGameBoardFragment - List of predefined board bg are set in the list to make it selected by user
 *
 * @author Harsh Patel
 */
class UpdateGameBoardFragment : BaseFragment() {

    val TAG = UpdateGameBoardFragment::class.java.simpleName

    var listOfBoards = mutableListOf<BoardData>();

    var updateGameBoardAdapter : UpdateGameBoardAdapter? = null

    fun newInstance(): UpdateGameBoardFragment {
        var awardFragment = UpdateGameBoardFragment()

        return awardFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_update_game_board, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initBoardData()
        initBoard()
    }

    private fun initBoardData() {
        // Get saved board
        val savedBoard = sharedPrefsHelper.get(Constants.Keys.KEY_USER_GAME_BOARD)

        for (i in 0..24) {
            if (savedBoard.equals("board_$i"))
                listOfBoards.add(BoardData("board_$i", true))
            else
                listOfBoards.add(BoardData("board_$i", false))
        }
    }

    private fun initBoard() {
        gameBoardRecyclerView.layoutManager = GridLayoutManager(context, 5)

        updateGameBoardAdapter = UpdateGameBoardAdapter(context, listOfBoards, rxBus)
        gameBoardRecyclerView.adapter = updateGameBoardAdapter
    }

}