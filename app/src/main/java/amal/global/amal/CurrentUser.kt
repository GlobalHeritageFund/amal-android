package amal.global.amal

import android.content.Context
import android.content.SharedPreferences
import java.util.*

class CurrentUser(var context: Context) {

    val preferenceName = "MyPreferences"

    val tokenKey = "AMALDeviceToken"

    val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
    }

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

}