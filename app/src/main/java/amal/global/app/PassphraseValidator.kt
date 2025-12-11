package global.amal.app

import android.content.Context

class PassphraseValidator(val context: Context) {

    private val currentUser: CurrentUser
        get() = CurrentUser(context)

    fun validate(passphrase: String): Promise<Unit> {
        return Promise { fulfill, reject ->
            RestTarget.entries.forEach {
                if (it.phrase.lowercase() == passphrase.lowercase()) {
                    val databaseTargets = currentUser.databaseTargets
                    //new code writes and reads target Strings as uppercase
                    //so this should work even with old prefs that may have lowercase
                    if (!databaseTargets.contains(it.phrase)) {
                        currentUser.databaseTargets = databaseTargets + listOf(it.phrase)
                    }
                    fulfill(Unit)
                }
            }
            //if none of the above match
            reject(Error("The passphrase was incorrect."))
        }
    }
}
