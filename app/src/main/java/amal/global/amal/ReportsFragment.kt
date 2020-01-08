package amal.global.amal

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*

interface ReportsDelegate {
    fun newReportTapped(reportsFragment: ReportsFragment)
    fun tappedReport(report: Report, reportsFragment: ReportsFragment)
}

class ReportsFragment : Fragment() {

    var delegate: ReportsDelegate? = null

    lateinit var adapter: ReportsAdapter

    lateinit var listView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        adapter = ReportsAdapter(context!!)
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.setTitle(R.string.title_report)

        listView = bind(R.id.report_list)

        listView.setLayoutManager(LinearLayoutManager(activity));

        listView.adapter = adapter

        listView.addOnItemClickListener(object: OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val report = adapter.reports[position]
                delegate?.tappedReport(report, this@ReportsFragment)
            }
        })

        view?.findViewById<FloatingActionButton>(R.id.new_report_button)?.setOnClickListener({
            delegate?.newReportTapped(this)
        })

    }
}
