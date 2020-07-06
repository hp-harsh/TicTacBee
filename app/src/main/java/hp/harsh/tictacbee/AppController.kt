package hp.harsh.tictacbee

import android.app.Application
import android.media.MediaPlayer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import hp.harsh.tictacbee.utils.RxBus
import hp.harsh.tictacbee.utils.SharedPrefsHelper

/**
 * @purpose AppController - application class that declares common functionality that is used throughout the app.
 *
 * Those functionality work as Singleton class because it creates its object once here in this class,
 * and used created object whenever required.
 *
 * @author Harsh Patel
 */
class AppController : Application() {

    private val TAG = AppController::class.java.simpleName

    val mediaPlayer : MediaPlayer? = null

    companion object {
        @get:Synchronized
        var instance: AppController? = null
            private set

        private lateinit var sharedPrefsHelper: SharedPrefsHelper
        private lateinit var auth: FirebaseAuth
        private lateinit var database: FirebaseDatabase
        private lateinit var databaseRef: DatabaseReference

        private lateinit var rxBus: RxBus

        fun getSharedPrefsHelper(): SharedPrefsHelper {
            return sharedPrefsHelper
        }

        fun getFirebaseAuthInstance(): FirebaseAuth {
            return auth
        }

        fun getFirebaseDbInstance(): FirebaseDatabase {
            return database
        }

        fun getFirebaseDbRef(): DatabaseReference {
            return databaseRef
        }

        fun getRxBus(): RxBus {
            return rxBus
        }
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        auth = Firebase.auth
        database = Firebase.database
        database.setPersistenceEnabled(false)
        databaseRef = database.reference
        databaseRef.keepSynced(false)

        sharedPrefsHelper = SharedPrefsHelper(this)
        rxBus = RxBus()

        var resID = getResources().getIdentifier("game_bg_sound3" +
                "", "raw", getPackageName())

        val mediaPlayer = MediaPlayer.create(this, resID)
        mediaPlayer.isLooping = true
        mediaPlayer.setVolume(0.4f,0.4f)
        //mediaPlayer.start()
    }
}
