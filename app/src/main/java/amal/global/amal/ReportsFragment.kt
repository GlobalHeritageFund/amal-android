package amal.global.amal

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*

interface ReportsDelegate {
    fun newReportTapped(reportsFragment: ReportsFragment)
}

class ReportsFragment : Fragment() {

    var delegate: ReportsDelegate? = null

    lateinit var listView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.setTitle(R.string.title_report)

        listView = bind(R.id.report_list)

        listView.setLayoutManager(LinearLayoutManager(activity));


        listView.adapter = ReportsAdapter(context!!)

        view?.findViewById<FloatingActionButton>(R.id.new_report_button)?.setOnClickListener({
            delegate?.newReportTapped(this)
        })

    }
}
