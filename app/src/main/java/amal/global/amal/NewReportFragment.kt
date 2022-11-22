package amal.global.amal

import amal.global.amal.databinding.FragmentNewReportBinding
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.Button
import android.widget.RadioButton
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.nio.file.Files.delete
import java.text.DateFormat
import java.util.*

interface NewReportFragmentDelegate {
    fun uploadReport(fragment: NewReportFragment, report: ReportDraft)
}

class NewReportFragment: Fragment() {

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
                showBottomSheetDialog()
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

    fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog_newreport, null)
        val btnCancel = view.findViewById<Button>(R.id.cancel_draft)
        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        val btnSave = view.findViewById<Button>(R.id.save_draft)
        btnSave.setOnClickListener {
            report.assessorEmail = binding.emailField.text.toString()
            report.creationDate = Date()
            report.title = binding.titleField.text.toString()
            val checkedId = binding.dbChooser.checkedRadioButtonId
            if (checkedId !== -1) report.restTarget = RestTarget.values()[checkedId]
            Log.d("new report"," got in bottom dialog save")
//            dao.insert(report)
        }
        val btnDelete = view.findViewById<Button>(R.id.delete_draft)
        btnDelete.setOnClickListener {
            Log.d("new report"," got in bottom dialog delete")
//            if (report.id != null) dao.delete(report)
        }
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }
}