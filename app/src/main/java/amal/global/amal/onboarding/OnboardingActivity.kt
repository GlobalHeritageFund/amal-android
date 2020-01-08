package amal.global.amal.onboarding

import amal.global.amal.CurrentUser
import amal.global.amal.R
import amal.global.amal.TabActivity
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_onboarding.*
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import android.view.View

class OnboardingActivity : AppCompatActivity() {

    var adapter: FragmentStatePagerAdapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0 -> OnboardingPage1()
                1 -> OnboardingPage2()
                2 -> OnboardingPage3()
                3 -> OnboardingPage4()
                else -> null
            }
        }

        override fun getCount() = 4
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

        this.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                if(position == adapter.count - 1){
                    skipButton.visibility = View.GONE
                    nextButton.text = "Done"
                } else {
                    skipButton.visibility = View.VISIBLE
                    nextButton.text = "Next";
                }
            }

            override fun onPageScrollStateChanged(state: Int) { }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }
        })
    }

    private fun finishOnboarding() {
        currentUser.onboardingComplete = true

        startActivity(Intent(this, TabActivity::class.java))

        finish()
    }
}