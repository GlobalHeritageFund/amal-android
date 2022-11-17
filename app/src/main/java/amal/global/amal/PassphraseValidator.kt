package amal.global.amal

import android.content.Context

class PassphraseValidator(val context: Context) {

    val currentUser: CurrentUser
        get() = CurrentUser(context)

    val eamenaPassphrase = "EAMENA"

    val eamenaTargetKey = "EAMENA"

    fun validate(passphrase: String): Promise<Unit> {
        return Promise({ fulfill, reject ->
            if (passphrase.lowercase() == eamenaPassphrase.lowercase()) {
                val databaseTargets = currentUser.databaseTargets
                if (!databaseTargets.contains(eamenaTargetKey)) {
                    currentUser.databaseTargets = databaseTargets + listOf(eamenaTargetKey)
                }
                fulfill(Unit)
            } else {
                reject(Error("The passphrase was incorrect."))
            }
        })
    }
}