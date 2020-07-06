package hp.harsh.tictacbee.events

import hp.harsh.tictacbee.models.User

data class OnBeeInvitationSend(var invitedUser: User) {
}