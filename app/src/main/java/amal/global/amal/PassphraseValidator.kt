package amal.global.amal

import android.content.Context

class PassphraseValidator() {

    fun validate(passphrase: String): Promise<Unit> {
        return Promise({ fulfill, reject ->
            if (passphrase == "EAMENA") {
                fulfill(Unit)
            } else {
                reject(Error("The passphrase was incorrect."))
            }
        })
    }
}