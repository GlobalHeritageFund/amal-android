package amal.global.amal

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_report_detail.*

class ReportDetailFragment : Fragment() {

    lateinit var report: Report

    lateinit var adapter: ReportDetailAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        adapter = ReportDetailAdapter(report)
        return inflater?.inflate(R.layout.fragment_report_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.title = report.title

        recyclerView.layoutManager = LinearLayoutManager(activity);

        recyclerView.adapter = adapter

        recyclerView.addOnItemClickListener(object: OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val image = report.images[position]
                print(image.metadata.name)
            }
        })

    }

}
