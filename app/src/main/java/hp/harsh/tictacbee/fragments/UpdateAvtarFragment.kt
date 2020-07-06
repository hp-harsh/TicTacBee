package hp.harsh.tictacbee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.adapters.UpdateAvtarAdapter
import hp.harsh.tictacbee.models.AvtarData
import hp.harsh.tictacbee.utils.Constants
import kotlinx.android.synthetic.main.fragment_update_avtar.*

/**
 * @purpose UpdateAvtarFragment - List of predefined avtars are set in list to make it selected by user
 *
 * @author Harsh Patel
 */
class UpdateAvtarFragment : BaseFragment() {

    val TAG = UpdateAvtarFragment::class.java.simpleName

    var listOfAvtar = mutableListOf<AvtarData>();

    var updateAvtarAdapter : UpdateAvtarAdapter? = null

    fun newInstance(): UpdateAvtarFragment {
        var awardFragment = UpdateAvtarFragment()

        return awardFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_update_avtar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initAvtarData()
        initAvtar()
    }

    private fun initAvtarData() {
        // Get saved avtar
        val savedAvtar = sharedPrefsHelper.get(Constants.Keys.KEY_USER_AVTAR)

        for (i in 0..26) {
            if (savedAvtar.equals("ic_avtar_$i"))
                listOfAvtar.add(AvtarData("ic_avtar_$i", true))
            else
                listOfAvtar.add(AvtarData("ic_avtar_$i", false))
        }
    }

    private fun initAvtar() {
        avtarRecyclerView.layoutManager = GridLayoutManager(context, 5)

        updateAvtarAdapter = UpdateAvtarAdapter(context, listOfAvtar, rxBus)
        avtarRecyclerView.adapter = updateAvtarAdapter
    }
}