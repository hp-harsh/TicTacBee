package hp.harsh.tictacbee.enums

/**
 * @purpose InvitationStatus - It is very important class and used almost everywhere in game status transaction.
 * It distinguishes sender and receiver user and it also responsible to provide game status.
 *
 * @author Harsh Patel
 */
enum class InvitationStatus(val statusName: String) {
    SEND_INVITATION("send"),
    RECEIVE_INVITATION("receive"),
    ACCEPTED_INVITATION("accepted"),
    DECLINED_INVITATION("declined"),
    PLAY_GAME("play"),
    CANCELLED_GAME("cancelled"),
    END_GAME("end")
}