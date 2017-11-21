package amal.global.amal

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

class TabActivity : AppCompatActivity(), GalleryDelegate, ReportsDelegate {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var fragment: Fragment? = null
        when (item.itemId) {
            R.id.navigation_assess -> {
                fragment = GalleryFragment().also { it.delegate = this }
            }
            R.id.navigation_capture -> {
                fragment = CaptureFragment()
            }
            R.id.navigation_report -> {
                fragment = ReportsFragment().also { it.delegate = this }
            }
        }
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.content, fragment).commit()
        }
        fragment != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab)

        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.navigation_capture
    }

    override fun imageTapped(fragment: GalleryFragment, image: Image) {
        val assessFragment = AssessFragment()
        assessFragment.image = image
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .replace(R.id.content, assessFragment)
                .addToBackStack(null)
                .commit()

    }

    override fun newReportTapped(reportsFragment: ReportsFragment) {
        val chooseImages = ChooseImagesFragment()
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .replace(R.id.content, chooseImages)
                .addToBackStack(null)
                .commit()
    }

}
