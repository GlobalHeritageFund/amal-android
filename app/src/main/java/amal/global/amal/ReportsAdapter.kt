package amal.global.amal

import amal.global.amal.ReportsAdapter.Companion.TYPE_REPORT
import android.content.Context
import android.content.SharedPreferences
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

interface ReportsAdapterDelegate {
    fun noReportsFound()
    fun reportsFound()
    fun noDraftReportsFound()
    fun draftReportsFound()
}

sealed class ReportItem {
    data class ReportsHeader(val reportType: String): ReportItem()
    data class PublishedReport(val report: Report): ReportItem()
    data class DraftReport(val draftReport: ReportDraft): ReportItem()
}

class ReportItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private fun bindDraftReport(item: ReportItem.DraftReport) {
        itemView.findViewById<TextView>(R.id.titleTextView).text = item.draftReport.title
        var count = item.draftReport.images.count()
        itemView.findViewById<TextView>(R.id.subtitleTextView).text = if (count == 1) "1 item" else count.toString() + " items"
        item.draftReport.images.firstOrNull()?.let { it.load(itemView.context).centerCrop().into(itemView.findViewById<ImageView>(R.id.imageView)) }
    }

    private fun bindPublishedReport(item: ReportItem.PublishedReport) {
        itemView.findViewById<TextView>(R.id.titleTextView).text = item.report.title
        var count = item.report.images.count()
        itemView.findViewById<TextView>(R.id.subtitleTextView).text = if (count == 1) "1 item" else count.toString() + " items"
        item.report.images.firstOrNull()?.let { it.load(itemView.context).centerCrop().into(itemView.findViewById<ImageView>(R.id.imageView)) }
    }

    private fun bindDivider(item: ReportItem.ReportsHeader) {
        itemView.findViewById<TextView>(R.id.divider_text).text = item.reportType
    }

    fun bind(reportItem: ReportItem) {
        when (reportItem) {
            is ReportItem.DraftReport -> bindDraftReport(reportItem)
            is ReportItem.PublishedReport -> bindPublishedReport(reportItem)
            is ReportItem.ReportsHeader -> bindDivider(reportItem)
        }
    }
}
class ReportsAdapter(val context: Context, val delegate: ReportsAdapterDelegate ) : RecyclerView.Adapter<ReportItemViewHolder>() {

    companion object {
        val TAG = "Reports Adapter"
        const val TYPE_REPORT = 0
        const val TYPE_DIVIDER = 1
        const val TYPE_EMPTY = "None to show"
        const val TYPE_PUBLISHED_REPORT = "Reports"
        const val TYPE_DRAFT = "Draft Reports"
    }

    val reports: MutableList<Report> = mutableListOf()
    val draftReports: MutableList<ReportDraft> = mutableListOf()
    val allReports: MutableList<ReportItem> = mutableListOf()

    init {

        val draftReportPreferenceName = "DraftReportPreferences" //change so don't have this hard coded in two places
        val preferences: SharedPreferences by lazy {
            context.getSharedPreferences(draftReportPreferenceName, Context.MODE_PRIVATE)
        }
        val draftReportsString = preferences.getString(draftReportPreferenceName,"")
        if (draftReportsString == "") {
            delegate.noDraftReportsFound()
        } else {
            draftReports.addAll(ReportDraft.jsonAdapter.fromJson(draftReportsString)!!.list)
            createReportItemList(draftReports, reports)
            delegate.draftReportsFound()
        }

        val reference = FirebaseDatabase.getInstance().reference.child("reports")

        val query = reference
                .orderByChild("authorDeviceToken")
                .equalTo(CurrentUser(context).token)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val map = snapshot.value as? HashMap<String, HashMap<String, Any>> ?: hashMapOf()

                val reportsSnapshot = map.entries
                        .map {
                            val value = it.value
                            value["firebaseID"] = it.key
                            val imagesMaps = value["images"] as? HashMap<*, *>
                                    ?: hashMapOf<String, Any>()
                            val images = imagesMaps.values.toList()
                            value["images"] = images
                            value
                        }
                        .filter { (it["uploadComplete"] as? Boolean) ?: false }
                        .mapNotNull { Report.jsonAdapter.fromJsonValue(it) }
                        .sortedByDescending { it.creationDateValue }
                reports.addAll(reportsSnapshot)
                Log.d(TAG, "got into addValueEventListener")
                if (reports.isEmpty()) {
                    delegate.noReportsFound()
                } else {
                    createReportItemList(draftReports, reports)
                    delegate.reportsFound()
                }

            }


            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, error.toString())
            }

        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportItemViewHolder {
        val layout = when (viewType) {
            TYPE_REPORT -> R.layout.subtitle_row
            TYPE_DIVIDER -> R.layout.list_divider
            else -> throw IllegalArgumentException("Invalid view type")
        }

//        val itemView = parent.inflate(R.layout.subtitle_row, false)
        val view = LayoutInflater
                .from(parent.context)
                .inflate(layout, parent, false)

        return ReportItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportItemViewHolder, position: Int) {
        holder.bind(allReports[position])
        if (holder.itemViewType == TYPE_DIVIDER) {
            val dividerLine = holder.itemView.findViewById<View>(R.id.divider_line)
            val divider = allReports[position] as ReportItem.ReportsHeader
            if (divider.reportType == TYPE_EMPTY) dividerLine.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return allReports.size
    }

    override fun getItemViewType(position: Int) = when (allReports[position]) {
        is ReportItem.DraftReport -> TYPE_REPORT
        is ReportItem.PublishedReport -> TYPE_REPORT
        is ReportItem.ReportsHeader -> TYPE_DIVIDER
    }

    private fun createReportItemList(draftReports: MutableList<ReportDraft>, reports: MutableList<Report>) {
        allReports.add(ReportItem.ReportsHeader(TYPE_DRAFT))
        if (draftReports.isNotEmpty()) {
            draftReports.forEach {
                allReports.add(ReportItem.DraftReport(it))
            }
        } else {
            allReports.add(ReportItem.ReportsHeader(TYPE_EMPTY))
        }
        allReports.add(ReportItem.ReportsHeader(TYPE_PUBLISHED_REPORT))
        if (reports.isNotEmpty()) {
            reports.forEach {
                allReports.add(ReportItem.PublishedReport(it))
            }
        } else {
            allReports.add(ReportItem.ReportsHeader(TYPE_EMPTY))
        }
        notifyDataSetChanged()
    }
}
