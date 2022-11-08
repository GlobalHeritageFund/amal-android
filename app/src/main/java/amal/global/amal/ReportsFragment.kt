package amal.global.amal

import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*

interface ReportsDelegate {
    fun newReportTapped(reportsFragment: ReportsFragment)
    fun tappedReport(report: Report, reportsFragment: ReportsFragment)
}

class ReportsFragment : Fragment(), ReportsAdapterDelegate {

    companion object {
        const val TAG = "Reports Fragment"
    }
    var delegate: ReportsDelegate? = null

    lateinit var adapter: ReportsAdapter

    lateinit var listView: RecyclerView
    lateinit var emptyView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        adapter = ReportsAdapter(requireContext()).also { it.delegate = this }

        return inflater.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = bind(R.id.report_list)

        listView.setLayoutManager(LinearLayoutManager(activity));

        listView.adapter = adapter

        listView.addOnItemClickListener(object: OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val report = adapter.reports[position]
                delegate?.tappedReport(report, this@ReportsFragment)
            }
        })

        emptyView = bind(R.id.empty_reports_view)

        view?.findViewById<FloatingActionButton>(R.id.new_report_button)?.setOnClickListener{
            delegate?.newReportTapped(this)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.setTitle(R.string.title_report)
    }

    override fun reportsFound() {
        Log.d(TAG, "reports found called")
        listView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE

    }

    override fun noReportsFound() {
        Log.d(TAG, "reports not found called")
        emptyView.visibility = View.VISIBLE
        listView.visibility = View.GONE
    }
}
