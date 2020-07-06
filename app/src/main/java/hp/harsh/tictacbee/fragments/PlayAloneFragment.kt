package hp.harsh.tictacbee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.app.itialltradetest.extensions.dpToPx
import com.app.itialltradetest.extensions.getIntTag
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.activities.HomeActivity
import hp.harsh.tictacbee.events.OnGameWon
import hp.harsh.tictacbee.extensions.scale
import hp.harsh.tictacbee.predicates.GameEngine
import hp.harsh.tictacbee.utils.CommonUtil
import hp.harsh.tictacbee.utils.Constants
import hp.harsh.tictacbee.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_play_alone.*
import java.util.ArrayList

/**
 * @purpose PlayAloneFragment - User can play alone for practicing itself. User is itself player 1 and player 2.
 *
 * It checks into game winning predicates to decide game status weather it is won, lost or draw after each move played.
 * If any player wins the game, it animates winning moves to make it highlight.
 *
 * @author Harsh Patel
 */
class PlayAloneFragment : BaseFragment(), View.OnClickListener {

    val TAG = PlayAloneFragment::class.java.simpleName

    private val compositeDisposable = CompositeDisposable()

    var activeBee = Constants.PlayAloneBees.BEE1
    var bee1Moves = arrayListOf<Int>()
    var bee2Moves = arrayListOf<Int>()
    var winnerId: String = ""

    fun newInstance(): PlayAloneFragment {
        var playAloneFragment = PlayAloneFragment()

        return playAloneFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_play_alone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
        initRxBusListener()
        setUpGameBoard()
    }

    private fun setUpGameBoard() {
        // we need square board. So we will find what is the min. Width or Height.
        // For that we will get device height and width
        // (Width is device width is 2/3/ because 1/3 is covered by User, Opponent and chat icon)

        val deviceWidth = (CommonUtil.getDeviceWidth(context as HomeActivity) / 3) * 2
        val deviceHeight = CommonUtil.getDeviceHeight(context as HomeActivity)

        var gameBoardSize = 1
        gameBoardSize = if (deviceHeight < deviceWidth) {
            (deviceHeight - context!!.dpToPx(50f))
        } else {
            (deviceWidth - -context!!.dpToPx(50f))
        }

        val params: ViewGroup.LayoutParams = frameBoard.layoutParams
        params.width = gameBoardSize
        params.height = gameBoardSize
        frameBoard.layoutParams = params

        setBackground(boardBg(),lnrGameBoard)
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            else -> {
                if (v is ImageView) {
                    setGameMove(v)
                }
            }
        }
    }

    private fun init() {
        img1.setOnClickListener(this)
        img2.setOnClickListener(this)
        img3.setOnClickListener(this)
        img4.setOnClickListener(this)
        img5.setOnClickListener(this)
        img6.setOnClickListener(this)
        img7.setOnClickListener(this)
        img8.setOnClickListener(this)
        img9.setOnClickListener(this)

        initGame()
    }

    private fun initRxBusListener() {
        compositeDisposable.add(
            rxBus
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { receiverObject ->
                    if (receiverObject != null) {
                        if (receiverObject is OnGameWon) {
                            winnerId = receiverObject.winnerId
                            val winnerMoves = receiverObject.winnerMoved

                            if (winnerId == "" && bee1Moves.size + bee2Moves.size == 9) {
                                // Game has been draw
                                ToastUtil.show(
                                    context,
                                    "" + getString(R.string.toast_play_draw)
                                )
                                clearAllClickListener()

                            } else if (winnerId == Constants.PlayAloneBees.BEE1) {

                                animateWinningMoved(winnerMoves)

                                ToastUtil.show(context, getString(R.string.toast_play_alone_bee1_win))
                                clearAllClickListener()


                            } else if (winnerId == "" + Constants.PlayAloneBees.BEE2) {

                                animateWinningMoved(winnerMoves)
                                ToastUtil.show(context, getString(R.string.toast_play_alone_bee2_win))
                                clearAllClickListener()


                            }
                        }
                    }
                }
        )
    }


    private fun initGame() {
        activeBee = Constants.PlayAloneBees.BEE1
        bee1Moves = arrayListOf()
        bee2Moves = arrayListOf()

        setImage(userAvtar(), imgUserAvtar)
        txtUserId.text = getString(R.string.bee1)

        setImage(Constants.PlayAloneBees.BEE2_CHAR, imgOpponentAvtar)
        txtOpponentId.text = getString(R.string.bee2)

        animFocusActiveBee()
    }

    fun setGameMove(img: ImageView) {
        img.setOnClickListener(null)

        var gameChar = ""
        if (activeBee == Constants.PlayAloneBees.BEE1) {

            bee1Moves.add(img.getIntTag())

            if (activeBee == Constants.PlayAloneBees.BEE1) {
                gameChar = gameChar()
            } else {
                gameChar = Constants.PlayAloneBees.BEE2_CHAR
            }

            setImageBg(context?.getColor(R.color.black_alpha_15)!!, img)

            activeBee = Constants.PlayAloneBees.BEE2
        } else if (activeBee == Constants.PlayAloneBees.BEE2) {

            bee2Moves.add(img.getIntTag())

            if (activeBee == Constants.PlayAloneBees.BEE1) {
                gameChar = gameChar()
            } else {
                gameChar = Constants.PlayAloneBees.BEE2_CHAR
            }

            setImageBg(context?.getColor(R.color.black_alpha_35)!!, img)

            activeBee = Constants.PlayAloneBees.BEE1
        }

        setImage(gameChar, img)
        img.scale(0f, 1f, 0f, 1f, 0.5f, 0.5f, 1000, false)

        GameEngine.checkResult(
            bee1Moves,
            bee2Moves,
            Constants.PlayAloneBees.BEE1,
            Constants.PlayAloneBees.BEE2
        )

        animFocusActiveBee()
    }

    fun clearAllClickListener() {
        img1.setOnClickListener(null)
        img2.setOnClickListener(null)
        img3.setOnClickListener(null)
        img4.setOnClickListener(null)
        img5.setOnClickListener(null)
        img6.setOnClickListener(null)
        img7.setOnClickListener(null)
        img8.setOnClickListener(null)
        img9.setOnClickListener(null)

        imgUserAvtar.clearAnimation()
        imgOpponentAvtar.clearAnimation()

        imgUserAvtar.setBackgroundResource(R.drawable.view_user_border_white)
        imgOpponentAvtar.setBackgroundResource(R.drawable.view_user_border_white)
    }

    private fun animateWinningMoved(winnerMoves: ArrayList<Int>) {
        for (moveIndex in winnerMoves) {
            var img: ImageView? = null
            when (moveIndex) {
                1 -> img = img1
                2 -> img = img2
                3 -> img = img3
                4 -> img = img4
                5 -> img = img5
                6 -> img = img6
                7 -> img = img7
                8 -> img = img8
                9 -> img = img9
            }
            img?.scale(1f, 0.7f, 1f, 0.7f, 0.5f, 0.5f, 1000, true)
        }
    }

    private fun animFocusActiveBee() {
        if (activeBee == Constants.PlayAloneBees.BEE1) {
            imgUserAvtar.scale(1f, 0.7f, 1f, 0.7f, 0.5f, 0.5f, 1000, true)
            imgUserAvtar.setBackgroundResource(R.drawable.view_user_border_golden)
            imgOpponentAvtar.clearAnimation()
            imgOpponentAvtar.setBackgroundResource(R.drawable.view_user_border_white)

            //frameEmptyView.setInvisible(false)
        } else {
            imgOpponentAvtar.scale(1f, 0.7f, 1f, 0.7f, 0.5f, 0.5f, 1000, true)
            imgOpponentAvtar.setBackgroundResource(R.drawable.view_user_border_golden)
            imgUserAvtar.clearAnimation()
            imgUserAvtar.setBackgroundResource(R.drawable.view_user_border_white)

            //frameEmptyView.setInvisible(true)
        }
    }
}