package amal.global.amal

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View

class TabActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_assess -> {
                supportFragmentManager.beginTransaction().replace(R.id.content, AssessFragment()).commit()
                supportActionBar!!.setTitle(R.string.title_assess)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_capture -> {
                supportFragmentManager.beginTransaction().replace(R.id.content, CaptureFragment()).commit()
                supportActionBar!!.setTitle(R.string.title_capture)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_report -> {
                supportFragmentManager.beginTransaction().replace(R.id.content, ReportFragment()).commit()
                supportActionBar!!.setTitle(R.string.title_report)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab)

        val navigation = findViewById<View>(R.id.navigation) as BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.navigation_capture
    }

}
