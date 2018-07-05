package amal.global.amal

import amal.global.amal.onboarding.OnboardingActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.content.Intent



class TabActivity : AppCompatActivity(),
        GalleryDelegate,
        ReportsDelegate,
        ChooseImagesFragmentDelegate,
        NewReportFragmentDelegate,
        ReportDetailFragmentDelegate,
        CaptureDelegate {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var fragment: Fragment = when (item.itemId) {
            R.id.navigation_assess -> {
                GalleryFragment().also { it.delegate = this }
            }
            R.id.navigation_capture -> {
                CaptureFragment().also { it.delegate = this }
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

        if (!CurrentUser(this).onboardingComplete) {
            val onboarding = Intent(this, OnboardingActivity::class.java)
            startActivity(onboarding)
            finish()
            return
        }

        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.navigation_capture
    }

    override fun imageTapped(fragment: GalleryFragment, image: LocalImage) {
        val assessFragment = AssessFragment()
        assessFragment.image = image
        pushFragment(assessFragment)
    }

    override fun newReportTapped(reportsFragment: ReportsFragment) {
        pushFragment(ChooseImagesFragment().also { it.delegate = this })
    }

    override fun tappedReport(report: Report, reportsFragment: ReportsFragment) {
        val fragment = ReportDetailFragment()
        fragment.report = report
        fragment.delegate = this
        pushFragment(fragment)
    }

    override fun choseImages(fragment: ChooseImagesFragment, images: List<LocalImage>) {
        val fragment = NewReportFragment()
        fragment.report.deviceToken = CurrentUser(this).token
        fragment.report.images = images
        fragment.delegate = this
        pushFragment(fragment)
    }

    override fun settingsButtonTapped(fragment: CaptureFragment) {
        val fragment = SettingsFragment()
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .replace(R.id.content, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun uploadReport(fragment: NewReportFragment, report: ReportDraft) {
        val uploader = ReportUploader(report)

        uploader.upload()

        uploader.promise.then {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.content, ReportsFragment().also { it.delegate = this })
                    .commit()

        }.catch { error ->
            Log.e("asdf", error.message)
        }

    }

    override fun pdfReportTapped(reportDetailFragment: ReportDetailFragment) {
        reportDetailFragment.report.fetchPDFURL().then({ uri ->
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(browserIntent)
        })
    }

    override fun webReportTapped(reportDetailFragment: ReportDetailFragment) {
        val browserIntent = Intent(Intent.ACTION_VIEW, reportDetailFragment.report.webURL)
        startActivity(browserIntent)
    }

    private fun pushFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .replace(R.id.content, fragment)
                .addToBackStack(null)
                .commit()

    }
}
