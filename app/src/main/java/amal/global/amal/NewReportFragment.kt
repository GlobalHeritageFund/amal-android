package amal.global.amal

import amal.global.amal.databinding.FragmentNewReportBinding
import android.content.Context
//is there a problem with not having appcompat context anymore import androidx.core.content.ContentProviderCompat.requireContext
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Log.d
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
}

class NewReportFragment: Fragment() {

    companion object {
        const val TAG = "New Report Fragment"
    }

    enum class NetworkStatus () {
        NOTCONNECTED,
        CONNECTEDBUTMETERED,
        CONNECTED;
    }

    private var _binding: FragmentNewReportBinding? = null
    private val binding get() = _binding!!

//    private lateinit var dao: AmalRoomDatabaseDao

    var report = ReportDraft()

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
                radioGroup.addView(tempButton)
            }
        }
        radioGroup.setOnCheckedChangeListener {
            group, checkedId ->
            if (checkedId !== -1) {
                var text = RestTarget.values()[checkedId].toString()
                binding.sendingToAmal.setText("Sending to $text")
            }
        }
        publishButton.setOnClickListener{
            setReportValues()
            //todo grey out publish button
            when (getNetworkAvailability(requireContext())) {
                NetworkStatus.NOTCONNECTED -> {
                    d(TAG, it.toString())
                    createNoNetworkAlert()
                }
                NetworkStatus.CONNECTEDBUTMETERED -> {
                    d(TAG, it.toString())
                    createMeteredAlert()
                }
                NetworkStatus.CONNECTED -> {
                    d(TAG, it.toString())
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
        super.onViewCreated(view, savedInstanceState)
        
        binding.emailField.setText(currentUser.email ?: "")

        val date = Date()
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
        binding.dateLabel.text = dateFormat.format(date)

//        val db = AmalRoomDatabase.getDatabase(requireContext().applicationContext)
//        dao = db.amalRoomDatabaseDao()
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

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        report.assessorEmail = binding.emailField.text.toString()
//        report.creationDate = Date()
//        report.title = binding.titleField.text.toString()
//        val checkedId = binding.dbChooser.checkedRadioButtonId
//        if (checkedId !== -1) report.restTarget = RestTarget.values()[checkedId]
//        when (item!!.itemId) {
//            R.id.uploadReport -> {
//                delegate?.uploadReport(this, report)
//                return true
//            }
//            else ->
//                return super.onOptionsItemSelected(item)
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
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

    private fun setReportValues() {
        report.assessorEmail = binding.emailField.text.toString()
        report.creationDate = Date()
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
            setReportValues()
           //TODO save report draft dao.insert(report)
            bottomSheetDialog.dismiss()
        }
        val btnDelete = view.findViewById<Button>(R.id.delete_draft)
        btnDelete.setOnClickListener {
            Log.d(TAG," got in bottom dialog delete")
            //TODO       if (report.id != null) dao.delete(report)
            bottomSheetDialog.dismiss()
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
            builder.setNeutralButton("Save Draft") { dialog, which -> dialog.dismiss() } //TODO implement save  dao.insert() before dismiss
            builder.setNegativeButton("Cancel") {dialog, which -> dialog.dismiss()}
            builder.show()
    }

    private fun createNoNetworkAlert() {
        val builder = AlertDialog.Builder(requireContext());
        builder.setMessage("You do not currently have a network connection.  Your report will be saved as a draft so you can upload it at another time.");
        builder.setPositiveButton("OK") { dialog, which ->
                //TODO save as draft dao.insert
                dialog.dismiss()
            }
        builder.show()
    }
}
