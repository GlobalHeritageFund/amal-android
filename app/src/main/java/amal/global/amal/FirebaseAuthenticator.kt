package amal.global.amal

import android.app.Activity
import android.content.Intent
import com.firebase.ui.auth.AuthUI

class FirebaseAuthenticator(val activity: Activity, val onComplete: () -> Unit): IntentRequest {

    override val requestCode: Int
        get() = 7344

    override fun start() {
        activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(listOf(
                                AuthUI.IdpConfig.EmailBuilder().build()
                        ))
                        .build(),
                requestCode
        )

    }

    override fun finalize(requestCode: Int, intent: Intent?): Boolean {
        val valid = requestCode == requestCode
        if (valid) { onComplete() }
        return valid
    }
}