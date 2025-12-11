package global.amal.app

import global.amal.app.databinding.ActivityTabBinding
import global.amal.app.onboarding.OnboardingActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.content.Intent
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider


interface IntentRequest {
    val requestCode: Int
    fun start()
    fun finalize(requestCode: Int, resultCode: Int, intent: Intent?): Boolean
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

    private data class UploadResult(
        val success: Boolean,
        val message: String,
        val reportInterface: ReportInterface? = null // To pass back if needed, though not directly used in dialog
    )

    private lateinit var binding: ActivityTabBinding
    private val activityViewModel: TabActivityViewModel by viewModels()

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


    fun registerAndStartIntentRequest(intentRequest: IntentRequest) {
        activityViewModel.addRequest(intentRequest)
        intentRequest.start()
    }

    fun finalizeIntentRequest(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        // Find and remove from ViewModel's list
        val intentRequest = activityViewModel.findAndRemoveRequest(requestCode)
        if (intentRequest == null) {
            Log.w(TAG,"finalizeIntentRequest: No request found for code $requestCode")
            return false
        }

        // Call finalize on the retrieved request
        Log.d(TAG,"finalizeIntentRequest: Found request for code $requestCode, calling finalize...")
        // Check result code before finalizing if necessary, or let finalize handle it
        if (resultCode != RESULT_OK) {
            Log.w(TAG,"finalizeIntentRequest: Result code is NOT OK ($resultCode) for request code $requestCode")
            // Still call finalize as it should signal completion regardless
            // return intentRequest.finalize(requestCode, resultCode, intent)
        }
        // Always call finalize to let the IntentRequest handle the result/completion signal
        return intentRequest.finalize(requestCode, resultCode, intent)
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
        Log.d(TAG, "onActivityResult called - Request: $requestCode, Result: $resultCode, Intent: $intent")

        // --- Attempt to retrieve the request WITHOUT immediately removing it ---
        val intentRequest = activityViewModel.currentIntentRequests.find { it.requestCode == requestCode } // Use find, not findAndRemoveRequest

        if (intentRequest != null) {
            Log.d(TAG, "Found IntentRequest for code $requestCode in ViewModel. Calling its finalize method.")
            // Let the IntentRequest's finalize method decide if it handled it and signal completion.
            // The IntentRequest's finalize should call onComplete.
            val handled = intentRequest.finalize(requestCode, resultCode, intent)

            // --- Remove the request AFTER finalize has been called ---
            activityViewModel.findAndRemoveRequest(requestCode) // Now remove it

            if (handled) {
                Log.d(TAG, "IntentRequest.finalize for $requestCode returned true.")
                return // If finalize handled it (returned true), we are done.
            } else {
                Log.w(TAG, "IntentRequest.finalize for $requestCode returned false.")
            }
        } else {
            Log.w(TAG, "onActivityResult: No IntentRequest found in ViewModel for code $requestCode. Did it get removed prematurely or ViewModel cleared?")
        }

        Log.d(TAG, "onActivityResult not fully handled by an IntentRequest or request not found, calling super.")
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
                        val builder = AlertDialog.Builder(this)

                        builder.setMessage("You activated the ${RestTarget[passphrase.uppercase()]?.phrase} database target.")

                        builder.setPositiveButton("OK") { dialog, which -> returnToSettings() }
                        builder.show()
                    }
                }
                .catch {
                    this.runOnUiThread {
                        val builder = AlertDialog.Builder(this)
                        builder.setMessage("No database target with that passphrase found.")
                        builder.setPositiveButton("OK"
                        ) { dialog, which -> }
                        builder.show()
                    }
                }
    }

    override fun uploadReport(fragment: NewReportFragment, report: ReportDraft) {
        // Show a progress indicator immediately (e.g., a non-cancelable dialog or update NewReportFragment's UI)
        // For simplicity, we'll rely on NewReportFragment to manage its own "uploading" state UI.
        // NewReportFragment should disable its upload button now.

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            Log.d(TAG, "User already authenticated. Proceeding with upload.")
            performUploadSequence(report, fragment)
        } else {
            // User is not authenticated, show login sheet
            Log.d(TAG, "User not authenticated. Showing login prompt.")
            val contentView = layoutInflater.inflate(R.layout.publish_action_sheet, null)
            val dialog = BottomSheetDialog(this)
            dialog.setContentView(contentView)

            contentView.findViewById<View>(R.id.logInView).setOnClickListener {
                dialog.dismiss()
                val authenticator = FirebaseAuthenticator(this) {
                    Log.d(TAG, "FirebaseAuthenticator onComplete triggered.")
                    runOnUiThread {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            // Auth successful (user is now non-null after the attempt)
                            Log.d(TAG, "Authentication successful via Authenticator (User: ${user.uid}). Proceeding with upload.")
                            performUploadSequence(report, fragment)
                        } else {
                            // Auth failed or was cancelled (user is still null after the attempt)
                            Log.w(TAG, "Authentication unsuccessful or cancelled via Authenticator.")
                            showUploadResultDialog(UploadResult(false, "Authentication failed or was cancelled. Report not uploaded."))
                            fragment.handleUploadAttemptComplete(false) // Signal to fragment
                        }
                    }
                }
                registerAndStartIntentRequest(authenticator)
            }

            contentView.findViewById<View>(R.id.cancelView).setOnClickListener {
                Log.d(TAG, "Login prompt cancelled.")
                dialog.dismiss()
                // User cancelled the login action sheet.
                // NewReportFragment's UI should already be re-enabled by itself if it has timeout or cancel for its "uploading" state.
                // Or, we can signal it:
                fragment.handleUploadAttemptComplete(false) // Signal to fragment that the attempt is over
            }
            dialog.show()
        }
    }

    private fun performUploadSequence(reportDraftToUpload: ReportDraft, callingFragment: NewReportFragment) {
        val uploaderPromise: Promise<ReportInterface>
        val targetSystemName: String
//changed toast from after networkconnected in newreport fragment because was showing up even when needed to login
        Toast.makeText(this, "Uploading report...", Toast.LENGTH_SHORT).show()
        if (reportDraftToUpload.restTarget != null) {
            Log.d(TAG, "Uploading to REST target: ${reportDraftToUpload.restTarget?.name}")
            val uploader = RestReportUploader(reportDraftToUpload)
            uploader.upload() // Starts the OkHttp calls
            uploaderPromise = uploader.promise
            targetSystemName = reportDraftToUpload.restTarget!!.name // Safe due to check
        } else {
            Log.d(TAG, "Uploading to Firebase")
            val uploader = FirebaseReportUploader(reportDraftToUpload)
            uploader.upload() // Starts Firebase calls
            uploaderPromise = uploader.promise
            targetSystemName = "Firebase"
        }

        uploaderPromise
            .then { uploadedReport -> // uploadedReport is ReportInterface
                // SUCCESS
                Log.d(TAG, "Upload successful to $targetSystemName.")
                DraftUtils.deleteDraft(this, reportDraftToUpload.id) // Delete the draft
                runOnUiThread {
                    showUploadResultDialog(
                        UploadResult(true, "Report successfully uploaded to $targetSystemName.")
                    )
                    // Navigate to reports list
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.content, ReportsFragment().also { it.delegate = this })
                        .commitAllowingStateLoss() // Use if there's any chance this is after onSaveInstanceState
                }
                callingFragment.handleUploadAttemptComplete(true)
            }
            .catch { error ->
                // FAILURE
                Log.e(TAG, "Upload failed for $targetSystemName: ${error.message}", error)
                DraftUtils.saveDraft(this, reportDraftToUpload)
                val technicalErrorMessage = parseUploadErrorMessage(error, targetSystemName)
                val finalErrorMessage = "$technicalErrorMessage\nThe report has been saved as a draft."
                runOnUiThread {
                    showUploadResultDialog(UploadResult(false, finalErrorMessage))
                }
                callingFragment.handleUploadAttemptComplete(false)
            }
    }

    private fun showUploadResultDialog(result: UploadResult) {
        if (isFinishing || isDestroyed) return

        AlertDialog.Builder(this)
            .setTitle(if (result.success) "Upload Successful" else "Upload Failed")
            .setMessage(result.message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun parseUploadErrorMessage(error: Error, serviceName: String): String {
        val errorMessage = error.message ?: "Unknown error from $serviceName."
        Log.e(TAG, "Original upload error to $serviceName: $errorMessage", error)
        Log.e(TAG,"Tried to post to ${RestTarget.fromPhrase(serviceName)?.url}.")

        // Add more specific parsing based on error.message content if needed
        if (errorMessage.contains("failed to connect to", true) || errorMessage.contains("UnknownHostException", true)) {
            return "Upload to $serviceName failed. Could not connect to the server. Please verify that you have database access."
        }
        if (errorMessage.contains("Permission denied", true) && serviceName == "Firebase") {
            return "Upload to Firebase failed. Permission denied. If you aren't logged in, please log in and try again."
        }
        if (errorMessage.contains("IOException", true) && errorMessage.contains("parse",true) && errorMessage.contains("JSON", true) ){
            return "Upload to $serviceName failed. The server's response was not in the expected format."
        }
        if (errorMessage.contains("REST API Error", true)){ // From our improved RestUploader
            return "Upload to $serviceName failed: $errorMessage"
        }
        return "Upload to $serviceName failed: $errorMessage"
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
        map.images = fragment.imageList
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
