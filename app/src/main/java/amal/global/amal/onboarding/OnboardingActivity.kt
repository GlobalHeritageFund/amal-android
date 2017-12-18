package amal.global.amal.onboarding

import amal.global.amal.CurrentUser
import amal.global.amal.R
import amal.global.amal.TabActivity
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_onboarding.*


class OnboardingActivity : AppCompatActivity() {

    val currentUser: CurrentUser by lazy {
        CurrentUser(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        this.skipButton.setOnClickListener({ view ->
            finishOnboarding()
        })

        this.nextButton.setOnClickListener({ view ->
            if (pager.currentItem == 2) {
                finishOnboarding()
            } else {
                pager.setCurrentItem(pager.currentItem + 1, true)
            }
        })

    }

    private fun finishOnboarding() {
        currentUser.onboardingComplete = true

        startActivity(Intent(this, TabActivity::class.java))

        finish()
    }
}