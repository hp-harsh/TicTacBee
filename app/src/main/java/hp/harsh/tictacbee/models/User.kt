package hp.harsh.tictacbee.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var username: String? = "",
    var userId: String? = "",
    var emailId: String? = "",
    var userAvtar: String? = "",
    var gameChar: String? = "",
    var boardBackground: String? = "",
    var preferredLanguage: String? = ""
)