package amal.global.amal

import amal.global.amal.databinding.FragmentAssessBinding
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
                              savedInstanceState: Bundle?): View? {
        adapter = ReportsAdapter(requireContext()).also { it.delegate = this }
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
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
        if (_binding!=null) {//I know this is hacky and bad, but just looking for a quick fix at the moment
            binding.progressBarReportsView.visibility = View.GONE
            binding.reportList.visibility = View.VISIBLE
            binding.emptyReportsView.visibility = View.GONE
        }
    }

    override fun noReportsFound() {
        if (_binding!=null) { //again, this is bad...
            binding.progressBarReportsView.visibility = View.GONE
            binding.emptyReportsView.visibility = View.VISIBLE
            binding.reportList.visibility = View.GONE
        }
    }
}
