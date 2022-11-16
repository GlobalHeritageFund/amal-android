package amal.global.amal

import amal.global.amal.databinding.FragmentNewReportBinding
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.RadioButton
import android.widget.Toast
import java.text.DateFormat
import java.util.*

interface NewReportFragmentDelegate {
    fun uploadReport(fragment: NewReportFragment, report: ReportDraft)
}

class NewReportFragment: Fragment() {

    private var _binding: FragmentNewReportBinding? = null
    private val binding get() = _binding!!

    var report = ReportDraft()

    var delegate: NewReportFragmentDelegate? = null

    var uploadItem: MenuItem? = null

    private val currentUser: CurrentUser
        get() = CurrentUser(this.requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        _binding = FragmentNewReportBinding.inflate(inflater, container, false)
        val view = binding.root
        val radioGroup = binding.dbChooser
        //both phrase and databaseTargets should be in uppercase
        val dbTargets = currentUser.databaseTargets
        RestTargets.values().forEach {
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
                var text = RestTargets.values()[checkedId].toString()
                binding.sendingToAmal.setText("Sending to $text")
                Toast.makeText(requireContext().applicationContext, text, Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_new_report, menu)
        uploadItem = menu.findItem(R.id.uploadReport)
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        report.assessorEmail = binding.emailField.text.toString()
        report.creationDate = Date()
        report.title = binding.titleField.text.toString()
        val checkedId = binding.dbChooser.checkedRadioButtonId
        if (checkedId !== -1) report.restTarget = RestTargets.values()[checkedId]
        when (item!!.itemId) {
            R.id.uploadReport -> {
                delegate?.uploadReport(this, report)
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}