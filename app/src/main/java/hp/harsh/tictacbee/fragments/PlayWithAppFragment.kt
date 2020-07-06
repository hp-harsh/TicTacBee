package hp.harsh.tictacbee.fragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
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
import kotlin.random.Random

/**
 * @purpose PlayWithAppFragment - User can play with app for practicing itself.
 *
 * App will decide its move after user does play its move.
 *
 * It checks into game winning predicates to decide game status weather it is won, lost or draw after each move played.
 * If any player wins the game, it animates winning moves to make it highlight.
 *
 * @author Harsh Patel
 */
class PlayWithAppFragment : BaseFragment(), View.OnClickListener {

    val TAG = PlayWithAppFragment::class.java.simpleName

    private val compositeDisposable = CompositeDisposable()

    var activeBee = Constants.PlayAloneBees.BEE1
    var bee1Moves = arrayListOf<Int>()
    var appMoves = arrayListOf<Int>()
    var winnerId: String = ""

    fun newInstance(): PlayWithAppFragment {
        var playAloneFragment = PlayWithAppFragment()

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

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
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

                            Log.i(TAG,"OnGameWon: winnerId = $winnerId, activeBee= $activeBee")

                            if (winnerId == "" && bee1Moves.size + appMoves.size == 9) {
                                // Game has been draw
                                ToastUtil.show(
                                    getContext(),
                                    "" + getString(R.string.toast_play_draw)
                                )
                                clearAllClickListener()

                            } else if (winnerId == "" && activeBee == Constants.PlayAloneBees.APP) {
                                Handler().postDelayed(Runnable {
                                    setAppMove()
                                },1000)
                            } else if (winnerId == Constants.PlayAloneBees.BEE1) {

                                animateWinningMoved(winnerMoves)

                                ToastUtil.show(getContext(), getString(R.string.toast_play_with_app_you_win))
                                clearAllClickListener()


                            } else if (winnerId == "" + Constants.PlayAloneBees.APP) {

                                animateWinningMoved(winnerMoves)
                                ToastUtil.show(getContext(), getString(R.string.toast_play_with_app_win))
                                clearAllClickListener()


                            }
                        }
                    }
                }
        )
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


    private fun initGame() {
        activeBee = Constants.PlayAloneBees.BEE1
        bee1Moves = arrayListOf()
        appMoves = arrayListOf()

        setImage(userAvtar(), imgUserAvtar)
        txtUserId.text = getString(R.string.you) + " \n" + userId()

        setImage(Constants.PlayAloneBees.BEE2_CHAR, imgOpponentAvtar)
        txtOpponentId.text = getString(R.string.app) + ""

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
                gameChar = Constants.PlayAloneBees.APP_CHAR
            }

            setImageBg(context?.getColor(R.color.black_alpha_15)!!, img)

            activeBee = Constants.PlayAloneBees.APP
        } else if (activeBee == Constants.PlayAloneBees.APP) {

            appMoves.add(img.getIntTag())

            if (activeBee == Constants.PlayAloneBees.BEE1) {
                gameChar = gameChar()
            } else {
                gameChar = Constants.PlayAloneBees.APP_CHAR
            }

            setImageBg(context?.getColor(R.color.black_alpha_35)!!, img)

            activeBee = Constants.PlayAloneBees.BEE1
        }

        setImage(gameChar, img)
        img.scale(0f, 1f, 0f, 1f, 0.5f, 0.5f, 1000, false)

        val gameResult = GameEngine.checkResult(
            bee1Moves,
            appMoves,
            Constants.PlayAloneBees.BEE1,
            Constants.PlayAloneBees.APP
        )

        Log.i(TAG, "gameResult: $gameResult")

        animFocusActiveBee()
    }

    private fun setAppMove() {
        Log.i(TAG,"setAppMove")
        var emptyMoves = arrayListOf<Int>()

        for (index in 1..9) {
            if (bee1Moves.contains(index) || appMoves.contains(index)) {

            } else {
                // Find un-played moves
                emptyMoves.add(index)
            }
        }

        val getRandomMove = emptyMoves.get(Random.nextInt(emptyMoves.size))

        if (getRandomMove == 1) {
            img1.performClick()
        } else if (getRandomMove == 2) {
            img2.performClick()
        } else if (getRandomMove == 3) {
            img3.performClick()
        } else if (getRandomMove == 4) {
            img4.performClick()
        } else if (getRandomMove == 5) {
            img5.performClick()
        } else if (getRandomMove == 6) {
            img6.performClick()
        } else if (getRandomMove == 7) {
            img7.performClick()
        } else if (getRandomMove == 8) {
            img8.performClick()
        } else if (getRandomMove == 9) {
            img9.performClick()
        }
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