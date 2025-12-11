package global.amal.app

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthenticator(private val activity: Activity, val onComplete: () -> Unit): IntentRequest {

    companion object {
        private const val TAG = "FirebaseAuthenticator"
        const val FIREBASE_AUTH_REQUEST_CODE = 7344
    }

    override val requestCode = FIREBASE_AUTH_REQUEST_CODE

    override fun start() {
        Log.d(TAG, "Starting FirebaseUI with request code: $requestCode")
        val providers = listOf(AuthUI.IdpConfig.EmailBuilder().build())
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()
        activity.startActivityForResult(signInIntent, requestCode)

    }

    //could the problem be with the result code
    override fun finalize(requestCode: Int, resultCode: Int, intent: Intent?): Boolean { // Added resultCode
        Log.d(TAG, "finalize called - Request: $requestCode, Result: $resultCode")
        if (requestCode != this.requestCode) {
            Log.w(TAG, "finalize received mismatched request code: $requestCode (expected ${this.requestCode})")
            return false
        }
        if (resultCode == Activity.RESULT_OK) {
            // Successfully signed in
            Log.d("FirebaseAuthenticator", "Sign-in successful. User: ${FirebaseAuth.getInstance().currentUser?.email}")
        } else {
            // Sign in failed or cancelled
            val response = IdpResponse.fromResultIntent(intent)
            if (response == null) {
                Log.w("FirebaseAuthenticator", "Sign-in cancelled by user.")
                // Optionally: have an onCancel or onFailure callback here
            } else {
                Log.e("FirebaseAuthenticator", "Sign-in failed: ", response.error)
                // Optionally: have an onFailure callback here
            }
        }
        Log.d(TAG, "finalize calling onComplete callback.")
        onComplete()
        return true
        }
}