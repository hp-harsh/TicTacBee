package hp.harsh.tictacbee.predicates

import hp.harsh.tictacbee.AppController
import hp.harsh.tictacbee.events.OnGameWon
import hp.harsh.tictacbee.utils.RxBus

/**
 * @purpose GameEngine - Responsible class to decide game status. It contains predefined winning moves.
 * If it satisfies, last moved player will be win and notify to the game fragment using RxBus Event.
 *
 * @author Harsh Patel
 */
object GameEngine {
    val rxBus: RxBus = AppController.getRxBus()

    val rowOne = arrayListOf<Int>(1, 2, 3)
    val rowTwo = arrayListOf<Int>(4, 5, 6)
    val rowThree = arrayListOf<Int>(7, 8, 9)
    val columnOne = arrayListOf<Int>(1, 4, 7)
    val columnTwo = arrayListOf<Int>(2, 5, 8)
    val columnThree = arrayListOf<Int>(3, 6, 9)
    val diagonalOne = arrayListOf<Int>(1, 5, 9)
    val diagonalTwo = arrayListOf<Int>(3, 5, 7)

    fun checkResult(
        senderBeeMoves: ArrayList<Int>,
        receiverBeeMoves: ArrayList<Int>,
        senderId: String,
        receiverId: String
    ) {
        when {
            senderBeeMoves.containsAll(rowOne) -> {
                rxBus.send(OnGameWon(senderId, rowOne))
            }
            senderBeeMoves.containsAll(rowTwo) -> {
                rxBus.send(OnGameWon(senderId, rowTwo))
            }
            senderBeeMoves.containsAll(rowThree) -> {
                rxBus.send(OnGameWon(senderId, rowThree))
            }
            senderBeeMoves.containsAll(columnOne) -> {
                rxBus.send(OnGameWon(senderId, columnOne))
            }
            senderBeeMoves.containsAll(columnTwo) -> {
                rxBus.send(OnGameWon(senderId, columnTwo))
            }
            senderBeeMoves.containsAll(columnThree) -> {
                rxBus.send(OnGameWon(senderId, columnThree))
            }
            senderBeeMoves.containsAll(diagonalOne) -> {
                rxBus.send(OnGameWon(senderId, diagonalOne))
            }
            senderBeeMoves.containsAll(diagonalTwo) -> {
                rxBus.send(OnGameWon(senderId, diagonalTwo))
            }
            receiverBeeMoves.containsAll(rowOne) -> {
                rxBus.send(OnGameWon(receiverId, rowOne))
            }
            receiverBeeMoves.containsAll(rowTwo) -> {
                rxBus.send(OnGameWon(receiverId, rowTwo))
            }
            receiverBeeMoves.containsAll(rowThree) -> {
                rxBus.send(OnGameWon(receiverId, rowThree))
            }
            receiverBeeMoves.containsAll(columnOne) -> {
                rxBus.send(OnGameWon(receiverId, columnOne))
            }
            receiverBeeMoves.containsAll(columnTwo) -> {
                rxBus.send(OnGameWon(receiverId, columnTwo))
            }
            receiverBeeMoves.containsAll(columnThree) -> {
                rxBus.send(OnGameWon(receiverId, columnThree))
            }
            receiverBeeMoves.containsAll(diagonalOne) -> {
                rxBus.send(OnGameWon(receiverId, diagonalOne))
            }
            receiverBeeMoves.containsAll(diagonalTwo) -> {
                rxBus.send(OnGameWon(receiverId, diagonalTwo))
            }
            else -> {
                return rxBus.send(OnGameWon("", ArrayList()))
            }
        }
    }
}