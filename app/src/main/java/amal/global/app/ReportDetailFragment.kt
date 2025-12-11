package global.amal.app

import global.amal.app.databinding.FragmentReportDetailBinding
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.*

interface ReportDetailFragmentDelegate {
    fun webReportTapped(reportDetailFragment: ReportDetailFragment)
    fun pdfReportTapped(reportDetailFragment: ReportDetailFragment)
}

class ReportDetailFragment : Fragment() {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!

    lateinit var report: Report

    lateinit var adapter: ReportDetailAdapter

    var delegate: ReportDetailFragmentDelegate? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        adapter = ReportDetailAdapter(requireContext(), report)
        setHasOptionsMenu(true)
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_report_detail, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.setTitle(R.string.title_assess)
        activity?.title = report.title

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)

        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnItemClickListener(object: OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val image = report.images[position]
                print(image.metadata.name)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        delegate = null
        super.onDetach()
    }

}
