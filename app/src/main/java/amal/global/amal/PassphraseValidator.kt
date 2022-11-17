package amal.global.amal

import android.content.Context

class PassphraseValidator(val context: Context) {

    val currentUser: CurrentUser
        get() = CurrentUser(context)

//    val eamenaPassphrase = "EAMENA"
//    val eamenaTargetKey = "EAMENA"

    fun validate(passphrase: String): Promise<Unit> {
        return Promise { fulfill, reject ->
            RestTarget.values().forEach {
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


//            if (passphrase.lowercase() == eamenaPassphrase.lowercase()) {
//                val databaseTargets = currentUser.databaseTargets
//                if (!databaseTargets.contains(eamenaTargetKey)) {
//                    currentUser.databaseTargets = databaseTargets + listOf(eamenaTargetKey)
//                }
//                fulfill(Unit)
//            } else {
//                reject(Error("The passphrase was incorrect."))
//            }
