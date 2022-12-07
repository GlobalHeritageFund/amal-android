package amal.global.amal

import amal.global.amal.databinding.FragmentReportsBinding
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
    fun tappedDraftReport(draftReport: ReportDraft, reportsFragment: ReportsFragment)
}

class ReportsFragment : Fragment(), ReportsAdapterDelegate, DraftReportsAdapterDelegate {

    companion object {
        const val TAG = "Reports Fragment"
    }

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    var delegate: ReportsDelegate? = null

    lateinit var adapter: ReportsAdapter
    lateinit var draftAdapter: DraftReportsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        adapter = ReportsAdapter(requireContext(),this)
        draftAdapter = DraftReportsAdapter(requireContext(), this)
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reportList.layoutManager = LinearLayoutManager(activity);

        binding.reportList.adapter = adapter

        binding.reportList.addOnItemClickListener(object: OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val report = adapter.reports[position]
                delegate?.tappedReport(report, this@ReportsFragment)
            }
        })

        binding.draftReportList.layoutManager = LinearLayoutManager(activity);

        binding.draftReportList.adapter = draftAdapter

        binding.draftReportList.addOnItemClickListener(object: OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val draftReport = draftAdapter.draftReports[position]
                delegate?.tappedDraftReport(draftReport, this@ReportsFragment)
            }
        })

        view?.findViewById<FloatingActionButton>(R.id.new_report_button)?.setOnClickListener{
            delegate?.newReportTapped(this)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.setTitle(R.string.title_report)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun reportsFound() {
        if (_binding!=null) {//quick fix at the moment, might want to change flow at some point
            binding.allReportsEmptyView.visibility = View.GONE
            binding.allReportsList.visibility = View.VISIBLE
            binding.progressBarReportsView.visibility = View.GONE
            binding.publishedReportEmptyView.visibility = View.GONE
        }
    }

    override fun noReportsFound() {
        if (_binding!=null) {
            binding.progressBarReportsView.visibility = View.GONE
            binding.publishedReportEmptyView.visibility = View.VISIBLE
            binding.reportList.visibility = View.GONE
        }
    }

    override fun noDraftReportsFound() {
        if (_binding!=null) {
            binding.draftReportEmptyView.visibility = View.VISIBLE
            binding.draftReportList.visibility = View.GONE
        }
    }

    override fun draftReportsFound() {
        if (_binding!=null) {
            binding.allReportsEmptyView.visibility = View.GONE
            binding.draftReportEmptyView.visibility = View.GONE
            binding.allReportsList.visibility = View.VISIBLE
        }
    }
}
