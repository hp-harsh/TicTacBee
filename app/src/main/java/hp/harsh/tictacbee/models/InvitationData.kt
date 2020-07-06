package hp.harsh.tictacbee.models

data class InvitationData(val senderName: String = "",
                          val senderId: String = "",
                          val senderAvtar: String = "",
                          val receiverName: String = "",
                          val receiverId: String = "",
                          val receiverAvtar: String = "",
                          var status: String = "") {
}