package hp.harsh.tictacbee.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.app.itialltradetest.extensions.*
import com.google.firebase.database.*
import hp.harsh.tictacbee.R
import hp.harsh.tictacbee.activities.HomeActivity
import hp.harsh.tictacbee.enums.GameStatus
import hp.harsh.tictacbee.enums.InvitationStatus
import hp.harsh.tictacbee.events.OnGameBackPressedEvent
import hp.harsh.tictacbee.events.OnGameWon
import hp.harsh.tictacbee.events.OnNewMessageReceived
import hp.harsh.tictacbee.extensions.scale
import hp.harsh.tictacbee.models.BeeMoveData
import hp.harsh.tictacbee.models.InvitationData
import hp.harsh.tictacbee.models.User
import hp.harsh.tictacbee.predicates.GameEngine
import hp.harsh.tictacbee.utils.CommonUtil
import hp.harsh.tictacbee.utils.Constants
import hp.harsh.tictacbee.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_play_with_bee.*
import java.util.ArrayList

/**
 * @purpose PlayWithBeeFragment - User can play with opponent user. Opponent user must be accepted by requesting user.
 *
 * One player can play one game at a time.
 *
 * Once game is in progress, both player can chat with each other via Chat functionality.
 * As soon as game is finished, no one can send and receive chat message. It will be cleared.
 *
 * It checks into game winning predicates to decide game status weather it is won, lost or draw after each move played.
 * If any player wins the game, it animates winning moves to make it highlight.
 *
 * @author Harsh Patel
 */
class PlayWithBeeFragment : BaseFragment(), View.OnClickListener {

    val TAG = PlayWithBeeFragment::class.java.simpleName

    private val compositeDisposable = CompositeDisposable()

    var receiversInvitationData: InvitationData? = null
    var opponentUser: User? = null

    var chatId: String = ""
    var gameId: String = ""
    var winnerId: String = ""

    var activeBee = ""
    var senderBeeMoves = arrayListOf<Int>()
    var receiverBeeMoves = arrayListOf<Int>()

    // This will hold id of the bee, in case, who is going to cancel game
    var canceledGameBy = ""

    // It stores bee id of last moved and all the indexes of played moved
    var beeMoveData: BeeMoveData? = null

    var gamePath: DatabaseReference? = null

    var isViewEnable = false

    fun newInstance(): PlayWithBeeFragment {
        var playWithBeeFragment = PlayWithBeeFragment()

        return playWithBeeFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isViewEnable = true
        return inflater.inflate(R.layout.fragment_play_with_bee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "onViewCreated")
        init()
        initRxBusListener()
        setUpGameBoard()

        getReceiverInvitationResponse()
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

        setBackground(boardBg(), lnrGameBoard)
    }

    override fun onDestroyView() {
        Log.i(TAG, "onDestroy")

        isViewEnable = false

        compositeDisposable.clear()

        // If user remove app from background
        endGame()

        super.onDestroyView()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgChat -> {
                validateAndStartChat()
            }

            R.id.frameEmptyView -> {
                ToastUtil.show(context, getString(R.string.toast_opponent_turn))
            }

            else -> {
                if (v is ImageView) {
                    setGameMove(v)
                }
            }
        }
    }

    private fun validateAndStartChat() {
        // Clear animation when user open chat
        imgChat.clearAnimation()

        // If opponent user has accepted invitation or game is already in progress,
        // Start chat

        if (receiversInvitationData?.status == InvitationStatus.ACCEPTED_INVITATION.statusName ||
            receiversInvitationData?.status == InvitationStatus.PLAY_GAME.statusName
        ) {
            // Show chat fragment if it is already exist. Other wise first create it
            if (!chatId.isEmptyString()) {
                if (!isFragmentExist(ChatFragment::class.java.simpleName)) {
                    addHiddenFragment(
                        ChatFragment().newInstance(
                            chatId,
                            receiversInvitationData
                        )
                    )
                }
                showFragment(ChatFragment::class.java.simpleName)
            }
        } else {
            ToastUtil.show(context, getString(R.string.toast_chat_not_initiated))
        }
    }

    private fun init() {
        imgChat.setOnClickListener(this)

        img1.setOnClickListener(this)
        img2.setOnClickListener(this)
        img3.setOnClickListener(this)
        img4.setOnClickListener(this)
        img5.setOnClickListener(this)
        img6.setOnClickListener(this)
        img7.setOnClickListener(this)
        img8.setOnClickListener(this)
        img9.setOnClickListener(this)

        frameEmptyView.setOnClickListener(this)
    }

    private fun initRxBusListener() {
        compositeDisposable.add(
            rxBus
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { receiverObject ->
                    if (receiverObject != null) {
                        if (receiverObject is OnGameBackPressedEvent) {
                            if (senderBeeMoves.size + receiverBeeMoves.size == 9) {
                                // Game Draw
                                endGame()

                            } else if (winnerId != "") {
                                // It means game has been finished
                                endGame()

                            } else {
                                // Game is in progress but user pressed back button
                                confirmCancelGame()
                            }

                        } else if (receiverObject is OnNewMessageReceived) {
                            if (imgChat.animation == null) {
                                // Check if animation is already running, do not start again
                                startImageChatAnim()
                            }

                        } else if (receiverObject is OnGameWon) {
                            winnerId = receiverObject.winnerId
                            val winnerMoves = receiverObject.winnerMoved

                            if (winnerId == "" && senderBeeMoves.size + receiverBeeMoves.size == 9) {
                                // Game has been draw
                                ToastUtil.show(context, "" + getString(R.string.toast_play_draw))
                                clearAllClickListener()

                                recordHistory(userId(), "" + GameStatus.DRAW.statusName)

                                recordHistory(
                                    "" + opponentUser?.userId,
                                    "" + GameStatus.DRAW.statusName
                                )

                            } else if (winnerId == userId()) {
                                // Logged in user wins
                                recordHistory(userId(), "" + GameStatus.WIN.statusName)

                                animateWinningMoved(winnerMoves)

                                ToastUtil.show(context, getString(R.string.toast_you_win))
                                clearAllClickListener()


                            } else if (winnerId == "" + opponentUser?.userId) {
                                // Opponent user wins
                                recordHistory(
                                    "" + userId(),
                                    "" + GameStatus.LOST.statusName
                                )

                                animateWinningMoved(winnerMoves)
                                ToastUtil.show(context, getString(R.string.toast_opponent_win))
                                clearAllClickListener()


                            }
                        }
                    }
                }
        )
    }

    private fun recordHistory(id: String, status: String) {
        var historyData: InvitationData? = receiversInvitationData
        historyData?.status = "" + status

        Log.i(TAG, "History Id: " + id)

        // Add history
        firebaseDbRef
            ?.child(Constants.DbTables.TABLE_HISTORY)
            ?.child("" + id)
            ?.child("" + System.currentTimeMillis())
            ?.setValue(historyData)

        // Remove Invitation
        firebaseDbRef
            ?.child(Constants.DbTables.TABLE_INVITATION)
            ?.child(id)
            ?.setValue(null)
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

    private fun setBeeInfo() {
        // Set user Info
        setImage(userAvtar(), imgUserAvtar)
        txtUserId.text = getString(R.string.you) + " \n" + userId()

        //Set Opponent Info
        if (receiversInvitationData?.senderId == userId()) {
            receiversInvitationData?.receiverAvtar?.let { setImage(it, imgOpponentAvtar) }
            txtOpponentId.text = "" + receiversInvitationData?.receiverId
        } else {
            receiversInvitationData?.senderAvtar?.let { setImage(it, imgOpponentAvtar) }
            txtOpponentId.text = "" + receiversInvitationData?.senderId
        }
    }

    private fun getReceiverInvitationResponse() {
        Log.i(TAG, "getReceiverInvitationResponse")
        // it will notify user about invited user response status (accepted, declined, waiting)
        firebaseDbRef
            ?.child(Constants.DbTables.TABLE_INVITATION)
            ?.child(userId())
            ?.addValueEventListener(getReceiverInvitationResponseListener)
    }

    val getReceiverInvitationResponseListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.value != null && isViewEnable) {
                receiversInvitationData =
                    dataSnapshot.getValue<InvitationData>(InvitationData::class.java)

                if (receiversInvitationData?.status == InvitationStatus.END_GAME.statusName
                    || receiversInvitationData?.status == InvitationStatus.CANCELLED_GAME.statusName
                ) {

                    // do nothing
                } else {
                    // get opponent user information
                    getOpponentInfo()
                }

                // Manage chat session
                manageResponse(receiversInvitationData)
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    private fun manageResponse(receiversInvitationData: InvitationData?) {
        Log.i(TAG, "manageResponse: " + receiversInvitationData?.status)
        when (receiversInvitationData?.status) {
            InvitationStatus.ACCEPTED_INVITATION.statusName -> {
            }

            InvitationStatus.DECLINED_INVITATION.statusName -> {
                notifyOpponentAboutUserDecliningGame()
            }

            InvitationStatus.SEND_INVITATION.statusName -> {
            }

            InvitationStatus.RECEIVE_INVITATION.statusName -> {
            }

            InvitationStatus.PLAY_GAME.statusName -> {
                ToastUtil.show(context, getString(R.string.toast_game_start))

                initGame()
            }

            InvitationStatus.END_GAME.statusName -> {
                replaceFragment(HomeFragment().newInstance())
            }

            InvitationStatus.CANCELLED_GAME.statusName -> {
                // For opponent user who did not canceled game
                if (canceledGameBy != userId()) {
                    notifyOpponentAboutCancellingGame()
                } else {
                    replaceFragment(HomeFragment().newInstance())
                }
            }
        }
    }

    fun getOpponentInfo() {
        Log.i(TAG, "getOpponentInfo")
        var opponentUserId = if (userId() == receiversInvitationData?.senderId) {
            receiversInvitationData?.receiverId
        } else {
            receiversInvitationData?.senderId
        }

        firebaseDbRef
            ?.child(Constants.DbTables.TABLE_USERS)
            ?.child("" + opponentUserId)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null && isViewEnable) {
                        opponentUser = dataSnapshot.getValue(User::class.java)
                        Log.i(TAG, "Opponent User: $opponentUser")

                        if (receiversInvitationData?.status == InvitationStatus.END_GAME.statusName
                            || receiversInvitationData?.status == InvitationStatus.CANCELLED_GAME.statusName
                        ) {
                            // Do nothing
                        } else {
                            // Set all basic data
                            setBeeInfo()

                            // Prepare chat
                            initChat()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
    }

    private fun initGame() {
        beeMoveData = BeeMoveData()

        activeBee = "" + receiversInvitationData?.receiverId
        senderBeeMoves = arrayListOf()
        receiverBeeMoves = arrayListOf()

        animFocusActiveBee()

        gameId = receiversInvitationData?.senderId + "_" + receiversInvitationData?.receiverId

        gamePath = firebaseDbRef
            ?.child(Constants.DbTables.TABLE_GAME)
            ?.child("" + gameId)

        // It will remove if any existing data for this path
        gamePath?.setValue(null)

        gamePath?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null && isViewEnable) {
                    beeMoveData = dataSnapshot.getValue(BeeMoveData::class.java)

                    manageGameMove(beeMoveData)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        if (activeBee == userId()) {
            frameEmptyView.setInvisible(false)
        } else {
            frameEmptyView.setInvisible(true)
        }
    }

    private fun manageGameMove(updatedBeeMoveData: BeeMoveData?) {
        Log.i(TAG, "updatedBeeMoveData: " + updatedBeeMoveData)
        var img: ImageView? = null

        when (updatedBeeMoveData?.lastMovedTag) {
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

        img?.setOnClickListener(null)

        var gameChar = ""
        Log.i(TAG, "activeBee: " + activeBee)
        if (activeBee == (receiversInvitationData?.senderId)) {

            senderBeeMoves.add(img?.getIntTag()!!)

            if (activeBee == userId()) {
                gameChar = gameChar()
            } else {
                gameChar = "" + opponentUser?.gameChar
            }

            setImageBg(context?.getColor(R.color.black_alpha_15)!!, img)

            activeBee = "" + receiversInvitationData?.receiverId
        } else if (activeBee == (receiversInvitationData?.receiverId)) {

            receiverBeeMoves.add(img?.getIntTag()!!)

            if (activeBee == userId()) {
                gameChar = gameChar()
            } else {
                gameChar = "" + opponentUser?.gameChar
            }

            setImageBg(context?.getColor(R.color.black_alpha_35)!!, img)

            activeBee = "" + receiversInvitationData?.senderId
        }

        setImage(gameChar, img!!)
        img?.scale(0f, 1f, 0f, 1f, 0.5f, 0.5f, 1000, false)

        GameEngine.checkResult(
            senderBeeMoves,
            receiverBeeMoves,
            "" + receiversInvitationData?.senderId,
            "" + receiversInvitationData?.receiverId
        )

        animFocusActiveBee()
    }

    private fun initChat() {
        chatId = receiversInvitationData?.senderId + "_" + receiversInvitationData?.receiverId

        firebaseDbRef
            ?.child(Constants.DbTables.TABLE_CHAT)
            ?.child("" + chatId)
            ?.setValue(null)

        if (!isFragmentExist(ChatFragment::class.java.simpleName)) {
            addHiddenFragment(ChatFragment().newInstance(chatId, receiversInvitationData))
        }
    }

    private fun updateDbForSenderUserInvitationStatus(status: String) {

        //Update record if there is any data available
        firebaseDbRef
            ?.child(Constants.DbTables.TABLE_INVITATION)
            ?.child("" + receiversInvitationData?.senderId)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.getValue() != null && isViewEnable) {
                        // If there is any data to the desired path, it will not be null
                        firebaseDbRef!!
                            .child(Constants.DbTables.TABLE_INVITATION)
                            .child("" + receiversInvitationData?.senderId)
                            .child("status")
                            .setValue(status)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                }
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })

        firebaseDbRef!!
            .child(Constants.DbTables.TABLE_INVITATION)
            .child("" + receiversInvitationData?.receiverId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.getValue() != null && isViewEnable) {
                        // If there is any data to the desired path, it will not be null
                        firebaseDbRef!!
                            .child(Constants.DbTables.TABLE_INVITATION)
                            .child("" + receiversInvitationData?.receiverId)
                            .child("status")
                            .setValue(status)
                            .addOnCompleteListener { task ->

                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            }!!)
        // It prevents to call this method multiple times
        Log.i(TAG, "replaceFragment: 9")
        if (currentFragmentTag() != HomeFragment::class.java.simpleName) {
            replaceFragment(HomeFragment().newInstance())
        }
    }

    private fun removeInvitationFromDb() {
        //Remove Invitation
        firebaseDbRef
            ?.child(Constants.DbTables.TABLE_INVITATION)
            ?.child(userId())
            ?.setValue(null)
    }

    private fun cancelGame() {
        updateDbForSenderUserInvitationStatus("" + InvitationStatus.CANCELLED_GAME.statusName)
    }

    private fun endGame() {
        updateDbForSenderUserInvitationStatus("" + InvitationStatus.END_GAME.statusName)
    }

    private fun startImageChatAnim() {
        // When user has new message and chat window is not opened, make it as chat icon blinking
        imgChat.scale(1f, 0.7f, 1f, 0.7f, 0.5f, 0.5f, 1500, true)
    }

    private fun animFocusActiveBee() {
        if (activeBee == userId()) {
            imgUserAvtar.scale(1f, 0.7f, 1f, 0.7f, 0.5f, 0.5f, 1000, true)
            imgUserAvtar.setBackgroundResource(R.drawable.view_user_border_golden)
            imgOpponentAvtar.clearAnimation()
            imgOpponentAvtar.setBackgroundResource(R.drawable.view_user_border_white)

            frameEmptyView.setInvisible(false)
        } else {
            imgOpponentAvtar.scale(1f, 0.7f, 1f, 0.7f, 0.5f, 0.5f, 1000, true)
            imgOpponentAvtar.setBackgroundResource(R.drawable.view_user_border_golden)
            imgUserAvtar.clearAnimation()
            imgUserAvtar.setBackgroundResource(R.drawable.view_user_border_white)

            frameEmptyView.setInvisible(true)
        }
    }

    fun setGameMove(img: ImageView) {
        val lastMovedTag = img.getIntTag()

        beeMoveData?.lastActiveBee = activeBee
        beeMoveData?.lastMovedTag = lastMovedTag

        when (lastMovedTag) {
            1 -> beeMoveData?.movedForIndex1 = userId()
            2 -> beeMoveData?.movedForIndex2 = userId()
            3 -> beeMoveData?.movedForIndex3 = userId()
            4 -> beeMoveData?.movedForIndex4 = userId()
            5 -> beeMoveData?.movedForIndex5 = userId()
            6 -> beeMoveData?.movedForIndex6 = userId()
            7 -> beeMoveData?.movedForIndex7 = userId()
            8 -> beeMoveData?.movedForIndex8 = userId()
            9 -> beeMoveData?.movedForIndex9 = userId()
        }

        gamePath?.setValue(beeMoveData)
    }

    fun clearAllClickListener() {
        imgChat.setOnClickListener(null)

        img1.setOnClickListener(null)
        img2.setOnClickListener(null)
        img3.setOnClickListener(null)
        img4.setOnClickListener(null)
        img5.setOnClickListener(null)
        img6.setOnClickListener(null)
        img7.setOnClickListener(null)
        img8.setOnClickListener(null)
        img9.setOnClickListener(null)

        imgChat.clearAnimation()
        imgUserAvtar.clearAnimation()
        imgOpponentAvtar.clearAnimation()

        imgUserAvtar.setBackgroundResource(R.drawable.view_user_border_white)
        imgOpponentAvtar.setBackgroundResource(R.drawable.view_user_border_white)
    }

    private fun confirmCancelGame() {
        // build alert dialog
        val dialogBuilder = getContext()?.let { AlertDialog.Builder(it) }

        // set message of alert dialog
        dialogBuilder?.setMessage(getString(R.string.alert_quit_message))
            // if the dialog is cancelable
            ?.setCancelable(true)
            // positive button text and action
            ?.setPositiveButton(
                getString(R.string.alert_positive_button_quit),
                DialogInterface.OnClickListener { dialog, id ->
                    // It will help to filter opponent alert dialog to show message
                    canceledGameBy = userId()

                    cancelGame()
                })
            // negative button text and action
            ?.setNegativeButton(
                getString(R.string.alert_negative_button_continue_game),
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                })

        // create dialog box
        val alert = dialogBuilder?.create()
        // set title for alert dialog box
        alert?.setTitle(getString(R.string.alert_quit_game_title))
        // show alert dialog
        alert?.show()
    }

    private fun notifyOpponentAboutCancellingGame() {
        // build alert dialog
        val dialogBuilder = getContext()?.let { AlertDialog.Builder(it) }

        // set message of alert dialog
        dialogBuilder?.setMessage(getString(R.string.alert_quit_game_opponent_message))
            // if the dialog is cancelable
            ?.setCancelable(false)
            // positive button text and action
            ?.setPositiveButton(
                getString(R.string.alert_positive_button_quit),
                DialogInterface.OnClickListener { dialog, id ->
                    Log.i(TAG, "replaceFragment: 10")
                    replaceFragment(HomeFragment().newInstance())
                })

        // create dialog box`
        val alert = dialogBuilder?.create()
        // set title for alert dialog box
        alert?.setTitle(getString(R.string.alert_positive_button_opponent_quit_game_))
        // show alert dialog
        alert?.show()
    }

    private fun notifyOpponentAboutUserDecliningGame() {
        // build alert dialog
        val dialogBuilder = getContext()?.let { AlertDialog.Builder(it) }

        // set message of alert dialog
        dialogBuilder?.setMessage(getString(R.string.alert_has_declined_game))
            // if the dialog is cancelable
            ?.setCancelable(false)
            // positive button text and action
            ?.setPositiveButton(
                getString(R.string.alert_positive_button_quit),
                DialogInterface.OnClickListener { dialog, id ->
                    removeInvitationFromDb()
                    replaceFragment(HomeFragment().newInstance())
                })

        // create dialog box`
        val alert = dialogBuilder?.create()
        // set title for alert dialog box
        alert?.setTitle(getString(R.string.splash_name))
        // show alert dialog
        alert?.show()
    }
}