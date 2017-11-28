package amal.global.amal

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

class TabActivity : AppCompatActivity(), GalleryDelegate, ReportsDelegate, ChooseImagesFragmentDelegate {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var fragment: Fragment = when (item.itemId) {
            R.id.navigation_assess -> {
                GalleryFragment().also { it.delegate = this }
            }
            R.id.navigation_capture -> {
                CaptureFragment()
            }
            R.id.navigation_report -> {
                ReportsFragment().also { it.delegate = this }
            }
            else -> {
                return@OnNavigationItemSelectedListener false
            }
        }
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
        true
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
        pushFragment(assessFragment)
    }

    override fun newReportTapped(reportsFragment: ReportsFragment) {
        pushFragment(ChooseImagesFragment().also { it.delegate = this })
    }

    override fun choseImages(fragment: ChooseImagesFragment, images: List<Image>) {
        //push new report form fragment
    }

    fun pushFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .replace(R.id.content, fragment)
                .addToBackStack(null)
                .commit()

    }
}
