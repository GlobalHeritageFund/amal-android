package amal.global.amal

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ReportsAdapter(context: Context, var reports: List<Report> = listOf()) : RecyclerView.Adapter<ReportsAdapter.MyViewHolder>() {

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
                val map = snapshot.value as? HashMap<String, Any> ?: hashMapOf()

                reports = map.values.mapNotNull { Report.fromJSON(it as HashMap<String, Any>) }
                notifyDataSetChanged()
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

        val count = report.images.count()
        holder.subtitle.text = if (count == 1) "1 item" else count.toString() + " items"
    }

    override fun getItemCount(): Int {
        return reports.size
    }
}
