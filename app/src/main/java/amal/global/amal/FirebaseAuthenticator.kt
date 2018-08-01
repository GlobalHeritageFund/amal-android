package amal.global.amal

import android.app.Activity
import android.content.Intent
import com.firebase.ui.auth.AuthUI

class FirebaseAuthenticator(val activity: Activity) {
    companion object {
        const val firebaseAuthResultCode = 7344
    }

    var onComplete: () -> Unit = { }

    fun start() {
        activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(listOf(
                                AuthUI.IdpConfig.EmailBuilder().build()
                        ))
                        .build(),
                FirebaseAuthenticator.firebaseAuthResultCode
        )

    }

    fun finalize(requestCode: Int, intent: Intent?): Boolean {
        val valid = requestCode == FirebaseAuthenticator.firebaseAuthResultCode
        if (valid) { onComplete() }
        return valid
    }
}