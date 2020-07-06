package hp.harsh.tictacbee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.extensions.rotate
import kotlinx.android.synthetic.main.fragment_no_internet_connection.*

/**
 * @purpose NoInternetConnectionFragment - Lucrative design to inform user that it does not have internet.
 *
 * It comes up automatically when user lost internet and will be disappeared when it retrieves its internet.
 *
 * @author Harsh Patel
 */
class NoInternetConnectionFragment : BaseFragment() {

    val TAG = NoInternetConnectionFragment::class.java.simpleName

    fun newInstance(): NoInternetConnectionFragment {
        var noInternetConnectionFragment = NoInternetConnectionFragment()

        return noInternetConnectionFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_no_internet_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        startProgress()
    }

    private fun startProgress() {
        imgNoInternetConnectionProgress.rotate()
    }

    override fun onDestroyView() {
        imgNoInternetConnectionProgress.clearAnimation()
        super.onDestroyView()
    }
}