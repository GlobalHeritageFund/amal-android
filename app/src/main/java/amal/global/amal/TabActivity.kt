package amal.global.amal

import amal.global.amal.onboarding.OnboardingActivity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.content.Intent
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AlertDialog
import android.view.View
import com.google.firebase.auth.FirebaseAuth

interface IntentRequest {
    val requestCode: Int

    fun start()
    fun finalize(requestCode: Int, intent: Intent?): Boolean
}

class TabActivity : AppCompatActivity(),
        GalleryDelegate,
        ReportsDelegate,
        ChooseImagesFragmentDelegate,
        NewReportFragmentDelegate,
        ReportDetailFragmentDelegate,
        AssessDelegate,
        CaptureDelegate,
        PassphraseFormFragmentDelegate,
        SettingsFragmentDelegate
{
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

    private var currentIntentRequests = mutableListOf<IntentRequest>()

    fun registerAndStartIntentRequest(intentRequest: IntentRequest) {
        currentIntentRequests.add(intentRequest)
        intentRequest.start()
    }

    fun finalizeIntentRequest(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        val index = currentIntentRequests
                .indexOfFirst { it.requestCode == requestCode }
        if (index == -1) { return false }
        val intentRequest = currentIntentRequests[index]
        currentIntentRequests.removeAt(index)
        if (resultCode != RESULT_OK ) { return false }
        return intentRequest.finalize(requestCode, intent)
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
        val assessFragment = AssessFragment().also { it.delegate = this }
        assessFragment.image = image
        pushFragment(assessFragment)
    }

    override fun importButtonTapped(fragment: GalleryFragment) {
        val imageImporter = ImageImporter(this, {
            fragment.updateData()
        })
        registerAndStartIntentRequest(imageImporter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (finalizeIntentRequest(requestCode, resultCode, intent)) {
            return
        }
        super.onActivityResult(requestCode, resultCode, intent)
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
        val fragment = SettingsFragment().also { it.delegate = this }
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .replace(R.id.content, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun signInTapped(fragment: SettingsFragment) {
    val authenticator = FirebaseAuthenticator(this, { fragment.configureView() })
    registerAndStartIntentRequest(authenticator)
}

    override fun signOutTapped(fragment: SettingsFragment) {
        CurrentUser(this).signOut()
    }

    override fun passphraseButtonTapped(fragment: SettingsFragment) {
        pushFragment(PassphraseFormFragment().also { it.delegate = this })
    }

    override fun passphraseEntered(passphrase: String, fragment: PassphraseFormFragment) {
        PassphraseValidator(this).validate(passphrase)
                .then {
                    this.runOnUiThread {
                        val builder = AlertDialog.Builder(this);
                        builder.setMessage("It worked");
                        builder.setPositiveButton("OK") { dialog, which -> }
                        builder.show()
                    }
                }
                .catch {
                    this.runOnUiThread {
                        val builder = AlertDialog.Builder(this);
                        builder.setMessage("It did not work");
                        builder.setPositiveButton("OK"
                        ) { dialog, which -> }
                        builder.show()
                    }
                }
    }

    override fun uploadReport(fragment: NewReportFragment, report: ReportDraft) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            upload(report)
            return
        }
        val contentView = layoutInflater.inflate(R.layout.publish_action_sheet, null)

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(contentView)

        contentView
                .findViewById<View>(R.id.logInView)
                .setOnClickListener({
                    dialog.dismiss()
                    val authenticator = FirebaseAuthenticator(this, { upload(report) })
                    registerAndStartIntentRequest(authenticator)
                })
        contentView
                .findViewById<View>(R.id.publishAnonymouslyView)
                .setOnClickListener({
                    fragment.uploadItem?.isEnabled = false
                    upload(report)
                            .catch {
                                fragment.uploadItem?.isEnabled = true
                            }
                    dialog.dismiss()
                })
        contentView
                .findViewById<View>(R.id.cancelView)
                .setOnClickListener({
                    dialog.dismiss()
                })

        dialog.show()
    }

    fun upload(report: ReportDraft): Promise<Report> {
        val uploader = ReportUploader(report)

        uploader.upload()
        
        return uploader.promise.then {
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

    override fun mapTapped(fragment: AssessFragment) {
        val map = MapFragment()
        map.images = listOf(fragment.image!!)
        pushFragment(map)
    }

    override fun editLocationTapped(fragment: AssessFragment) {
        val editLocation = EditLocationFragment()
        editLocation.image = fragment.image
        pushFragment(editLocation)
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
