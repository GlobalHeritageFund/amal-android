package amal.global.amal

import amal.global.amal.databinding.FragmentNewReportBinding
import android.content.Context
import android.content.SharedPreferences
//is there a problem with not having appcompat context anymore import androidx.core.content.ContentProviderCompat.requireContext
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.Button
import android.widget.RadioButton
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
    }

    enum class NetworkStatus() {
        NOTCONNECTED,
        CONNECTEDBUTMETERED,
        CONNECTED;
    }

    private var _binding: FragmentNewReportBinding? = null
    var reportDraftList: MutableList<ReportDraft> = mutableListOf()

    private val binding get() = _binding!!
    val draftReportPreferenceName = ReportsAdapter.DRAFT_REPORT_PREFERENCE
    val preferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences(draftReportPreferenceName, Context.MODE_PRIVATE)
    }

    var existingDraft: ReportDraft? = null
    var report = ReportDraft() //this creates a new draft with default values

    var delegate: NewReportFragmentDelegate? = null

    var cancelItem: MenuItem? = null

    private val currentUser: CurrentUser
        get() = CurrentUser(this.requireContext())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        _binding = FragmentNewReportBinding.inflate(inflater, container, false)
        val view = binding.root
        val radioGroup = binding.dbChooser
        //both phrase and databaseTargets should be in uppercase
        val dbTargets = currentUser.databaseTargets
        val publishButton = binding.publishReportButton
        RestTarget.values().forEach {
            if (it.phrase in dbTargets) {
                var tempButton = RadioButton(requireContext())
                tempButton.text = it.toString()
                tempButton.id = it.ordinal
                if (existingDraft != null && existingDraft?.restTarget == it) {
                    tempButton.isChecked = true
                    binding.sendingToAmal.text = getString(R.string.sending_to_db,it.toString())
                }
                radioGroup.addView(tempButton)
            }
        }

        radioGroup.setOnCheckedChangeListener {
            group, checkedId ->
            if (checkedId !== -1) {
                binding.sendingToAmal.text = getString(R.string.sending_to_db, RestTarget.values()[checkedId].toString())
            }
        }
        publishButton.setOnClickListener{
            setFinalReportValues()
            when (getNetworkAvailability(requireContext())) {
                NetworkStatus.NOTCONNECTED -> {
                    createNoNetworkAlert()
                }
                NetworkStatus.CONNECTEDBUTMETERED -> {
                    createMeteredAlert()
                }
                NetworkStatus.CONNECTED -> {
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.setTitle(R.string.title_report)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var date: Date

        super.onViewCreated(view, savedInstanceState)

        if (existingDraft != null && !existingDraft?.title.isNullOrEmpty()) {
            binding.titleField.setText(existingDraft?.title)
        }

        if (existingDraft != null && !existingDraft?.assessorEmail.isNullOrEmpty()) {
            binding.emailField.setText(existingDraft?.assessorEmail)
        } else {
            binding.emailField.setText(currentUser.email ?: "")
        }

        //chose to stay with initial creation date instead of resetting date when review draft
        if (existingDraft != null) date = existingDraft!!.creationDate
        else date = report.creationDate
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
        binding.dateLabel.text = dateFormat.format(date)

        var result = preferences.getString(draftReportPreferenceName, null)
        if (!result.isNullOrEmpty()) {
            reportDraftList = ReportDraft.jsonAdapter.fromJson(result)!!.list
        }

        if (existingDraft != null) setReportValuesFromExistingDraft()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.itemId) {
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

    private fun getNetworkAvailability(context: Context): NetworkStatus {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.isActiveNetworkMetered
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
        report.title = existingDraft!!.title ?: ""
        report.assessorEmail = existingDraft!!.assessorEmail
        report.restTarget = existingDraft!!.restTarget //could be null
    }

    private fun setFinalReportValues() {
        report.assessorEmail = binding.emailField.text.toString()
        report.title = binding.titleField.text.toString()
        val checkedId = binding.dbChooser.checkedRadioButtonId
        if (checkedId !== -1) report.restTarget = RestTarget.values()[checkedId]
    }

    private fun showCancelBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog_newreport_cancel, null)
        val btnCancel = view.findViewById<Button>(R.id.cancel_draft)
        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        val btnSaveDraft = view.findViewById<Button>(R.id.save_draft)
        btnSaveDraft.setOnClickListener {
            setFinalReportValues()
            addReportToDraftList()
            saveListToPrefs()
            bottomSheetDialog.dismiss()
        }
        val btnDelete = view.findViewById<Button>(R.id.delete_draft)
        btnDelete.setOnClickListener {
            if (existingDraft != null) {
                reportDraftList.removeAll { it.id == report.id }
                saveListToPrefs()
            }
            bottomSheetDialog.dismiss()
            delegate?.returnToReports()
        }
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun createMeteredAlert() {
            val builder = AlertDialog.Builder(requireContext());
            builder.setMessage("You are currently using a metered network, so you may need to pay for the data you upload.  Would you like to continue to upload now, or save your report draft to your phone so that you can upload it later?");
            builder.setPositiveButton("Upload Now") { dialog, which ->
                delegate?.uploadReport(this, report)
                dialog.dismiss()
            }
            builder.setNeutralButton("Save Draft") { dialog, which ->
                addReportToDraftList()
                saveListToPrefs()
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") {dialog, which -> dialog.dismiss()}
            builder.show()
    }

    private fun createNoNetworkAlert() {
        val builder = AlertDialog.Builder(requireContext());
        builder.setMessage("You do not currently have a network connection.  Your report will be saved as a draft so you can upload it at another time.");
        builder.setPositiveButton("OK") { dialog, which ->
            addReportToDraftList()
            saveListToPrefs()
            dialog.dismiss()
        }
        builder.show()
    }
    private fun addReportToDraftList() {
        if (existingDraft != null) {
            reportDraftList.removeAll {it.id == report.id}
            reportDraftList.add(report)
        } else {
            reportDraftList.add(report)
        }
    }

    private fun saveListToPrefs() {
        val editor = preferences.edit()
        editor.putString(draftReportPreferenceName,ReportDraft.jsonAdapter.toJson(ReportDraft.Companion.DraftWrapper(reportDraftList)))
        editor.apply()
        delegate?.returnToReports()
    }
}
