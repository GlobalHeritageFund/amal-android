package global.amal.app
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
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

    private val titleTextView: TextView? = itemView.findViewById(R.id.titleTextView)
    private val subtitleTextView: TextView? = itemView.findViewById(R.id.subtitleTextView)
    private val imageView: ImageView? = itemView.findViewById(R.id.imageView)
    private val dividerTextView: TextView? = itemView.findViewById(R.id.divider_text) // For divider layout
    private val dividerLine: View? = itemView.findViewById(R.id.divider_line) // For divider layout

    private fun bindDraftReport(item: ReportItem.DraftReport) {
        titleTextView?.text = item.draftReport.title
        val count = item.draftReport.images.count()
        subtitleTextView?.text = if (count == 1) "1 item" else count.toString() + " items"
        item.draftReport.images.firstOrNull()?.let { img ->
            imageView?.let { iv -> img.load(itemView.context).centerCrop().into(iv) }
            // item.draftReport.images.firstOrNull()?.let { it.load(itemView.context).centerCrop().into(itemView.findViewById<ImageView>(R.id.imageView)) }
        }
    }

    private fun bindPublishedReport(item: ReportItem.PublishedReport) {
        titleTextView?.text = item.report.title
        val count = item.report.images.count()
        subtitleTextView?.text = if (count == 1) "1 item" else count.toString() + " items"
        item.report.images.firstOrNull()?.let { img ->
            imageView?.let { iv -> img.load(itemView.context).centerCrop().into(iv) }
//        item.report.images.firstOrNull()?.let { it.load(itemView.context).centerCrop().into(itemView.findViewById<ImageView>(R.id.imageView)) }
        }
    }

    private fun bindDivider(item: ReportItem.ReportsHeader) {
        dividerTextView?.text = item.reportType
    }

    fun bind(reportItem: ReportItem) {
        when (reportItem) {
            is ReportItem.DraftReport -> bindDraftReport(reportItem)
            is ReportItem.PublishedReport -> bindPublishedReport(reportItem)
            is ReportItem.ReportsHeader -> bindDivider(reportItem)
            else -> Log.d("ReportItemViewHolder","Unknown ReportItem Type")
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
        ERROR("Cannot download reports from database.  If you are not signed in, please go to settings and sign in now."),
        RESPONSE_RECEIVED(""),
        LOADING("Loading...");
    }

    val allReports: MutableList<ReportItem> = mutableListOf()
    private var dbStatus: DatabaseStatus = DatabaseStatus.LOADING
    private var draftReports: List<ReportDraft> = emptyList()
    private var publishedReports: List<Report> = emptyList()

    init {

//        val preferences: SharedPreferences = context.getSharedPreferences(DRAFT_REPORT_PREFERENCE, Context.MODE_PRIVATE)
//        val draftReportsString = preferences.getString(DRAFT_REPORT_PREFERENCE,"")
//        val draftReports: List<ReportDraft> =
//            if (draftReportsString != "") {
//                try {
//                    draftReportsString?.let { ReportDraft.jsonAdapter.fromJson(it)?.list } ?: emptyList()
//                } catch (e: Exception) { // Catch potential JsonDataException
//                    Log.e(TAG, "Error parsing draft reports from JSON", e)
//                    emptyList()
//                }
//            } else {
//                emptyList()
//            }
//        createReportItemList(draftReports, emptyList<Report>())
//
//        if (haveNetwork(context)) {
//            dbStatus = DatabaseStatus.LOADING
//            val reference = FirebaseDatabase.getInstance().reference.child("reports")
//            val query = reference
//                    .orderByChild("authorDeviceToken")
//                    .equalTo(CurrentUser(context).token)
//
//            query.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    dbStatus = DatabaseStatus.RESPONSE_RECEIVED
//
//                    val map = snapshot.value as? HashMap<String, HashMap<String, Any>>
//                            ?: hashMapOf()
//
//                    val reportsSnapshot = map.entries
//                            .map {
//                                val value = it.value
//                                value["firebaseID"] = it.key
//                                val imagesMaps = value["images"] as? HashMap<*, *>
//                                        ?: hashMapOf<String, Any>()
//                                val images = imagesMaps.values.toList()
//                                value["images"] = images
//                                value
//                            }
//                            .filter { (it["uploadComplete"] as? Boolean) ?: false }
//                            .mapNotNull { Report.jsonAdapter.fromJsonValue(it) }
//                            .sortedByDescending { it.creationDateValue }
//                    createReportItemList(draftReports, reportsSnapshot)
//                }
//                override fun onCancelled(error: DatabaseError) {
//                    Log.d(TAG, error.toString())
//                    dbStatus = DatabaseStatus.ERROR
//                    createReportItemList(draftReports, emptyList())
//                }
//            })
//        } else {
//            dbStatus = DatabaseStatus.NOT_CONNECTED
//            createReportItemList(draftReports, emptyList())
//        }
    }
    fun refreshData() {
        // Reset status for a fresh load
        dbStatus = DatabaseStatus.LOADING
        // Immediately show loading state for published reports
        //todo does this need to be instantiated??
        //Load drafts first, as they are local and fast
        loadDraftReports()

        // Then attempt to load published reports
        loadPublishedReportsFromFirebase()
    }

    private fun loadDraftReports() {
        val preferences: SharedPreferences = context.getSharedPreferences(DRAFT_REPORT_PREFERENCE, Context.MODE_PRIVATE)
        val draftReportsString = preferences.getString(DRAFT_REPORT_PREFERENCE, "")

        if (draftReportsString.isNullOrEmpty()) {
            this.draftReports = emptyList()
            updateRecyclerViewData()
            return
        }

        try {
            // Attempt to deserialize and immediately access the list (to trigger property access/type checks)
            val wrapper = ReportDraft.jsonAdapter.fromJson(draftReportsString)
            // The toList() helps force any lazy property resolution
            this.draftReports = wrapper?.list?.toList() ?: emptyList()

        } catch (e: Exception) {
            // trying to stop the recurring crash by removing the corrupted data
            Log.e(TAG, "FATAL ERROR: Corrupt draft reports JSON in SharedPreferences. Clearing all drafts to prevent recurring crash.", e)
            preferences.edit().remove(DRAFT_REPORT_PREFERENCE).apply()
            this.draftReports = emptyList()
        }

        (context as? Activity)?.runOnUiThread {
            updateRecyclerViewData()
        }
    }

    private fun loadPublishedReportsFromFirebase() {
        if (haveNetwork(context)) {
            dbStatus = DatabaseStatus.LOADING // Explicitly set for this part
            // Update UI to show loading for published reports if not already shown
            updateRecyclerViewData()

            val reference = FirebaseDatabase.getInstance().reference.child("reports")
            val query = reference
                .orderByChild("authorDeviceToken")
                .equalTo(CurrentUser(context).token) // Assuming CurrentUser(context).token is safe

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dbStatus = DatabaseStatus.RESPONSE_RECEIVED
                    val map = snapshot.value as? HashMap<String, HashMap<String, Any>> ?: hashMapOf()
                    publishedReports = map.entries
                        .map {
                            val value = it.value
                            value["firebaseID"] = it.key
                            val imagesMaps = value["images"] as? HashMap<*, *> ?: hashMapOf<String, Any>()
                            val images = imagesMaps.values.toList()
                            value["images"] = images
                            value
                        }
                        .filter { (it["uploadComplete"] as? Boolean) ?: false }
                        .mapNotNull { Report.jsonAdapter.fromJsonValue(it) }
                        .sortedByDescending { it.creationDateValue }
                    updateRecyclerViewData()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, error.toString())
                    dbStatus = DatabaseStatus.ERROR
                    publishedReports = emptyList() // Ensure it's empty on error
                    updateRecyclerViewData()
                }
            })
        } else {
            dbStatus = DatabaseStatus.NOT_CONNECTED
            publishedReports = emptyList() // Ensure it's empty if no network
            updateRecyclerViewData()
        }
    }

    private fun updateRecyclerViewData() {
        allReports.clear()
        allReports.add(ReportItem.ReportsHeader(TYPE_DRAFT))
        if (draftReports.isNotEmpty()) {
            draftReports.forEach {
                allReports.add(ReportItem.DraftReport(it))
            }
        } else {
            allReports.add(ReportItem.ReportsHeader(TYPE_EMPTY))
        }

        allReports.add(ReportItem.ReportsHeader(TYPE_PUBLISHED_REPORT))
        if (publishedReports.isNotEmpty()) {
            publishedReports.forEach {
                allReports.add(ReportItem.PublishedReport(it))
            }
        } else {
            // Show status only for published reports section
            when (dbStatus) {
                DatabaseStatus.ERROR -> allReports.add(ReportItem.ReportsHeader(DatabaseStatus.ERROR.displayPhrase))
                DatabaseStatus.RESPONSE_RECEIVED -> allReports.add(ReportItem.ReportsHeader(TYPE_EMPTY)) // Data received but list is empty
                DatabaseStatus.LOADING -> allReports.add(ReportItem.ReportsHeader(DatabaseStatus.LOADING.displayPhrase))
                DatabaseStatus.NOT_CONNECTED -> allReports.add(ReportItem.ReportsHeader(DatabaseStatus.NOT_CONNECTED.displayPhrase))
            }
        }

        if (draftReports.isEmpty() && publishedReports.isEmpty() && dbStatus == DatabaseStatus.RESPONSE_RECEIVED) {
            delegate.showEmptyScreen()
        } else {
            delegate.showReportsList()
        }
        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportItemViewHolder {
        val layout = when (viewType) {
            TYPE_REPORT -> R.layout.subtitle_row
            TYPE_DIVIDER -> R.layout.list_divider
            else -> throw IllegalArgumentException("Invalid view type")
        }

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
        else -> Log.d(TAG,"Unknown Item Type")
    }

//    private fun createReportItemList(draftReports: List<ReportDraft>, reports: List<Report>) {
//        allReports.clear()
//        allReports.add(ReportItem.ReportsHeader(TYPE_DRAFT))
//        if (draftReports.isNotEmpty()) {
//            draftReports.forEach {
//                allReports.add(ReportItem.DraftReport(it))
//            }
//        } else {
//            allReports.add(ReportItem.ReportsHeader(TYPE_EMPTY))
//        }
//        allReports.add(ReportItem.ReportsHeader(TYPE_PUBLISHED_REPORT))
//        if (reports.isNotEmpty()) {
//            reports.forEach {
//                allReports.add(ReportItem.PublishedReport(it))
//            }
//        } else {
//            when (dbStatus) {
//                DatabaseStatus.ERROR -> allReports.add(ReportItem.ReportsHeader(DatabaseStatus.ERROR.displayPhrase))
//                DatabaseStatus.RESPONSE_RECEIVED -> allReports.add(ReportItem.ReportsHeader(TYPE_EMPTY))
//                DatabaseStatus.LOADING -> allReports.add(ReportItem.ReportsHeader(DatabaseStatus.LOADING.displayPhrase))
//                DatabaseStatus.NOT_CONNECTED -> allReports.add(ReportItem.ReportsHeader(DatabaseStatus.NOT_CONNECTED.displayPhrase))
//            }
//        }
//        if (draftReports.isEmpty() && reports.isEmpty() && dbStatus == DatabaseStatus.RESPONSE_RECEIVED) {
//            delegate.showEmptyScreen()
//        } else {
//            delegate.showReportsList()
//        }
//        notifyDataSetChanged()
//    }

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
