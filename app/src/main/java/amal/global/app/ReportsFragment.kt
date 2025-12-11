package global.amal.app

import global.amal.app.databinding.FragmentReportsBinding
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.*

interface ReportsDelegate {
    fun newReportTapped(reportsFragment: ReportsFragment)
    fun tappedReport(report: Report, reportsFragment: ReportsFragment)
    fun tappedDraftReport(draftReport: ReportDraft, reportsFragment: ReportsFragment)
}

class ReportsFragment : Fragment(), ReportsAdapterDelegate {

    companion object {
        const val TAG = "Reports Fragment"
    }

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    var delegate: ReportsDelegate? = null

    lateinit var adapter: ReportsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        adapter = ReportsAdapter(requireContext(),this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.title_report)

        binding.reportList.layoutManager = LinearLayoutManager(requireActivity())
        binding.reportList.adapter = adapter

        adapter.refreshData()

        binding.reportList.addOnItemClickListener(object: OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                // Check bounds to prevent crash if data changes rapidly
                if (position < 0 || position >= adapter.allReports.size) return
                if (adapter.getItemViewType(position) == ReportsAdapter.TYPE_DIVIDER) return
                reportClickHandle(position)
            }
        })

        view.findViewById<FloatingActionButton>(R.id.new_report_button)?.setOnClickListener{
            delegate?.newReportTapped(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        delegate = null
        super.onDetach()
    }


    override fun showEmptyScreen() {
        if (_binding!=null) {
            binding.progressBarReportsView.visibility = View.GONE
            binding.emptyReportsView.visibility = View.VISIBLE
            binding.reportList.visibility = View.GONE
        }
    }

    override fun showReportsList() {
        if (_binding!=null) {//quick fix at the moment, might want to change flow at some point
            binding.progressBarReportsView.visibility = View.GONE
            binding.emptyReportsView.visibility = View.GONE
            binding.reportList.visibility = View.VISIBLE
        }
    }

    fun reportClickHandle(position: Int) {
        val reportItem = adapter.allReports[position]
        if (reportItem is ReportItem.PublishedReport) delegate?.tappedReport(reportItem.report, this@ReportsFragment)
        if (reportItem is ReportItem.DraftReport) delegate?.tappedDraftReport(reportItem.draftReport, this)
    }
}
