package amal.global.amal

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class CurrentUser(var context: Context) {

    val isLoggedIn: Boolean
        get() = FirebaseAuth.getInstance().currentUser != null

    val email: String?
        get() = FirebaseAuth.getInstance().currentUser?.email ?: ""

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    private val preferenceName = "MyPreferences"

    val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
    }

    private val tokenKey = "AMALDeviceToken"

    var token: String
        get() {
            var result = preferences.getString(tokenKey, "not found")
            if (result == "not found") {
                result = UUID.randomUUID().toString()
                this.token = result
            }
            return result
        }
        set(value) {
            val editor = preferences.edit()
            editor.putString(tokenKey, value)
            editor.apply()
        }

    private val onboardingKey = "AMALOnboardingComplete"

    var onboardingComplete: Boolean
        get() {
            return preferences.getBoolean(onboardingKey, false)
        }
        set(value) {
            val editor = preferences.edit()
            editor.putBoolean(onboardingKey, value)
            editor.apply()
        }

}