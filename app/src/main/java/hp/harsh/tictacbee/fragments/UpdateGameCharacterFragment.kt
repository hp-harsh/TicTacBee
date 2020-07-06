package hp.harsh.tictacbee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.adapters.UpdateGameCharacterAdapter
import hp.harsh.tictacbee.models.CharaterData
import hp.harsh.tictacbee.utils.Constants
import kotlinx.android.synthetic.main.fragment_update_game_character.*

/**
 * @purpose UpdateGameCharacterFragment - List of predefined board bg are set in the list to make it selected by user
 *
 * @author Harsh Patel
 */
class UpdateGameCharacterFragment : BaseFragment() {

    val TAG = UpdateGameCharacterFragment::class.java.simpleName

    var listOfCharacters = mutableListOf<CharaterData>();

    var updateGameCharacterAdapter : UpdateGameCharacterAdapter? = null

    fun newInstance(): UpdateGameCharacterFragment {
        var awardFragment = UpdateGameCharacterFragment()

        return awardFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_update_game_character, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initCharacterData()
        initCharater()
    }

    private fun initCharacterData() {
        // Get saved avtar
        val savedCharacter = sharedPrefsHelper.get(Constants.Keys.KEY_USER_GAME_CHARACTER)

        println("savedCharacter: $savedCharacter");

        for (i in 0..80) {
            if (savedCharacter.equals("ic_char_$i"))
                listOfCharacters.add(CharaterData("ic_char_$i", true))
            else
                listOfCharacters.add(CharaterData("ic_char_$i", false))
        }
    }

    private fun initCharater() {
        gameCharacterRecyclerView.layoutManager = GridLayoutManager(context, 5)

        updateGameCharacterAdapter = UpdateGameCharacterAdapter(context, listOfCharacters, rxBus)
        gameCharacterRecyclerView.adapter = updateGameCharacterAdapter
    }

}