package amal.global.amal.onboarding

import amal.global.amal.CurrentUser
import amal.global.amal.R
import amal.global.amal.TabActivity
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import kotlinx.android.synthetic.main.activity_onboarding.*
import android.support.v4.app.FragmentStatePagerAdapter

class OnboardingActivity : AppCompatActivity() {

    var adapter: FragmentStatePagerAdapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0 -> OnboardingPage1()
                1 -> OnboardingPage2()
                2 -> OnboardingPage3()
                else -> null
            }
        }

        override fun getCount(): Int {
            return 3
        }
    }

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
            if (pager.currentItem == adapter.count - 1) {
                finishOnboarding()
            } else {
                pager.setCurrentItem(pager.currentItem + 1, true)
            }
        })

        this.pager.adapter = adapter
    }

    private fun finishOnboarding() {
        currentUser.onboardingComplete = true

        startActivity(Intent(this, TabActivity::class.java))

        finish()
    }
}