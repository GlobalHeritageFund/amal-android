package amal.global.amal

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.EditText
import android.widget.TextView
import java.util.*

interface NewReportFragmentDelegate {
    fun uploadReport(fragment: NewReportFragment, report: ReportDraft)
}

class NewReportFragment: Fragment() {

    lateinit var titleField: EditText
    lateinit var dateLabel: TextView
    lateinit var emailField: EditText

    var report = ReportDraft()

    var delegate: NewReportFragmentDelegate? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_new_report, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_new_report, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity.setTitle(R.string.title_report)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleField = bind(R.id.report_title_field)
        dateLabel = bind(R.id.report_creation_date)
        emailField = bind(R.id.report_assessor_email)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
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