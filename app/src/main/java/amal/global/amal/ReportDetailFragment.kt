package amal.global.amal

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.*
import kotlinx.android.synthetic.main.fragment_report_detail.*

interface ReportDetailFragmentDelegate {
    fun webReportTapped(reportDetailFragment: ReportDetailFragment)
    fun pdfReportTapped(reportDetailFragment: ReportDetailFragment)
}

class ReportDetailFragment : Fragment() {

    lateinit var report: Report

    lateinit var adapter: ReportDetailAdapter

    var delegate: ReportDetailFragmentDelegate? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        adapter = ReportDetailAdapter(context!!, report)
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_report_detail, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_report_detail, menu)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.getItemId()) {
            R.id.menu_item_pdf_report -> {
                delegate?.pdfReportTapped(this)
                return true
            }
            R.id.menu_item_web_report -> {
                delegate?.webReportTapped(this)
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

}
