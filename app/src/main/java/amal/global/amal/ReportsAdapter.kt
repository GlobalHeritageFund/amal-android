package amal.global.amal

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ReportsAdapter(var context: Context, var reports: List<Report> = listOf()) : RecyclerView.Adapter<SubtitleViewHolder>() {

    init {
        val reference = FirebaseDatabase.getInstance().reference.child("reports")

        val query = reference
                .orderByChild("authorDeviceToken")
                .equalTo(CurrentUser(context).token)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = snapshot.value as? HashMap<String, HashMap<String, Any>> ?: hashMapOf()

                reports = map.entries
                        .map {
                            var value = it.value
                            value["firebaseID"] = it.key
                            val imagesMaps = value["images"] as? HashMap<*, *> ?: hashMapOf<String, Any>()
                            val images = imagesMaps.values.toList()
                            value["images"] = images
                            value
                        }
                        .filter { (it["uploadComplete"] as? Boolean) ?: false }
                        .mapNotNull { Report.jsonAdapter.fromJsonValue(it) }
                        .sortedByDescending { it.creationDateValue }
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("asdf", error.toString())
            }

        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtitleViewHolder {
        val itemView = parent.inflate(R.layout.subtitle_row, false)
        return SubtitleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SubtitleViewHolder, position: Int) {
        val report = reports[position]
        holder.title.text = report.title

        val count = report.images.count()
        holder.subtitle.text = if (count == 1) "1 item" else count.toString() + " items"

        report.images.firstOrNull()?.let { it.load(context).centerCrop().into(holder.imageView) }
    }

    override fun getItemCount(): Int {
        return reports.size
    }
}
