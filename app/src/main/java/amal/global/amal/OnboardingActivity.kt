package amal.global.amal

import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.R.id.edit
import android.content.SharedPreferences



class OnboardingActivity : AppCompatActivity() {

    val currentUser: CurrentUser by lazy {
        CurrentUser(this)
    }

    private fun finishOnboarding() {
        currentUser.onboardingComplete = true

        startActivity(Intent(this, TabActivity::class.java))

        finish()
    }
}