package hp.harsh.tictacbee.models

data class ChatData(
    val messageSenderId: String = "",
    val messageReceiverId: String = "",
    val message: String = "",
    val messageTimestamp: Long = -1
) {
}