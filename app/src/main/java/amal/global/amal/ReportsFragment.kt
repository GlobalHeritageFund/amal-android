package amal.global.amal

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*

interface ReportsDelegate {
    fun newReportTapped(reportsFragment: ReportsFragment)
}

class ReportsFragment : Fragment() {

    var delegate: ReportsDelegate? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_report, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity.setTitle(R.string.title_report)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_report, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.getItemId()) {
            R.id.menu_item_add_report -> {
                delegate?.newReportTapped(this)
                return true
            }
            else ->
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item)
        }
    }

}
