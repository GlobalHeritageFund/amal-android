package amal.global.amal

import amal.global.amal.ReportsAdapter.Companion.TYPE_DRAFT
import amal.global.amal.ReportsAdapter.Companion.TYPE_EMPTY
import amal.global.amal.ReportsAdapter.Companion.TYPE_PUBLISHED_REPORT
import amal.global.amal.ReportsAdapter.Companion.TYPE_REPORT
import android.content.Context
import android.content.SharedPreferences
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.ContactsContract.Data
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
    fun showEmptyScreen()
    fun showReportsList()
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
        val DRAFT_REPORT_PREFERENCE = "DraftReportPreferences"
        const val TYPE_REPORT = 0
        const val TYPE_DIVIDER = 1
        const val TYPE_EMPTY = "None to show"
        const val TYPE_PUBLISHED_REPORT = "Reports"
        const val TYPE_DRAFT = "Draft Reports"
    }

    enum class DatabaseStatus(val displayPhrase: String) {
        NOT_CONNECTED("No network connection - cannot download"),
        ERROR("Database error - cannot download reports"),
        RESPONSE_RECEIVED(""),
        LOADING("Loading...");
    }

    val allReports: MutableList<ReportItem> = mutableListOf()
    var dbStatus: DatabaseStatus = DatabaseStatus.NOT_CONNECTED

    init {

        val draftReports: List<ReportDraft>
        val preferences: SharedPreferences = context.getSharedPreferences(DRAFT_REPORT_PREFERENCE, Context.MODE_PRIVATE)
        val draftReportsString = preferences.getString(DRAFT_REPORT_PREFERENCE,"")
        if (draftReportsString != "") draftReports = ReportDraft.jsonAdapter.fromJson(draftReportsString)!!.list
        else draftReports = emptyList()
        createReportItemList(draftReports, emptyList<Report>())

        if (haveNetwork(context)) {
            dbStatus = DatabaseStatus.LOADING
            val reference = FirebaseDatabase.getInstance().reference.child("reports")
            val query = reference
                    .orderByChild("authorDeviceToken")
                    .equalTo(CurrentUser(context).token)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dbStatus = DatabaseStatus.RESPONSE_RECEIVED

                    val map = snapshot.value as? HashMap<String, HashMap<String, Any>>
                            ?: hashMapOf()

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
                    createReportItemList(draftReports, reportsSnapshot)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, error.toString())
                    dbStatus = DatabaseStatus.ERROR
                }
            })
        } else {
            dbStatus = DatabaseStatus.NOT_CONNECTED
        }
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
            if (divider.reportType != TYPE_DRAFT  && divider.reportType != TYPE_PUBLISHED_REPORT) dividerLine.visibility = View.INVISIBLE
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

    private fun createReportItemList(draftReports: List<ReportDraft>, reports: List<Report>) {
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
            when (dbStatus) {
                DatabaseStatus.ERROR -> allReports.add(ReportItem.ReportsHeader(DatabaseStatus.ERROR.displayPhrase))
                DatabaseStatus.RESPONSE_RECEIVED -> allReports.add(ReportItem.ReportsHeader(TYPE_EMPTY))
                DatabaseStatus.LOADING -> allReports.add(ReportItem.ReportsHeader(DatabaseStatus.LOADING.displayPhrase))
                DatabaseStatus.NOT_CONNECTED -> allReports.add(ReportItem.ReportsHeader(DatabaseStatus.NOT_CONNECTED.displayPhrase))
            }
        }
        if (draftReports.isEmpty() && reports.isEmpty() && dbStatus == DatabaseStatus.RESPONSE_RECEIVED) {
            delegate.showEmptyScreen()
        } else {
            delegate.showReportsList()
        }
        notifyDataSetChanged()
    }

    private fun haveNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return connectivityManager.activeNetwork != null
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnectedOrConnecting
        }
    }
}
