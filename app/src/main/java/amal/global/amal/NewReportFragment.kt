package amal.global.amal

import amal.global.amal.databinding.FragmentNewReportBinding
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        _binding = FragmentNewReportBinding.inflate(inflater, container, false)
        val view = binding.root
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
        report.uploadToEAMENA = binding.eamenaSwitch.isChecked
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