package amal.global.amal

import amal.global.amal.databinding.ActivityTabBinding
import amal.global.amal.onboarding.OnboardingActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.content.Intent
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.appcompat.app.AlertDialog
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.*

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
        SettingsFragmentDelegate,
        EditLocationFragmentDelegate
{

    companion object {
        const val TAG = "Tab Activity"
    }

    private lateinit var binding: ActivityTabBinding

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
        binding = ActivityTabBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

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

//    override fun imageTapped(fragment: GalleryFragment, image: LocalImage) {
//        Log.d(TAG,"imageTapped called")
//        val assessFragment = AssessFragment().also { it.delegate = this }
//        assessFragment.image = image
//        pushFragment(assessFragment)
//    }

    override fun choseImagesToAssess(fragment: GalleryFragment, images: List<LocalImage>) {
        val assessFragment = AssessFragment().also { it.delegate = this }
        assessFragment.imageList = images
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

    override fun tappedDraftReport(draftReport: ReportDraft, reportsFragment: ReportsFragment) {
        val fragment = NewReportFragment()
        fragment.existingDraft = draftReport
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
        pushFragment(fragment)
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

                        builder.setMessage("You activated the ${RestTarget[passphrase.uppercase()]} database target.");

                        builder.setPositiveButton("OK") { dialog, which -> returnToSettings() }
                        builder.show()
                    }
                }
                .catch {
                    this.runOnUiThread {
                        val builder = AlertDialog.Builder(this);
                        builder.setMessage("No database target with that passphrase found.");
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
                .setOnClickListener {
                    dialog.dismiss()
                    val authenticator = FirebaseAuthenticator(this, { upload(report) })
                    registerAndStartIntentRequest(authenticator)
                }
        contentView
                .findViewById<View>(R.id.publishAnonymouslyView)
                .setOnClickListener {
//                    fragment.uploadItem?.isEnabled = false
                    upload(report)
                            .catch {
                                this.runOnUiThread {
//                                    fragment.uploadItem?.isEnabled = true
                                }
                            }
                    dialog.dismiss()
                }
        contentView
                .findViewById<View>(R.id.cancelView)
                .setOnClickListener {
                    dialog.dismiss()
                }

        dialog.show()
    }

    fun upload(report: ReportDraft): Promise<ReportInterface> {

        val promise: Promise<ReportInterface>
        if (report.restTarget != null) {
            var uploader = RestReportUploader(report)
            uploader.upload()
            promise = uploader.promise
        } else {
            var uploader = FirebaseReportUploader(report)
            uploader.upload()
            promise = uploader.promise
        }

        return promise.then {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.content, ReportsFragment().also { it.delegate = this })
                    .commit()

        }.catch { error ->
            error.message?.let { Log.e("Upload to target ", it) }
        }
    }

    override fun pdfReportTapped(reportDetailFragment: ReportDetailFragment) {
        reportDetailFragment.report.fetchPDFURL().then { uri ->
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(browserIntent)
        }
    }

    override fun webReportTapped(reportDetailFragment: ReportDetailFragment) {
        val browserIntent = Intent(Intent.ACTION_VIEW, reportDetailFragment.report.webURL)
        startActivity(browserIntent)
    }

    override fun mapTapped(fragment: AssessFragment) {
        val map = MapFragment()
        map.images = fragment.imageList as List<Image>
        pushFragment(map)
    }

    override fun editLocationTapped(fragment: AssessFragment) {
        val editLocation = EditLocationFragment().also{it.delegate = this}
        editLocation.imageList = fragment.imageList
        pushFragment(editLocation)
    }

    override fun deleteButtonTapped(fragment: AssessFragment, imageList: List<LocalImage>?) {
        imageList?.forEach {
            if (it.filePath != null && it.settingsPath != null) {
                PhotoStorage(this).deleteImage(it.filePath, it.settingsPath)
            }
        }
        binding.navigation.selectedItemId = R.id.navigation_assess
    }

    override fun returnToAssessFragment(fragment: EditLocationFragment) {
        supportFragmentManager.popBackStack()
    }

    override fun saveButtonTapped(fragment: AssessFragment) {
        supportFragmentManager.popBackStack()
    }

    override fun returnToReports() {
        val fragment = ReportsFragment().also{ it.delegate = this }
        pushFragment(fragment)
    }

    private fun returnToSettings() {
        val fragment = SettingsFragment().also { it.delegate = this }
        pushFragment(fragment)
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
