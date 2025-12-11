package global.amal.app

import global.amal.app.databinding.FragmentNewReportBinding
import android.content.Context
import android.content.SharedPreferences
//is there a problem with not having appcompat context anymore import androidx.core.content.ContentProviderCompat.requireContext
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.DateFormat
import java.util.*

interface NewReportFragmentDelegate {
    fun uploadReport(fragment: NewReportFragment, report: ReportDraft)
    fun returnToReports()
}

class NewReportFragment: Fragment() {

    companion object {
        const val TAG = "New Report Fragment"
        const val FIREBASE_RADIO_ID = 0x01000000//set to avoid collisions with generated IDs for rest dbs
    }

    enum class NetworkStatus {
        NOTCONNECTED,
        CONNECTEDBUTMETERED,
        CONNECTED;
    }

    private var _binding: FragmentNewReportBinding? = null
    private var reportDraftList: MutableList<ReportDraft> = mutableListOf()

    private val binding get() = _binding!!
    private val draftReportPreferenceName = ReportsAdapter.DRAFT_REPORT_PREFERENCE
    private val preferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences(draftReportPreferenceName, Context.MODE_PRIVATE)
    }

    var existingDraft: ReportDraft? = null
    var report = ReportDraft() //this creates a new draft with default values
    private val lastSelectedDbPrefKey = CurrentUser.PREF_LAST_SELECTED_DB
    private val firebaseDbIdentifier = CurrentUser.FIREBASE_DB_IDENTIFIER
    //to map radiobutton ids to resttarget
    private val idToTarget = mutableMapOf<Int, RestTarget>()

    var delegate: NewReportFragmentDelegate? = null

    private var cancelItem: MenuItem? = null
    //right not choosing not to bother with enabling and disabling the cancel and publish buttons

    private var isUploading = false

    private val currentUser: CurrentUser
        get() = CurrentUser(this.requireContext())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        _binding = FragmentNewReportBinding.inflate(inflater, container, false)
        val view = binding.root
        val radioGroup = binding.dbChooser
        //both phrase and databaseTargets should be in uppercase
        val dbTargets = currentUser.databaseTargets
        val publishButton = binding.publishReportButton

        val firebaseRadioButton = RadioButton(requireContext())
        firebaseRadioButton.text = getString(R.string.firebase_default_db)
        firebaseRadioButton.id = FIREBASE_RADIO_ID
        radioGroup.addView(firebaseRadioButton)

        val lastSelectedDb = preferences.getString(lastSelectedDbPrefKey, firebaseDbIdentifier)
        var defaultCheckId = FIREBASE_RADIO_ID

        idToTarget.clear()
        RestTarget.entries.forEach { target ->
            if (target.phrase in dbTargets) {
                val rb = RadioButton(requireContext()).apply {
                    text = target.toString()
                    id = View.generateViewId()
                }
                idToTarget[rb.id] = target
                radioGroup.addView(rb)

               // Pick default selection based on existing draft or last preference
                if (existingDraft?.restTarget == target || lastSelectedDb == target.name) {
                    defaultCheckId = rb.id
               }
           }
        }

        // Set the default check *after* adding all buttons

        radioGroup.check(defaultCheckId)
        updateSendingToText(defaultCheckId) // Update text based on initial check


        radioGroup.setOnCheckedChangeListener {
                _, checkedId ->
            updateSendingToText(checkedId)
        }
        publishButton.setOnClickListener{
            if (isUploading) {
                Toast.makeText(requireContext(), "Upload in progress...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            setFinalReportValues()
            when (getNetworkAvailability(requireContext())) {
                NetworkStatus.NOTCONNECTED -> {
                    createNoNetworkAlert()
                }
                NetworkStatus.CONNECTEDBUTMETERED -> {
                    createMeteredAlert()
                }
                NetworkStatus.CONNECTED -> {
                    isUploading = true
                    delegate?.uploadReport(this, report)
                }
            }
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_new_report, menu)
        cancelItem = menu.findItem(R.id.cancelReport)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val date: Date

        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = getString(R.string.title_report)

        if (existingDraft != null && !existingDraft?.title.isNullOrEmpty()) {
            binding.titleField.setText(existingDraft?.title)
        }

        if (existingDraft != null && !existingDraft?.assessorEmail.isNullOrEmpty()) {
            binding.emailField.setText(existingDraft?.assessorEmail)
        } else {
            binding.emailField.setText(currentUser.email)
        }

        if (existingDraft != null) {
            val checkId = if (existingDraft!!.restTarget != null &&
                existingDraft!!.restTarget!!.phrase in currentUser.databaseTargets) {
                // map enum to its RadioButton id
                idToTarget.entries.firstOrNull { it.value == existingDraft!!.restTarget }?.key
                    ?: FIREBASE_RADIO_ID
                } else {
                    FIREBASE_RADIO_ID
                }
            binding.dbChooser.check(checkId) // Re-check based on draft
            updateSendingToText(checkId)     // Update text
        } else {
            // Initial check based on prefs is handled in onCreateView
            updateSendingToText(binding.dbChooser.checkedRadioButtonId) // Ensure text is correct on view creation
        }

        //chose to stay with initial creation date instead of resetting date when review draft
        if (existingDraft != null) date = existingDraft!!.creationDate
        else date = report.creationDate
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
        binding.dateLabel.text = dateFormat.format(date)

        val result = preferences.getString(draftReportPreferenceName, null)
        if (!result.isNullOrEmpty()) {
            try {
                reportDraftList = ReportDraft.jsonAdapter.fromJson(result)?.list?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing draft list from JSON", e)
                reportDraftList = mutableListOf()
            }
        }

        if (existingDraft != null) setReportValuesFromExistingDraft()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cancelReport -> {
                showCancelBottomSheetDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView() //should these be switched?
        _binding = null
    }

    override fun onDetach() {
        delegate = null
        super.onDetach()
    }


    private fun getNetworkAvailability(context: Context): NetworkStatus {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //connectivityManager.isActiveNetworkMetered
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return NetworkStatus.NOTCONNECTED
            val activeNetWork = connectivityManager.getNetworkCapabilities(network) ?: return NetworkStatus.NOTCONNECTED
            if (activeNetWork.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                return if (connectivityManager.isActiveNetworkMetered) NetworkStatus.CONNECTEDBUTMETERED
                else NetworkStatus.CONNECTED
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return NetworkStatus.NOTCONNECTED
            if (networkInfo.isConnectedOrConnecting) return NetworkStatus.CONNECTED
        }
        return NetworkStatus.NOTCONNECTED
    }

    private fun setReportValuesFromExistingDraft() {
        //non-null asserted bc only calling if existingDraft not null
        report.id = existingDraft!!.id //overwriting id created when new ReportDraft created
        report.images = existingDraft!!.images
        report.creationDate = existingDraft!!.creationDate //overwriting date created when ReportDraft created
        report.title = existingDraft!!.title
        report.assessorEmail = existingDraft!!.assessorEmail
        report.restTarget = existingDraft!!.restTarget //could be null
        report.deviceToken = existingDraft!!.deviceToken
    }

    //TODO this is called often - verify added logic isn't slowing things down too much
    private fun setFinalReportValues() {
        report.assessorEmail = binding.emailField.text.toString()
        report.title = binding.titleField.text.toString()
        val checkedId = binding.dbChooser.checkedRadioButtonId
        val editor = preferences.edit()

        if (checkedId == FIREBASE_RADIO_ID) {
            report.restTarget = null
            editor.putString(lastSelectedDbPrefKey, firebaseDbIdentifier)
        } else {
            val target = idToTarget[checkedId]
            if (target != null) {
                report.restTarget = target
                editor.putString(lastSelectedDbPrefKey, target.name) // Save Enum *name*
            } else {
                Log.e(TAG, "Error setting RestTarget from checkedId: $checkedId. Defaulting to Firebase.")
                report.restTarget = null
                editor.putString(lastSelectedDbPrefKey, firebaseDbIdentifier)
            }
        }
        editor.apply() // Apply the preference change
    }

    private fun showCancelBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog_newreport_cancel, null)
        val btnCancel = view.findViewById<Button>(R.id.cancel_draft)
        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
            delegate?.returnToReports()
        }
        val btnSaveDraft = view.findViewById<Button>(R.id.save_updated_draft)
        btnSaveDraft.setOnClickListener {
            setFinalReportValues()
            DraftUtils.saveDraft(requireContext(), report)
            bottomSheetDialog.dismiss()
            delegate?.returnToReports() // Navigate back AFTER saving
        }
        val btnDelete = view.findViewById<Button>(R.id.delete_draft)
        btnDelete.setOnClickListener {
            if (existingDraft != null) {
                DraftUtils.deleteDraft(requireContext(), existingDraft!!.id)
            } else {
                // TODO No draft existed to delete, maybe clear fields or just navigate
                Log.d(TAG, "Delete tapped but no existing draft to delete.")
            }
            bottomSheetDialog.dismiss()
            delegate?.returnToReports()
        }
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun createMeteredAlert() {
            val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("You are currently using a metered network, so you may need to pay for the data you upload.  Would you like to continue to upload now, or save your report draft to your phone so that you can upload it later?")
        builder.setPositiveButton("Upload Now") { dialog, _ ->
                delegate?.uploadReport(this, report)
                dialog.dismiss()
            }
            builder.setNeutralButton("Save Draft") { dialog, _ ->
                setFinalReportValues() // TODO make sure calling this so often won't screw up various ids
                // ADDED: Use DraftUtils
                DraftUtils.saveDraft(requireContext(), report)
                dialog.dismiss()
                delegate?.returnToReports() // Navigate back AFTER saving
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss()}
            builder.show()
    }

    private fun createNoNetworkAlert() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("You do not currently have a network connection.  Your report will be saved as a draft so you can upload it at another time.")
        builder.setPositiveButton("OK") { dialog, _ ->
            setFinalReportValues() // Ensure report object is up-to-date
            DraftUtils.saveDraft(requireContext(), report)
            dialog.dismiss()
            delegate?.returnToReports() // Navigate back AFTER saving
        }
        builder.show()
    }

    // Call this method from TabActivity after upload attempt (success or failure)
    fun handleUploadAttemptComplete(success: Boolean) {
        activity?.runOnUiThread {
            isUploading = false
            // If success=false, the error dialog is already shown by TabActivity.
            // If success=true, navigation has already happened.
            // This is mainly to reset the UI state of NewReportFragment if it's still visible.
        }
    }

    private fun updateSendingToText(checkedId: Int) {
        val selectedText = when (checkedId) {
            FIREBASE_RADIO_ID -> getString(R.string.sending_to_firebase)
            else -> {
                val target = idToTarget[checkedId]
                if (target != null) {
                    getString(R.string.sending_to_db, target.toString())
                } else {
                    Log.e(TAG, "Invalid checkedId for RestTarget: $checkedId")
                    getString(R.string.sending_to_unknown)
                }
            }
        }
        binding.sendingToAmal.text = selectedText
        binding.sendingToAmal.visibility = View.VISIBLE
    }
}
