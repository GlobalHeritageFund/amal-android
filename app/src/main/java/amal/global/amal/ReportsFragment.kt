package amal.global.amal

import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

interface ReportsDelegate {
    fun newReportTapped(reportsFragment: ReportsFragment)
}

class ReportsFragment : Fragment() {

    var delegate: ReportsDelegate? = null

    lateinit var listView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity.setTitle(R.string.title_report)

        listView = bind(R.id.report_list)

        listView.setLayoutManager(LinearLayoutManager(activity));


        listView.adapter = ReportsAdapter(
                context,
                listOf<Report>(Report(listOf(), "token", Date(), "title", "email@email.com"))
        )

        view?.findViewById<FloatingActionButton>(R.id.new_report_button)?.setOnClickListener({
            delegate?.newReportTapped(this)
        })

    }
}

class ReportsAdapter(context: Context, var reports: List<Report>) : RecyclerView.Adapter<ReportsAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title = view.bind<TextView>(R.id.report_cell_title)
        var subtitle = view.bind<TextView>(R.id.report_cell_subtitle)
    }

    init {
        val reference = FirebaseDatabase.getInstance().reference.child("reports")

         val query = reference
                 .orderByChild("authorDeviceToken")
                 .equalTo(CurrentUser(context).token)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("asdf", snapshot.value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("asdf", error.toString())
            }

        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = parent.inflate(R.layout.report_item_row, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val report = reports[position]
        holder.title.text = report.title
    }

    override fun getItemCount(): Int {
        return reports.size
    }
}
