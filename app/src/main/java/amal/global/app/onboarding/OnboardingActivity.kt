package global.amal.app.onboarding

import global.amal.app.CurrentUser
import global.amal.app.R
import global.amal.app.TabActivity
import global.amal.app.databinding.ActivityOnboardingBinding
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import android.view.View

class OnboardingActivity : AppCompatActivity() {

    lateinit var binding: ActivityOnboardingBinding

    var adapter: FragmentStatePagerAdapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> OnboardingPage1()
                1 -> OnboardingPage2()
                2 -> OnboardingPage3()
                3 -> OnboardingPage4()
                else -> OnboardingPage1()
            }
        }

        override fun getCount() = 4
    }

    val currentUser: CurrentUser by lazy {
        CurrentUser(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        this.binding.skipButton.setOnClickListener({ view ->
            finishOnboarding()
        })

        this.binding.nextButton.setOnClickListener({ view ->
            if (binding.pager.currentItem == adapter.count - 1) {
                finishOnboarding()
            } else {
                binding.pager.setCurrentItem(binding.pager.currentItem + 1, true)
            }
        })

        this.binding.pager.adapter = adapter

        this.binding.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                if(position == adapter.count - 1){
                    binding.skipButton.visibility = View.GONE
                    binding.nextButton.text = getString(R.string.done)
                } else {
                    binding.skipButton.visibility = View.VISIBLE
                    binding.nextButton.text = getString(R.string.next)
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