package amal.global.amal

import android.app.Fragment
import android.app.FragmentTransaction
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View

class TabActivity : AppCompatActivity(), GalleryDelegate {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_assess -> {
                val assess = GalleryFragment()
                assess.delegate = this
                supportFragmentManager.beginTransaction().replace(R.id.content, assess).commit()
                supportActionBar?.setTitle(R.string.title_assess)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_capture -> {
                supportFragmentManager.beginTransaction().replace(R.id.content, CaptureFragment()).commit()
                supportActionBar?.setTitle(R.string.title_capture)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_report -> {
                supportFragmentManager.beginTransaction().replace(R.id.content, ReportFragment()).commit()
                supportActionBar?.setTitle(R.string.title_report)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
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
                .remove(fragment)
                .add(R.id.content, assessFragment)
                .addToBackStack(null)
                .commit()

    }

}
