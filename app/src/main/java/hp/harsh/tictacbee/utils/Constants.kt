package hp.harsh.tictacbee.utils

/**
 * @purpose Constants - Keys for SharedPreferences, Firebase tables, Game defaults and Other common keys are declared here
 *
 * @author Harsh Patel
 */
object Constants {

    const val SHARED_PREFERENCE = "bee_shared_preference_key"

    object Keys {
        const val KEY_USER_NAME = "user_name_key"
        const val KEY_USER_ID = "user_id_key"
        const val KEY_USER_EMAIL = "user_email_key"

        const val KEY_USER_AVTAR = "user_avtar_key"
        const val KEY_USER_GAME_CHARACTER = "user_char_key"
        const val KEY_USER_GAME_BOARD = "user_board_key"
        const val KEY_USER_GAME_LANGUAGE = "user_language_key"
        const val KEY_IS_LANGUAGE_CHANGED = "user_language_changed_key"
    }

    object DbTables {
        const val TABLE_USERS = "users"
        const val TABLE_USERS_IDENTITY = "users_identity"
        const val TABLE_INVITATION = "user_invitations"
        const val TABLE_CHAT = "user_chat"
        const val TABLE_GAME = "user_game"
        const val TABLE_HISTORY = "game_history"
    }

    object GameDefaults {
        const val DEFAULT_USER_AVTAR = "ic_avtar_0"
        const val DEFAULT_GAME_CHAR = "ic_char_0"
        const val DEFAULT_BOARD_BG = "board_0"
        const val DEFAULT_PREFERRED_LANG = "en"
    }

    object PlayAloneBees {
        const val BEE1 = "bee1"
        const val BEE2 = "bee2"
        const val APP = "app"
        const val BEE2_CHAR = "ic_honey"
        const val APP_CHAR = "ic_honey"
    }
}