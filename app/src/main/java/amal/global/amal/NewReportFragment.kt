package amal.global.amal

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_new_report.*
import java.util.*

interface NewReportFragmentDelegate {
    fun uploadReport(fragment: NewReportFragment, report: ReportDraft)
}

class NewReportFragment: Fragment() {

    var report = ReportDraft()

    var delegate: NewReportFragmentDelegate? = null

    private val currentUser: CurrentUser
        get() = CurrentUser(this.requireContext())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_new_report, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_new_report, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.setTitle(R.string.title_report)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        emailField.setText(currentUser.email ?: "")
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        report.assessorEmail = emailField.text.toString()
        report.creationDate = Date()
        report.title = titleField.text.toString()
        when (item!!.getItemId()) {
            R.id.menu_item_upload_report -> {
                delegate?.uploadReport(this, report)
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }
}