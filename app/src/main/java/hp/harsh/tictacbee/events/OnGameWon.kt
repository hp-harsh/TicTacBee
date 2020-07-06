package hp.harsh.tictacbee.events

import hp.harsh.tictacbee.models.BoardData

data class OnGameWon(var winnerId: String, var winnerMoved: ArrayList<Int>) {

}